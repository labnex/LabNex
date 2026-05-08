package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
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
import com.labnex.app.databinding.BottomsheetCreateGroupBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.DropdownHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.groups.CreateGroup;
import com.labnex.app.models.groups.GroupsItem;
import com.labnex.app.viewmodels.GroupsViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class CreateGroupBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateGroupBinding binding;
	private GroupsViewModel viewModel;
	private boolean isEditMode = false;
	private long groupId = -1;
	private List<GroupsItem> allGroups = new ArrayList<>();

	public static CreateGroupBottomSheet newInstance() {
		return new CreateGroupBottomSheet();
	}

	public static CreateGroupBottomSheet newInstance(GroupsItem group) {
		CreateGroupBottomSheet sheet = new CreateGroupBottomSheet();
		Bundle args = new Bundle();
		args.putLong("group_id", group.getId());
		args.putSerializable("group", group);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			groupId = getArguments().getLong("group_id", -1);
			isEditMode = groupId > 0;
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateGroupBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(GroupsViewModel.class);

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

		if (isEditMode) {
			binding.switchSubgroup.setVisibility(View.GONE);
		}

		if (isEditMode && getArguments() != null) {
			GroupsItem group = (GroupsItem) getArguments().getSerializable("group");
			if (group != null) {
				binding.sheetTitle.setText(R.string.edit_group);
				binding.btnSubmit.setText(R.string.update);
				binding.nameInput.setText(group.getName());
				binding.descriptionInput.setText(
						group.getDescription() != null ? group.getDescription() : "");
				binding.switchEmails.setChecked(group.isEmailsEnabled());
				binding.switchLfs.setChecked(group.isLfsEnabled());
				binding.switchMentions.setChecked(group.isMentionsDisabled());

				String vis = group.getVisibility();
				binding.chipInternal.setChecked("internal".equals(vis));
				binding.chipPublic.setChecked("public".equals(vis));
				binding.chipPrivate.setChecked(!"internal".equals(vis) && !"public".equals(vis));
			}
		}

		binding.switchSubgroup.setOnCheckedChangeListener(
				(btn, checked) -> {
					binding.parentGroupLayout.setVisibility(checked ? View.VISIBLE : View.GONE);
				});

		binding.btnSubmit.setOnClickListener(v -> submitGroup());
		observeViewModel();

		return binding.getRoot();
	}

	private void submitGroup() {
		String name =
				binding.nameInput.getText() != null
						? binding.nameInput.getText().toString().trim()
						: "";
		if (name.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.group_name_required));
			return;
		}

		String description =
				binding.descriptionInput.getText() != null
						? binding.descriptionInput.getText().toString().trim()
						: "";
		CreateGroup group = getCreateGroup(name, description);

		if (isEditMode) {
			viewModel.updateGroup(requireContext(), groupId, group);
		} else {
			viewModel.createGroup(requireContext(), group);
		}
	}

	@NonNull private CreateGroup getCreateGroup(String name, String description) {
		String visibility =
				binding.chipInternal.isChecked()
						? "internal"
						: binding.chipPublic.isChecked() ? "public" : "private";

		CreateGroup group = new CreateGroup();
		group.setName(name);
		group.setDescription(description);
		group.setPath(name.replace(" ", "-").toLowerCase());
		group.setVisibility(visibility);
		group.setEmailsEnabled(binding.switchEmails.isChecked());
		group.setLfsEnabled(binding.switchLfs.isChecked());
		group.setMentionsDisabled(binding.switchMentions.isChecked());

		if (binding.switchSubgroup.isChecked()) {
			String selectedName = binding.parentGroupInput.getText().toString();
			for (GroupsItem g : allGroups) {
				if (g.getFullPath().equals(selectedName)) {
					group.setParentId((long) g.getId());
					Log.e("GroupID", String.valueOf(g.getId()));
					break;
				}
			}
		}

		return group;
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
				.getActionSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(
										requireContext(),
										getString(
												isEditMode
														? R.string.group_updated
														: R.string.group_created));
								AppUIStateManager.refreshData();
								if (getActivity() instanceof BaseActivity) {
									((BaseActivity) getActivity()).triggerGlobalRefresh();
								}
								viewModel.clearActionSuccess();
								dismiss();
							}
						});

		viewModel
				.getGroupsList()
				.observe(
						getViewLifecycleOwner(),
						groups -> {
							if (groups != null) {
								allGroups = groups;
								List<String> names = new ArrayList<>();
								for (GroupsItem g : groups) names.add(g.getFullPath());

								ArrayAdapter<String> adapter =
										new ArrayAdapter<>(
												requireContext(),
												R.layout.item_dropdown_entry,
												names) {
											@NonNull @Override
											public View getView(
													int pos,
													@Nullable View convertView,
													@NonNull ViewGroup parent) {
												return DropdownHelper.createItemView(
														pos,
														convertView,
														parent,
														this,
														item -> R.drawable.ic_groups,
														item -> item);
											}

											@Override
											public View getDropDownView(
													int pos,
													@Nullable View convertView,
													@NonNull ViewGroup parent) {
												return getView(pos, convertView, parent);
											}
										};
								binding.parentGroupInput.setAdapter(adapter);
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
