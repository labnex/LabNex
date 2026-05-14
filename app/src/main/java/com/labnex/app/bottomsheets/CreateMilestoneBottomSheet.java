package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.labnex.app.R;
import com.labnex.app.databinding.BottomsheetCreateMilestoneBinding;
import com.labnex.app.databinding.ItemPickerCardBinding;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.milestone.CrudeMilestone;
import com.labnex.app.models.milestone.Milestones;
import com.labnex.app.viewmodels.MilestonesViewModel;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author mmarif
 */
public class CreateMilestoneBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateMilestoneBinding binding;
	private MilestonesViewModel viewModel;
	private long projectId;
	private String type;
	private boolean isEditMode = false;
	private int milestoneId;
	ItemPickerCardBinding startCard;
	ItemPickerCardBinding dueCard;
	private boolean startDateSet = false;
	private boolean dueDateSet = false;

	public static CreateMilestoneBottomSheet newInstance(
			String type, long projectId, @Nullable Milestones milestone) {
		CreateMilestoneBottomSheet sheet = new CreateMilestoneBottomSheet();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putLong("projectId", projectId);
		if (milestone != null) args.putSerializable("milestone", milestone);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			projectId = getArguments().getLong("projectId", 0);
			type = getArguments().getString("type", "project");
			Milestones milestone = (Milestones) getArguments().getSerializable("milestone");
			if (milestone != null) {
				isEditMode = true;
				milestoneId = milestone.getId();
			}
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateMilestoneBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(MilestonesViewModel.class);

		binding.btnClose.setOnClickListener(v -> dismiss());

		startCard = ItemPickerCardBinding.bind(binding.cardStartDate.getRoot());
		dueCard = ItemPickerCardBinding.bind(binding.cardDueDate.getRoot());

		startCard.cardTitle.setText(R.string.start_date);
		dueCard.cardTitle.setText(R.string.due_date);
		startCard.cardSubtitle.setText(R.string.tap_to_select_date);
		dueCard.cardSubtitle.setText(R.string.tap_to_select_date);

		binding.descriptionInput.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == MotionEvent.ACTION_UP
							|| event.getAction() == MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});

		if (isEditMode && getArguments() != null) {
			Milestones m = (Milestones) getArguments().getSerializable("milestone");
			if (m != null) {
				binding.sheetTitle.setText(R.string.edit_milestone);
				binding.btnSubmit.setText(R.string.update);
				binding.titleInput.setText(m.getTitle());
				binding.descriptionInput.setText(
						m.getDescription() != null ? m.getDescription() : "");
				if (m.getStartDate() != null && !m.getStartDate().isEmpty()) {
					startCard.cardSubtitle.setText(m.getStartDate());
					startCard.cardClear.setVisibility(View.VISIBLE);
					startDateSet = true;
				}
				if (m.getDueDate() != null && !m.getDueDate().isEmpty()) {
					dueCard.cardSubtitle.setText(m.getDueDate());
					dueCard.cardClear.setVisibility(View.VISIBLE);
					dueDateSet = true;
				}
			}
		}

		binding.cardStartDate
				.getRoot()
				.setOnClickListener(
						v ->
								showDatePicker(
										date -> {
											startCard.cardSubtitle.setText(date);
											startCard.cardClear.setVisibility(View.VISIBLE);
											startDateSet = true;
										}));

		binding.cardDueDate
				.getRoot()
				.setOnClickListener(
						v ->
								showDatePicker(
										date -> {
											dueCard.cardSubtitle.setText(date);
											dueCard.cardClear.setVisibility(View.VISIBLE);
											dueDateSet = true;
										}));

		startCard.cardClear.setOnClickListener(
				v -> {
					startCard.cardSubtitle.setText(R.string.tap_to_select_date);
					startCard.cardClear.setVisibility(View.GONE);
					startDateSet = false;
				});

		dueCard.cardClear.setOnClickListener(
				v -> {
					dueCard.cardSubtitle.setText(R.string.tap_to_select_date);
					dueCard.cardClear.setVisibility(View.GONE);
					dueDateSet = false;
				});

		binding.btnSubmit.setOnClickListener(v -> submitMilestone());
		observeViewModel();

		return binding.getRoot();
	}

	private void showDatePicker(DateSelectedListener listener) {
		MaterialDatePicker<Long> picker =
				MaterialDatePicker.Builder.datePicker()
						.setTitleText(getString(R.string.select_date))
						.setSelection(MaterialDatePicker.todayInUtcMilliseconds())
						.build();

		picker.addOnPositiveButtonClickListener(
				selection -> {
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(selection);
					String date =
							String.format(
									Locale.US,
									"%d-%02d-%02d",
									cal.get(Calendar.YEAR),
									cal.get(Calendar.MONTH) + 1,
									cal.get(Calendar.DAY_OF_MONTH));
					listener.onDateSelected(date);
				});

		picker.show(getParentFragmentManager(), "datePicker");
	}

	interface DateSelectedListener {
		void onDateSelected(String date);
	}

	private void submitMilestone() {
		String title =
				binding.titleInput.getText() != null
						? binding.titleInput.getText().toString().trim()
						: "";
		if (title.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.title_required));
			return;
		}

		String description =
				binding.descriptionInput.getText() != null
						? binding.descriptionInput.getText().toString().trim()
						: "";

		String startDate = startDateSet ? startCard.cardSubtitle.getText().toString() : "";
		String dueDate = dueDateSet ? dueCard.cardSubtitle.getText().toString() : "";

		CrudeMilestone milestone =
				new CrudeMilestone()
						.name(title)
						.description(description)
						.due_date(dueDate.isEmpty() ? null : dueDate)
						.start_date(startDate.isEmpty() ? null : startDate);

		if (isEditMode) {
			viewModel.updateMilestone(requireContext(), type, projectId, milestoneId, milestone);
		} else {
			viewModel.createMilestone(requireContext(), type, projectId, milestone);
		}
	}

	private void observeViewModel() {
		viewModel
				.getIsActionLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.btnSubmit.setText(null);
								binding.btnSubmit.setEnabled(false);
								binding.loadingIndicator.setVisibility(View.VISIBLE);
							} else {
								binding.loadingIndicator.setVisibility(View.GONE);
								binding.btnSubmit.setText(
										isEditMode ? R.string.update : R.string.create);
								binding.btnSubmit.setEnabled(true);
							}
						});

		viewModel
				.getCreateSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success) && !isEditMode) {
								Toasty.show(requireContext(), R.string.milestone_created);
								viewModel.clearCreateSuccess();
								dismiss();
							}
						});

		viewModel
				.getEditSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success) && isEditMode) {
								Toasty.show(requireContext(), R.string.milestone_updated);
								viewModel.clearEditSuccess();
								dismiss();
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg == null) return;
							switch (errorMsg) {
								case "auth_error":
									Toasty.show(
											requireContext(), getString(R.string.not_authorized));
									break;
								case "access_forbidden_403":
									Toasty.show(
											requireContext(),
											getString(R.string.access_forbidden_403));
									break;
								case "not_found":
									Toasty.show(requireContext(), getString(R.string.not_found));
									break;
								case "generic_error":
									Toasty.show(
											requireContext(), getString(R.string.generic_error));
									break;
								default:
									Toasty.show(requireContext(), errorMsg);
									break;
							}
							viewModel.clearError();
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applySheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
