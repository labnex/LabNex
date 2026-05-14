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
import com.labnex.app.databinding.BottomsheetCreateBranchBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.BranchesViewModel;

/**
 * @author mmarif
 */
public class CreateBranchBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateBranchBinding binding;
	private BranchesViewModel viewModel;
	private String branch;
	private long projectId;

	public static CreateBranchBottomSheet newInstance(long projectId, @Nullable String defaultRef) {
		CreateBranchBottomSheet sheet = new CreateBranchBottomSheet();
		Bundle args = new Bundle();
		args.putLong("projectId", projectId);
		if (defaultRef != null) args.putString("defaultRef", defaultRef);
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
		binding = BottomsheetCreateBranchBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(BranchesViewModel.class);

		if (getArguments() != null && getArguments().containsKey("defaultRef")) {
			binding.branchRefInput.setText(getArguments().getString("defaultRef"));
		}

		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.btnCreate.setOnClickListener(
				v -> {
					branch =
							binding.branchNameInput.getText() != null
									? binding.branchNameInput.getText().toString().trim()
									: "";
					String ref =
							binding.branchRefInput.getText() != null
									? binding.branchRefInput.getText().toString().trim()
									: "";

					if (branch.isEmpty() || ref.isEmpty()) {
						Toasty.show(requireContext(), getString(R.string.branch_ref_required));
						return;
					}

					if (!branch.matches("^[a-zA-Z0-9._\\-]+$")) {
						Toasty.show(requireContext(), getString(R.string.branch_name_invalid));
						return;
					}
					viewModel.createBranch(requireContext(), projectId, branch, ref);
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
								binding.btnCreate.setText(null);
								binding.btnCreate.setEnabled(false);
								binding.loadingIndicator.setVisibility(View.VISIBLE);
							} else {
								binding.loadingIndicator.setVisibility(View.GONE);
								binding.btnCreate.setText(R.string.create);
								binding.btnCreate.setEnabled(true);
							}
						});

		viewModel
				.getActionSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(
										requireContext(),
										getString(R.string.branch_created, branch));
								AppUIStateManager.refreshData();
								viewModel.clearActionSuccess();
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
