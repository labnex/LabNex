package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.ProjectsApi;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.models.groups.GroupsItem;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.models.snippets.SnippetsItem;
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
	private int cachedUserId = -1;

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
					public void onResponse(
							@NonNull Call<User> call, @NonNull Response<User> response) {
						if (response.isSuccessful()
								&& response.code() == 200
								&& response.body() != null) {
							User user = response.body();
							userInfo.setValue(user);
							cachedUserId = user.getId();
							pendingCalls++;
							fetchStarredCount(ctx);
						} else if (response.code() == 401) {
							error.setValue("auth_error");
							starredCount.setValue(0);
						} else {
							starredCount.setValue(0);
						}
						onCallComplete();
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						error.setValue(t.getMessage());
						starredCount.setValue(0);
						onCallComplete();
					}
				});
	}

	private void fetchProjectsCount(Context ctx) {
		Call<List<Projects>> call = RetrofitClient.getApiInterface(ctx).getProjects(1, 1);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Projects>> call,
							@NonNull Response<List<Projects>> response) {
						projectsCount.setValue(
								response.isSuccessful()
										? parseTotalHeader(response.headers().get("x-total"))
										: 0);
						onCallComplete();
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Projects>> call, @NonNull Throwable t) {
						projectsCount.setValue(0);
						onCallComplete();
					}
				});
	}

	private void fetchGroupsCount(Context ctx) {
		Call<List<GroupsItem>> call =
				RetrofitClient.getApiInterface(ctx).getGroups(false, null, null, 1, 1);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<GroupsItem>> call,
							@NonNull Response<List<GroupsItem>> response) {
						groupsCount.setValue(
								response.isSuccessful()
										? parseTotalHeader(response.headers().get("x-total"))
										: 0);
						onCallComplete();
					}

					@Override
					public void onFailure(
							@NonNull Call<List<GroupsItem>> call, @NonNull Throwable t) {
						groupsCount.setValue(0);
						onCallComplete();
					}
				});
	}

	private void fetchStarredCount(Context ctx) {
		if (cachedUserId <= 0) {
			starredCount.setValue(0);
			onCallComplete();
			return;
		}

		Call<List<Projects>> call =
				RetrofitClient.getApiInterface(ctx).getStarredProjects(cachedUserId, 1, 1);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Projects>> call,
							@NonNull Response<List<Projects>> response) {
						starredCount.setValue(
								response.isSuccessful()
										? parseTotalHeader(response.headers().get("x-total"))
										: 0);
						onCallComplete();
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Projects>> call, @NonNull Throwable t) {
						starredCount.setValue(0);
						onCallComplete();
					}
				});
	}

	private void fetchIssuesCount(Context ctx) {
		Call<List<Issues>> call =
				RetrofitClient.getApiInterface(ctx).getIssues(null, "opened", null, 1, 1);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issues>> call,
							@NonNull Response<List<Issues>> response) {
						issuesCount.setValue(
								response.isSuccessful()
										? parseTotalHeader(response.headers().get("x-total"))
										: 0);
						onCallComplete();
					}

					@Override
					public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
						issuesCount.setValue(0);
						onCallComplete();
					}
				});
	}

	private void fetchMergeRequestsCount(Context ctx) {
		Call<List<MergeRequests>> call =
				RetrofitClient.getApiInterface(ctx)
						.getMergeRequests(null, "opened", null, null, null, 1, 1);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<MergeRequests>> call,
							@NonNull Response<List<MergeRequests>> response) {
						mergeRequestsCount.setValue(
								response.isSuccessful()
										? parseTotalHeader(response.headers().get("x-total"))
										: 0);
						onCallComplete();
					}

					@Override
					public void onFailure(
							@NonNull Call<List<MergeRequests>> call, @NonNull Throwable t) {
						mergeRequestsCount.setValue(0);
						onCallComplete();
					}
				});
	}

	private void fetchSnippetsCount(Context ctx) {
		Call<List<SnippetsItem>> call = RetrofitClient.getApiInterface(ctx).getSnippets(1, 1);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<SnippetsItem>> call,
							@NonNull Response<List<SnippetsItem>> response) {
						snippetsCount.setValue(
								response.isSuccessful()
										? parseTotalHeader(response.headers().get("x-total"))
										: 0);
						onCallComplete();
					}

					@Override
					public void onFailure(
							@NonNull Call<List<SnippetsItem>> call, @NonNull Throwable t) {
						snippetsCount.setValue(0);
						onCallComplete();
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
