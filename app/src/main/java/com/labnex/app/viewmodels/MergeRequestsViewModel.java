package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.merge_requests.CrudeMergeRequest;
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
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> editSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<ProjectsContext> projectContextForMr = new MutableLiveData<>();

	public LiveData<ProjectsContext> getProjectContextForMr() {
		return projectContextForMr;
	}

	public LiveData<List<MergeRequests>> getMrList() {
		return mrList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public LiveData<Boolean> getActionSuccess() {
		return actionSuccess;
	}

	public LiveData<Boolean> getCreateSuccess() {
		return createSuccess;
	}

	public LiveData<Boolean> getEditSuccess() {
		return editSuccess;
	}

	private String currentSource;
	private long currentId;
	private String currentState;
	private String currentSearch;
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;
	private String currentScope = "created_by_me";
	private String currentOrderBy = "created_at";
	private String currentSort = "desc";

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

	public void clearActionSuccess() {
		actionSuccess.setValue(false);
	}

	public void clearCreateSuccess() {
		createSuccess.setValue(false);
	}

	public void clearEditSuccess() {
		editSuccess.setValue(false);
	}

	public void createMergeRequest(Context ctx, long projectId, CrudeMergeRequest mr) {
		isActionLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.createMergeRequest(projectId, mr)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MergeRequests> c,
									@NonNull Response<MergeRequests> r) {
								ApiResponseHandler.handleAction(
										r, isActionLoading, actionSuccess, error);
								if (r.isSuccessful()) createSuccess.setValue(true);
							}

							@Override
							public void onFailure(
									@NonNull Call<MergeRequests> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void updateMergeRequest(Context ctx, long projectId, long mrIid, CrudeMergeRequest mr) {
		isActionLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.updateMergeRequest(projectId, mrIid, mr)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MergeRequests> c,
									@NonNull Response<MergeRequests> r) {
								ApiResponseHandler.handleAction(
										r, isActionLoading, actionSuccess, error);
								if (r.isSuccessful()) editSuccess.setValue(true);
							}

							@Override
							public void onFailure(
									@NonNull Call<MergeRequests> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void fetchProjectForMr(Context ctx, MergeRequests mr) {
		if (ctx == null) return;
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
									projectContextForMr.setValue(pc);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Projects> c, @NonNull Throwable t) {}
						});
	}

	public void clearProjectContextForMr() {
		projectContextForMr.setValue(null);
	}

	public void loadMergeRequests(
			Context ctx, String source, long id, String scope, String state, String search) {
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
							@NonNull Call<List<MergeRequests>> c,
							@NonNull Response<List<MergeRequests>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<MergeRequests> body = r.body();
									List<MergeRequests> current =
											(page == 1)
													? new ArrayList<>()
													: mrList.getValue() != null
															? new ArrayList<>(mrList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									mrList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(
							@NonNull Call<List<MergeRequests>> c, @NonNull Throwable t) {
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
