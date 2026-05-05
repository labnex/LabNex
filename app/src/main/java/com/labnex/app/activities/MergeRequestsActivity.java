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
import com.labnex.app.adapters.MergeRequestsAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityMergeRequestsBinding;
import com.labnex.app.databinding.BottomsheetMergeRequestsMenuBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.viewmodels.MergeRequestsViewModel;
import java.util.ArrayList;
import java.util.Objects;

/**
 * @author mmarif
 */
public class MergeRequestsActivity extends BaseActivity
		implements MergeRequestsAdapter.OnMrClickListener {

	private ActivityMergeRequestsBinding binding;
	private MergeRequestsViewModel viewModel;
	private MergeRequestsAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private String source;
	private int id;
	private final String scope = "created_by_me";
	private String filter = "opened";
	private String searchQuery = "";
	private ProjectsContext projectsContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMergeRequestsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(MergeRequestsViewModel.class);
		viewModel.setResultLimit(getAccount().getMaxPageLimit());

		source = getIntent().getStringExtra("source");
		id = getIntent().getIntExtra("id", 0);

		if ("mr".equals(source)) {
			projectsContext = ProjectsContext.fromIntent(getIntent());
		}

		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnMenu.setOnClickListener(v -> showFilterBottomSheet());
		binding.dockContainer.removeView(binding.newMergeRequest);

		if (!"my_merge_requests".equals(source)
				&& !"group".equals(source)
				&& projectsContext != null
				&& !projectsContext.getProject().isArchived()) {
			binding.dockContainer.addView(binding.newMergeRequest);
			binding.newMergeRequest.setOnClickListener(
					v -> {
						ProjectsContext pc =
								new ProjectsContext(
										projectsContext.getProjectName(),
										projectsContext.getPath(),
										projectsContext.getProjectId(),
										ctx);
						startActivity(pc.getIntent(ctx, CreateMergeRequestActivity.class));
					});
		}

		setupRecyclerView();
		setupPullToRefresh();
		observeViewModel();
		viewModel.loadMergeRequests(ctx, source, id, scope, filter, searchQuery);
	}

	@Override
	protected void onGlobalRefresh() {
		viewModel.loadMergeRequests(ctx, source, id, scope, filter, searchQuery);
	}

	private void setupRecyclerView() {
		adapter = new MergeRequestsAdapter(ctx, new ArrayList<>(), this);
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
				() -> viewModel.loadMergeRequests(ctx, source, id, scope, filter, searchQuery));
	}

	private void showFilterBottomSheet() {
		BottomSheetDialog dialog = new BottomSheetDialog(this);
		BottomsheetMergeRequestsMenuBinding fb =
				BottomsheetMergeRequestsMenuBinding.inflate(LayoutInflater.from(this));
		dialog.setContentView(fb.getRoot());
		UIHelper.applySheetStyle(dialog, true);

		fb.search.setText(searchQuery);

		fb.chipOpened.setChecked("opened".equals(filter));
		fb.chipMerged.setChecked("merged".equals(filter));
		fb.chipClosed.setChecked("closed".equals(filter));

		String currentScope = viewModel.getCurrentScope();
		fb.chipScopeCreated.setChecked("created_by_me".equals(currentScope));
		fb.chipScopeAssigned.setChecked("assigned_to_me".equals(currentScope));
		fb.chipScopeReview.setChecked("reviews_for_me".equals(currentScope));
		fb.chipScopeAll.setChecked("all".equals(currentScope));

		fb.chipOrderCreated.setChecked("created_at".equals(viewModel.getCurrentOrderBy()));
		fb.chipOrderUpdated.setChecked("updated_at".equals(viewModel.getCurrentOrderBy()));
		fb.chipDesc.setChecked("desc".equals(viewModel.getCurrentSort()));
		fb.chipAsc.setChecked("asc".equals(viewModel.getCurrentSort()));

		fb.btnApply.setOnClickListener(
				v -> {
					searchQuery = Objects.requireNonNull(fb.search.getText()).toString().trim();

					if (fb.chipOpened.isChecked()) filter = "opened";
					else if (fb.chipMerged.isChecked()) filter = "merged";
					else filter = "closed";

					if (fb.chipScopeCreated.isChecked()) viewModel.setCurrentScope("created_by_me");
					else if (fb.chipScopeAssigned.isChecked())
						viewModel.setCurrentScope("assigned_to_me");
					else if (fb.chipScopeReview.isChecked())
						viewModel.setCurrentScope("reviews_for_me");
					else viewModel.setCurrentScope("all");

					viewModel.setCurrentOrderBy(
							fb.chipOrderCreated.isChecked() ? "created_at" : "updated_at");
					viewModel.setCurrentSort(fb.chipDesc.isChecked() ? "desc" : "asc");

					viewModel.loadMergeRequests(
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
				.getMrList()
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
				.getNavigateToMr()
				.observe(
						this,
						mrCtx -> {
							if (mrCtx != null) {
								startActivity(
										mrCtx.getIntent(ctx, MergeRequestDetailActivity.class));
								viewModel.clearNavigation();
							}
						});
	}

	@Override
	public void onMrClick(MergeRequests mr) {
		viewModel.fetchAndNavigateMr(ctx, mr);
	}

	@Override
	public void onAuthorClick(MergeRequests mr) {
		if (mr.getAuthor() != null) {
			Intent intent = new Intent(ctx, ProfileActivity.class);
			intent.putExtra("source", "mr");
			intent.putExtra("userId", mr.getAuthor().getId());
			startActivity(intent);
		}
	}
}
