package com.labnex.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	private String type;
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

		mode = getIntent().getStringExtra("mode");
		if (mode == null) mode = "create";

		if (getIntent().getStringExtra("type") != null) {
			type = getIntent().getStringExtra("type");
		}

		if ("edit".equals(mode)) {

			originalFilename = getIntent().getStringExtra("filename");
			originalBranch = getIntent().getStringExtra("branch");
			String fileContent = getIntent().getStringExtra("fileContent");

			binding.filename.setText(originalFilename);
			binding.chooseBranch.setText(originalBranch);
			binding.fileContent.setText(fileContent);
			binding.create.setText(R.string.update);
			binding.commitMessage.setText(
					getString(R.string.edit_commit_message, originalFilename));
		}

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.chooseBranch.setOnClickListener(
				branches -> {
					bsBundle.putInt("projectId", projectId);
					bsBundle.putString("source", "create_file");
					BranchesBottomSheet bottomSheet = new BranchesBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "createFileBranchesBottomSheet");
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
							Objects.requireNonNull(binding.chooseBranch.getText()).toString();
					String commitMessage =
							Objects.requireNonNull(binding.commitMessage.getText()).toString();
					String fileContent =
							Objects.requireNonNull(binding.fileContent.getText()).toString();

					if (filename.isEmpty() || commitMessage.isEmpty() || fileContent.isEmpty()) {

						enableButton();
						Snackbar.info(
								this,
								binding.bottomAppBar,
								getString(R.string.all_fields_are_required));
						return;
					}

					if ("edit".equals(mode)) {
						updateFile(filename, branch, commitMessage, fileContent);
					} else {
						createNewFile(filename, branch, commitMessage, fileContent);
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
