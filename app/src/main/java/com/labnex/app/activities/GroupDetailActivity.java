package com.labnex.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.adapters.ProjectsAdapter;
import com.labnex.app.bottomsheets.CreateGroupBottomSheet;
import com.labnex.app.bottomsheets.GenericMenuBottomSheet;
import com.labnex.app.bottomsheets.GroupDetailBottomSheet;
import com.labnex.app.bottomsheets.LabelActionsBottomSheet;
import com.labnex.app.databinding.ActivityGroupDetailBinding;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.models.groups.GroupsItem;
import com.labnex.app.viewmodels.GroupsViewModel;
import com.labnex.app.viewmodels.ProjectsViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class GroupDetailActivity extends BaseActivity {

	private ActivityGroupDetailBinding binding;
	private GroupsViewModel groupsViewModel;
	private ProjectsViewModel projectsViewModel;
	private ProjectsAdapter projectsAdapter;
	private long groupId;
	private GroupsItem groupsItem;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, binding.scrollView, null, null);

		groupsViewModel = new ViewModelProvider(this).get(GroupsViewModel.class);
		projectsViewModel = new ViewModelProvider(this).get(ProjectsViewModel.class);
		projectsViewModel.setResultLimit(getAccount().getMaxPageLimit());

		groupId = getIntent().getIntExtra("groupId", 0);

		binding.btnBack.setOnClickListener(v -> finish());
		showGroupMenu();

		binding.groupActions.issuesFrame.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, IssuesActivity.class);
					intent.putExtra("source", "group");
					intent.putExtra("id", (int) groupId);
					startActivity(intent);
				});

		binding.groupActions.mergeRequestsFrame.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, MergeRequestsActivity.class);
					intent.putExtra("source", "group");
					intent.putExtra("id", (int) groupId);
					startActivity(intent);
				});

		binding.groupActions.labelsFrame.setOnClickListener(
				v -> {
					Bundle args = new Bundle();
					args.putString("source", "labels");
					args.putLong("groupId", groupId);
					GroupDetailBottomSheet bottomSheet = new GroupDetailBottomSheet();
					bottomSheet.setArguments(args);
					bottomSheet.show(getSupportFragmentManager(), "groupDetailBottomSheet");
				});

		binding.groupActions.membersFrame.setOnClickListener(
				v -> {
					Bundle args = new Bundle();
					args.putString("source", "members");
					args.putLong("groupId", groupId);
					GroupDetailBottomSheet bottomSheet = new GroupDetailBottomSheet();
					bottomSheet.setArguments(args);
					bottomSheet.show(getSupportFragmentManager(), "groupDetailBottomSheet");
				});

		setupProjectsList();
		observeGroups();
		observeProjects();

		groupsViewModel.loadGroupDetail(ctx, (int) groupId);
		projectsViewModel.loadProjects(ctx, "group", (int) groupId);
	}

	private void setupProjectsList() {
		projectsAdapter = new ProjectsAdapter(ctx, new ArrayList<>(), "");
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		binding.recyclerView.setAdapter(projectsAdapter);
	}

	private void observeGroups() {
		groupsViewModel
				.getIsDetailLoading()
				.observe(
						this,
						loading -> {
							binding.progressBar.setVisibility(
									Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
						});

		groupsViewModel
				.getGroupDetail()
				.observe(
						this,
						group -> {
							if (group == null) return;

							groupsItem = group;
							binding.infoFrame.setVisibility(View.VISIBLE);

							if (group.getAvatarUrl() != null && !group.getAvatarUrl().isEmpty()) {
								Glide.with(ctx)
										.load(group.getAvatarUrl())
										.diskCacheStrategy(DiskCacheStrategy.ALL)
										.placeholder(R.drawable.ic_spinner)
										.centerCrop()
										.into(binding.groupAvatar);
							} else {
								binding.groupAvatar.setImageDrawable(
										AvatarGenerator.getLetterAvatar(ctx, group.getName(), 56));
							}

							binding.groupName.setText(group.getName());
							binding.groupPath.setText(group.getFullPath());

							if ("private".equalsIgnoreCase(group.getVisibility())) {
								binding.visibilityIcon.setImageResource(R.drawable.ic_lock);
								binding.visibilityIcon.setVisibility(View.VISIBLE);
							} else {
								binding.visibilityIcon.setVisibility(View.GONE);
							}

							if (group.getDescription() != null
									&& !group.getDescription().isEmpty()) {
								binding.groupDescription.setVisibility(View.VISIBLE);
								binding.groupDescription.setText(group.getDescription());
							}
						});

		groupsViewModel
				.getError()
				.observe(
						this,
						errorMsg -> {
							if (errorMsg != null) {
								Toasty.show(ctx, getString(R.string.generic_server_response_error));
								groupsViewModel.clearError();
							}
						});
	}

	private void observeProjects() {
		projectsViewModel
				.getProjectsList()
				.observe(
						this,
						projects -> {
							if (Boolean.TRUE.equals(projectsViewModel.getIsLoading().getValue()))
								return;

							if (projects != null && !projects.isEmpty()) {
								binding.projectsHeader.setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								projectsAdapter.updateList(projects);
							}
						});

		projectsViewModel
				.getError()
				.observe(
						this,
						errorMsg -> {
							if (errorMsg != null) {
								Toasty.show(ctx, getString(R.string.generic_server_response_error));
								projectsViewModel.clearError();
							}
						});
	}

	private void showGroupMenu() {
		binding.btnMenu.setOnClickListener(
				v -> {
					List<GenericMenuItemModel> items = new ArrayList<>();
					items.add(
							new GenericMenuItemModel(
									"create_label",
									R.string.create_new_label,
									R.drawable.ic_add,
									com.google.android.material.R.attr.colorPrimaryContainer,
									com.google.android.material.R.attr.colorOnPrimaryContainer));
					items.add(
							new GenericMenuItemModel(
									"edit_group",
									R.string.edit_group,
									R.drawable.ic_edit,
									com.google.android.material.R.attr.colorSecondaryContainer,
									com.google.android.material.R.attr.colorOnSecondaryContainer));

					GenericMenuBottomSheet sheet =
							GenericMenuBottomSheet.newInstance(
									groupsItem.getName(), getString(R.string.group), items);
					sheet.setOnMenuItemClickListener(
							id -> {
								switch (id) {
									case "create_label":
										Bundle bsBundle = new Bundle();
										bsBundle.putString("source", "labels");
										bsBundle.putLong("groupId", groupId);
										LabelActionsBottomSheet bottomSheet =
												new LabelActionsBottomSheet();
										bottomSheet.setArguments(bsBundle);
										bottomSheet.show(
												getSupportFragmentManager(),
												"labelActionsBottomSheet");
										break;
									case "edit_group":
										CreateGroupBottomSheet.newInstance(groupsItem)
												.show(
														getSupportFragmentManager(),
														"editGroupSheet");
										break;
								}
							});
					sheet.show(getSupportFragmentManager(), "groupMenuSheet");
				});
	}
}
