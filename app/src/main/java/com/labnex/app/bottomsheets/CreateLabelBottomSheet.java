package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.databinding.BottomsheetCreateLabelBinding;
import com.labnex.app.helpers.ColorInverter;
import com.labnex.app.helpers.LabelStylingHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.labels.CrudeLabel;
import com.labnex.app.models.labels.Labels;
import com.labnex.app.viewmodels.LabelsViewModel;
import java.util.Objects;

/**
 * @author mmarif
 */
public class CreateLabelBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateLabelBinding binding;
	private LabelsViewModel viewModel;
	private String type;
	private long id;
	private boolean isEditMode = false;
	private long labelId;
	private String selectedColor = "#2E7D32";
	private LabelStylingHelper stylingHelper;

	public static CreateLabelBottomSheet newInstance(String type, long id, Labels label) {
		CreateLabelBottomSheet sheet = new CreateLabelBottomSheet();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putLong("id", id);
		args.putSerializable("label", label);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			type = getArguments().getString("type", "project");
			id = getArguments().getLong("id", 0);
			Labels label = (Labels) getArguments().getSerializable("label");
			if (label != null) {
				isEditMode = true;
				labelId = label.getId();
			}
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateLabelBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(LabelsViewModel.class);
		stylingHelper = LabelStylingHelper.getInstance(requireContext());

		binding.btnClose.setOnClickListener(v -> dismiss());

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
			Labels label = (Labels) getArguments().getSerializable("label");
			if (label != null) {
				binding.sheetTitle.setText(R.string.edit_label);
				binding.btnSubmit.setText(R.string.update);
				binding.nameInput.setText(label.getName());
				binding.descriptionInput.setText(
						label.getDescription() != null ? label.getDescription().toString() : "");
				selectedColor = label.getColor();
				if (!selectedColor.startsWith("#")) selectedColor = "#" + selectedColor;
			}
		}

		binding.nameInput.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

					@Override
					public void onTextChanged(CharSequence s, int st, int b, int c) {
						updateLivePreview(s.toString(), selectedColor);
					}

					@Override
					public void afterTextChanged(Editable s) {}
				});

		updateLivePreview(
				Objects.requireNonNull(binding.nameInput.getText()).toString(), selectedColor);

		binding.colorPickerTrigger.setOnClickListener(
				v -> {
					ColorPickerBottomSheet picker =
							ColorPickerBottomSheet.newInstance(selectedColor);
					picker.setOnColorSelectedListener(
							hex -> {
								selectedColor = hex;
								updateLivePreview(
										Objects.requireNonNull(binding.nameInput.getText())
												.toString(),
										selectedColor);
							});
					picker.show(getChildFragmentManager(), "colorPicker");
				});

		binding.btnSubmit.setOnClickListener(v -> submitLabel());
		observeViewModel();

		return binding.getRoot();
	}

	private void updateLivePreview(String name, String colorStr) {
		String colorHex = colorStr.startsWith("#") ? colorStr : "#" + colorStr;
		try {
			int color = Color.parseColor(colorHex);
			int contrast = ColorInverter.getContrastColor(color);
			binding.previewCard.setCardBackgroundColor(color);

			if (LabelStylingHelper.isScopedLabel(name)) {
				stylingHelper.styleScopedLabel(
						name,
						colorHex,
						String.format("#%06X", (0xFFFFFF & contrast)),
						binding.labelPreviewText,
						binding.labelPreviewValue,
						13,
						6,
						12);
			} else {
				binding.labelPreviewValue.setVisibility(View.GONE);
				String displayName = name.isEmpty() ? getString(R.string.label_name) : name;
				stylingHelper.styleRegularLabel(
						displayName,
						colorHex,
						String.format("#%06X", (0xFFFFFF & contrast)),
						binding.labelPreviewText,
						13,
						6,
						12);
			}
			binding.colorIndicator.setBackgroundTintList(ColorStateList.valueOf(color));
		} catch (Exception e) {
			binding.previewCard.setCardBackgroundColor(Color.LTGRAY);
		}
	}

	private void submitLabel() {
		String name =
				binding.nameInput.getText() != null
						? binding.nameInput.getText().toString().trim()
						: "";
		if (name.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.label_title_empty));
			return;
		}

		String description =
				binding.descriptionInput.getText() != null
						? binding.descriptionInput.getText().toString().trim()
						: "";
		String color = selectedColor.startsWith("#") ? selectedColor : "#" + selectedColor;

		CrudeLabel label = new CrudeLabel();
		label.setDescription(description);
		label.setColor(color);

		if (isEditMode) {
			label.setNew_name(name);
			viewModel.updateLabel(requireContext(), type, id, labelId, label);
		} else {
			label.setName(name);
			viewModel.createLabel(requireContext(), type, id, label);
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
								Toasty.show(requireContext(), R.string.label_created);
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
								Toasty.show(requireContext(), R.string.label_updated);
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
