package com.labnex.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.FilesAdapter;
import com.labnex.app.bottomsheets.BranchesBottomSheet;
import com.labnex.app.bottomsheets.ContentViewerBottomSheet;
import com.labnex.app.bottomsheets.GenericMenuBottomSheet;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityFilesBrowserBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.models.repository.Tree;
import com.labnex.app.viewmodels.FilesViewModel;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

/**
 * @author mmarif
 */
public class FilesBrowserActivity extends BaseActivity
		implements FilesAdapter.FilesAdapterListener, BranchesBottomSheet.OnBranchPickedListener {

	private ActivityFilesBrowserBinding binding;
	private FilesViewModel viewModel;
	private FilesAdapter adapter;
	private ProjectsContext projectsContext;
	private int projectId;
	private String branch;
	private String currentPath = "";
	private boolean canModify;
	private String projectName;
	private ActivityResultLauncher<Intent> downloadLauncher;
	private byte[] pendingDownloadData;
	private Tree pendingDownloadTree;
	private Tree pendingViewTree;
	private Utils.FileType pendingViewFileType;
	private boolean isDownloading = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityFilesBrowserBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(FilesViewModel.class);

		projectsContext = ProjectsContext.fromIntent(getIntent());
		projectId = getIntent().getIntExtra("projectId", -1);
		projectName = getIntent().getStringExtra("projectName");
		String projectPath = getIntent().getStringExtra("path");
		branch = getIntent().getStringExtra("branch");
		canModify = getIntent().getBooleanExtra("canModify", false);

		if (projectId == -1 || branch == null) {
			finish();
			return;
		}

		if (projectsContext == null) {
			projectsContext = new ProjectsContext(projectName, projectPath, projectId, ctx);
		}

		downloadLauncher =
				registerForActivityResult(
						new ActivityResultContracts.StartActivityForResult(),
						result -> {
							if (result.getResultCode() == Activity.RESULT_OK
									&& result.getData() != null) {
								saveDownloadedFile(result.getData().getData());
							}
						});

		setupRecyclerView();
		observeViewModel();
		setupDockListeners();
		viewModel.loadFiles(ctx, projectId, branch, currentPath);
	}

	private void setupRecyclerView() {
		adapter = new FilesAdapter(ctx, new ArrayList<>(), this);

		LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);
		binding.recyclerView.setHasFixedSize(true);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(ctx);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					viewModel.loadFiles(ctx, projectId, branch, currentPath);
				});
	}

	private void setupDockListeners() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnMenu.setOnClickListener(v -> showFilesMenu());
	}

	private void showFilesMenu() {
		List<GenericMenuItemModel> items = new ArrayList<>();

		items.add(
				new GenericMenuItemModel(
						"switch_branch",
						R.string.switch_branch,
						R.drawable.ic_branch,
						com.google.android.material.R.attr.colorPrimaryContainer,
						com.google.android.material.R.attr.colorOnPrimaryContainer));

		if (canModify) {
			items.add(
					new GenericMenuItemModel(
							"add_file",
							R.string.create_file,
							R.drawable.ic_add,
							com.google.android.material.R.attr.colorPrimaryContainer,
							com.google.android.material.R.attr.colorOnPrimaryContainer));
		}

		GenericMenuBottomSheet sheet =
				GenericMenuBottomSheet.newInstance(projectName, branch, items);
		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "switch_branch":
							BranchesBottomSheet.newPickerInstance(projectId, this)
									.show(getSupportFragmentManager(), "branchesSheet");
							break;
						case "add_file":
							openCreateFile();
							break;
					}
				});
		sheet.show(getSupportFragmentManager(), "filesMenuSheet");
	}

	@Override
	public void onFileMenuClick(Tree tree) {
		List<GenericMenuItemModel> items = new ArrayList<>();

		if (canModify) {
			items.add(
					new GenericMenuItemModel(
							"edit_file",
							R.string.edit,
							R.drawable.ic_edit,
							com.google.android.material.R.attr.colorPrimaryContainer,
							com.google.android.material.R.attr.colorOnPrimaryContainer));
		}

		items.add(
				new GenericMenuItemModel(
						"download_file",
						R.string.download,
						R.drawable.ic_download,
						com.google.android.material.R.attr.colorPrimaryContainer,
						com.google.android.material.R.attr.colorOnPrimaryContainer));

		if (canModify) {
			items.add(
					new GenericMenuItemModel(
							"delete_file",
							R.string.delete,
							R.drawable.ic_trash,
							com.google.android.material.R.attr.colorErrorContainer,
							com.google.android.material.R.attr.colorOnErrorContainer));
		}

		GenericMenuBottomSheet sheet =
				GenericMenuBottomSheet.newInstance(tree.getName(), currentPath, items);
		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "edit_file":
							openEditFile(tree);
							break;
						case "download_file":
							downloadFile(tree);
							break;
						case "delete_file":
							confirmDeleteFile(tree);
							break;
					}
				});
		sheet.show(getSupportFragmentManager(), "fileMenuSheet");
	}

	private void openEditFile(Tree tree) {
		Intent intent = new Intent(ctx, CreateFileActivity.class);
		intent.putExtra("mode", "edit");
		intent.putExtra("projectId", projectId);
		intent.putExtra("filename", tree.getName());
		intent.putExtra("branch", branch);
		intent.putExtra("filePath", tree.getPath());
		if (projectsContext != null) {
			intent.putExtra("projectsContext", projectsContext);
		}
		startActivity(intent);
	}

	private void confirmDeleteFile(Tree tree) {
		new MaterialAlertDialogBuilder(ctx)
				.setTitle(getString(R.string.delete_dialog_title, tree.getName()))
				.setMessage(R.string.delete_file_dialog_message)
				.setPositiveButton(
						R.string.proceed,
						(dialog, which) -> {
							Intent intent = new Intent(ctx, CreateFileActivity.class);
							intent.putExtra("mode", "delete");
							intent.putExtra("projectId", projectId);
							intent.putExtra("filename", tree.getName());
							intent.putExtra("branch", branch);
							intent.putExtra("filePath", tree.getPath());
							if (projectsContext != null) {
								intent.putExtra("projectsContext", projectsContext);
							}
							startActivity(intent);
						})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void openCreateFile() {
		Intent intent = new Intent(ctx, CreateFileActivity.class);
		intent.putExtra("type", "new");
		intent.putExtra("projectId", projectId);
		intent.putExtra("branch", branch);
		intent.putExtra("path", currentPath);
		if (projectsContext != null) {
			intent = projectsContext.getIntent(ctx, CreateFileActivity.class);
			intent.putExtra("type", "new");
			intent.putExtra("projectId", projectId);
			intent.putExtra("branch", branch);
			intent.putExtra("path", currentPath);
		}
		startActivity(intent);
	}

	@Override
	public void onBranchPicked(String selectedBranch) {
		this.branch = selectedBranch;
		this.currentPath = "";
		viewModel.loadFiles(ctx, projectId, branch, currentPath);
	}

	@Override
	public void onClickFile(Tree tree) {
		switch (tree.getType()) {
			case "tree":
				currentPath = tree.getPath();
				viewModel.loadFiles(ctx, projectId, branch, currentPath);
				break;
			case "blob":
				openFile(tree);
				break;
		}
	}

	private void saveDownloadedFile(Uri uri) {
		if (pendingDownloadData == null) return;

		try {
			OutputStream outputStream = getContentResolver().openOutputStream(uri);
			if (outputStream != null) {
				outputStream.write(pendingDownloadData);
				outputStream.close();
				Toasty.show(ctx, getString(R.string.file_downloaded));
			}
		} catch (IOException e) {
			Toasty.show(ctx, getString(R.string.generic_error));
		}
		pendingDownloadData = null;
	}

	@Override
	public void onBreadcrumbClick(String path) {
		currentPath = path;
		viewModel.loadFiles(ctx, projectId, branch, currentPath);
	}

	private void downloadFile(Tree tree) {
		pendingDownloadTree = tree;
		isDownloading = true;
		viewModel.fetchFileContents(ctx, projectId, tree.getPath(), branch);
	}

	private void openFile(Tree tree) {
		String extension = FilenameUtils.getExtension(tree.getName()).toLowerCase();
		Utils.FileType fileType = Utils.getFileType(extension);

		switch (fileType) {
			case IMAGE:
			case TEXT:
				pendingViewTree = tree;
				pendingViewFileType = fileType;
				isDownloading = false;
				viewModel.fetchFileContents(ctx, projectId, tree.getPath(), branch);
				break;
			default:
				Toasty.show(ctx, getString(R.string.exclude_files_in_fileviewer));
				break;
		}
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							binding.progressBar.setVisibility(
									Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getFileList()
				.observe(
						this,
						list -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;

							adapter.setCurrentPath(currentPath);

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
				.getFileContents()
				.observe(
						this,
						contents -> {
							if (contents == null) return;

							if (isDownloading && pendingDownloadTree != null) {
								pendingDownloadData =
										Base64.getDecoder()
												.decode(
														contents.getContent()
																.getBytes(StandardCharsets.UTF_8));
								Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
								intent.addCategory(Intent.CATEGORY_OPENABLE);
								intent.putExtra(Intent.EXTRA_TITLE, pendingDownloadTree.getName());
								intent.setType("*/*");
								downloadLauncher.launch(intent);
								pendingDownloadTree = null;

							} else if (pendingViewTree != null) {
								String extension =
										FilenameUtils.getExtension(pendingViewTree.getName())
												.toLowerCase();

								Map<String, String> metadata = new HashMap<>();
								if (projectsContext != null) {
									String fileUrl =
											projectsContext.getProject().getWebUrl()
													+ "/-/blob/"
													+ branch
													+ "/"
													+ pendingViewTree.getPath();
									metadata.put("URL", fileUrl);
								}

								if (pendingViewFileType == Utils.FileType.IMAGE) {
									byte[] imageData =
											Base64.getDecoder()
													.decode(
															contents.getContent()
																	.getBytes(
																			StandardCharsets
																					.UTF_8));
									ContentViewerBottomSheet sheet =
											ContentViewerBottomSheet.newInstance(
													null,
													imageData,
													pendingViewTree.getName(),
													null,
													null,
													metadata,
													ContentViewerBottomSheet.Feature.SHOW_TITLE,
													ContentViewerBottomSheet.Feature.IMAGE_PREVIEW);
									sheet.show(getSupportFragmentManager(), "contentViewer");

								} else {
									String content = Utils.decodeBase64(contents.getContent());
									boolean isMarkdown =
											"md".equals(extension) || "markdown".equals(extension);

									List<ContentViewerBottomSheet.Feature> features =
											new ArrayList<>();
									features.add(ContentViewerBottomSheet.Feature.SHOW_TITLE);
									features.add(ContentViewerBottomSheet.Feature.ALLOW_COPY);
									features.add(ContentViewerBottomSheet.Feature.ALLOW_SHARE);

									if (isMarkdown) {
										features.add(
												ContentViewerBottomSheet.Feature.MARKDOWN_PREVIEW);
										features.add(
												ContentViewerBottomSheet.Feature.START_IN_MARKDOWN);
									} else {
										features.add(
												ContentViewerBottomSheet.Feature.SYNTAX_HIGHLIGHT);
									}

									ContentViewerBottomSheet sheet =
											ContentViewerBottomSheet.newInstance(
													content,
													null,
													pendingViewTree.getName(),
													projectsContext,
													extension,
													metadata,
													features.toArray(
															new ContentViewerBottomSheet.Feature
																	[0]));
									sheet.show(getSupportFragmentManager(), "contentViewer");
								}

								pendingViewTree = null;
								pendingViewFileType = null;
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

	private final OnBackPressedCallback backCallback =
			new OnBackPressedCallback(true) {
				@Override
				public void handleOnBackPressed() {
					if (currentPath != null && currentPath.contains("/")) {
						currentPath = currentPath.substring(0, currentPath.lastIndexOf("/"));
					} else if (currentPath != null && !currentPath.isEmpty()) {
						currentPath = "";
					} else {
						finish();
						return;
					}
					viewModel.loadFiles(ctx, projectId, branch, currentPath);
				}
			};

	@Override
	public void onResume() {
		super.onResume();
		getOnBackPressedDispatcher().addCallback(this, backCallback);
	}

	@Override
	protected void onPause() {
		super.onPause();
		backCallback.remove();
	}

	@Override
	protected void onGlobalRefresh() {
		viewModel.loadFiles(ctx, projectId, branch, currentPath);
	}
}
