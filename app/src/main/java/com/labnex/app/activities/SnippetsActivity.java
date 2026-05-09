package com.labnex.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.SnippetsAdapter;
import com.labnex.app.bottomsheets.ContentViewerBottomSheet;
import com.labnex.app.bottomsheets.CreateSnippetBottomSheet;
import com.labnex.app.bottomsheets.GenericMenuBottomSheet;
import com.labnex.app.databinding.ActivitySnippetsBinding;
import com.labnex.app.databinding.BottomsheetSnippetFilesBinding;
import com.labnex.app.databinding.ItemSnippetFileBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.FileIcon;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.models.snippets.FilesItem;
import com.labnex.app.models.snippets.SnippetsItem;
import com.labnex.app.viewmodels.SnippetsViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;

/**
 * @author mmarif
 */
public class SnippetsActivity extends BaseActivity
		implements SnippetsAdapter.OnSnippetClickListener {

	private ActivitySnippetsBinding binding;
	private SnippetsViewModel viewModel;
	private SnippetsAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivitySnippetsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(SnippetsViewModel.class);

		int userId = getAccount().getUserId();

		binding.btnBack.setOnClickListener(v -> finish());
		binding.newSnippet.setOnClickListener(
				v -> {
					CreateSnippetBottomSheet sheet = CreateSnippetBottomSheet.newInstance();
					sheet.show(getSupportFragmentManager(), "createSnippetSheet");
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
				.getSingleViewerPayload()
				.observe(
						this,
						payload -> {
							if (payload == null) return;
							openViewer(payload.content, payload.fileName);
							viewModel.clearSingleViewerPayload();
						});

		viewModel
				.getMultiFileList()
				.observe(
						this,
						files -> {
							if (files == null) return;
							showFilePickerSheet(files);
							viewModel.clearMultiFileList();
						});

		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.progressBar.setVisibility(View.VISIBLE);
								binding.pullToRefresh.setRefreshing(false);
							} else {
								binding.progressBar.setVisibility(View.GONE);
								binding.pullToRefresh.setRefreshing(false);
							}
						});
	}

	private void showFilePickerSheet(SnippetsItem snippetsItem) {
		BottomSheetDialog dialog = new BottomSheetDialog(this);
		BottomsheetSnippetFilesBinding fb =
				BottomsheetSnippetFilesBinding.inflate(getLayoutInflater());
		dialog.setContentView(fb.getRoot());
		UIHelper.applySheetStyle(dialog, true);

		fb.sheetTitle.setText(snippetsItem.getTitle());

		List<FilesItem> files = snippetsItem.getFiles();
		fb.filesList.setLayoutManager(new LinearLayoutManager(this));
		fb.filesList.setAdapter(
				new SnippetFilePickerAdapter(
						files,
						file -> {
							dialog.dismiss();
							viewModel.fetchMultiFileContent(
									ctx, viewModel.getCurrentSnippet().getId(), file);
						}));
		dialog.show();
	}

	private void openViewer(String content, String fileName) {
		String ext = FilenameUtils.getExtension(fileName);
		Utils.FileType type = Utils.getFileType(ext);
		boolean isImage = type == Utils.FileType.IMAGE;
		boolean isMarkdown = "md".equalsIgnoreCase(ext) || "markdown".equalsIgnoreCase(ext);

		List<ContentViewerBottomSheet.Feature> features = new ArrayList<>();
		features.add(ContentViewerBottomSheet.Feature.SHOW_TITLE);
		if (isImage) {
			features.add(ContentViewerBottomSheet.Feature.IMAGE_PREVIEW);
		} else {
			features.add(ContentViewerBottomSheet.Feature.ALLOW_COPY);
			features.add(ContentViewerBottomSheet.Feature.ALLOW_SHARE);
			if (isMarkdown) {
				features.add(ContentViewerBottomSheet.Feature.MARKDOWN_PREVIEW);
				features.add(ContentViewerBottomSheet.Feature.START_IN_MARKDOWN);
			} else if (type == Utils.FileType.TEXT) {
				features.add(ContentViewerBottomSheet.Feature.SYNTAX_HIGHLIGHT);
			}
		}

		SnippetsItem snippet = viewModel.getCurrentSnippet();
		String title = snippet.getTitle();
		String webUrl = snippet.getWebUrl();

		Map<String, String> meta = new HashMap<>();
		if (webUrl != null && !webUrl.isEmpty()) {
			meta.put("URL", webUrl);
		}

		ContentViewerBottomSheet viewer =
				ContentViewerBottomSheet.newInstance(
						isImage ? null : content,
						isImage ? content.getBytes() : null,
						title,
						null,
						isImage ? null : ext,
						meta,
						features.toArray(new ContentViewerBottomSheet.Feature[0]));
		viewer.show(getSupportFragmentManager(), "contentViewer");
	}

	@Override
	public void onSnippetMenuClick(SnippetsItem snippet, int position) {
		List<GenericMenuItemModel> items = new ArrayList<>();
		items.add(
				new GenericMenuItemModel(
						"edit",
						R.string.edit,
						R.drawable.ic_edit,
						com.google.android.material.R.attr.colorPrimaryContainer,
						com.google.android.material.R.attr.colorOnPrimaryContainer));
		items.add(
				new GenericMenuItemModel(
						"delete",
						R.string.delete,
						R.drawable.ic_trash,
						com.google.android.material.R.attr.colorErrorContainer,
						com.google.android.material.R.attr.colorOnErrorContainer));

		GenericMenuBottomSheet sheet =
				GenericMenuBottomSheet.newInstance(
						snippet.getTitle(), getString(R.string.snippet), items);
		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "edit":
							openEditSnippet(snippet);
							break;
						case "delete":
							onSnippetDelete(snippet, position);
							break;
					}
				});
		sheet.show(getSupportFragmentManager(), "snippetMenuSheet");
	}

	private void openEditSnippet(SnippetsItem snippet) {
		List<CreateSnippetBottomSheet.FileEntry> files = new ArrayList<>();
		if (snippet.getFiles() != null) {
			for (FilesItem f : snippet.getFiles()) {
				files.add(new CreateSnippetBottomSheet.FileEntry(f.getPath(), ""));
			}
		} else if (snippet.getFileName() != null) {
			files.add(new CreateSnippetBottomSheet.FileEntry(snippet.getFileName(), ""));
		}

		CreateSnippetBottomSheet sheet =
				CreateSnippetBottomSheet.newInstance(
						snippet.getId(),
						snippet.getTitle(),
						snippet.getDescription() != null ? snippet.getDescription() : "",
						snippet.getVisibility(),
						files);
		sheet.show(getSupportFragmentManager(), "editSnippetSheet");
	}

	@Override
	public void onSnippetClick(SnippetsItem snippet) {
		viewModel.loadSnippetAndOpen(ctx, snippet.getId());
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

	private static class SnippetFilePickerAdapter
			extends RecyclerView.Adapter<SnippetFilePickerAdapter.Holder> {

		interface OnFilePicked {
			void onPicked(FilesItem file);
		}

		private final List<FilesItem> files;
		private final OnFilePicked listener;

		SnippetFilePickerAdapter(List<FilesItem> files, OnFilePicked listener) {
			this.files = files;
			this.listener = listener;
		}

		@NonNull @Override
		public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			ItemSnippetFileBinding itemSnippetFileBinding =
					ItemSnippetFileBinding.inflate(
							LayoutInflater.from(parent.getContext()), parent, false);
			return new Holder(itemSnippetFileBinding);
		}

		@Override
		public void onBindViewHolder(@NonNull Holder holder, int position) {
			FilesItem f = files.get(position);
			holder.itemSnippetFileBinding.fileName.setText(f.getPath());
			holder.itemSnippetFileBinding.fileIcon.setImageResource(
					FileIcon.getIconResource(f.getPath(), "file"));
			holder.itemView.setOnClickListener(v -> listener.onPicked(f));
			holder.itemSnippetFileBinding.getRoot().updateAppearance(position, getItemCount());
		}

		@Override
		public int getItemCount() {
			return files.size();
		}

		static class Holder extends RecyclerView.ViewHolder {
			ItemSnippetFileBinding itemSnippetFileBinding;

			Holder(ItemSnippetFileBinding itemSnippetFileBinding) {
				super(itemSnippetFileBinding.getRoot());
				this.itemSnippetFileBinding = itemSnippetFileBinding;
			}
		}
	}
}
