package com.labnex.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.labnex.app.adapters.CommitsAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityCommitsBinding;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.CommitsViewModel;

/**
 * @author mmarif
 */
public class CommitsActivity extends BaseActivity implements BottomSheetListener {

	private ActivityCommitsBinding binding;
	private CommitsViewModel commitsViewModel;
	private CommitsAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private int projectId;
	public ProjectsContext projectsContext;
	private String source;
	private int mergeRequestIid;
	private String branch;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityCommitsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		commitsViewModel = new ViewModelProvider(this).get(CommitsViewModel.class);
		projectsContext = ProjectsContext.fromIntent(getIntent());
		resultLimit = getAccount().getMaxPageLimit();

		if (getIntent().getStringExtra("source") != null) {
			source = getIntent().getStringExtra("source");
		}
		if (getIntent().getStringExtra("branch") != null) {
			branch = getIntent().getStringExtra("branch");
		}
		mergeRequestIid = getIntent().getIntExtra("mergeRequestIid", 0);
		projectId = getIntent().getIntExtra("projectId", 0);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

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
	public void onButtonClicked(String text) {}

	private void fetchDataAsync() {

		commitsViewModel
				.getCommits(
						ctx,
						source,
						projectId,
						mergeRequestIid,
						branch,
						resultLimit,
						page,
						CommitsActivity.this,
						binding.bottomAppBar)
				.observe(
						CommitsActivity.this,
						mainList -> {
							adapter = new CommitsAdapter(CommitsActivity.this, mainList, projectId);
							adapter.setLoadMoreListener(
									new CommitsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											commitsViewModel.loadMore(
													ctx,
													source,
													projectId,
													mergeRequestIid,
													branch,
													resultLimit,
													page,
													adapter,
													CommitsActivity.this,
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
}
