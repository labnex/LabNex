package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.labnex.app.databinding.BottomsheetForkProjectBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.DropdownHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.CreateProjectViewModel;
import com.labnex.app.viewmodels.ProjectDetailViewModel;
import java.util.List;

/**
 * @author mmarif
 */
public class ForkProjectBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetForkProjectBinding binding;
	private ProjectDetailViewModel projectViewModel;
	private long projectId;
	private List<CreateProjectViewModel.NamespaceItem> namespaceItems;
	private String name;
	private String path;

	public static ForkProjectBottomSheet newInstance(long projectId, String name, String path) {
		ForkProjectBottomSheet sheet = new ForkProjectBottomSheet();
		Bundle args = new Bundle();
		args.putLong("projectId", projectId);
		args.putString("name", name);
		args.putString("path", path);
		sheet.setArguments(args);
		return sheet;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetForkProjectBinding.inflate(inflater, container, false);
		projectViewModel =
				new ViewModelProvider(requireActivity()).get(ProjectDetailViewModel.class);
		CreateProjectViewModel createProjectViewModel =
				new ViewModelProvider(this).get(CreateProjectViewModel.class);

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

		if (getArguments() != null) {
			projectId = getArguments().getLong("projectId");
			name = getArguments().getString("name", "");
			path = getArguments().getString("path", "");
		}

		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.nameInput.setText(name);
		binding.pathInput.setText(path);

		createProjectViewModel.loadNamespaces(
				requireContext(),
				((BaseActivity) requireActivity()).getAccount().getUserId(),
				((BaseActivity) requireActivity()).getAccount().getUserInfo().getUsername());

		createProjectViewModel
				.getNamespaces()
				.observe(
						getViewLifecycleOwner(),
						items -> {
							namespaceItems = items;
							ArrayAdapter<CreateProjectViewModel.NamespaceItem> adapter =
									new ArrayAdapter<>(
											requireContext(), R.layout.item_dropdown_entry, items) {
										@NonNull @Override
										public View getView(
												int pos, @Nullable View v, @NonNull ViewGroup p) {
											return DropdownHelper.createItemView(
													pos,
													v,
													p,
													this,
													item ->
															"group".equals(item.kind)
																	? R.drawable.ic_groups
																	: R.drawable.ic_person,
													item -> item.fullPath);
										}

										@Override
										public View getDropDownView(
												int pos, @Nullable View v, @NonNull ViewGroup p) {
											return getView(pos, v, p);
										}
									};
							binding.namespaceInput.setAdapter(adapter);
							if (!items.isEmpty()
									&& binding.namespaceInput.getText().toString().isEmpty()) {
								binding.namespaceInput.setText(items.get(0).fullPath, false);
							}
						});

		binding.btnFork.setOnClickListener(
				v -> {
					String forkName =
							binding.nameInput.getText() != null
									? binding.nameInput.getText().toString().trim()
									: name;
					String forkPath =
							binding.pathInput.getText() != null
									? binding.pathInput.getText().toString().trim()
									: path;
					String description =
							binding.descriptionInput.getText() != null
									? binding.descriptionInput.getText().toString().trim()
									: "";
					String visibility = binding.chipPublic.isChecked() ? "public" : "private";

					Long namespaceId = null;
					if (namespaceItems != null) {
						int pos = binding.namespaceInput.getListSelection();
						if (pos >= 0
								&& pos < namespaceItems.size()
								&& "group".equals(namespaceItems.get(pos).kind)) {
							namespaceId = (long) namespaceItems.get(pos).id;
						}
					}

					projectViewModel.forkProject(
							requireContext(),
							projectId,
							forkName,
							forkPath,
							description,
							visibility,
							namespaceId);
				});

		observeViewModel();
		return binding.getRoot();
	}

	private void observeViewModel() {
		projectViewModel
				.getIsActionLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.btnFork.setText(null);
								binding.btnFork.setEnabled(false);
								binding.loadingIndicator.setVisibility(View.VISIBLE);
							} else {
								binding.loadingIndicator.setVisibility(View.GONE);
								binding.btnFork.setText(R.string.fork);
								binding.btnFork.setEnabled(true);
							}
						});

		projectViewModel
				.getForkSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(requireContext(), R.string.project_forked);
								AppUIStateManager.refreshData();
								if (getActivity() instanceof BaseActivity) {
									((BaseActivity) getActivity()).triggerGlobalRefresh();
								}
								projectViewModel.clearForkSuccess();
								dismiss();
							}
						});

		projectViewModel
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
							projectViewModel.clearError();
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
