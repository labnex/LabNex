package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.IssueContext;
import com.labnex.app.contexts.MergeRequestContext;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.Projects;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ExploreViewModel extends ViewModel {

	public static final String SCOPE_PROJECTS = "projects";
	public static final String SCOPE_ISSUES = "issues";
	public static final String SCOPE_MERGE_REQUESTS = "merge_requests";
	public static final String SCOPE_USERS = "users";

	public static class SearchResult {
		public final String scope;
		public final List<?> data;

		public SearchResult(String scope, List<?> data) {
			this.scope = scope;
			this.data = data;
		}
	}

	private final MutableLiveData<SearchResult> searchResult =
			new MutableLiveData<>(new SearchResult(SCOPE_PROJECTS, new ArrayList<>()));
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<String> activeScope = new MutableLiveData<>(SCOPE_PROJECTS);
	private final MutableLiveData<MergeRequestContext> navigateToMr = new MutableLiveData<>();
	private final MutableLiveData<IssueContext> navigateToIssue = new MutableLiveData<>();

	private String currentQuery = "";
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean needsDataLoad = true;

	public LiveData<SearchResult> getSearchResult() {
		return searchResult;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<MergeRequestContext> getNavigateToMr() {
		return navigateToMr;
	}

	public LiveData<IssueContext> getNavigateToIssue() {
		return navigateToIssue;
	}

	public void setScope(String scope) {
		activeScope.setValue(scope);
	}

	public String getScope() {
		return activeScope.getValue();
	}

	public boolean needsDataLoad() {
		return needsDataLoad;
	}

	public void search(Context ctx, String query) {
		this.currentQuery = query;
		this.currentPage = 1;
		this.error.setValue(null);
		this.isLoading.setValue(true);
		executeRequest(ctx, query, 1, true);
	}

	public void loadNextPage(Context ctx) {
		currentPage++;
		executeRequest(ctx, currentQuery, currentPage, false);
	}

	private void executeRequest(Context ctx, String query, int page, boolean isNewSearch) {
		String scope = getScope();
		if (scope == null) scope = SCOPE_PROJECTS;

		switch (scope) {
			case SCOPE_PROJECTS:
				handleCall(
						RetrofitClient.getApiInterface(ctx)
								.searchProjects(query, resultLimit, page),
						SCOPE_PROJECTS,
						isNewSearch);
				break;
			case SCOPE_ISSUES:
				handleCall(
						RetrofitClient.getApiInterface(ctx).searchIssues(query, resultLimit, page),
						SCOPE_ISSUES,
						isNewSearch);
				break;
			case SCOPE_MERGE_REQUESTS:
				handleCall(
						RetrofitClient.getApiInterface(ctx)
								.searchMergeRequests(query, resultLimit, page),
						SCOPE_MERGE_REQUESTS,
						isNewSearch);
				break;
			case SCOPE_USERS:
				handleCall(
						RetrofitClient.getApiInterface(ctx).searchUsers(query, resultLimit, page),
						SCOPE_USERS,
						isNewSearch);
				break;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void handleCall(Call<List<T>> call, String requestScope, boolean isNewSearch) {
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<T>> call, @NonNull Response<List<T>> response) {
						if (!requestScope.equals(getScope())) return;

						List<T> newData = response.body();
						List<T> currentList;

						if (isNewSearch || searchResult.getValue() == null) {
							currentList = new ArrayList<>();
						} else {
							currentList = new ArrayList<>((List<T>) searchResult.getValue().data);
						}

						if (response.isSuccessful() && newData != null && !newData.isEmpty()) {
							currentList.addAll(newData);
							searchResult.setValue(new SearchResult(requestScope, currentList));
						} else if (response.isSuccessful() && isNewSearch) {
							searchResult.setValue(
									new SearchResult(requestScope, new ArrayList<>()));
						} else if (!response.isSuccessful() && isNewSearch) {
							searchResult.setValue(
									new SearchResult(requestScope, new ArrayList<>()));
							error.setValue(ApiResponseHandler.getErrorMessageStatic(response));
						}
						isLoading.setValue(false);
						needsDataLoad = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<T>> call, @NonNull Throwable t) {
						if (!requestScope.equals(getScope())) return;
						if (isNewSearch) {
							searchResult.setValue(
									new SearchResult(requestScope, new ArrayList<>()));
						}
						isLoading.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}

	public void fetchAndNavigateMr(Context ctx, MergeRequests mr) {
		RetrofitClient.getApiInterface(ctx)
				.getProjectInfo(mr.getProjectId())
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Projects> c, @NonNull Response<Projects> r) {
								if (r.isSuccessful() && r.body() != null) {
									ProjectsContext pc = new ProjectsContext(r.body(), ctx);
									pc.saveToDB(ctx);
									navigateToMr.setValue(new MergeRequestContext(mr, pc));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Projects> c, @NonNull Throwable t) {}
						});
	}

	public void fetchAndNavigateIssue(Context ctx, Issues issue) {
		RetrofitClient.getApiInterface(ctx)
				.getProjectInfo(issue.getProjectId())
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Projects> c, @NonNull Response<Projects> r) {
								if (r.isSuccessful() && r.body() != null) {
									ProjectsContext pc = new ProjectsContext(r.body(), ctx);
									pc.saveToDB(ctx);
									navigateToIssue.setValue(new IssueContext(issue, pc));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Projects> c, @NonNull Throwable t) {}
						});
	}

	public void clearMrNavigation() {
		navigateToMr.setValue(null);
	}

	public void clearIssueNavigation() {
		navigateToIssue.setValue(null);
	}
}
