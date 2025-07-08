package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetMilestoneActionsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.milestone.CrudeMilestone;
import com.labnex.app.models.milestone.Milestones;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class MilestoneActionsBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetMilestoneActionsBinding binding;
	private int projectId;
	private static UpdateInterface UpdateInterface;

	public interface UpdateInterface {
		void updateDataListener(String str);
	}

	public static void setUpdateListener(UpdateInterface updateInterface) {
		UpdateInterface = updateInterface;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetMilestoneActionsBinding.inflate(inflater, container, false);

		Bundle bundle = getArguments();
		assert bundle != null;
		projectId = bundle.getInt("projectId");
		String source = bundle.getString("source", "");
		String type = bundle.getString("type", "");

		binding.closeBs.setOnClickListener(v -> dismiss());

		binding.bsTitle.setText(getString(R.string.create_milestone));
		binding.create.setText(getString(R.string.create));

		binding.startDate.setOnClickListener(v -> showDatePicker((TextInputEditText) v));
		binding.dueDate.setOnClickListener(v -> showDatePicker((TextInputEditText) v));

		binding.create.setOnClickListener(
				v -> {
					disableButton();
					String title =
							Objects.requireNonNull(binding.title.getText()).toString().trim();
					String description =
							Objects.requireNonNull(binding.description.getText()).toString().trim();
					String dueDate =
							Objects.requireNonNull(binding.dueDate.getText()).toString().trim();
					String startDate =
							Objects.requireNonNull(binding.startDate.getText()).toString().trim();

					if (title.isEmpty()) {
						enableButton();
						Snackbar.info(
								requireContext(),
								binding.mainBsFrame,
								getString(R.string.title_required));
						return;
					}

					if (dueDate.equals(startDate)) {
						enableButton();
						Snackbar.info(
								requireContext(),
								binding.mainBsFrame,
								getString(R.string.dates_same_error));
						return;
					}

					createNewMilestone(title, description, dueDate, startDate);
				});

		return binding.getRoot();
	}

	private void showDatePicker(TextInputEditText editText) {

		MaterialDatePicker<Long> datePicker =
				MaterialDatePicker.Builder.datePicker()
						.setTitleText(getString(R.string.select_date))
						.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
						.build();

		datePicker.addOnPositiveButtonClickListener(
				selection -> {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(selection);

					String date =
							String.format(
									Locale.US,
									"%d-%02d-%02d",
									calendar.get(Calendar.YEAR),
									calendar.get(Calendar.MONTH) + 1,
									calendar.get(Calendar.DAY_OF_MONTH));

					editText.setText(date);
				});

		datePicker.show(getParentFragmentManager(), "M3DatePicker");
	}

	private void createNewMilestone(
			String title, String description, String dueDate, String startDate) {

		CrudeMilestone milestone =
				new CrudeMilestone()
						.name(title)
						.description(description)
						.due_date(dueDate)
						.start_date(startDate);

		Call<Milestones> call =
				RetrofitClient.getApiInterface(requireContext())
						.createProjectMilestone(projectId, milestone);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Milestones> call,
							@NonNull retrofit2.Response<Milestones> response) {
						if (response.code() == 201) {
							if (UpdateInterface != null)
								UpdateInterface.updateDataListener("created");
							dismiss();
						} else if (response.code() == 401) {
							enableButton();
							Snackbar.info(
									requireContext(),
									binding.mainBsFrame,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {
							enableButton();
							Snackbar.info(
									requireContext(),
									binding.mainBsFrame,
									getString(R.string.access_forbidden_403));
						} else {
							enableButton();
							Snackbar.info(
									requireContext(),
									binding.mainBsFrame,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Milestones> call, @NonNull Throwable t) {
						enableButton();
						Snackbar.info(
								requireContext(),
								binding.mainBsFrame,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void disableButton() {
		binding.create.setEnabled(false);
		binding.create.setAlpha(0.5f);
	}

	private void enableButton() {
		binding.create.setEnabled(true);
		binding.create.setAlpha(1f);
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_milestone_actions);

		dialog.setOnShowListener(
				dialogInterface -> {
					BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
					View bottomSheet =
							bottomSheetDialog.findViewById(
									com.google.android.material.R.id.design_bottom_sheet);
					if (bottomSheet != null) {
						BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
						behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
						behavior.setPeekHeight(bottomSheet.getHeight());
						behavior.setHideable(false);
					}
				});

		if (dialog.getWindow() != null) {
			WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
			params.height = WindowManager.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setAttributes(params);
		}

		return dialog;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		try {
			BottomSheetListener bottomSheetListener = (BottomSheetListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}
}
