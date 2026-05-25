package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.models.todo.ToDoItem;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class TodoViewModel extends ViewModel {

	public static final String FILTER_ALL = "all";
	public static final String FILTER_ISSUES = "issues";
	public static final String FILTER_MERGE_REQUESTS = "merge_requests";

	public static final String STATE_PENDING = "pending";
	public static final String STATE_DONE = "done";

	private final MutableLiveData<List<ToDoItem>> todoList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Projects> fetchedProject = new MutableLiveData<>();
	private final MutableLiveData<MergeRequests> fetchedMr = new MutableLiveData<>();
	private final MutableLiveData<Issues> fetchedIssue = new MutableLiveData<>();

	private String currentType = FILTER_ALL;
	private String currentState = STATE_PENDING;
	private String currentAction = null;
	private boolean needsDataLoad = true;

	public LiveData<List<ToDoItem>> getTodoList() {
		return todoList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Projects> getFetchedProject() {
		return fetchedProject;
	}

	public LiveData<MergeRequests> getFetchedMr() {
		return fetchedMr;
	}

	public LiveData<Issues> getFetchedIssue() {
		return fetchedIssue;
	}

	public String getCurrentType() {
		return currentType;
	}

	public String getCurrentState() {
		return currentState;
	}

	public String getCurrentAction() {
		return currentAction;
	}

	public void setCurrentType(String type) {
		this.currentType = type;
	}

	public void setCurrentState(String state) {
		this.currentState = state;
	}

	public void setCurrentAction(String action) {
		this.currentAction = action;
	}

	public boolean needsDataLoad() {
		return needsDataLoad;
	}

	public void loadTodos(Context ctx) {
		if (ctx == null) return;
		isLoading.setValue(true);

		String type = FILTER_ALL.equals(currentType) ? null : currentType;
		String state = currentState;
		String action = currentAction;

		RetrofitClient.getApiInterface(ctx)
				.getAllTodos(type, state, action)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<ToDoItem>> c,
									@NonNull Response<List<ToDoItem>> r) {
								ApiResponseHandler.handleFetch(
										r,
										isLoading,
										() -> {
											List<ToDoItem> items = r.body();
											if (items != null) {
												todoList.setValue(items);
												needsDataLoad = false;
											} else {
												todoList.setValue(new ArrayList<>());
											}
										},
										error);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<ToDoItem>> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								todoList.setValue(new ArrayList<>());
								error.setValue(t.getMessage());
							}
						});
	}

	public void markAsDone(Context ctx, long todoId) {
		RetrofitClient.getApiInterface(ctx)
				.markTodoAsDone(todoId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<ToDoItem> c, @NonNull Response<ToDoItem> r) {
								if (r.isSuccessful()) {
									List<ToDoItem> current = todoList.getValue();
									if (current != null) {
										current.removeIf(todo -> todo.getId() == todoId);
										todoList.setValue(new ArrayList<>(current));
									}
									error.setValue("marked_done");
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(@NonNull Call<ToDoItem> c, @NonNull Throwable t) {
								error.setValue(t.getMessage());
							}
						});
	}

	public void clearError() {
		error.setValue(null);
	}

	public void clearFetchedData() {
		fetchedProject.setValue(null);
		fetchedMr.setValue(null);
		fetchedIssue.setValue(null);
	}

	public void fetchProjectForTodo(Context ctx, long projectId) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getProjectInfo(projectId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Projects> c, @NonNull Response<Projects> r) {
								ApiResponseHandler.handleFetch(
										r,
										isLoading,
										() -> fetchedProject.setValue(r.body()),
										error);
							}

							@Override
							public void onFailure(@NonNull Call<Projects> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void fetchMrForTodo(Context ctx, long projectId, long mrIid) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getMergeRequest(projectId, mrIid)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MergeRequests> c,
									@NonNull Response<MergeRequests> r) {
								ApiResponseHandler.handleFetch(
										r, isLoading, () -> fetchedMr.setValue(r.body()), error);
							}

							@Override
							public void onFailure(
									@NonNull Call<MergeRequests> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void fetchIssueForTodo(Context ctx, long projectId, long issueIid) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getIssue(projectId, issueIid)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Issues> c, @NonNull Response<Issues> r) {
								ApiResponseHandler.handleFetch(
										r, isLoading, () -> fetchedIssue.setValue(r.body()), error);
							}

							@Override
							public void onFailure(@NonNull Call<Issues> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void markAllAsDone(Context ctx) {
		if (ctx == null) return;

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.markAllTodoAsDone()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<ToDoItem> c, @NonNull Response<ToDoItem> r) {
								isLoading.setValue(false);
								if (r.isSuccessful()) {
									todoList.setValue(new ArrayList<>());
									needsDataLoad = true;
									error.setValue("all_marked_done");
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(@NonNull Call<ToDoItem> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}
}
