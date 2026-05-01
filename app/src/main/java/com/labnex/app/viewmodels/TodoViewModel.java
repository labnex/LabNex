package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.models.todo.ToDoItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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

	private final MutableLiveData<List<ToDoItem>> allTodos =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<List<ToDoItem>> filteredTodos = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private String currentFilter = FILTER_ALL;

	public LiveData<List<ToDoItem>> getFilteredTodos() {
		return filteredTodos;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public String getFilter() {
		return currentFilter;
	}

	public void setFilter(String filter) {
		if (filter.equals(currentFilter)) return;
		currentFilter = filter;
		applyFilter();
	}

	public void loadTodos(Context ctx) {
		isLoading.setValue(true);

		Call<List<ToDoItem>> call = RetrofitClient.getApiInterface(ctx).getAllTodos();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<ToDoItem>> call,
							@NonNull Response<List<ToDoItem>> response) {
						isLoading.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							allTodos.setValue(response.body());
							applyFilter();
						} else {
							allTodos.setValue(new ArrayList<>());
							filteredTodos.setValue(null);
							error.setValue("generic_error");
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<ToDoItem>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						allTodos.setValue(new ArrayList<>());
						filteredTodos.setValue(null);
						error.setValue(t.getMessage());
					}
				});
	}

	public void markAsDone(Context ctx, long todoId) {
		Call<ToDoItem> call = RetrofitClient.getApiInterface(ctx).markTodoAsDone((int) todoId);
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ToDoItem> call, @NonNull Response<ToDoItem> response) {
						if (response.isSuccessful()) {
							List<ToDoItem> current = allTodos.getValue();
							if (current != null) {
								current.removeIf(todo -> todo.getId() == todoId);
								allTodos.setValue(current);
								applyFilter();
							}
							error.setValue("marked_done");
						} else {
							error.setValue("generic_error");
						}
					}

					@Override
					public void onFailure(@NonNull Call<ToDoItem> call, @NonNull Throwable t) {
						error.setValue(t.getMessage());
					}
				});
	}

	private void applyFilter() {
		List<ToDoItem> source = allTodos.getValue();
		if (source == null) source = new ArrayList<>();

		List<ToDoItem> filtered =
				switch (currentFilter) {
					case FILTER_ISSUES ->
							source.stream()
									.filter(item -> "Issue".equalsIgnoreCase(item.getTargetType()))
									.collect(Collectors.toList());
					case FILTER_MERGE_REQUESTS ->
							source.stream()
									.filter(
											item ->
													"MergeRequest"
															.equalsIgnoreCase(item.getTargetType()))
									.collect(Collectors.toList());
					default -> new ArrayList<>(source);
				};
		filteredTodos.setValue(filtered);
	}

	public void clearError() {
		error.setValue(null);
	}
}
