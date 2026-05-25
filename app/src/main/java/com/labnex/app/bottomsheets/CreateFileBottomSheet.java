package com.labnex.app.bottomsheets;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.BottomsheetCreateFileBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.labnex.app.helpers.attachments.AttachmentManager;
import com.labnex.app.helpers.attachments.AttachmentUtils;
import com.labnex.app.models.commits.CommitAction;
import com.labnex.app.models.projects.ForkedFromProject;
import com.labnex.app.viewmodels.CreateFileViewModel;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 * @author mmarif
 */
public class CreateFileBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCreateFileBinding binding;
	private CreateFileViewModel viewModel;

	private long projectId;
	private String branch;
	private String mode;

	private String editFilePath;
	private final List<FileEntry> files = new ArrayList<>();
	private int selectedFileIndex = 0;
	private boolean pendingCreateMr = false;

	private AttachmentManager attachmentManager;
	private ActivityResultLauncher<Intent> filePickerLauncher;
	private ForkedFromProject upstreamProject;

	public static class FileEntry implements Serializable {
		public String filePath;
		public String content;
		public String encoding;
		public String attachmentFileName;

		public FileEntry(String filePath, String content) {
			this.filePath = filePath;
			this.content = content;
			this.encoding = "text";
		}
	}

	public static CreateFileBottomSheet newInstance(
			long projectId,
			String branch,
			String mode,
			@Nullable String filePath,
			@Nullable ProjectsContext projectsContext,
			@Nullable ForkedFromProject upstreamProject) {
		CreateFileBottomSheet sheet = new CreateFileBottomSheet();
		Bundle args = new Bundle();
		args.putLong("projectId", projectId);
		args.putString("branch", branch);
		args.putString("mode", mode);
		if (filePath != null) args.putString("filePath", filePath);
		if (projectsContext != null) args.putSerializable("projectsContext", projectsContext);
		if (upstreamProject != null) args.putSerializable("upstreamProject", upstreamProject);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			projectId = getArguments().getLong("projectId");
			branch = getArguments().getString("branch");
			mode = getArguments().getString("mode", "create");
			editFilePath = getArguments().getString("filePath");
			upstreamProject = (ForkedFromProject) getArguments().getSerializable("upstreamProject");
		}

		filePickerLauncher =
				registerForActivityResult(
						new ActivityResultContracts.StartActivityForResult(),
						result -> {
							if (result.getResultCode() == Activity.RESULT_OK
									&& result.getData() != null) {
								Uri uri = result.getData().getData();
								if (uri != null) {
									attachmentManager.handleFilePickerResult(uri);
								}
							}
						});
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateFileBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(CreateFileViewModel.class);

		setupAttachmentManager();
		setupUI();
		observeViewModel();

		if ("edit".equals(mode) && editFilePath != null) {
			loadFileForEdit();
		} else if ("delete".equals(mode)) {
			setupDeleteMode();
		}

		return binding.getRoot();
	}

	private void setupAttachmentManager() {
		attachmentManager = new AttachmentManager(requireContext());
		attachmentManager.setMaxFileCount(1);
		attachmentManager.setListener(
				new AttachmentManager.AttachmentListener() {
					@Override
					public void onAttachmentsChanged(int count) {}

					@Override
					public void onAttachmentAdded(Uri uri) {
						applyAttachmentToCurrentFile(uri);
						attachmentManager.clear();
					}

					@Override
					public void onAttachmentRemoved(int position) {}

					@Override
					public void onAttachmentRejected(String reason) {
						Toasty.show(requireContext(), reason);
					}
				});
	}

	private void applyAttachmentToCurrentFile(Uri uri) {
		if (selectedFileIndex >= files.size()) return;

		String fileName = AttachmentUtils.queryName(requireContext(), uri);
		String extension = FilenameUtils.getExtension(fileName).toLowerCase();
		Utils.FileType fileType = Utils.getFileType(extension);

		FileEntry entry = files.get(selectedFileIndex);

		try (InputStream is = requireContext().getContentResolver().openInputStream(uri)) {

			if (fileType == Utils.FileType.TEXT || fileType == Utils.FileType.UNKNOWN) {
				entry.content = new java.util.Scanner(is).useDelimiter("\\A").next();
				entry.encoding = "text";
			} else {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				byte[] data = new byte[4096];
				int nRead;
				if (is != null) {
					while ((nRead = is.read(data, 0, data.length)) != -1) {
						buffer.write(data, 0, nRead);
					}
				}
				buffer.flush();
				entry.content = Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP);
				entry.encoding = "base64";
			}

			entry.filePath = fileName;
			entry.attachmentFileName = fileName;
			updateFileCardLabel(selectedFileIndex);
			refreshFileUI();

		} catch (Exception e) {
			Toasty.show(requireContext(), getString(R.string.generic_error));
		}
	}

	private void refreshFileUI() {
		FileEntry entry = files.get(selectedFileIndex);
		binding.filePathInput.setText(entry.filePath);

		if ("base64".equals(entry.encoding)) {
			binding.fileContentInput.setText(getString(R.string.binary_file_attached));
			binding.fileContentInput.setEnabled(false);
			binding.btnExpand.setEnabled(false);
			binding.contentLayout.setEndIconMode(TextInputLayout.END_ICON_NONE);
		} else {
			binding.fileContentInput.setText(entry.content != null ? entry.content : "");
			binding.fileContentInput.setEnabled(true);
			binding.btnExpand.setEnabled(true);
			binding.contentLayout.setEndIconMode(TextInputLayout.END_ICON_CLEAR_TEXT);
		}
	}

	private void setupUI() {
		binding.btnClose.setOnClickListener(v -> dismiss());
		attachmentManager.registerFilePicker(filePickerLauncher);

		binding.cardBranch.cardTitle.setText(R.string.branch);
		binding.cardBranch.cardSubtitle.setText(branch);
		binding.cardBranch.cardIcon.setImageResource(R.drawable.ic_branch);
		binding.cardBranch
				.getRoot()
				.setOnClickListener(
						v -> {
							BranchesBottomSheet sheet =
									BranchesBottomSheet.newPickerInstance(
											projectId,
											selectedBranch -> {
												branch = selectedBranch;
												binding.cardBranch.cardSubtitle.setText(
														selectedBranch);
											});
							sheet.show(getParentFragmentManager(), "branchesPicker");
						});

		binding.filePathInput.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

					@Override
					public void onTextChanged(CharSequence s, int st, int b, int c) {}

					@Override
					public void afterTextChanged(Editable s) {
						if (selectedFileIndex < files.size()) {
							files.get(selectedFileIndex).filePath = s.toString().trim();
							updateFileCardLabel(selectedFileIndex);
						}
					}
				});

		binding.fileContentInput.setOnTouchListener(
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

		binding.btnExpand.setOnClickListener(
				v -> {
					if (selectedFileIndex >= files.size()) return;

					FileEntry entry = files.get(selectedFileIndex);
					String currentContent =
							binding.fileContentInput.getText() != null
									? binding.fileContentInput.getText().toString()
									: "";
					if (!"base64".equals(entry.encoding)) {
						entry.content = currentContent;
					}

					String ext = FilenameUtils.getExtension(entry.filePath);
					EditorBottomSheet.EditorMode editorMode;
					if ("md".equalsIgnoreCase(ext) || "markdown".equalsIgnoreCase(ext)) {
						editorMode = EditorBottomSheet.EditorMode.MARKDOWN;
					} else if (isCodeExtension(ext)) {
						editorMode = EditorBottomSheet.EditorMode.CODE;
					} else {
						editorMode = EditorBottomSheet.EditorMode.STANDARD;
					}

					EditorBottomSheet editor =
							EditorBottomSheet.newInstance(
									"base64".equals(entry.encoding) ? "" : entry.content,
									null,
									editorMode,
									ext);
					editor.setEditorListener(
							newContent -> {
								entry.content = newContent;
								entry.encoding = "text";
								binding.fileContentInput.setText(newContent);
							});
					editor.show(getChildFragmentManager(), "fullscreenEditor");
				});

		binding.btnAddFile.setOnClickListener(v -> addFile());

		binding.btnAttach.setOnClickListener(v -> attachmentManager.openFilePicker());

		binding.btnSubmit.setOnClickListener(v -> submitCommit());

		if ("delete".equals(mode)) {
			binding.fileContentLayout.setVisibility(View.GONE);
			binding.btnExpand.setVisibility(View.GONE);
			binding.btnAttach.setVisibility(View.GONE);
			binding.fileCardsLayout.setVisibility(View.GONE);
			binding.btnAddFile.setVisibility(View.GONE);
			binding.sheetTitle.setText(R.string.delete_file);
			binding.btnSubmit.setText(R.string.delete);
			binding.filePathInput.setEnabled(false);
		} else if ("edit".equals(mode)) {
			binding.fileCardsLayout.setVisibility(View.GONE);
			binding.btnAddFile.setVisibility(View.GONE);
			binding.sheetTitle.setText(R.string.edit_file);
			binding.btnSubmit.setText(R.string.update);
			binding.filePathInput.setEnabled(false);
		} else {
			binding.sheetTitle.setText(R.string.create_files);
			binding.btnSubmit.setText(R.string.commit);
			files.clear();
			files.add(new FileEntry("untitled.java", ""));
			files.add(new FileEntry("app/untitled.kt", ""));
			refreshFileCards();
			selectFile(0);
		}

		binding.switchCreateMr.setChecked(false);
	}

	private void loadFileForEdit() {
		viewModel.loadFileForEdit(requireContext(), projectId, editFilePath, branch);
	}

	private void setupDeleteMode() {
		binding.filePathInput.setText(editFilePath);
	}

	private void addFile() {
		if (files.size() >= 5) {
			Toasty.show(requireContext(), getString(R.string.max_files_reached));
			return;
		}
		files.add(new FileEntry("", ""));
		refreshFileCards();
		selectFile(files.size() - 1);
	}

	private void refreshFileCards() {
		binding.fileChipsGroup.removeAllViews();
		for (int i = 0; i < files.size(); i++) {
			addFileCard(i, files.get(i).filePath);
		}
	}

	private void addFileCard(int index, String path) {
		Chip chip =
				(Chip)
						getLayoutInflater()
								.inflate(R.layout.item_chip, binding.fileChipsGroup, false);
		chip.setText(path.isEmpty() ? getString(R.string.untitled) : getFileName(path));
		chip.setCheckable(true);
		chip.setChecked(index == selectedFileIndex);
		chip.setCloseIconVisible(files.size() > 1);
		chip.setOnClickListener(v -> selectFile(index));
		chip.setOnCloseIconClickListener(v -> removeFile(index));
		binding.fileChipsGroup.addView(chip);
	}

	private String getFileName(String path) {
		int lastSlash = path.lastIndexOf('/');
		return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
	}

	private void updateFileCardLabel(int index) {
		if (index < binding.fileChipsGroup.getChildCount()) {
			Chip chip = (Chip) binding.fileChipsGroup.getChildAt(index);
			String path = files.get(index).filePath;
			chip.setText(path.isEmpty() ? getString(R.string.untitled) : getFileName(path));
		}
	}

	private void selectFile(int index) {
		if (selectedFileIndex < files.size() && selectedFileIndex != index) {
			FileEntry entry = files.get(selectedFileIndex);
			entry.filePath =
					binding.filePathInput.getText() != null
							? binding.filePathInput.getText().toString().trim()
							: "";
			if (!"base64".equals(entry.encoding)) {
				entry.content =
						binding.fileContentInput.getText() != null
								? binding.fileContentInput.getText().toString()
								: "";
			}
		}

		selectedFileIndex = index;
		refreshFileUI();

		for (int i = 0; i < binding.fileChipsGroup.getChildCount(); i++) {
			((Chip) binding.fileChipsGroup.getChildAt(i)).setChecked(i == index);
		}
	}

	private void removeFile(int index) {
		if (files.size() <= 1) return;
		files.remove(index);
		if (selectedFileIndex >= files.size()) {
			selectedFileIndex = files.size() - 1;
		}
		refreshFileCards();
		selectFile(selectedFileIndex);
	}

	private boolean isCodeExtension(String ext) {
		if (ext == null) return false;
		Utils.FileType type = Utils.getFileType(ext);
		return type == Utils.FileType.TEXT
				&& !"md".equalsIgnoreCase(ext)
				&& !"markdown".equalsIgnoreCase(ext);
	}

	private void submitCommit() {
		if (selectedFileIndex < files.size()) {
			FileEntry entry = files.get(selectedFileIndex);
			entry.filePath =
					binding.filePathInput.getText() != null
							? binding.filePathInput.getText().toString().trim()
							: "";
			if (!"base64".equals(entry.encoding)) {
				entry.content =
						binding.fileContentInput.getText() != null
								? binding.fileContentInput.getText().toString()
								: "";
			}
		}

		String commitMessage =
				binding.commitMessageInput.getText() != null
						? binding.commitMessageInput.getText().toString().trim()
						: "";
		if (commitMessage.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.commit_message_required));
			return;
		}

		String newBranch =
				binding.newBranchInput.getText() != null
						? binding.newBranchInput.getText().toString().trim()
						: "";

		String targetBranch = newBranch.isEmpty() ? branch : newBranch;
		String parentBranch = newBranch.isEmpty() ? null : branch;

		if ("delete".equals(mode)) {
			String filePath =
					binding.filePathInput.getText() != null
							? binding.filePathInput.getText().toString().trim()
							: "";
			if (filePath.isEmpty()) {
				Toasty.show(requireContext(), getString(R.string.file_path_required));
				return;
			}
			CommitAction action = new CommitAction("delete", filePath, null, null);
			viewModel.createCommit(
					requireContext(),
					projectId,
					targetBranch,
					parentBranch,
					commitMessage,
					Collections.singletonList(action));
			return;
		}

		List<CommitAction> actions = new ArrayList<>();
		for (FileEntry entry : files) {
			if (entry.filePath.isEmpty()) {
				Toasty.show(requireContext(), getString(R.string.file_path_required));
				return;
			}
			if (entry.content.isEmpty()) {
				Toasty.show(requireContext(), getString(R.string.content_required));
				return;
			}
			String actionType = "edit".equals(mode) ? "update" : "create";
			actions.add(
					new CommitAction(actionType, entry.filePath, entry.content, entry.encoding));
		}

		if (actions.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.at_least_one_file));
			return;
		}

		pendingCreateMr = binding.switchCreateMr.isChecked();

		viewModel.createCommit(
				requireContext(), projectId, targetBranch, parentBranch, commitMessage, actions);
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
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
										"edit".equals(mode)
												? R.string.update
												: "delete".equals(mode)
														? R.string.delete
														: R.string.commit);
								binding.btnSubmit.setEnabled(true);
							}
						});

		viewModel
				.getFileContents()
				.observe(
						getViewLifecycleOwner(),
						contents -> {
							if (contents != null && "edit".equals(mode)) {
								String content = Utils.decodeBase64(contents.getContent());
								FileEntry entry = new FileEntry(editFilePath, content);
								files.clear();
								files.add(entry);
								binding.filePathInput.setText(editFilePath);
								binding.fileContentInput.setText(content);
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
												"delete".equals(mode)
														? R.string.file_deleted
														: "edit".equals(mode)
																? R.string.file_updated
																: R.string.files_created));
								AppUIStateManager.refreshData();
								if (getActivity() instanceof BaseActivity) {
									((BaseActivity) getActivity()).triggerGlobalRefresh();
								}
								viewModel.clearActionSuccess();

								if (pendingCreateMr) {
									String commitMessage =
											binding.commitMessageInput.getText() != null
													? binding.commitMessageInput
															.getText()
															.toString()
															.trim()
													: "";

									String newBranch =
											binding.newBranchInput.getText() != null
													? binding.newBranchInput
															.getText()
															.toString()
															.trim()
													: "";
									String sourceBranch = newBranch.isEmpty() ? branch : newBranch;

									CreateMergeRequestBottomSheet.newInstance(
													"project",
													projectId,
													true,
													true,
													sourceBranch,
													commitMessage,
													null,
													upstreamProject)
											.show(getParentFragmentManager(), "createMrSheet");
								}

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
