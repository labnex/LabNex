package com.labnex.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.labnex.app.R;
import com.labnex.app.adapters.IssuesAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityIssuesBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.viewmodels.IssuesViewModel;

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
	private final String state = "opened";
	private String source;
	private int id;
	public ProjectsContext projectsContext;
	public static boolean updateIssuesList = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityIssuesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);
		projectsContext = ProjectsContext.fromIntent(getIntent());
		resultLimit = getAccount().getMaxPageLimit();

		CreateIssueActivity.setUpdateListener(this);

		if (getIntent().getStringExtra("source") != null) {
			source = getIntent().getStringExtra("source");
		}
		id = getIntent().getIntExtra("id", 0);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		Bundle bsBundle = new Bundle();

		if (source.equalsIgnoreCase("my_issues") || projectsContext.getProject().isArchived()) {
			binding.newIssue.setVisibility(View.GONE);
		} else {
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
											fetchDataAsync();
											binding.progressBar.setVisibility(View.VISIBLE);
										},
										250));

		fetchDataAsync();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (updateIssuesList) {
			page = 1;
			fetchDataAsync();
			updateIssuesList = false;
		}
	}

	private void fetchDataAsync() {

		issuesViewModel
				.getIssues(
						ctx,
						source,
						id,
						scope,
						state,
						resultLimit,
						page,
						IssuesActivity.this,
						binding.bottomAppBar)
				.observe(
						IssuesActivity.this,
						mainList -> {
							adapter = new IssuesAdapter(IssuesActivity.this, mainList);
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
													state,
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

								binding.recyclerView.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {

								adapter.notifyDataChanged();
								binding.recyclerView.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
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
		fetchDataAsync();
	}
}
