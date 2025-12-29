package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetLabelActionsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.labels.CrudeLabel;
import com.labnex.app.models.labels.Labels;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.flag.BubbleFlag;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class LabelActionsBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetLabelActionsBinding bottomSheetLabelActionsBinding;
	private int groupId;
	private int projectId;
	private String labelColor = "";
	private ColorPickerPreferenceManager colorManager;
	private String source;
	private String type;

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

		bottomSheetLabelActionsBinding =
				BottomSheetLabelActionsBinding.inflate(inflater, container, false);

		Bundle bundle = getArguments();
		assert bundle != null;

		groupId = bundle.getInt("groupId");
		projectId = bundle.getInt("projectId");

		if (bundle.getString("source") != null) {
			source = bundle.getString("source");
		} else {
			source = "";
		}

		if (bundle.getString("type") != null) {
			type = bundle.getString("type");
		} else {
			type = "";
		}

		bottomSheetLabelActionsBinding.closeBs.setOnClickListener(close -> dismiss());

		colorManager = ColorPickerPreferenceManager.getInstance(requireContext());
		colorManager.clearSavedAllData();
		bottomSheetLabelActionsBinding.colorPicker.setBackgroundColor(
				colorManager.getColor("colorPickerDialogLabels", Color.RED));

		if (source.equalsIgnoreCase("edit")) {

			bottomSheetLabelActionsBinding.bsTitle.setText(
					getString(R.string.update_label, bundle.getString("name")));
			bottomSheetLabelActionsBinding.title.setText(bundle.getString("name"));
			bottomSheetLabelActionsBinding.description.setText(bundle.getString("description"));
			bottomSheetLabelActionsBinding.create.setText(getString(R.string.update));
			labelColor = bundle.getString("color");

			bottomSheetLabelActionsBinding.colorPicker.setBackgroundColor(
					colorManager.getColor("colorPickerDialogLabels", Color.parseColor(labelColor)));

			bottomSheetLabelActionsBinding.colorPicker.setOnClickListener(
					v -> {
						colorPicker();
					});

			bottomSheetLabelActionsBinding.create.setOnClickListener(
					save -> {
						disableButton();
						String title =
								Objects.requireNonNull(
												bottomSheetLabelActionsBinding.title.getText())
										.toString();
						String newLabelColor;

						if (labelColor.isEmpty()) {

							newLabelColor =
									String.format(
											"#%06X",
											(0xFFFFFF
													& ContextCompat.getColor(
															requireContext(),
															R.color.label_default_color)));
						} else {

							newLabelColor = labelColor;
						}

						if (title.isEmpty()) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetLabelActionsBinding.mainBsFrame,
									getString(R.string.label_title_empty));
							return;
						}

						updateLabel(
								title,
								bundle.getInt("id"),
								Objects.requireNonNull(
												bottomSheetLabelActionsBinding.description
														.getText())
										.toString(),
								newLabelColor);
					});
		} else {

			bottomSheetLabelActionsBinding.colorPicker.setOnClickListener(
					v -> {
						colorPicker();
					});

			bottomSheetLabelActionsBinding.create.setOnClickListener(
					save -> {
						disableButton();
						String title =
								Objects.requireNonNull(
												bottomSheetLabelActionsBinding.title.getText())
										.toString();
						String newLabelColor;

						if (labelColor.isEmpty()) {

							newLabelColor =
									String.format(
											"#%06X",
											(0xFFFFFF
													& ContextCompat.getColor(
															requireContext(),
															R.color.label_default_color)));
						} else {

							newLabelColor = labelColor;
						}

						if (title.isEmpty()) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetLabelActionsBinding.mainBsFrame,
									getString(R.string.label_title_empty));
							return;
						}

						createNewLabel(
								title,
								Objects.requireNonNull(
												bottomSheetLabelActionsBinding.description
														.getText())
										.toString(),
								newLabelColor);
					});
		}

		return bottomSheetLabelActionsBinding.getRoot();
	}

	private void colorPicker() {

		ColorPickerDialog.Builder builder =
				new ColorPickerDialog.Builder(requireContext())
						.setPreferenceName("colorPickerDialogLabels")
						.setPositiveButton(
								getString(R.string.ok),
								(ColorEnvelopeListener)
										(envelope, clicked) -> {
											bottomSheetLabelActionsBinding.colorPicker
													.setBackgroundColor(envelope.getColor());
											labelColor =
													String.format(
															"#%06X",
															(0xFFFFFF & envelope.getColor()));
										})
						.attachAlphaSlideBar(true)
						.attachBrightnessSlideBar(true)
						.setBottomSpace(16);

		builder.getColorPickerView().setFlagView(new BubbleFlag(requireContext()));

		if (!labelColor.equalsIgnoreCase("")) {
			int labelColorCurrent = Color.parseColor(labelColor);
			builder.getColorPickerView().setInitialColor(labelColorCurrent);
		} else {
			colorManager.setColor("colorPickerDialogLabels", Color.RED);
		}

		builder.getColorPickerView().setLifecycleOwner(this);
		builder.show();
	}

	private void createNewLabel(String title, String description, String color) {

		CrudeLabel createLabelFunc = new CrudeLabel();
		createLabelFunc.setName(title);
		createLabelFunc.setDescription(description);
		createLabelFunc.setColor(color);

		Call<Labels> call;
		if (type.equalsIgnoreCase("project")) {
			call =
					RetrofitClient.getApiInterface(requireContext())
							.createProjectLabel(projectId, createLabelFunc);
		} else {
			call =
					RetrofitClient.getApiInterface(requireContext())
							.createGroupLabel(groupId, createLabelFunc);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Labels> call,
							@NonNull retrofit2.Response<Labels> response) {

						if (response.code() == 201) {

							dismiss();
							UpdateInterface.updateDataListener("created");
						} else if (response.code() == 401) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetLabelActionsBinding.mainBsFrame,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetLabelActionsBinding.mainBsFrame,
									getString(R.string.access_forbidden_403));
						} else {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetLabelActionsBinding.mainBsFrame,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Labels> call, @NonNull Throwable t) {

						enableButton();
						labelColor = "";
						Snackbar.info(
								requireContext(),
								bottomSheetLabelActionsBinding.mainBsFrame,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void updateLabel(String title, int id, String description, String color) {

		CrudeLabel updateLabel = new CrudeLabel();
		updateLabel.setNew_name(title);
		updateLabel.setDescription(description);
		updateLabel.setColor(color);

		Call<Labels> call;
		if (type.equalsIgnoreCase("project")) {
			call =
					RetrofitClient.getApiInterface(requireContext())
							.updateProjectLabel(projectId, id, updateLabel);
		} else {
			call =
					RetrofitClient.getApiInterface(requireContext())
							.updateGroupLabel(groupId, id, updateLabel);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Labels> call,
							@NonNull retrofit2.Response<Labels> response) {

						if (response.code() == 200) {

							dismiss();
							UpdateInterface.updateDataListener("updated");
						} else if (response.code() == 401) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetLabelActionsBinding.mainBsFrame,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetLabelActionsBinding.mainBsFrame,
									getString(R.string.access_forbidden_403));
						} else {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetLabelActionsBinding.mainBsFrame,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Labels> call, @NonNull Throwable t) {

						enableButton();
						labelColor = "";
						Snackbar.info(
								requireContext(),
								bottomSheetLabelActionsBinding.mainBsFrame,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void disableButton() {
		bottomSheetLabelActionsBinding.create.setEnabled(false);
		bottomSheetLabelActionsBinding.create.setAlpha(.5F);
	}

	private void enableButton() {
		bottomSheetLabelActionsBinding.create.setEnabled(true);
		bottomSheetLabelActionsBinding.create.setAlpha(1F);
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_label_actions);

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
						behavior.setHideable(true);
						behavior.setSkipCollapsed(true);
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
