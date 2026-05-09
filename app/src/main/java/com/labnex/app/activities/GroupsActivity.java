package com.labnex.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.adapters.GroupsAdapter;
import com.labnex.app.bottomsheets.CreateGroupBottomSheet;
import com.labnex.app.bottomsheets.GenericMenuBottomSheet;
import com.labnex.app.databinding.ActivityGroupsBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.GroupsHierarchyBuilder;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.models.groups.GroupsItem;
import com.labnex.app.viewmodels.GroupsViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class GroupsActivity extends BaseActivity {

	private ActivityGroupsBinding binding;
	private GroupsViewModel viewModel;
	private GroupsAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityGroupsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(GroupsViewModel.class);

		binding.btnBack.setOnClickListener(v -> finish());

		binding.newGroup.setOnClickListener(
				v -> {
					if (getAccount().getAccount().getInstanceUrl().contains("gitlab.com")) {
						new com.google.android.material.dialog.MaterialAlertDialogBuilder(ctx)
								.setTitle(getString(R.string.gitlab_saas))
								.setMessage(
										getString(R.string.gitlab_saas_message)
												+ "\n\n"
												+ getString(R.string.gitlab_saas_message_extra))
								.setNegativeButton(getString(R.string.close), null)
								.show();
					} else {
						CreateGroupBottomSheet.newInstance()
								.show(getSupportFragmentManager(), "createGroupSheet");
					}
				});

		setupRecyclerView();
		setupPullToRefresh();
		observeViewModel();
		viewModel.loadGroups(ctx);
	}

	@Override
	protected void onGlobalRefresh() {
		viewModel.loadGroups(ctx);
	}

	private void setupRecyclerView() {
		adapter =
				new GroupsAdapter(
						ctx,
						new ArrayList<>(),
						new GroupsAdapter.OnGroupClickListener() {
							@Override
							public void onGroupClick(GroupsItem group) {
								Intent intent = new Intent(ctx, GroupDetailActivity.class);
								intent.putExtra("groupId", group.getId());
								startActivity(intent);
							}

							@Override
							public void onGroupMenuClick(GroupsItem group) {
								List<GenericMenuItemModel> items = new ArrayList<>();
								items.add(
										new GenericMenuItemModel(
												"edit",
												R.string.edit,
												R.drawable.ic_edit,
												com.google.android.material.R.attr
														.colorPrimaryContainer,
												com.google.android.material.R.attr
														.colorOnPrimaryContainer));

								GenericMenuBottomSheet sheet =
										GenericMenuBottomSheet.newInstance(
												group.getName(), null, items);
								sheet.setOnMenuItemClickListener(
										id -> {
											if ("edit".equals(id)) {
												CreateGroupBottomSheet.newInstance(group)
														.show(
																getSupportFragmentManager(),
																"editGroupSheet");
											}
										});
								sheet.show(getSupportFragmentManager(), "groupMenuSheet");
							}
						});

		LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(ctx);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupPullToRefresh() {
		binding.pullToRefresh.setOnRefreshListener(() -> viewModel.loadGroups(ctx));
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.pullToRefresh.setRefreshing(false);
								binding.progressBar.setVisibility(View.VISIBLE);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {
								binding.progressBar.setVisibility(View.GONE);
								binding.pullToRefresh.setRefreshing(false);
							}
						});

		viewModel
				.getGroupsList()
				.observe(
						this,
						list -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;
							if (list == null || list.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
							} else {
								List<GroupsItem> hierarchical =
										GroupsHierarchyBuilder.buildHierarchyRecursive(list);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								adapter.updateList(hierarchical);
							}
						});

		viewModel
				.getError()
				.observe(
						this,
						errorMsg -> {
							if (errorMsg == null) return;
							switch (errorMsg) {
								case "auth_error":
									Toasty.show(ctx, getString(R.string.not_authorized));
									break;
								case "access_forbidden_403":
									Toasty.show(ctx, getString(R.string.access_forbidden_403));
									break;
								case "not_found":
									Toasty.show(ctx, getString(R.string.not_found));
									break;
								case "generic_error":
									Toasty.show(ctx, getString(R.string.generic_error));
									break;
								default:
									Toasty.show(ctx, errorMsg);
									break;
							}
							viewModel.clearError();
						});
	}
}
