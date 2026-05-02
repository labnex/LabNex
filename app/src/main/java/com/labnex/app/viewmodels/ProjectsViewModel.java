package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.models.projects.Projects;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ProjectsViewModel extends ViewModel {

	private final MutableLiveData<List<Projects>> projectsList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();

	private String currentSource;
	private int currentUserId;
	private int currentPage = 1;
	private int resultLimit;
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public LiveData<List<Projects>> getProjectsList() {
		return projectsList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public void setResultLimit(int limit) {
		this.resultLimit = limit;
	}

	public void loadProjects(Context ctx, String source, int userId) {
		this.currentSource = source;
		this.currentUserId = userId;
		currentPage = 1;
		isLastPage = false;
		isLoadingMore = false;
		isLoading.setValue(true);

		fetchProjects(ctx, 1);
	}

	public void loadNextPage(Context ctx) {
		if (isLoadingMore || isLastPage) return;
		isLoadingMore = true;
		currentPage++;
		fetchProjects(ctx, currentPage);
	}

	private void fetchProjects(Context ctx, int page) {
		Call<List<Projects>> call;

		if ("starred".equalsIgnoreCase(currentSource)) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getStarredProjects(currentUserId, resultLimit, page);
		} else if ("forks".equalsIgnoreCase(currentSource)) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectForks(currentUserId, resultLimit, page);
		} else if ("group".equalsIgnoreCase(currentSource)) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getGroupProjects(currentUserId, resultLimit, page);
		} else {
			call = RetrofitClient.getApiInterface(ctx).getProjects(resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Projects>> call,
							@NonNull Response<List<Projects>> response) {
						isLoading.setValue(false);
						isLoadingMore = false;

						if (response.isSuccessful()) {
							String totalHeader = response.headers().get("x-total");
							List<Projects> body = response.body();
							List<Projects> current =
									(page == 1)
											? new ArrayList<>()
											: projectsList.getValue() != null
													? new ArrayList<>(projectsList.getValue())
													: new ArrayList<>();
							if (body != null) current.addAll(body);
							projectsList.setValue(current);
							checkLastPage(
									body != null ? body.size() : 0, totalHeader, current.size());
						} else {
							if (page == 1) projectsList.setValue(new ArrayList<>());
							if (response.code() == 401) error.setValue("auth_error");
							else if (response.code() == 403) error.setValue("access_forbidden_403");
							else error.setValue("generic_error");
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Projects>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) projectsList.setValue(new ArrayList<>());
						error.setValue(t.getMessage());
					}
				});
	}

	private void checkLastPage(int bodySize, String totalHeader, int fullListSize) {
		if (bodySize < resultLimit) {
			isLastPage = true;
		} else if (totalHeader != null) {
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
