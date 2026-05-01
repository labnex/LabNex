package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.models.events.Events;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ActivitiesViewModel extends ViewModel {

	public static final String FILTER_ALL = "all";
	public static final String FILTER_ISSUE = "issue";
	public static final String FILTER_NOTE = "note";
	public static final String FILTER_PROJECT = "project";
	public static final String FILTER_MERGE_REQUEST = "merge_request";
	public static final String FILTER_MILESTONE = "milestone";
	public static final String FILTER_SNIPPET = "snippet";
	public static final String FILTER_EPIC = "epic";
	public static final String FILTER_USER = "user";

	private final MutableLiveData<List<Events>> events = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private String currentFilter = FILTER_ALL;

	private int currentPage = 1;
	private int resultLimit;
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public LiveData<List<Events>> getEvents() {
		return events;
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

	public void setResultLimit(int limit) {
		this.resultLimit = limit;
	}

	public void setFilter(String filter) {
		if (filter.equals(currentFilter)) return;
		currentFilter = filter;
	}

	public void loadEvents(Context ctx) {
		currentPage = 1;
		isLastPage = false;
		isLoadingMore = false;
		isLoading.setValue(true);

		String targetType = FILTER_ALL.equals(currentFilter) ? null : currentFilter;

		Call<List<Events>> call =
				RetrofitClient.getApiInterface(ctx).getEvents(resultLimit, currentPage, targetType);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Events>> call,
							@NonNull Response<List<Events>> response) {
						isLoading.setValue(false);
						if (response.isSuccessful()) {
							String totalHeader = response.headers().get("x-total");
							checkLastPage(
									response.body() != null ? response.body().size() : 0,
									totalHeader);
							events.setValue(
									response.body() != null ? response.body() : new ArrayList<>());
						} else {
							events.setValue(new ArrayList<>());
							handleError(response.code());
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Events>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						events.setValue(new ArrayList<>());
						error.setValue(t.getMessage());
					}
				});
	}

	public void loadNextPage(Context ctx) {
		if (isLoadingMore || isLastPage) return;
		isLoadingMore = true;
		currentPage++;

		String targetType = FILTER_ALL.equals(currentFilter) ? null : currentFilter;

		Call<List<Events>> call =
				RetrofitClient.getApiInterface(ctx).getEvents(resultLimit, currentPage, targetType);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Events>> call,
							@NonNull Response<List<Events>> response) {
						isLoadingMore = false;
						if (response.isSuccessful() && response.body() != null) {
							List<Events> current = events.getValue();
							if (current == null) current = new ArrayList<>();
							current.addAll(response.body());
							events.setValue(current);

							String totalHeader = response.headers().get("x-total");
							checkLastPage(response.body().size(), totalHeader);
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Events>> call, @NonNull Throwable t) {
						isLoadingMore = false;
						error.setValue(t.getMessage());
					}
				});
	}

	private void checkLastPage(int bodySize, String totalHeader) {
		if (bodySize < resultLimit) {
			isLastPage = true;
		} else if (totalHeader != null) {
			try {
				int total = Integer.parseInt(totalHeader);
				List<Events> current = events.getValue();
				if (current != null && current.size() >= total) isLastPage = true;
			} catch (NumberFormatException ignored) {
			}
		}
	}

	private void handleError(int code) {
		if (code == 401) error.setValue("auth_error");
		else if (code == 403) error.setValue("access_forbidden_403");
		else error.setValue("generic_error");
	}

	public void clearError() {
		error.setValue(null);
	}
}
