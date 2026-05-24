package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.ProjectsApi;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.models.user.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class HomeViewModel extends ViewModel {

	private final MutableLiveData<User> userInfo = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();

	private final MutableLiveData<Integer> projectsCount = new MutableLiveData<>(-1);
	private final MutableLiveData<Integer> groupsCount = new MutableLiveData<>(-1);
	private final MutableLiveData<Integer> starredCount = new MutableLiveData<>(-1);
	private final MutableLiveData<Integer> issuesCount = new MutableLiveData<>(-1);
	private final MutableLiveData<Integer> mergeRequestsCount = new MutableLiveData<>(-1);
	private final MutableLiveData<Integer> snippetsCount = new MutableLiveData<>(-1);
	private final MutableLiveData<Integer> mvpCount = new MutableLiveData<>(-1);
	private final MutableLiveData<List<com.labnex.app.database.models.Projects>> mvpProjects =
			new MutableLiveData<>();

	private int pendingCalls = 0;
	private boolean isRefreshing = false;
	private long cachedUserId = -1;

	public LiveData<User> getUserInfo() {
		return userInfo;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getHasLoadedOnce() {
		return hasLoadedOnce;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Integer> getProjectsCount() {
		return projectsCount;
	}

	public LiveData<Integer> getGroupsCount() {
		return groupsCount;
	}

	public LiveData<Integer> getStarredCount() {
		return starredCount;
	}

	public LiveData<Integer> getIssuesCount() {
		return issuesCount;
	}

	public LiveData<Integer> getMergeRequestsCount() {
		return mergeRequestsCount;
	}

	public LiveData<Integer> getSnippetsCount() {
		return snippetsCount;
	}

	public LiveData<Integer> getMvpCount() {
		return mvpCount;
	}

	public LiveData<List<com.labnex.app.database.models.Projects>> getMvpProjects() {
		return mvpProjects;
	}

	public void loadAll(Context ctx, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue()) && !isRefresh) return;
		if (Boolean.TRUE.equals(hasLoadedOnce.getValue()) && !isRefresh) return;

		isRefreshing = isRefresh;
		isLoading.setValue(true);

		pendingCalls = 6;
		fetchUserInfo(ctx);
		fetchProjectsCount(ctx);
		fetchGroupsCount(ctx);
		fetchIssuesCount(ctx);
		fetchMergeRequestsCount(ctx);
		fetchSnippetsCount(ctx);
	}

	public void loadMvp(Context ctx, int accountId) {
		ProjectsApi projectsApi = BaseApi.getInstance(ctx, ProjectsApi.class);
		if (projectsApi != null) {
			projectsApi
					.fetchAllMostVisited(accountId)
					.observeForever(
							projects -> {
								mvpProjects.setValue(projects);
								mvpCount.setValue(projects != null ? projects.size() : 0);
							});
		}
	}

	private void fetchUserInfo(Context ctx) {
		Call<User> call = RetrofitClient.getApiInterface(ctx).getCurrentUser();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(@NonNull Call<User> c, @NonNull Response<User> r) {
						if (r.isSuccessful() && r.body() != null) {
							User user = r.body();
							userInfo.setValue(user);
							cachedUserId = user.getId();
							pendingCalls++;
							fetchStarredCount(ctx);
						} else {
							if (r.code() == 401) error.setValue("auth_error");
							else error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
							starredCount.setValue(0);
						}
						onCallComplete();
					}

					@Override
					public void onFailure(@NonNull Call<User> c, @NonNull Throwable t) {
						error.setValue(t.getMessage());
						starredCount.setValue(0);
						onCallComplete();
					}
				});
	}

	private void fetchProjectsCount(Context ctx) {
		RetrofitClient.getApiInterface(ctx).getProjects(1, 1).enqueue(countCallback(projectsCount));
	}

	private void fetchGroupsCount(Context ctx) {
		RetrofitClient.getApiInterface(ctx)
				.getGroups(false, null, null, 1, 1)
				.enqueue(countCallback(groupsCount));
	}

	private void fetchStarredCount(Context ctx) {
		if (cachedUserId <= 0) {
			starredCount.setValue(0);
			onCallComplete();
			return;
		}
		RetrofitClient.getApiInterface(ctx)
				.getStarredProjects(cachedUserId, 1, 1)
				.enqueue(countCallback(starredCount));
	}

	private void fetchIssuesCount(Context ctx) {
		RetrofitClient.getApiInterface(ctx)
				.getIssues(null, "opened", null, 1, 1)
				.enqueue(countCallback(issuesCount));
	}

	private void fetchMergeRequestsCount(Context ctx) {
		RetrofitClient.getApiInterface(ctx)
				.getMergeRequests(null, "opened", null, null, null, 1, 1)
				.enqueue(countCallback(mergeRequestsCount));
	}

	private void fetchSnippetsCount(Context ctx) {
		RetrofitClient.getApiInterface(ctx).getSnippets(1, 1).enqueue(countCallback(snippetsCount));
	}

	private <T> Callback<List<T>> countCallback(MutableLiveData<Integer> target) {
		return new Callback<>() {
			@Override
			public void onResponse(@NonNull Call<List<T>> c, @NonNull Response<List<T>> r) {
				target.setValue(
						r.isSuccessful() ? parseTotalHeader(r.headers().get("x-total")) : 0);
				onCallComplete();
			}

			@Override
			public void onFailure(@NonNull Call<List<T>> c, @NonNull Throwable t) {
				target.setValue(0);
				onCallComplete();
			}
		};
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

	private synchronized void onCallComplete() {
		pendingCalls--;
		if (pendingCalls <= 0) {
			isLoading.setValue(false);
			hasLoadedOnce.setValue(true);
			if (isRefreshing) {
				AppUIStateManager.invalidateUI();
				isRefreshing = false;
			}
		}
	}
}
