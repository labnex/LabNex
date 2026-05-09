package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.databinding.BottomsheetCreateProjectBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.DropdownHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.CreateProjectViewModel;

/**
 * @author mmarif
 */
public class CreateProjectBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateProjectBinding binding;
	private CreateProjectViewModel viewModel;
	private ArrayAdapter<CreateProjectViewModel.NamespaceItem> namespaceAdapter;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateProjectBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(CreateProjectViewModel.class);

		viewModel.loadNamespaces(
				requireContext(),
				((BaseActivity) requireActivity()).getAccount().getUserId(),
				((BaseActivity) requireActivity()).getAccount().getUserInfo().getUsername());

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

		binding.btnCreate.setOnClickListener(
				v -> {
					String name =
							binding.nameInput.getText() != null
									? binding.nameInput.getText().toString().trim()
									: "";
					if (name.isEmpty()) {
						Toasty.show(requireContext(), getString(R.string.project_name_required));
						return;
					}

					String description =
							binding.descriptionInput.getText() != null
									? binding.descriptionInput.getText().toString().trim()
									: "";
					String defaultBranch =
							binding.branchInput.getText() != null
									? binding.branchInput.getText().toString().trim()
									: "main";
					String visibility = binding.chipPublic.isChecked() ? "public" : "private";
					boolean initWithReadme = binding.switchReadme.isChecked();
					boolean lfsEnabled = binding.switchLfs.isChecked();
					boolean emailsEnabled = binding.switchEmails.isChecked();

					CreateProjectViewModel.NamespaceItem selectedNs = null;
					int nsPos = binding.namespaceInput.getListSelection();
					if (nsPos >= 0 && namespaceAdapter != null) {
						selectedNs = namespaceAdapter.getItem(nsPos);
					}
					Integer namespaceId =
							(selectedNs != null && "group".equals(selectedNs.kind))
									? selectedNs.id
									: null;

					viewModel.createProject(
							requireContext(),
							name,
							description,
							visibility,
							initWithReadme,
							defaultBranch,
							lfsEnabled,
							emailsEnabled,
							namespaceId);
				});

		observeViewModel();

		return binding.getRoot();
	}

	private void observeViewModel() {
		viewModel
				.getNamespaces()
				.observe(
						getViewLifecycleOwner(),
						items -> {
							namespaceAdapter =
									new ArrayAdapter<>(
											requireContext(), R.layout.item_dropdown_entry, items) {
										@NonNull @Override
										public View getView(
												int position,
												@Nullable View convertView,
												@NonNull ViewGroup parent) {
											return DropdownHelper.createItemView(
													position,
													convertView,
													parent,
													this,
													item ->
															"group".equals(item.kind)
																	? R.drawable.ic_groups
																	: R.drawable.ic_person,
													item -> item.fullPath);
										}

										@Override
										public View getDropDownView(
												int position,
												@Nullable View convertView,
												@NonNull ViewGroup parent) {
											return getView(position, convertView, parent);
										}
									};
							binding.namespaceInput.setAdapter(namespaceAdapter);
							if (!items.isEmpty()
									&& binding.namespaceInput.getText().toString().isEmpty()) {
								binding.namespaceInput.setText(items.get(0).fullPath, false);
							}
						});

		viewModel
				.getIsLoading()
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
				.getIsSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(requireContext(), getString(R.string.project_created));
								AppUIStateManager.refreshData();
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
