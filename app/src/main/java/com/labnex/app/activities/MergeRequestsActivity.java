package com.labnex.app.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.labnex.app.R;
import com.labnex.app.adapters.MergeRequestsAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityMergeRequestsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.viewmodels.MergeRequestsViewModel;
import java.util.Objects;

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

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());
		Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(0).getIcon())
				.setColorFilter(
						getResources().getColor(R.color.md_light_theme_text_color, null),
						PorterDuff.Mode.SRC_IN);

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					page = 1;
					if (menuItem.getItemId() == R.id.open) {

						Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(0).getIcon())
								.setColorFilter(
										getResources()
												.getColor(R.color.md_light_theme_text_color, null),
										PorterDuff.Mode.SRC_IN);
						Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(1).getIcon())
								.clearColorFilter();
						Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(2).getIcon())
								.clearColorFilter();

						binding.progressBar.setVisibility(View.VISIBLE);
						filter = "opened";
						fetchDataAsync(filter);
					}
					if (menuItem.getItemId() == R.id.merged) {

						Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(1).getIcon())
								.setColorFilter(
										getResources()
												.getColor(R.color.md_light_theme_text_color, null),
										PorterDuff.Mode.SRC_IN);
						Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(0).getIcon())
								.clearColorFilter();
						Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(2).getIcon())
								.clearColorFilter();

						binding.progressBar.setVisibility(View.VISIBLE);
						filter = "merged";
						fetchDataAsync(filter);
					}
					if (menuItem.getItemId() == R.id.closed) {

						Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(2).getIcon())
								.setColorFilter(
										getResources()
												.getColor(R.color.md_light_theme_text_color, null),
										PorterDuff.Mode.SRC_IN);
						Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(1).getIcon())
								.clearColorFilter();
						Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(0).getIcon())
								.clearColorFilter();

						binding.progressBar.setVisibility(View.VISIBLE);
						filter = "closed";
						fetchDataAsync(filter);
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
							adapter =
									new MergeRequestsAdapter(
											MergeRequestsActivity.this, mainList, projectsContext);
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
					getString(R.string.mr_created));
		}

		adapter.clearAdapter();
		page = 1;
		fetchDataAsync(filter);
	}
}
