package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.MergeRequestContext;
import com.labnex.app.contexts.ProjectsContext;
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
public class MergeRequestsViewModel extends ViewModel {

	private final MutableLiveData<List<MergeRequests>> mrList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<MergeRequestContext> navigateToMr = new MutableLiveData<>();

	public LiveData<List<MergeRequests>> getMrList() {
		return mrList;
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

	private String currentSource;
	private int currentId;
	private String currentState;
	private String currentSearch;
	private int currentPage = 1;
	private int resultLimit;
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;
	private String currentScope = "created_by_me";
	private String currentOrderBy = "created_at";
	private String currentSort = "desc";

	public void setResultLimit(int limit) {
		this.resultLimit = limit;
	}

	public String getCurrentScope() {
		return currentScope;
	}

	public void setCurrentScope(String scope) {
		this.currentScope = scope;
	}

	public String getCurrentOrderBy() {
		return currentOrderBy;
	}

	public void setCurrentOrderBy(String orderBy) {
		this.currentOrderBy = orderBy;
	}

	public String getCurrentSort() {
		return currentSort;
	}

	public void setCurrentSort(String sort) {
		this.currentSort = sort;
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

	public void clearNavigation() {
		navigateToMr.setValue(null);
	}

	public void loadMergeRequests(
			Context ctx, String source, int id, String scope, String state, String search) {
		this.currentSource = source;
		this.currentId = id;
		this.currentScope = scope;
		this.currentState = state;
		this.currentSearch = search;
		currentPage = 1;
		isLastPage = false;
		isLoadingMore = false;
		isLoading.setValue(true);
		fetch(ctx, 1);
	}

	public void loadNextPage(Context ctx) {
		if (isLoadingMore || isLastPage) return;
		isLoadingMore = true;
		currentPage++;
		fetch(ctx, currentPage);
	}

	private void fetch(Context ctx, int page) {
		Call<List<MergeRequests>> call =
				switch (currentSource != null ? currentSource : "") {
					case "mr" ->
							RetrofitClient.getApiInterface(ctx)
									.getProjectMergeRequests(
											currentId,
											currentState,
											currentSearch,
											currentSort,
											currentOrderBy,
											resultLimit,
											page);
					case "group" ->
							RetrofitClient.getApiInterface(ctx)
									.getGroupMergeRequests(
											currentId,
											currentState,
											currentSearch,
											currentScope,
											currentSort,
											currentOrderBy,
											resultLimit,
											page);
					default ->
							RetrofitClient.getApiInterface(ctx)
									.getMergeRequests(
											currentScope,
											currentState,
											currentSearch,
											currentSort,
											currentOrderBy,
											resultLimit,
											page);
				};

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<MergeRequests>> call,
							@NonNull Response<List<MergeRequests>> response) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (response.isSuccessful()) {
							String totalHeader = response.headers().get("x-total");
							List<MergeRequests> body = response.body();
							List<MergeRequests> current =
									(page == 1)
											? new ArrayList<>()
											: mrList.getValue() != null
													? new ArrayList<>(mrList.getValue())
													: new ArrayList<>();
							if (body != null) current.addAll(body);
							mrList.setValue(current);
							checkLastPage(
									body != null ? body.size() : 0, totalHeader, current.size());
						} else {
							if (page == 1) mrList.setValue(new ArrayList<>());
							if (response.code() == 401) error.setValue("auth_error");
							else if (response.code() == 403) error.setValue("access_forbidden_403");
							else error.setValue("generic_error");
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<MergeRequests>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) mrList.setValue(new ArrayList<>());
						error.setValue(t.getMessage());
					}
				});
	}

	private void checkLastPage(int bodySize, String totalHeader, int fullListSize) {
		if (bodySize < resultLimit) isLastPage = true;
		else if (totalHeader != null) {
			try {
				if (fullListSize >= Integer.parseInt(totalHeader)) isLastPage = true;
			} catch (NumberFormatException ignored) {
			}
		}
	}

	public void clearError() {
		error.setValue(null);
	}
}
