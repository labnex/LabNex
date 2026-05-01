package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
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

	private String currentQuery = "";
	private int currentPage = 1;
	private int resultLimit;

	public LiveData<SearchResult> getSearchResult() {
		return searchResult;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public void setScope(String scope) {
		activeScope.setValue(scope);
	}

	public String getScope() {
		return activeScope.getValue();
	}

	public void setResultLimit(int limit) {
		this.resultLimit = limit;
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
							if (response.code() == 401) error.setValue("auth_error");
							else if (response.code() == 403) error.setValue("access_forbidden_403");
							else error.setValue("generic_error");
						}
						isLoading.setValue(false);
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
}
