package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
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
	private final int resultLimit = Constants.getResultLimit();
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
							@NonNull Call<List<Events>> c, @NonNull Response<List<Events>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<Events> body = r.body();
									List<Events> current = new ArrayList<>();
									if (body != null) current.addAll(body);
									events.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<Events>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
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
							@NonNull Call<List<Events>> c, @NonNull Response<List<Events>> r) {
						isLoadingMore = false;
						if (r.isSuccessful() && r.body() != null) {
							List<Events> current = events.getValue();
							if (current == null) current = new ArrayList<>();
							current.addAll(r.body());
							events.setValue(current);
							String totalHeader = r.headers().get("x-total");
							checkLastPage(r.body().size(), totalHeader, current.size());
						} else {
							error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Events>> c, @NonNull Throwable t) {
						isLoadingMore = false;
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
