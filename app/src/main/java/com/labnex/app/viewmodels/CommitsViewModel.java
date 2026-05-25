package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.commits.Commits;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CommitsViewModel extends ViewModel {

	private final MutableLiveData<List<Commits>> commitsList =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();

	private String source;
	private long projectId;
	private long mergeRequestIid;
	private String branch;
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public LiveData<List<Commits>> getCommitsList() {
		return commitsList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void loadCommits(
			Context ctx, String source, long projectId, long mergeRequestIid, String branch) {
		if (ctx == null) return;
		this.source = source;
		this.projectId = projectId;
		this.mergeRequestIid = mergeRequestIid;
		this.branch = branch;
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
		Call<List<Commits>> call;
		if ("mr".equals(source)) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getMergeRequestCommits(projectId, mergeRequestIid, resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectCommits(projectId, branch, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Commits>> c, @NonNull Response<List<Commits>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<Commits> body = r.body();
									List<Commits> current =
											(page == 1)
													? new ArrayList<>()
													: commitsList.getValue() != null
															? new ArrayList<>(
																	commitsList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									commitsList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<Commits>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) commitsList.setValue(new ArrayList<>());
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
}
