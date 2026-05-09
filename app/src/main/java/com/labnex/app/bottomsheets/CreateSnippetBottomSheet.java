package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.databinding.BottomsheetCreateSnippetBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.snippets.SnippetCreateModel;
import com.labnex.app.viewmodels.SnippetsViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;

/**
 * @author mmarif
 */
public class CreateSnippetBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateSnippetBinding binding;
	private SnippetsViewModel viewModel;
	private final List<FileEntry> files = new ArrayList<>();
	private int selectedFileIndex = 0;
	private int snippetId = -1;
	private boolean isEditMode = false;
	private final List<String> originalFileNames = new ArrayList<>();

	public static class FileEntry implements java.io.Serializable {
		public String fileName;
		public String content;

		public FileEntry(String fileName, String content) {
			this.fileName = fileName;
			this.content = content;
		}
	}

	public static CreateSnippetBottomSheet newInstance() {
		return new CreateSnippetBottomSheet();
	}

	public static CreateSnippetBottomSheet newInstance(
			int snippetId,
			String title,
			String description,
			String visibility,
			List<FileEntry> existingFiles) {
		CreateSnippetBottomSheet sheet = new CreateSnippetBottomSheet();
		Bundle args = new Bundle();
		args.putInt("snippet_id", snippetId);
		args.putString("title", title);
		args.putString("description", description);
		args.putString("visibility", visibility);
		args.putSerializable("files", new ArrayList<>(existingFiles));
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			snippetId = getArguments().getInt("snippet_id", -1);
			isEditMode = snippetId > 0;
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateSnippetBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(SnippetsViewModel.class);

		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.btnAddFile.setOnClickListener(v -> addFile());
		binding.btnExpand.setOnClickListener(v -> openFullscreenEditor());

		binding.filenameInput.addTextChangedListener(
				new android.text.TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

					@Override
					public void onTextChanged(CharSequence s, int st, int b, int c) {}

					@Override
					public void afterTextChanged(android.text.Editable s) {
						if (selectedFileIndex < files.size()) {
							files.get(selectedFileIndex).fileName = s.toString().trim();
							updateFileChip(selectedFileIndex);
							updateExpandButton();
						}
					}
				});

		binding.btnSubmit.setOnClickListener(v -> submitSnippet());
		observeViewModel();

		binding.descriptionInput.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == MotionEvent.ACTION_UP
							|| event.getAction() == MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});

		if (isEditMode && getArguments() != null) {
			binding.sheetTitle.setText(R.string.edit_snippet);
			binding.btnSubmit.setText(R.string.update);
			binding.titleInput.setText(getArguments().getString("title", ""));
			binding.descriptionInput.setText(getArguments().getString("description", ""));
			String vis = getArguments().getString("visibility", "private");
			binding.chipPublic.setChecked("public".equals(vis));

			//noinspection unchecked
			List<FileEntry> existing = (List<FileEntry>) getArguments().getSerializable("files");
			if (existing != null && !existing.isEmpty()) {
				files.addAll(existing);
				originalFileNames.clear();
				for (FileEntry entry : existing) {
					originalFileNames.add(entry.fileName);
				}
			} else {
				files.add(new FileEntry("file1.md", ""));
				originalFileNames.add("file1.md");
			}

			int total = files.size();
			int[] remaining = {total};
			for (int i = 0; i < files.size(); i++) {
				final FileEntry entry = files.get(i);
				viewModel.fetchSingleFileForEdit(
						requireContext(),
						snippetId,
						entry,
						() -> {
							remaining[0]--;
							if (remaining[0] <= 0) {
								refreshFileChips();
								selectFile(0);
							}
						});
			}
		} else {
			files.add(new FileEntry("file1.md", ""));
			refreshFileChips();
			selectFile(0);
		}

		return binding.getRoot();
	}

	private void addFile() {
		if (files.size() >= 10) {
			Toasty.show(requireContext(), getString(R.string.max_files_reached));
			return;
		}

		int maxNum = 0;
		java.util.regex.Pattern p = java.util.regex.Pattern.compile("^file(\\d+)\\.");
		for (FileEntry f : files) {
			java.util.regex.Matcher m = p.matcher(f.fileName);
			if (m.find()) {
				try {
					int num = Integer.parseInt(Objects.requireNonNull(m.group(1)));
					if (num > maxNum) maxNum = num;
				} catch (NumberFormatException ignored) {
				}
			}
		}

		String name = "file" + (maxNum + 1) + ".md";
		files.add(new FileEntry(name, ""));
		refreshFileChips();
		selectFile(files.size() - 1);
	}

	private void refreshFileChips() {
		binding.fileChipsGroup.removeAllViews();
		for (int i = 0; i < files.size(); i++) {
			addFileChip(i, files.get(i).fileName);
		}
	}

	private void addFileChip(int index, String name) {
		Chip chip =
				(Chip)
						getLayoutInflater()
								.inflate(R.layout.item_chip, binding.fileChipsGroup, false);
		chip.setText(name.isEmpty() ? getString(R.string.untitled) : name);
		chip.setCheckable(true);
		chip.setChecked(index == selectedFileIndex);
		chip.setCloseIconVisible(files.size() > 1);
		chip.setOnClickListener(v -> selectFile(index));
		chip.setOnCloseIconClickListener(v -> removeFile(index));
		binding.fileChipsGroup.addView(chip);
	}

	private void updateFileChip(int index) {
		if (index < binding.fileChipsGroup.getChildCount()) {
			Chip chip = (Chip) binding.fileChipsGroup.getChildAt(index);
			String name = files.get(index).fileName;
			chip.setText(name.isEmpty() ? getString(R.string.untitled) : name);
		}
	}

	private void selectFile(int index) {
		if (selectedFileIndex < files.size() && selectedFileIndex != index) {
			files.get(selectedFileIndex).content =
					binding.contentInput.getText() != null
							? binding.contentInput.getText().toString()
							: "";
		}

		selectedFileIndex = index;
		FileEntry entry = files.get(index);
		binding.filenameInput.setText(entry.fileName);
		binding.contentInput.setText(entry.content != null ? entry.content : "");

		binding.contentInput.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == MotionEvent.ACTION_UP
							|| event.getAction() == MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});

		for (int i = 0; i < binding.fileChipsGroup.getChildCount(); i++) {
			((Chip) binding.fileChipsGroup.getChildAt(i)).setChecked(i == index);
		}

		updateExpandButton();
	}

	private void removeFile(int index) {
		if (files.size() <= 1) return;
		files.remove(index);
		if (selectedFileIndex >= files.size()) {
			selectedFileIndex = files.size() - 1;
		}
		refreshFileChips();
		selectFile(selectedFileIndex);
	}

	private void updateExpandButton() {
		if (selectedFileIndex < files.size()) {
			String name = files.get(selectedFileIndex).fileName;
			binding.btnExpand.setEnabled(name != null && !name.isEmpty());
		}
	}

	private void openFullscreenEditor() {
		if (selectedFileIndex >= files.size()) return;

		files.get(selectedFileIndex).content =
				binding.contentInput.getText() != null
						? binding.contentInput.getText().toString()
						: "";

		FileEntry entry = files.get(selectedFileIndex);
		String ext = FilenameUtils.getExtension(entry.fileName);

		EditorBottomSheet.EditorMode mode;
		if ("md".equalsIgnoreCase(ext) || "markdown".equalsIgnoreCase(ext)) {
			mode = EditorBottomSheet.EditorMode.MARKDOWN;
		} else if (isCodeExtension(ext)) {
			mode = EditorBottomSheet.EditorMode.CODE;
		} else {
			mode = EditorBottomSheet.EditorMode.STANDARD;
		}

		EditorBottomSheet editor = EditorBottomSheet.newInstance(entry.content, null, mode, ext);
		editor.setEditorListener(
				newContent -> {
					files.get(selectedFileIndex).content = newContent;
					binding.contentInput.setText(newContent);
				});
		editor.show(getChildFragmentManager(), "fullscreenEditor");
	}

	private boolean isCodeExtension(String ext) {
		if (ext == null) return false;
		com.labnex.app.helpers.Utils.FileType type = com.labnex.app.helpers.Utils.getFileType(ext);
		return type == com.labnex.app.helpers.Utils.FileType.TEXT
				&& !"md".equalsIgnoreCase(ext)
				&& !"markdown".equalsIgnoreCase(ext);
	}

	private void submitSnippet() {
		String title =
				binding.titleInput.getText() != null
						? binding.titleInput.getText().toString().trim()
						: "";
		if (title.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.title_required));
			return;
		}

		if (selectedFileIndex < files.size()) {
			files.get(selectedFileIndex).content =
					binding.contentInput.getText() != null
							? binding.contentInput.getText().toString()
							: "";
		}

		String description =
				binding.descriptionInput.getText() != null
						? binding.descriptionInput.getText().toString().trim()
						: "";
		String visibility = binding.chipPublic.isChecked() ? "public" : "private";

		List<SnippetCreateModel.File> modelFiles = new ArrayList<>();
		java.util.Set<String> names = new java.util.HashSet<>();
		for (FileEntry f : files) {
			if (f.fileName == null || f.fileName.isEmpty()) continue;
			if (!names.add(f.fileName)) {
				Toasty.show(requireContext(), getString(R.string.duplicate_file_name));
				return;
			}
			if (f.content == null || f.content.trim().isEmpty()) {
				Toasty.show(requireContext(), getString(R.string.empty_file_content));
				return;
			}
			modelFiles.add(new SnippetCreateModel.File(f.fileName, f.content));
		}

		if (modelFiles.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.at_least_one_file));
			return;
		}

		if (isEditMode) {
			viewModel.updateSnippet(
					requireContext(),
					snippetId,
					title,
					description,
					visibility,
					modelFiles,
					originalFileNames);
		} else {
			viewModel.createSnippet(requireContext(), title, description, visibility, modelFiles);
		}
	}

	private void observeViewModel() {
		viewModel
				.getIsActionLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.btnSubmit.setText(null);
								binding.btnSubmit.setEnabled(false);
								binding.loadingIndicator.setVisibility(View.VISIBLE);
							} else {
								binding.loadingIndicator.setVisibility(View.GONE);
								binding.btnSubmit.setText(
										isEditMode ? R.string.update : R.string.create);
								binding.btnSubmit.setEnabled(true);
							}
						});

		viewModel
				.getActionSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(
										requireContext(),
										getString(
												isEditMode
														? R.string.snippet_updated
														: R.string.snippet_created));
								AppUIStateManager.refreshData();
								if (getActivity() instanceof BaseActivity) {
									((BaseActivity) getActivity()).triggerGlobalRefresh();
								}
								viewModel.clearActionSuccess();
								dismiss();
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg == null) return;
							switch (errorMsg) {
								case "auth_error":
									Toasty.show(
											requireContext(), getString(R.string.not_authorized));
									break;
								case "access_forbidden_403":
									Toasty.show(
											requireContext(),
											getString(R.string.access_forbidden_403));
									break;
								case "not_found":
									Toasty.show(requireContext(), getString(R.string.not_found));
									break;
								case "generic_error":
									Toasty.show(
											requireContext(), getString(R.string.generic_error));
									break;
								default:
									Toasty.show(requireContext(), errorMsg);
									break;
							}
							viewModel.clearError();
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
