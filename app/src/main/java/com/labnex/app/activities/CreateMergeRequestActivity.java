package com.labnex.app.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.BranchesBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityCreateMergeRequestBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.merge_requests.CrudeMergeRequest;
import com.labnex.app.models.merge_requests.MergeRequests;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class CreateMergeRequestActivity extends BaseActivity
		implements BottomSheetListener, BranchesBottomSheet.MrUpdateInterface {

	ActivityCreateMergeRequestBinding binding;
	private int projectId;

	private static UpdateInterface UpdateInterface;

	public interface UpdateInterface {
		void updateDataListener(String str);
	}

	public static void setUpdateListener(UpdateInterface updateInterface) {
		UpdateInterface = updateInterface;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityCreateMergeRequestBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		BranchesBottomSheet.setMrUpdateListener(this);
		Bundle bsBundle = new Bundle();

		ProjectsContext projectsContext = ProjectsContext.fromIntent(getIntent());
		projectId = projectsContext.getProjectId();

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.targetBranch.setOnClickListener(
				target -> {
					bsBundle.putInt("projectId", projectId);
					bsBundle.putString("type", "target");
					bsBundle.putString("source", "create_mr");
					BranchesBottomSheet bottomSheet = new BranchesBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "branchesBottomSheet");
				});
		binding.sourceBranch.setOnClickListener(
				source -> {
					bsBundle.putInt("projectId", projectId);
					bsBundle.putString("type", "source");
					bsBundle.putString("source", "create_mr");
					BranchesBottomSheet bottomSheet = new BranchesBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "branchesBottomSheet");
				});

		binding.create.setOnClickListener(
				create -> {
					disableButton();
					String title = Objects.requireNonNull(binding.title.getText()).toString();
					String description =
							Objects.requireNonNull(binding.description.getText()).toString();
					String targetBranch =
							Objects.requireNonNull(binding.targetBranch.getText()).toString();
					String sourceBranch =
							Objects.requireNonNull(binding.sourceBranch.getText()).toString();

					if (title.isEmpty()) {

						enableButton();
						Snackbar.info(
								CreateMergeRequestActivity.this,
								binding.bottomAppBar,
								getString(R.string.title_required));
						return;
					}
					if (targetBranch.isEmpty() || sourceBranch.isEmpty()) {

						enableButton();
						Snackbar.info(
								CreateMergeRequestActivity.this,
								binding.bottomAppBar,
								getString(R.string.source_target_branch_empty_error));
						return;
					}
					if (targetBranch.equalsIgnoreCase(sourceBranch)) {

						enableButton();
						Snackbar.info(
								CreateMergeRequestActivity.this,
								binding.bottomAppBar,
								getString(R.string.mr_branches_are_same));
						return;
					}

					createMergeRequest(title, description, targetBranch, sourceBranch);
				});
	}

	@Override
	public void mrUpdateDataListener(String str, String type) {

		if (type.equalsIgnoreCase("target")) {
			binding.targetBranch.setText(str);
		} else if (type.equalsIgnoreCase("source")) {
			binding.sourceBranch.setText(str);
		}
	}

	private void createMergeRequest(
			String title, String description, String targetBranch, String sourceBranch) {

		CrudeMergeRequest createMr = new CrudeMergeRequest();
		createMr.setTitle(title);
		createMr.setDescription(description);
		createMr.setTargetBranch(targetBranch);
		createMr.setSourceBranch(sourceBranch);

		Call<MergeRequests> call =
				RetrofitClient.getApiInterface(ctx).createMergeRequest(projectId, createMr);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<MergeRequests> call,
							@NonNull retrofit2.Response<MergeRequests> response) {

						if (response.code() == 201) {

							UpdateInterface.updateDataListener("created");
							finish();
						} else if (response.code() == 401) {

							enableButton();
							Snackbar.info(
									CreateMergeRequestActivity.this,
									binding.bottomAppBar,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							enableButton();
							Snackbar.info(
									CreateMergeRequestActivity.this,
									binding.bottomAppBar,
									getString(R.string.access_forbidden_403));
						} else {

							enableButton();
							Snackbar.info(
									CreateMergeRequestActivity.this,
									binding.bottomAppBar,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<MergeRequests> call, @NonNull Throwable t) {

						enableButton();
						Snackbar.info(
								CreateMergeRequestActivity.this,
								binding.bottomAppBar,
								getString(R.string.generic_server_response_error));
					}
				});
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
	public void onButtonClicked(String text) {}
}
