package com.labnex.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.SnippetsAdapter;
import com.labnex.app.bottomsheets.ContentViewerBottomSheet;
import com.labnex.app.databinding.ActivitySnippetsBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.snippets.SnippetsItem;
import com.labnex.app.viewmodels.SnippetsViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mmarif
 */
public class SnippetsActivity extends BaseActivity
		implements SnippetsAdapter.OnSnippetClickListener {

	private ActivitySnippetsBinding binding;
	private SnippetsViewModel viewModel;
	private SnippetsAdapter adapter;

	private final ActivityResultLauncher<Intent> createSnippetLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == RESULT_OK) {
							viewModel.loadSnippets(ctx);
						}
					});

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivitySnippetsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(SnippetsViewModel.class);
		viewModel.setResultLimit(getAccount().getMaxPageLimit());

		int userId = getAccount().getUserId();

		binding.btnBack.setOnClickListener(v -> finish());
		binding.newSnippet.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, SnippetDetailActivity.class);
					intent.putExtra("MODE", "CREATE");
					createSnippetLauncher.launch(intent);
				});

		setupRecyclerView(userId);
		setupPullToRefresh();
		observeViewModel();
		viewModel.loadSnippets(ctx);
	}

	@Override
	protected void onGlobalRefresh() {
		viewModel.loadSnippets(ctx);
	}

	private void setupRecyclerView(int userId) {
		adapter = new SnippetsAdapter(ctx, new ArrayList<>(), this, userId);
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
		binding.pullToRefresh.setOnRefreshListener(() -> viewModel.loadSnippets(ctx));
	}

	private void observeViewModel() {
		viewModel
				.getSnippetList()
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
								case "generic_error":
									Toasty.show(ctx, getString(R.string.generic_error));
									break;
								case "deleted":
									Toasty.show(ctx, getString(R.string.snippet_deleted));
									break;
								case "delete_error":
									Toasty.show(ctx, getString(R.string.delete_snippet_error));
									break;
								default:
									Toasty.show(ctx, errorMsg);
									break;
							}
							viewModel.clearError();
						});

		viewModel
				.getSnippetDetail()
				.observe(
						this,
						snippet -> {
							if (snippet == null) return;

							String fileName = null;
							String filePath = null;
							String ref = "main";

							if (snippet.getFiles() != null && !snippet.getFiles().isEmpty()) {
								fileName = snippet.getFiles().get(0).getPath();
								String rawUrl = snippet.getFiles().get(0).getRawUrl();
								if (rawUrl != null && !rawUrl.isEmpty()) {
									Pattern p = Pattern.compile(".*/raw/([^/]+)/(.+)");
									Matcher m = p.matcher(rawUrl);
									if (m.find()) {
										ref = m.group(1);
										filePath = m.group(2);
									}
								} else {
									filePath = fileName;
								}
							} else if (snippet.getFileName() != null) {
								fileName = snippet.getFileName();
								filePath = fileName;
							}

							if (fileName != null) {
								viewModel.loadSnippetFileContent(
										ctx, snippet.getId(), ref, filePath, fileName);
							}

							viewModel.clearSnippetDetail();
						});

		viewModel
				.getFileContent()
				.observe(
						this,
						content -> {
							if (content == null) return;

							String ext = viewModel.getFileExtension().getValue();
							if (ext == null) ext = "";
							Utils.FileType type = Utils.getFileType(ext);
							boolean isImage = type == Utils.FileType.IMAGE;
							boolean isMarkdown =
									"md".equalsIgnoreCase(ext) || "markdown".equalsIgnoreCase(ext);

							List<ContentViewerBottomSheet.Feature> features = new ArrayList<>();
							features.add(ContentViewerBottomSheet.Feature.SHOW_TITLE);

							if (isImage) {
								features.add(ContentViewerBottomSheet.Feature.IMAGE_PREVIEW);
							} else {
								features.add(ContentViewerBottomSheet.Feature.ALLOW_COPY);
								features.add(ContentViewerBottomSheet.Feature.ALLOW_SHARE);
								if (isMarkdown) {
									features.add(ContentViewerBottomSheet.Feature.MARKDOWN_PREVIEW);
								} else if (type == Utils.FileType.TEXT) {
									features.add(ContentViewerBottomSheet.Feature.SYNTAX_HIGHLIGHT);
								}
							}

							ContentViewerBottomSheet viewer =
									ContentViewerBottomSheet.newInstance(
											isImage ? null : content,
											isImage ? content.getBytes() : null,
											viewModel.getSnippetTitle(),
											null,
											isImage ? null : ext,
											features.toArray(
													new ContentViewerBottomSheet.Feature[0]));

							viewer.show(getSupportFragmentManager(), "contentViewer");
							viewModel.clearFileContent();
						});

		Observer<Boolean> loaderObserver =
				loading -> {
					boolean listLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
					boolean detailLoading =
							Boolean.TRUE.equals(viewModel.getIsDetailLoading().getValue());

					if (listLoading || detailLoading) {
						binding.progressBar.setVisibility(View.VISIBLE);
						binding.pullToRefresh.setRefreshing(false);
					} else {
						binding.progressBar.setVisibility(View.GONE);
						binding.pullToRefresh.setRefreshing(false);
					}
				};

		viewModel.getIsLoading().observe(this, loaderObserver);
		viewModel.getIsDetailLoading().observe(this, loaderObserver);
	}

	@Override
	public void onSnippetClick(SnippetsItem snippet) {
		viewModel.loadSnippetDetail(ctx, snippet.getId());
	}

	@Override
	public void onSnippetDelete(SnippetsItem snippet, int position) {
		new MaterialAlertDialogBuilder(ctx)
				.setTitle(R.string.delete_snippet)
				.setMessage(R.string.delete_snippet_confirmation)
				.setNeutralButton(R.string.cancel, null)
				.setPositiveButton(
						R.string.delete,
						(dialog, which) -> viewModel.deleteSnippet(ctx, snippet.getId(), position))
				.show();
	}
}
