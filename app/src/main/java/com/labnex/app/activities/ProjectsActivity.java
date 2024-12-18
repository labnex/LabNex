package com.labnex.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.labnex.app.R;
import com.labnex.app.adapters.ProjectsAdapter;
import com.labnex.app.databinding.ActivityProjectsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.viewmodels.ProjectsViewModel;
import java.util.Objects;

/**
 * @author mmarif
 */
public class ProjectsActivity extends BaseActivity
		implements CreateProjectActivity.UpdateInterface {

	private ActivityProjectsBinding binding;
	private ProjectsViewModel projectsViewModel;
	private ProjectsAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private int userId;
	private String source;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityProjectsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		projectsViewModel = new ViewModelProvider(this).get(ProjectsViewModel.class);

		CreateProjectActivity.setUpdateListener(this);

		resultLimit = getAccount().getMaxPageLimit();
		userId = getAccount().getUserId();

		if (getIntent().getStringExtra("source") != null) {

			source = getIntent().getStringExtra("source");

			if (Objects.requireNonNull(source).equalsIgnoreCase("starred")) {
				binding.projectsText.setText(R.string.starred_projects);
			}
		}

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.newProject.setOnClickListener(
				createProject -> {
					Intent intent = new Intent(ctx, CreateProjectActivity.class);
					ctx.startActivity(intent);
				});

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

	private void fetchDataAsync() {

		projectsViewModel
				.getProjects(
						ctx,
						source,
						"multi",
						userId,
						resultLimit,
						page,
						ProjectsActivity.this,
						binding.bottomAppBar)
				.observe(
						ProjectsActivity.this,
						listMain -> {
							adapter = new ProjectsAdapter(ProjectsActivity.this, listMain, source);
							adapter.setLoadMoreListener(
									new ProjectsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											projectsViewModel.loadMoreProjects(
													ctx,
													source,
													"multi",
													userId,
													resultLimit,
													page,
													adapter,
													ProjectsActivity.this,
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
					getString(R.string.project_created));
		}

		adapter.clearAdapter();
		page = 1;
		fetchDataAsync();
	}
}
