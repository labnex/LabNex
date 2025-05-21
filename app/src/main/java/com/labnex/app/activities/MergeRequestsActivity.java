package com.labnex.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.labnex.app.R;
import com.labnex.app.adapters.MergeRequestsAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityMergeRequestsBinding;
import com.labnex.app.databinding.BottomSheetMergeRequestsMenuBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.viewmodels.MergeRequestsViewModel;

/**
 * @author mmarif
 */
public class MergeRequestsActivity extends BaseActivity
		implements CreateMergeRequestActivity.UpdateInterface {

	private ActivityMergeRequestsBinding binding;
	private MergeRequestsViewModel mergeRequestsViewModel;
	private MergeRequestsAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private final String scope = "created_by_me";
	private String source;
	private int projectId;
	public ProjectsContext projectsContext;
	public static boolean updateMergeRequestList = false;
	private String filter = "opened";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityMergeRequestsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		mergeRequestsViewModel = new ViewModelProvider(this).get(MergeRequestsViewModel.class);
		projectsContext = ProjectsContext.fromIntent(getIntent());
		resultLimit = getAccount().getMaxPageLimit();

		CreateMergeRequestActivity.setUpdateListener(this);

		if (getIntent().getStringExtra("source") != null) {
			source = getIntent().getStringExtra("source");
		}
		projectId = getIntent().getIntExtra("projectId", 0);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
		adapter = new MergeRequestsAdapter(this, null, projectsContext);
		binding.recyclerView.setAdapter(adapter);

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					if (menuItem.getItemId() == R.id.menu) {
						showFilterBottomSheet();
						return true;
					}
					return false;
				});

		Bundle bsBundle = new Bundle();

		if (source.equalsIgnoreCase("my_merge_requests")
				|| projectsContext.getProject().isArchived()) {
			binding.newMergeRequest.setVisibility(View.GONE);
		} else {
			binding.newMergeRequest.setOnClickListener(
					accounts -> {
						ProjectsContext project =
								new ProjectsContext(
										projectsContext.getProjectName(),
										projectsContext.getPath(),
										projectsContext.getProjectId(),
										ctx);
						Intent intent = project.getIntent(ctx, CreateMergeRequestActivity.class);
						ctx.startActivity(intent);
					});
		}

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											binding.pullToRefresh.setRefreshing(false);
											fetchDataAsync(filter);
											binding.progressBar.setVisibility(View.VISIBLE);
										},
										250));

		fetchDataAsync(filter);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (updateMergeRequestList) {
			page = 1;
			fetchDataAsync(filter);
			updateMergeRequestList = false;
		}
	}

	private void showFilterBottomSheet() {

		BottomSheetMergeRequestsMenuBinding sheetBinding =
				BottomSheetMergeRequestsMenuBinding.inflate(LayoutInflater.from(this), null, false);
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		ChipGroup chipGroup = sheetBinding.mergeRequestFilterChips;
		if ("opened".equals(filter)) {
			sheetBinding.chipOpened.setChecked(true);
		} else if ("merged".equals(filter)) {
			sheetBinding.chipMerged.setChecked(true);
		} else {
			sheetBinding.chipClosed.setChecked(true);
		}

		chipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					page = 1;
					binding.progressBar.setVisibility(View.VISIBLE);
					if (checkedIds.contains(R.id.chip_opened)) {
						filter = "opened";
					} else if (checkedIds.contains(R.id.chip_merged)) {
						filter = "merged";
					} else if (checkedIds.contains(R.id.chip_closed)) {
						filter = "closed";
					}
					fetchDataAsync(filter);
					bottomSheetDialog.dismiss();
				});

		bottomSheetDialog.show();
	}

	private void fetchDataAsync(String filter) {
		mergeRequestsViewModel
				.getMergeRequests(
						ctx,
						source,
						projectId,
						scope,
						filter,
						resultLimit,
						page,
						MergeRequestsActivity.this,
						binding.bottomAppBar)
				.observe(
						MergeRequestsActivity.this,
						mainList -> {
							if (mainList != null) {
								adapter.updateList(mainList);
								adapter.setLoadMoreListener(
										new MergeRequestsAdapter.OnLoadMoreListener() {
											@Override
											public void onLoadMore() {
												page += 1;
												mergeRequestsViewModel.loadMore(
														ctx,
														source,
														projectId,
														scope,
														filter,
														resultLimit,
														page,
														adapter,
														MergeRequestsActivity.this,
														binding.bottomAppBar);
												binding.progressBar.setVisibility(View.VISIBLE);
											}

											@Override
											public void onLoadFinished() {
												binding.progressBar.setVisibility(View.GONE);
											}
										});

								if (adapter.getItemCount() > 0) {
									binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								} else {
									binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								}
							}
							binding.progressBar.setVisibility(View.GONE);
						});
	}

	@Override
	public void updateDataListener(String str) {

		if (str.equalsIgnoreCase("created")) {
			Snackbar.info(
					ctx,
					findViewById(android.R.id.content),
					binding.bottomAppBar,
					getString(R.string.mr_created));
		}

		adapter.clearAdapter();
		page = 1;
		fetchDataAsync(filter);
	}
}
