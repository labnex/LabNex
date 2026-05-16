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
import com.labnex.app.R;
import com.labnex.app.databinding.BottomsheetCreateWikiBinding;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.wikis.CrudeWiki;
import com.labnex.app.models.wikis.Wiki;
import com.labnex.app.viewmodels.WikisViewModel;

/**
 * @author mmarif
 */
public class CreateWikiBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateWikiBinding binding;
	private WikisViewModel viewModel;
	private long projectId;
	private String type;
	private boolean isEditMode = false;
	private String originalSlug;

	public static CreateWikiBottomSheet newInstance(
			String type, long projectId, @Nullable Wiki wiki) {
		CreateWikiBottomSheet sheet = new CreateWikiBottomSheet();
		Bundle args = new Bundle();
		args.putLong("projectId", projectId);
		args.putString("type", type);
		if (wiki != null) args.putSerializable("wiki", wiki);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			projectId = getArguments().getLong("projectId", 0);
			type = getArguments().getString("type");
			Wiki wiki = (Wiki) getArguments().getSerializable("wiki");
			if (wiki != null) {
				isEditMode = true;
				originalSlug = wiki.getSlug();
			}
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateWikiBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(WikisViewModel.class);

		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.contentInput.setOnTouchListener(
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
			Wiki w = (Wiki) getArguments().getSerializable("wiki");
			if (w != null) {
				binding.sheetTitle.setText(R.string.edit_page);
				binding.btnSubmit.setText(R.string.update);
				binding.titleInput.setText(w.getTitle());
				binding.contentInput.setText(w.getContent());
			}
		}

		binding.btnExpand.setOnClickListener(
				v -> {
					String content =
							binding.contentInput.getText() != null
									? binding.contentInput.getText().toString()
									: "";
					EditorBottomSheet editor =
							EditorBottomSheet.newInstance(
									content, null, EditorBottomSheet.EditorMode.MARKDOWN, null);
					editor.setEditorListener(
							newContent -> binding.contentInput.setText(newContent));
					editor.show(getChildFragmentManager(), "fullscreenEditor");
				});

		binding.btnSubmit.setOnClickListener(v -> submitWiki());
		observeViewModel();

		return binding.getRoot();
	}

	private void submitWiki() {
		String title =
				binding.titleInput.getText() != null
						? binding.titleInput.getText().toString().trim()
						: "";
		String content =
				binding.contentInput.getText() != null
						? binding.contentInput.getText().toString().trim()
						: "";

		if (title.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.title_required));
			return;
		}
		if (content.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.content_required));
			return;
		}

		CrudeWiki wiki = new CrudeWiki();
		wiki.setTitle(title);
		wiki.setContent(content);

		if (isEditMode) {
			viewModel.updateWiki(requireContext(), type, projectId, originalSlug, wiki);
		} else {
			viewModel.createWiki(requireContext(), type, projectId, wiki);
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
								Toasty.show(requireContext(), R.string.wiki_page_created);
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
								Toasty.show(requireContext(), R.string.wiki_page_updated);
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
