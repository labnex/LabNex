package com.labnex.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.labnex.app.R;
import com.labnex.app.adapters.FilesAdapter;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityFilesBrowserBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.repository.Tree;
import com.labnex.app.viewmodels.FilesViewModel;
import java.util.ArrayList;

/**
 * @author mmarif
 */
public class FilesBrowserActivity extends BaseActivity
		implements FilesAdapter.FilesAdapterListener, CreateFileActivity.UpdateInterface {

	private ActivityFilesBrowserBinding binding;
	private FilesViewModel filesViewModel;
	private FilesAdapter filesAdapter;
	private String source;
	private int projectId;
	private String branch;
	private int resultLimit;
	public ProjectsContext projectsContext;
	private String path;
	private boolean pathSetter = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityFilesBrowserBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		CreateFileActivity.setUpdateListener(FilesBrowserActivity.this);

		filesViewModel = new ViewModelProvider(this).get(FilesViewModel.class);
		projectsContext = ProjectsContext.fromIntent(getIntent());
		resultLimit = getAccount().getMaxPageLimit();

		if (getIntent().getStringExtra("source") != null) {
			source = getIntent().getStringExtra("source");
		}
		projectId = getIntent().getIntExtra("projectId", 0);
		if (getIntent().getStringExtra("branch") != null) {
			branch = getIntent().getStringExtra("branch");
		}

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

		binding.bottomBarTitleText.setText(getString(R.string.files_ref, branch));
		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.newFile.setOnClickListener(
				releases -> {
					ProjectsContext project =
							new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = project.getIntent(ctx, CreateFileActivity.class);
					intent.putExtra("type", "new");
					intent.putExtra("projectId", projectId);
					intent.putExtra("branch", branch);
					ctx.startActivity(intent);
				});

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											binding.pullToRefresh.setRefreshing(false);
											fetchDataAsync();
											binding.progressBar.setVisibility(View.VISIBLE);
										},
										250));

		OnBackPressedCallback onBackPressedCallback =
				new OnBackPressedCallback(true) {
					@Override
					public void handleOnBackPressed() {

						if (!pathSetter) {
							finish();
						}

						if (path != null && path.contains("/")) {
							path = path.substring(0, path.lastIndexOf("/"));
							pathSetter = true;
						} else {
							path = "";
							pathSetter = false;
						}
						refresh();
					}
				};
		getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

		fetchDataAsync();
	}

	@Override
	public void onClickFile(Tree tree) {

		switch (tree.getType()) {
			case "tree":
				path = tree.getPath();
				pathSetter = true;
				refresh();
				break;
			case "blob":
				Intent intent = projectsContext.getIntent(ctx, FileViewActivity.class);
				intent.putExtra("tree", tree);
				intent.putExtra("ref", branch);
				ctx.startActivity(intent);
				break;
		}
	}

	public void refresh() {
		binding.progressBar.setVisibility(View.VISIBLE);
		fetchDataAsync();
	}

	@Override
	public void createFileDataListener(String str, String newBranch) {

		if (str.equalsIgnoreCase("created")) {
			Snackbar.info(
					ctx,
					findViewById(android.R.id.content),
					binding.bottomAppBar,
					getString(R.string.new_file_created));

			path = "";
			branch = newBranch;
			binding.bottomBarTitleText.setText(getString(R.string.files_ref, branch));
			fetchDataAsync();
		}
	}

	private void fetchDataAsync() {

		filesAdapter = new FilesAdapter(FilesBrowserActivity.this, new ArrayList<>(), this);
		binding.recyclerView.setAdapter(filesAdapter);

		filesViewModel
				.getLink()
				.observe(
						FilesBrowserActivity.this,
						next -> {
							if (next != null && !next.isEmpty()) {
								Uri uri = Uri.parse(next);
								String pageToken = uri.getQueryParameter("page_token");
								filesAdapter.setLoadMoreListener(
										new FilesAdapter.OnLoadMoreListener() {
											@Override
											protected void onLoadMore() {
												filesViewModel.loadMore(
														ctx,
														projectId,
														branch,
														pageToken,
														path,
														resultLimit,
														filesAdapter,
														FilesBrowserActivity.this,
														binding.bottomAppBar);
												binding.progressBar.setVisibility(View.VISIBLE);
											}

											@Override
											public void onLoadFinished() {
												binding.progressBar.setVisibility(View.GONE);
											}
										});
							}
						});

		filesViewModel
				.getFiles(
						ctx,
						projectId,
						branch,
						"",
						path,
						resultLimit,
						FilesBrowserActivity.this,
						binding.bottomAppBar)
				.observe(
						FilesBrowserActivity.this,
						mainList -> {
							if (mainList == null || mainList.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
								binding.progressBar.setVisibility(View.GONE);
								filesAdapter.updateList(new ArrayList<>());
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								filesAdapter.updateList(mainList);
								binding.progressBar.setVisibility(View.GONE);
							}
						});
	}
}
