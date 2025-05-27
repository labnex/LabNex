package com.labnex.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.BranchesBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityCreateFileBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.branches.Branches;
import com.labnex.app.models.repository.CrudeFile;
import com.labnex.app.models.repository.FileContents;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class CreateFileActivity extends BaseActivity
		implements BottomSheetListener, BranchesBottomSheet.CreateFileUpdateInterface {

	ActivityCreateFileBinding binding;
	public ProjectsContext projectsContext;
	private int projectId;
	private String mode;
	private String originalFilename;
	private String originalBranch;

	ActivityResultLauncher<Intent> codeEditorActivityResultLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {
							Intent data = result.getData();
							assert data != null;
							binding.fileContent.setText(
									data.getStringExtra("fileContentFromActivity"));
						}
					});

	private static UpdateInterface UpdateInterface;

	public interface UpdateInterface {
		void createFileDataListener(String str, String newBranch);
	}

	public static void setUpdateListener(UpdateInterface updateInterface) {
		UpdateInterface = updateInterface;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityCreateFileBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		mode = getIntent().getStringExtra("mode");
		if (mode == null) mode = "create";

		projectsContext = ProjectsContext.fromIntent(getIntent());
		if (projectsContext != null) {
			projectId = projectsContext.getProjectId();
		} else {
			projectId = getIntent().getIntExtra("projectId", -1);
			if (projectId == -1) {
				finish();
				return;
			}
		}

		Bundle bsBundle = new Bundle();
		BranchesBottomSheet.setCreateFileUpdateListener(CreateFileActivity.this);

		originalFilename = getIntent().getStringExtra("filename");
		originalBranch = getIntent().getStringExtra("branch");
		String fileContent = getIntent().getStringExtra("fileContent");

		if ("edit".equals(mode)) {
			binding.bottomBarTitleText.setText(
					getString(R.string.edit_commit_message, originalFilename));
			binding.filename.setText(originalFilename);
			binding.chooseBranch.setText(originalBranch);
			binding.branchEdit.setVisibility(View.GONE);
			binding.fileContent.setText(fileContent);
			binding.create.setText(R.string.update);
			binding.commitMessage.setText(
					getString(R.string.edit_commit_message, originalFilename));
		} else if ("delete".equals(mode)) {
			binding.bottomBarTitleText.setText(
					getString(R.string.delete_commit_message, originalFilename));
			binding.filename.setText(originalFilename);
			binding.filename.setEnabled(false);
			binding.chooseBranch.setVisibility(View.GONE);
			binding.branchEdit.setVisibility(View.VISIBLE);
			binding.branchEdit.setText(originalBranch);
			binding.fileContent.setText("");
			binding.fileContent.setVisibility(View.GONE);
			binding.openCe.setVisibility(View.GONE);
			binding.create.setText(R.string.delete);
			binding.commitMessage.setText(
					getString(R.string.delete_commit_message, originalFilename));
		} else {
			binding.bottomBarTitleText.setText(R.string.create_file);
			binding.branchEdit.setVisibility(View.GONE);
		}

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.chooseBranch.setOnClickListener(
				branches -> {
					if (!"delete".equals(mode)) {
						bsBundle.putInt("projectId", projectId);
						bsBundle.putString("source", "create_file");
						bsBundle.putString("type", mode);
						BranchesBottomSheet bottomSheet = new BranchesBottomSheet();
						bottomSheet.setArguments(bsBundle);
						bottomSheet.show(
								getSupportFragmentManager(), "createFileBranchesBottomSheet");
					}
				});

		MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(ctx);

		binding.openCe.setOnClickListener(
				ce -> {
					if (binding.filename.getText().toString().isEmpty()) {
						materialAlertDialogBuilder
								.setMessage(R.string.ce_no_filename_filled)
								.setPositiveButton(
										R.string.ignore,
										(dialog, whichButton) -> {
											launchCodeEditorActivityForResult(
													Objects.requireNonNull(
																	binding.fileContent.getText())
															.toString(),
													FilenameUtils.getExtension(
															String.valueOf(
																	binding.filename.getText())));
										})
								.setNeutralButton(R.string.cancel, null)
								.show();
					} else {
						launchCodeEditorActivityForResult(
								Objects.requireNonNull(binding.fileContent.getText()).toString(),
								FilenameUtils.getExtension(
										String.valueOf(binding.filename.getText())));
					}
				});

		binding.create.setOnClickListener(
				create -> {
					disableButton();

					String filename = Objects.requireNonNull(binding.filename.getText()).toString();
					String branch =
							"delete".equalsIgnoreCase(mode)
									? Objects.requireNonNull(binding.branchEdit.getText())
											.toString()
									: Objects.requireNonNull(binding.chooseBranch.getText())
											.toString();
					String commitMessage =
							Objects.requireNonNull(binding.commitMessage.getText()).toString();
					String fileContentNew =
							"delete".equalsIgnoreCase(mode)
									? ""
									: Objects.requireNonNull(binding.fileContent.getText())
											.toString();

					if (filename.isEmpty()
							|| branch.isEmpty()
							|| commitMessage.isEmpty()
							|| (!"delete".equalsIgnoreCase(mode) && fileContentNew.isEmpty())) {
						enableButton();
						Snackbar.info(
								this,
								binding.bottomAppBar,
								getString(R.string.all_fields_are_required));
						return;
					}

					if ("edit".equalsIgnoreCase(mode)) {
						updateFile(filename, branch, commitMessage, fileContentNew);
					} else if ("delete".equalsIgnoreCase(mode)) {
						deleteFile(filename, branch, commitMessage);
					} else {
						createNewFile(filename, branch, commitMessage, fileContentNew);
					}
				});
	}

	private void createNewFile(
			String filename, String branch, String commitMessage, String fileContent) {

		CrudeFile crudeFile = new CrudeFile();
		crudeFile.setContent(fileContent);
		crudeFile.setCommitMessage(commitMessage);
		crudeFile.setBranch(branch);

		Call<FileContents> call =
				RetrofitClient.getApiInterface(ctx).createFile(projectId, filename, crudeFile);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<FileContents> call,
							@NonNull retrofit2.Response<FileContents> response) {
						handleResponse(response, branch);
					}

					@Override
					public void onFailure(@NonNull Call<FileContents> call, @NonNull Throwable t) {
						enableButton();
						Snackbar.info(
								CreateFileActivity.this,
								binding.bottomAppBar,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void updateFile(
			String filename, String branch, String commitMessage, String fileContent) {

		CrudeFile crudeFile = new CrudeFile();
		crudeFile.setContent(fileContent);
		crudeFile.setCommitMessage(commitMessage);
		crudeFile.setBranch(branch);

		Call<FileContents> call =
				RetrofitClient.getApiInterface(ctx).updateFile(projectId, filename, crudeFile);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<FileContents> call,
							@NonNull retrofit2.Response<FileContents> response) {
						handleResponse(response, branch);
					}

					@Override
					public void onFailure(@NonNull Call<FileContents> call, @NonNull Throwable t) {
						enableButton();
						Snackbar.info(
								CreateFileActivity.this,
								binding.bottomAppBar,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void deleteFile(String filename, String branch, String commitMessage) {

		Call<Branches> branchCheckCall =
				RetrofitClient.getApiInterface(ctx).getBranch(projectId, branch);

		branchCheckCall.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Branches> call,
							@NonNull retrofit2.Response<Branches> response) {
						if (response.isSuccessful() && response.code() == 200) {
							performFileDeletion(filename, branch, commitMessage);
						} else if (response.code() == 404) {
							Call<Branches> createBranchCall =
									RetrofitClient.getApiInterface(ctx)
											.createBranch(projectId, branch, originalBranch);
							createBranchCall.enqueue(
									new Callback<>() {
										@Override
										public void onResponse(
												@NonNull Call<Branches> call,
												@NonNull retrofit2.Response<Branches> response) {
											if (response.isSuccessful() && response.code() == 201) {
												performFileDeletion(
														filename, branch, commitMessage);
											} else {
												enableButton();
												String errorMessage =
														getString(R.string.generic_error);
												try (okhttp3.ResponseBody errorBody =
														response.errorBody()) {
													if (errorBody != null) {
														errorMessage = errorBody.string();
													}
												} catch (Exception ignored) {
												}
												Snackbar.info(
														CreateFileActivity.this,
														binding.bottomAppBar,
														getString(
																R.string.branch_creation_failed,
																errorMessage));
											}
										}

										@Override
										public void onFailure(
												@NonNull Call<Branches> call,
												@NonNull Throwable t) {
											enableButton();
											Snackbar.info(
													CreateFileActivity.this,
													binding.bottomAppBar,
													getString(
															R.string
																	.generic_server_response_error));
										}
									});
						} else {
							enableButton();
							String errorMessage = getString(R.string.generic_error);
							try (okhttp3.ResponseBody errorBody = response.errorBody()) {
								if (errorBody != null) {
									errorMessage = errorBody.string();
								}
							} catch (Exception ignored) {
							}
							Snackbar.info(
									CreateFileActivity.this,
									binding.bottomAppBar,
									getString(R.string.branch_check_failed, errorMessage));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Branches> call, @NonNull Throwable t) {
						enableButton();
						Snackbar.info(
								CreateFileActivity.this,
								binding.bottomAppBar,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void performFileDeletion(String encodedFilename, String branch, String commitMessage) {

		CrudeFile crudeFile = new CrudeFile();
		crudeFile.setBranch(branch);
		crudeFile.setCommitMessage(commitMessage);

		Call<Void> call =
				RetrofitClient.getApiInterface(ctx)
						.deleteFile(projectId, encodedFilename, crudeFile);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {
						if (response.code() == 204) {
							UpdateInterface.createFileDataListener("deleted", branch);
							finish();
						} else if (response.code() == 401) {
							enableButton();
							Snackbar.info(
									CreateFileActivity.this,
									binding.bottomAppBar,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {
							enableButton();
							Snackbar.info(
									CreateFileActivity.this,
									binding.bottomAppBar,
									getString(R.string.access_forbidden_403));
						} else {
							enableButton();
							String errorMessage = getString(R.string.generic_error);
							try (okhttp3.ResponseBody errorBody = response.errorBody()) {
								if (errorBody != null) {
									errorMessage = errorBody.string();
								}
							} catch (Exception ignored) {
							}
							Snackbar.info(
									CreateFileActivity.this, binding.bottomAppBar, errorMessage);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						enableButton();
						Snackbar.info(
								CreateFileActivity.this,
								binding.bottomAppBar,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void handleResponse(retrofit2.Response<FileContents> response, String branch) {

		if (response.code() == 201 || response.code() == 200) {
			UpdateInterface.createFileDataListener(
					"edit".equals(mode) ? "updated" : "created", branch);
			finish();
		} else if (response.code() == 401) {
			enableButton();
			Snackbar.info(this, binding.bottomAppBar, getString(R.string.not_authorized));
		} else if (response.code() == 403) {
			enableButton();
			Snackbar.info(this, binding.bottomAppBar, getString(R.string.access_forbidden_403));
		} else {
			enableButton();
			Snackbar.info(this, binding.bottomAppBar, getString(R.string.generic_error));
		}
	}

	public void launchCodeEditorActivityForResult(String fileContent, String fileExtension) {

		Intent intent = new Intent(this, CodeEditorActivity.class);
		intent.putExtra("fileExtension", fileExtension);
		intent.putExtra("fileContent", fileContent);
		codeEditorActivityResultLauncher.launch(intent);
	}

	private void disableButton() {
		binding.create.setEnabled(false);
		binding.create.setAlpha(.5F);
	}

	private void enableButton() {
		binding.create.setEnabled(true);
		binding.create.setAlpha(1F);
	}

	@Override
	public void createFileUpdateDataListener(String str, String type) {

		binding.chooseBranch.setText(str);
	}

	@Override
	public void onButtonClicked(String text) {}
}
