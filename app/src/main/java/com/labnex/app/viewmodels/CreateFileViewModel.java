package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.models.commits.CommitAction;
import com.labnex.app.models.commits.Commits;
import com.labnex.app.models.commits.CrudeCommit;
import com.labnex.app.models.repository.FileContents;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CreateFileViewModel extends ViewModel {

	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<FileContents> fileContents = new MutableLiveData<>();

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getActionSuccess() {
		return actionSuccess;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<FileContents> getFileContents() {
		return fileContents;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void clearActionSuccess() {
		actionSuccess.setValue(false);
	}

	public void loadFileForEdit(Context ctx, int projectId, String filePath, String branch) {
		if (ctx == null) return;

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getFileContents(projectId, filePath, branch)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<FileContents> c,
									@NonNull Response<FileContents> r) {
								isLoading.setValue(false);
								if (r.isSuccessful() && r.body() != null) {
									fileContents.setValue(r.body());
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<FileContents> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void createCommit(
			Context ctx,
			int projectId,
			String branch,
			String startBranch,
			String commitMessage,
			List<CommitAction> actions) {
		if (ctx == null) return;

		CrudeCommit commit = new CrudeCommit();
		commit.setBranch(branch);
		if (startBranch != null && !startBranch.isEmpty()) {
			commit.setStartBranch(startBranch);
		}
		commit.setCommitMessage(commitMessage);
		commit.setActions(actions);

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.createCommit(projectId, commit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Commits> c, @NonNull Response<Commits> r) {
								ApiResponseHandler.handleAction(r, isLoading, actionSuccess, error);
							}

							@Override
							public void onFailure(@NonNull Call<Commits> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}
}
