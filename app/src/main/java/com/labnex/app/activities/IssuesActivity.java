package com.labnex.app.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.labnex.app.R;
import com.labnex.app.adapters.IssuesAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityIssuesBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.viewmodels.IssuesViewModel;
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

		Objects.requireNonNull(binding.bottomAppBar.getMenu().getItem(0).getIcon())
				.setColorFilter(
						getResources().getColor(R.color.md_light_theme_text_color, null),
						PorterDuff.Mode.SRC_IN);

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					page = 1;
					if (menuItem.getItemId() == R.id.open) {

						updateIconColors(0);
						binding.progressBar.setVisibility(View.VISIBLE);
						filter = "opened";
						fetchDataAsync(filter);
					}
					if (menuItem.getItemId() == R.id.closed) {

						updateIconColors(1);
						binding.progressBar.setVisibility(View.VISIBLE);
						filter = "closed";
						fetchDataAsync(filter);
					}
					return true;
				});

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
											fetchDataAsync(filter);
											binding.progressBar.setVisibility(View.VISIBLE);
										},
										250));

		updateIconColors(0);
		fetchDataAsync(filter);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (updateIssuesList) {
			page = 1;
			fetchDataAsync(filter);
			updateIssuesList = false;
		}
	}

	private void fetchDataAsync(String filter) {

		issuesViewModel
				.getIssues(
						ctx,
						source,
						id,
						scope,
						filter,
						resultLimit,
						page,
						IssuesActivity.this,
						binding.bottomAppBar)
				.observe(
						IssuesActivity.this,
						mainList -> {
							if (mainList != null) {
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
														filter,
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
							}
							binding.progressBar.setVisibility(View.GONE);
						});
	}

	private void updateIconColors(int selectedIndex) {

		Menu menu = binding.bottomAppBar.getMenu();
		for (int i = 0; i < menu.size(); i++) {

			Drawable icon = Objects.requireNonNull(menu.getItem(i).getIcon());
			if (i == selectedIndex) {

				icon.setColorFilter(
						getResources().getColor(R.color.md_theme_primary_dark, null),
						PorterDuff.Mode.SRC_IN);
				int width = (int) (icon.getIntrinsicWidth() * 1.3);
				int height = (int) (icon.getIntrinsicHeight() * 1.3);
				icon.setBounds(0, 0, width, height);
			} else {

				icon.clearColorFilter();
				icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
			}
			menu.getItem(i).setIcon(icon);
		}
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
		fetchDataAsync(filter);
	}
}
