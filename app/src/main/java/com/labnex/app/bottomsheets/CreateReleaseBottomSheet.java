package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.labnex.app.R;
import com.labnex.app.databinding.BottomsheetCreateReleaseBinding;
import com.labnex.app.databinding.ItemPickerCardBinding;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.release.CrudeRelease;
import com.labnex.app.models.release.Releases;
import com.labnex.app.viewmodels.ReleasesViewModel;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author mmarif
 */
public class CreateReleaseBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateReleaseBinding binding;
	private ReleasesViewModel viewModel;
	private long projectId;
	private boolean isEditMode = false;
	private String originalTagName;
	private ItemPickerCardBinding releasedAtCard;
	private boolean releasedAtSet = false;

	public static CreateReleaseBottomSheet newInstance(long projectId, @Nullable Releases release) {
		CreateReleaseBottomSheet sheet = new CreateReleaseBottomSheet();
		Bundle args = new Bundle();
		args.putLong("projectId", projectId);
		if (release != null) args.putSerializable("release", release);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			projectId = getArguments().getLong("projectId", 0);
			Releases release = (Releases) getArguments().getSerializable("release");
			if (release != null) {
				isEditMode = true;
				originalTagName = release.getTagName();
			}
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateReleaseBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(ReleasesViewModel.class);

		binding.btnClose.setOnClickListener(v -> dismiss());

		releasedAtCard = ItemPickerCardBinding.bind(binding.cardReleasedAt.getRoot());
		releasedAtCard.cardTitle.setText(R.string.released_at);
		releasedAtCard.cardSubtitle.setText(R.string.released_at_hint);
		releasedAtCard.cardIcon.setImageResource(R.drawable.ic_calendar);

		releasedAtCard.cardClear.setOnClickListener(
				v -> {
					releasedAtCard.cardSubtitle.setText(R.string.released_at_hint);
					releasedAtCard.cardClear.setVisibility(View.GONE);
					releasedAtSet = false;
				});

		binding.descriptionInput.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == android.view.MotionEvent.ACTION_UP
							|| event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});

		if (isEditMode && getArguments() != null) {
			Releases r = (Releases) getArguments().getSerializable("release");
			if (r != null) {
				binding.sheetTitle.setText(R.string.edit_release);
				binding.btnSubmit.setText(R.string.update);
				binding.tagNameInput.setText(r.getTagName());
				binding.tagNameInput.setEnabled(false);
				binding.nameInput.setText(r.getName() != null ? r.getName() : "");
				binding.descriptionInput.setText(
						r.getDescription() != null ? r.getDescription() : "");
				binding.refLayout.setVisibility(View.GONE);
				binding.cardReleasedAt.getRoot().setVisibility(View.GONE);
				if (r.getReleasedAt() != null && !r.getReleasedAt().isEmpty()) {
					releasedAtCard.cardSubtitle.setText(r.getReleasedAt());
					releasedAtCard.cardClear.setVisibility(View.VISIBLE);
					releasedAtSet = true;
				}
			}
		}

		binding.btnExpand.setOnClickListener(v -> openFullscreenEditor());

		binding.cardReleasedAt
				.getRoot()
				.setOnClickListener(
						v ->
								showDatePicker(
										date -> {
											releasedAtCard.cardSubtitle.setText(date);
											releasedAtCard.cardClear.setVisibility(View.VISIBLE);
											releasedAtSet = true;
										}));

		binding.btnSubmit.setOnClickListener(v -> submitRelease());
		observeViewModel();

		return binding.getRoot();
	}

	private void openFullscreenEditor() {
		String content =
				binding.descriptionInput.getText() != null
						? binding.descriptionInput.getText().toString()
						: "";
		EditorBottomSheet editor =
				EditorBottomSheet.newInstance(
						content, null, EditorBottomSheet.EditorMode.MARKDOWN, null);
		editor.setEditorListener(newContent -> binding.descriptionInput.setText(newContent));
		editor.show(getChildFragmentManager(), "fullscreenEditor");
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
									"%d-%02d-%02dT00:00:00Z",
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

	private void submitRelease() {
		String tagName =
				binding.tagNameInput.getText() != null
						? binding.tagNameInput.getText().toString().trim()
						: "";
		if (tagName.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.tag_name_required));
			return;
		}

		String name =
				binding.nameInput.getText() != null
						? binding.nameInput.getText().toString().trim()
						: "";
		String ref =
				binding.refInput.getText() != null
						? binding.refInput.getText().toString().trim()
						: "";
		String description =
				binding.descriptionInput.getText() != null
						? binding.descriptionInput.getText().toString().trim()
						: "";
		String releasedAt = releasedAtSet ? releasedAtCard.cardSubtitle.getText().toString() : null;

		CrudeRelease release = new CrudeRelease();
		release.setTagName(tagName);
		if (!name.isEmpty()) release.setName(name);
		if (!ref.isEmpty() && !isEditMode) release.setRef(ref);
		if (!description.isEmpty()) release.setDescription(description);
		if (releasedAt != null) release.setReleasedAt(releasedAt);

		if (isEditMode) {
			viewModel.updateRelease(requireContext(), projectId, originalTagName, release);
		} else {
			viewModel.createRelease(requireContext(), projectId, release);
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
								Toasty.show(requireContext(), R.string.release_created);
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
								Toasty.show(requireContext(), R.string.release_updated);
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
			UIHelper.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
