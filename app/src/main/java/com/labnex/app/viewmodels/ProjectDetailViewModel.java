package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.CrudeProject;
import com.labnex.app.models.projects.Projects;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ProjectDetailViewModel extends ViewModel {

	private final MutableLiveData<Projects> projectInfo = new MutableLiveData<>();
	private final MutableLiveData<Map<String, Float>> languageStats = new MutableLiveData<>();
	private final MutableLiveData<String> readmeContent = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isStarred = new MutableLiveData<>();
	private final MutableLiveData<Integer> mrCount = new MutableLiveData<>(-1);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Integer> starCount = new MutableLiveData<>(-1);
	private final MutableLiveData<Boolean> forkSuccess = new MutableLiveData<>(false);

	public LiveData<Integer> getStarCount() {
		return starCount;
	}

	public LiveData<Projects> getProjectInfo() {
		return projectInfo;
	}

	public LiveData<Map<String, Float>> getLanguageStats() {
		return languageStats;
	}

	public LiveData<String> getReadmeContent() {
		return readmeContent;
	}

	public LiveData<Boolean> getIsStarred() {
		return isStarred;
	}

	public LiveData<Integer> getMrCount() {
		return mrCount;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Boolean> getActionSuccess() {
		return actionSuccess;
	}

	public LiveData<Boolean> getForkSuccess() {
		return forkSuccess;
	}

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public void clearActionSuccess() {
		actionSuccess.setValue(false);
	}

	public void clearForkSuccess() {
		forkSuccess.setValue(false);
	}

	public void forkProject(
			Context ctx,
			long projectId,
			String name,
			String path,
			String description,
			String visibility,
			Long namespaceId) {
		isActionLoading.setValue(true);

		CrudeProject forkBody = new CrudeProject();
		if (name != null && !name.isEmpty()) forkBody.setName(name);
		if (path != null && !path.isEmpty()) forkBody.setPath(path);
		if (description != null && !description.isEmpty()) forkBody.setDescription(description);
		if (visibility != null) forkBody.setVisibility(visibility);
		if (namespaceId != null) forkBody.setNamespaceId(namespaceId);

		RetrofitClient.getApiInterface(ctx)
				.forkProject(projectId, forkBody)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Projects> c, @NonNull Response<Projects> r) {
								ApiResponseHandler.handleAction(
										r, isActionLoading, actionSuccess, error);
								if (r.isSuccessful()) forkSuccess.setValue(true);
							}

							@Override
							public void onFailure(@NonNull Call<Projects> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void loadProject(Context ctx, long projectId) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getProjectInfo(projectId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Projects> c, @NonNull Response<Projects> r) {
								ApiResponseHandler.handleFetch(
										r, isLoading, () -> projectInfo.setValue(r.body()), error);
							}

							@Override
							public void onFailure(@NonNull Call<Projects> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void loadLanguageStats(Context ctx, long projectId) {
		RetrofitClient.getApiInterface(ctx)
				.getProjectLanguages(projectId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Map<String, Float>> c,
									@NonNull Response<Map<String, Float>> r) {
								if (r.isSuccessful() && r.body() != null) {
									languageStats.setValue(r.body());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Map<String, Float>> c, @NonNull Throwable t) {}
						});
	}

	public void loadReadme(Context ctx, long projectId, String branch, String readmePath) {
		RetrofitClient.getApiInterface(ctx)
				.getProjectFileContent(projectId, readmePath, branch)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<com.labnex.app.models.repository.FileContents> c,
									@NonNull Response<com.labnex.app.models.repository.FileContents>
													r) {
								if (r.isSuccessful() && r.body() != null) {
									readmeContent.setValue(
											Utils.decodeBase64(r.body().getContent()));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<com.labnex.app.models.repository.FileContents> c,
									@NonNull Throwable t) {}
						});
	}

	public void checkStarStatus(Context ctx, long userId, long projectId) {
		RetrofitClient.getApiInterface(ctx)
				.getStarredProjects(userId, 100, 1)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<java.util.List<Projects>> c,
									@NonNull Response<java.util.List<Projects>> r) {
								if (r.isSuccessful() && r.body() != null) {
									for (Projects p : r.body()) {
										if (p.getId() == projectId) {
											isStarred.setValue(true);
											return;
										}
									}
								}
								isStarred.setValue(false);
							}

							@Override
							public void onFailure(
									@NonNull Call<java.util.List<Projects>> c,
									@NonNull Throwable t) {
								isStarred.setValue(false);
							}
						});
	}

	public void toggleStar(Context ctx, long projectId) {
		isActionLoading.setValue(true);
		Boolean starred = isStarred.getValue();
		boolean newStarState = starred == null || !starred;

		Call<Projects> call =
				newStarState
						? RetrofitClient.getApiInterface(ctx).starProject(projectId)
						: RetrofitClient.getApiInterface(ctx).unstarProject(projectId);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Projects> c, @NonNull Response<Projects> r) {
						if (r.isSuccessful()) {
							isStarred.setValue(newStarState);
							if (r.body() != null && r.body().getStarCount() >= 0) {
								starCount.setValue(r.body().getStarCount());
							} else {
								Integer current = starCount.getValue();
								if (current != null && current >= 0) {
									starCount.setValue(newStarState ? current + 1 : current - 1);
								}
							}
							actionSuccess.setValue(true);
							isActionLoading.setValue(false);
						} else {
							ApiResponseHandler.handleAction(
									r, isActionLoading, actionSuccess, error);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Projects> c, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}

	public void loadMrCount(Context ctx, long projectId) {
		RetrofitClient.getApiInterface(ctx)
				.getProjectMergeRequests(projectId, "opened", null, null, null, 1, 1)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<MergeRequests>> c,
									@NonNull Response<List<MergeRequests>> r) {
								mrCount.setValue(
										r.isSuccessful()
												? parseTotalHeader(r.headers().get("x-total"))
												: 0);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<MergeRequests>> c, @NonNull Throwable t) {
								mrCount.setValue(0);
							}
						});
	}

	private int parseTotalHeader(String header) {
		if (header != null) {
			try {
				return Integer.parseInt(header);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}

	public void clearError() {
		error.setValue(null);
	}
}
