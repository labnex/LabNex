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
import com.labnex.app.adapters.IssuesAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityIssuesBinding;
import com.labnex.app.databinding.BottomSheetIssuesMenuBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.viewmodels.IssuesViewModel;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author mmarif
 */
public class IssuesActivity extends BaseActivity implements CreateIssueActivity.UpdateInterface {

	private ActivityIssuesBinding binding;
	private IssuesViewModel issuesViewModel;
	private IssuesAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private final String scope = "created_by_me";
	private String source;
	private int id;
	public ProjectsContext projectsContext;
	public static boolean updateIssuesList = false;
	private String filter = "opened";
	private String searchQuery = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityIssuesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);
		resultLimit = getAccount().getMaxPageLimit();

		CreateIssueActivity.setUpdateListener(this);

		if (getIntent().getStringExtra("source") != null) {
			source = getIntent().getStringExtra("source");
		}
		id = getIntent().getIntExtra("id", 0);

		if ("project".equals(source)) {
			projectsContext = ProjectsContext.fromIntent(getIntent());
		}

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
		adapter = new IssuesAdapter(this, new ArrayList<>());
		binding.recyclerView.setAdapter(adapter);

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					if (menuItem.getItemId() == R.id.menu) {
						showFilterBottomSheet();
						return true;
					}
					return false;
				});

		if ("my_issues".equals(source)
				|| "group".equals(source)
				|| (projectsContext != null && projectsContext.getProject().isArchived())) {
			binding.newIssue.setVisibility(View.GONE);
		} else if ("project".equals(source)) {
			binding.newIssue.setOnClickListener(
					accounts -> {
						ProjectsContext project =
								new ProjectsContext(
										projectsContext.getProjectName(),
										projectsContext.getPath(),
										projectsContext.getProjectId(),
										ctx);
						Intent intent = project.getIntent(ctx, CreateIssueActivity.class);
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
											fetchDataAsync(filter, searchQuery);
											binding.progressBar.setVisibility(View.VISIBLE);
										},
										250));

		fetchDataAsync(filter, searchQuery);
	}

	private void showFilterBottomSheet() {

		BottomSheetIssuesMenuBinding sheetBinding =
				BottomSheetIssuesMenuBinding.inflate(LayoutInflater.from(this), null, false);
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		sheetBinding.search.setText(searchQuery);

		ChipGroup chipGroup = sheetBinding.issueFilterChips;
		if ("opened".equals(filter)) {
			sheetBinding.chipOpened.setChecked(true);
		} else {
			sheetBinding.chipClosed.setChecked(true);
		}

		chipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					page = 1;
					binding.progressBar.setVisibility(View.VISIBLE);
					if (checkedIds.contains(R.id.chip_opened)) {
						filter = "opened";
					} else if (checkedIds.contains(R.id.chip_closed)) {
						filter = "closed";
					}
					searchQuery =
							Objects.requireNonNull(sheetBinding.search.getText()).toString().trim();
					fetchDataAsync(filter, searchQuery);
					bottomSheetDialog.dismiss();
				});

		sheetBinding.searchLayout.setEndIconOnClickListener(
				v -> {
					page = 1;
					binding.progressBar.setVisibility(View.VISIBLE);
					searchQuery =
							Objects.requireNonNull(sheetBinding.search.getText()).toString().trim();
					fetchDataAsync(filter, searchQuery);
					bottomSheetDialog.dismiss();
				});

		bottomSheetDialog.show();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (updateIssuesList) {
			page = 1;
			fetchDataAsync(filter, searchQuery);
			updateIssuesList = false;
		}
	}

	private void fetchDataAsync(String filter, String searchQuery) {
		issuesViewModel
				.getIssues(
						ctx,
						source,
						id,
						scope,
						filter,
						searchQuery,
						resultLimit,
						page,
						IssuesActivity.this,
						binding.bottomAppBar)
				.observe(
						IssuesActivity.this,
						mainList -> {
							if (mainList != null) {
								adapter.updateList(mainList);
								adapter.setLoadMoreListener(
										new IssuesAdapter.OnLoadMoreListener() {
											@Override
											public void onLoadMore() {
												page += 1;
												issuesViewModel.loadMore(
														ctx,
														source,
														id,
														scope,
														filter,
														searchQuery,
														resultLimit,
														page,
														adapter,
														IssuesActivity.this,
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
					getString(R.string.issue_created));
		}

		adapter.clearAdapter();
		page = 1;
		fetchDataAsync(filter, searchQuery);
	}
}
