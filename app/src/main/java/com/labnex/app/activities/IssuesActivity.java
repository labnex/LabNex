package com.labnex.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.labnex.app.R;
import com.labnex.app.adapters.IssuesAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityIssuesBinding;
import com.labnex.app.databinding.BottomsheetIssuesMenuBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.viewmodels.IssuesViewModel;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author mmarif
 */
public class IssuesActivity extends BaseActivity implements IssuesAdapter.OnIssueClickListener {

	private ActivityIssuesBinding binding;
	private IssuesViewModel viewModel;
	private IssuesAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private String source;
	private int id;
	private String filter = "opened";
	private String searchQuery = "";
	private ProjectsContext projectsContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityIssuesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(IssuesViewModel.class);
		viewModel.setResultLimit(getAccount().getMaxPageLimit());

		source = getIntent().getStringExtra("source");
		id = getIntent().getIntExtra("id", 0);

		if ("project".equals(source)) {
			projectsContext = ProjectsContext.fromIntent(getIntent());
		}

		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnMenu.setOnClickListener(v -> showFilterBottomSheet());
		binding.dockContainer.removeView(binding.newIssue);

		if (!"my_issues".equals(source)
				&& !"group".equals(source)
				&& projectsContext != null
				&& !projectsContext.getProject().isArchived()) {
			binding.dockContainer.addView(binding.newIssue);
			binding.newIssue.setOnClickListener(
					v -> {
						ProjectsContext pc =
								new ProjectsContext(
										projectsContext.getProjectName(),
										projectsContext.getPath(),
										projectsContext.getProjectId(),
										ctx);
						startActivity(pc.getIntent(ctx, CreateIssueActivity.class));
					});
		}

		setupRecyclerView();
		setupPullToRefresh();
		observeViewModel();
		viewModel.loadIssues(ctx, source, id, viewModel.getCurrentScope(), filter, searchQuery);
	}

	@Override
	protected void onGlobalRefresh() {
		viewModel.loadIssues(ctx, source, id, viewModel.getCurrentScope(), filter, searchQuery);
	}

	private void setupRecyclerView() {
		adapter = new IssuesAdapter(ctx, new ArrayList<>(), this);
		LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(ctx);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupPullToRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() ->
						viewModel.loadIssues(
								ctx, source, id, viewModel.getCurrentScope(), filter, searchQuery));
	}

	private void showFilterBottomSheet() {
		BottomSheetDialog dialog = new BottomSheetDialog(this);
		BottomsheetIssuesMenuBinding fb =
				BottomsheetIssuesMenuBinding.inflate(LayoutInflater.from(this));
		dialog.setContentView(fb.getRoot());
		UIHelper.applySheetStyle(dialog, true);

		fb.searchQueryInput.setText(searchQuery);
		fb.chipOpened.setChecked("opened".equals(filter));
		fb.chipClosed.setChecked("closed".equals(filter));

		String scope = viewModel.getCurrentScope();
		fb.chipScopeCreated.setChecked("created_by_me".equals(scope));
		fb.chipScopeAssigned.setChecked("assigned_to_me".equals(scope));
		fb.chipScopeAll.setChecked("all".equals(scope));

		fb.chipOrderCreated.setChecked("created_at".equals(viewModel.getCurrentOrderBy()));
		fb.chipOrderUpdated.setChecked("updated_at".equals(viewModel.getCurrentOrderBy()));
		fb.chipDesc.setChecked("desc".equals(viewModel.getCurrentSort()));
		fb.chipAsc.setChecked("asc".equals(viewModel.getCurrentSort()));

		fb.btnApply.setOnClickListener(
				v -> {
					searchQuery =
							Objects.requireNonNull(fb.searchQueryInput.getText()).toString().trim();

					filter = fb.chipOpened.isChecked() ? "opened" : "closed";

					if (fb.chipScopeCreated.isChecked()) viewModel.setCurrentScope("created_by_me");
					else if (fb.chipScopeAssigned.isChecked())
						viewModel.setCurrentScope("assigned_to_me");
					else viewModel.setCurrentScope("all");

					viewModel.setCurrentOrderBy(
							fb.chipOrderCreated.isChecked() ? "created_at" : "updated_at");
					viewModel.setCurrentSort(fb.chipDesc.isChecked() ? "desc" : "asc");

					viewModel.loadIssues(
							ctx, source, id, viewModel.getCurrentScope(), filter, searchQuery);
					dialog.dismiss();
				});

		dialog.show();
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.progressBar.setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {
								binding.progressBar.setVisibility(View.GONE);
								binding.pullToRefresh.setRefreshing(false);
							}
						});

		viewModel
				.getIssueList()
				.observe(
						this,
						list -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;
							if (list == null || list.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								adapter.updateList(list);
								scrollListener.resetState();
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
								case "generic_error":
									Toasty.show(ctx, getString(R.string.generic_error));
									break;
								default:
									Toasty.show(ctx, errorMsg);
									break;
							}
							viewModel.clearError();
						});

		viewModel
				.getNavigateToIssue()
				.observe(
						this,
						issueCtx -> {
							if (issueCtx != null) {
								startActivity(issueCtx.getIntent(ctx, IssueDetailActivity.class));
								viewModel.clearNavigation();
							}
						});
	}

	@Override
	public void onIssueClick(Issues issue) {
		viewModel.fetchAndNavigateIssue(ctx, issue);
	}

	@Override
	public void onAuthorClick(Issues issue) {
		if (issue.getAuthor() != null) {
			Intent intent = new Intent(ctx, ProfileActivity.class);
			intent.putExtra("source", "issues");
			intent.putExtra("userId", issue.getAuthor().getId());
			startActivity(intent);
		}
	}
}
