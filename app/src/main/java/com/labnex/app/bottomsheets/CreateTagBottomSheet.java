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
import com.labnex.app.databinding.BottomsheetCreateTagBinding;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.TagsViewModel;

/**
 * @author mmarif
 */
public class CreateTagBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateTagBinding binding;
	private TagsViewModel viewModel;
	private long projectId;

	public static CreateTagBottomSheet newInstance(long projectId) {
		CreateTagBottomSheet sheet = new CreateTagBottomSheet();
		Bundle args = new Bundle();
		args.putLong("projectId", projectId);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			projectId = getArguments().getLong("projectId", 0);
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateTagBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(TagsViewModel.class);

		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.btnSubmit.setOnClickListener(
				v -> {
					String tagName =
							binding.tagNameInput.getText() != null
									? binding.tagNameInput.getText().toString().trim()
									: "";
					String ref =
							binding.refInput.getText() != null
									? binding.refInput.getText().toString().trim()
									: "";
					String message =
							binding.messageInput.getText() != null
									? binding.messageInput.getText().toString().trim()
									: "";

					if (tagName.isEmpty()) {
						Toasty.show(requireContext(), getString(R.string.tag_name_empty));
						return;
					}
					if (ref.isEmpty()) {
						Toasty.show(requireContext(), getString(R.string.ref_empty));
						return;
					}

					viewModel.createTag(
							requireContext(),
							projectId,
							tagName,
							ref,
							message.isEmpty() ? null : message);
				});

		observeViewModel();

		return binding.getRoot();
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
								binding.btnSubmit.setText(R.string.create);
								binding.btnSubmit.setEnabled(true);
							}
						});

		viewModel
				.getCreateSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(requireContext(), R.string.tag_created);
								viewModel.clearCreateSuccess();
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
