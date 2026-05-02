package com.labnex.app.activities;

import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.adapters.ProjectsAdapter;
import com.labnex.app.bottomsheets.CreateProjectBottomSheet;
import com.labnex.app.databinding.ActivityProjectsBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.ProjectsViewModel;
import java.util.ArrayList;

/**
 * @author mmarif
 */
public class ProjectsActivity extends BaseActivity {

	private ActivityProjectsBinding binding;
	private ProjectsViewModel viewModel;
	private ProjectsAdapter adapter;
	private String source;
	private int userId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityProjectsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(ProjectsViewModel.class);

		viewModel.setResultLimit(getAccount().getMaxPageLimit());
		userId = getAccount().getUserId();

		if (getIntent().getStringExtra("source") != null) {
			source = getIntent().getStringExtra("source");
			if ("forks".equalsIgnoreCase(source)) {
				binding.dockContainer.removeView(binding.addNew);
				userId = getIntent().getIntExtra("projectId", 0);
			}
		}

		setupRecyclerView();
		setupPullToRefresh();
		observeViewModel();

		binding.btnBack.setOnClickListener(v -> finish());

		binding.addNew.setOnClickListener(
				v -> {
					CreateProjectBottomSheet sheet = new CreateProjectBottomSheet();
					sheet.show(getSupportFragmentManager(), "createProjectSheet");
				});

		viewModel.loadProjects(ctx, source, userId);
	}

	private void setupRecyclerView() {
		adapter = new ProjectsAdapter(ctx, new ArrayList<>(), source != null ? source : "projects");
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
				() -> viewModel.loadProjects(ctx, source, userId));
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
				.getProjectsList()
				.observe(
						this,
						projects -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;

							if (projects == null || projects.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								adapter.updateList(projects);
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
	}

	@Override
	protected void onGlobalRefresh() {
		viewModel.loadProjects(ctx, source, userId);
	}
}
