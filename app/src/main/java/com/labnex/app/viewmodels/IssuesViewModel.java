package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.IssueContext;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.issues.CrudeIssue;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.projects.Projects;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssuesViewModel extends ViewModel {

	private final MutableLiveData<List<Issues>> issueList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<IssueContext> navigateToIssue = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> editSuccess = new MutableLiveData<>(false);

	private String currentSource;
	private long currentId;
	private String currentScope = "created_by_me";
	private String currentState;
	private String currentSearch;
	private String currentOrderBy = "created_at";
	private String currentSort = "desc";
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public LiveData<List<Issues>> getIssueList() {
		return issueList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<IssueContext> getNavigateToIssue() {
		return navigateToIssue;
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

	public void clearNavigation() {
		navigateToIssue.setValue(null);
	}

	public void loadIssues(
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
		Call<List<Issues>> call =
				switch (currentSource != null ? currentSource : "") {
					case "project" ->
							RetrofitClient.getApiInterface(ctx)
									.getProjectIssues(
											currentId,
											currentState,
											currentSearch,
											currentScope,
											resultLimit,
											page);
					case "group" ->
							RetrofitClient.getApiInterface(ctx)
									.getGroupIssues(
											currentId,
											currentState,
											currentSearch,
											resultLimit,
											page,
											currentScope);
					default ->
							RetrofitClient.getApiInterface(ctx)
									.getIssues(
											currentScope,
											currentState,
											currentSearch,
											resultLimit,
											page);
				};

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issues>> c, @NonNull Response<List<Issues>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<Issues> body = r.body();
									List<Issues> current =
											(page == 1)
													? new ArrayList<>()
													: issueList.getValue() != null
															? new ArrayList<>(issueList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									issueList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<Issues>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) issueList.setValue(new ArrayList<>());
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

	public void createIssue(Context ctx, String type, long id, CrudeIssue issue) {
		isActionLoading.setValue(true);
		Call<Issues> call =
				"project".equals(type)
						? RetrofitClient.getApiInterface(ctx).createIssue(id, issue)
						: RetrofitClient.getApiInterface(ctx).createGroupIssue(id, issue);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(@NonNull Call<Issues> c, @NonNull Response<Issues> r) {
						ApiResponseHandler.handleAction(r, isActionLoading, actionSuccess, error);
						if (r.isSuccessful()) createSuccess.setValue(true);
					}

					@Override
					public void onFailure(@NonNull Call<Issues> c, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}

	public void updateIssue(
			Context ctx, String type, long projectId, long issueIid, CrudeIssue issue) {
		isActionLoading.setValue(true);
		Call<Issues> call =
				"project".equals(type)
						? RetrofitClient.getApiInterface(ctx)
								.updateIssue(projectId, issueIid, issue)
						: RetrofitClient.getApiInterface(ctx)
								.updateGroupIssue(projectId, issueIid, issue);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(@NonNull Call<Issues> c, @NonNull Response<Issues> r) {
						ApiResponseHandler.handleAction(r, isActionLoading, actionSuccess, error);
						if (r.isSuccessful()) editSuccess.setValue(true);
					}

					@Override
					public void onFailure(@NonNull Call<Issues> c, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}
}
