package com.labnex.app.activities;

import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.adapters.CommitsAdapter;
import com.labnex.app.databinding.ActivityCommitsBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.CommitsViewModel;
import java.util.ArrayList;

/**
 * @author mmarif
 */
public class CommitsActivity extends BaseActivity {

	private ActivityCommitsBinding binding;
	private CommitsViewModel viewModel;
	private CommitsAdapter adapter;

	private String source;
	private long projectId;
	private long mergeRequestIid;
	private String branch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityCommitsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(CommitsViewModel.class);

		source = getIntent().getStringExtra("source");
		projectId = getIntent().getLongExtra("projectId", 0);
		mergeRequestIid = getIntent().getLongExtra("mergeRequestIid", 0);
		branch = getIntent().getStringExtra("branch");

		binding.btnBack.setOnClickListener(v -> finish());

		setupRecyclerView();
		setupPullToRefresh();
		observeViewModel();
		viewModel.loadCommits(ctx, source, projectId, mergeRequestIid, branch);
	}

	@Override
	protected void onGlobalRefresh() {
		viewModel.loadCommits(ctx, source, projectId, mergeRequestIid, branch);
	}

	private void setupRecyclerView() {
		adapter = new CommitsAdapter(ctx, new ArrayList<>(), projectId);

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
		binding.pullToRefresh.setOnRefreshListener(
				() -> viewModel.loadCommits(ctx, source, projectId, mergeRequestIid, branch));
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
				.getCommitsList()
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
