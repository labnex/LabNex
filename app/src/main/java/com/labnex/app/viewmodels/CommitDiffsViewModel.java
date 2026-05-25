package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.commits.Diff;
import com.labnex.app.models.commits.MrChanges;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CommitDiffsViewModel extends ViewModel {

	private final MutableLiveData<List<Diff>> diffsList = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();

	private long projectId;
	private String sha;
	private long mrIid;
	private boolean isMrChanges;
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public LiveData<List<Diff>> getDiffsList() {
		return diffsList;
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

	public void loadDiffs(Context ctx, long projectId, String sha) {
		if (ctx == null) return;
		this.projectId = projectId;
		this.sha = sha;
		this.isMrChanges = false;
		currentPage = 1;
		isLastPage = false;
		isLoadingMore = false;
		isLoading.setValue(true);
		fetch(ctx, 1);
	}

	public void loadMrChanges(Context ctx, long projectId, long mrIid) {
		if (ctx == null) return;
		this.projectId = projectId;
		this.mrIid = mrIid;
		this.isMrChanges = true;
		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.getMrChanges(projectId, mrIid)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MrChanges> c, @NonNull Response<MrChanges> r) {
								ApiResponseHandler.handleFetch(
										r,
										isLoading,
										() -> {
											MrChanges body = r.body();
											diffsList.setValue(
													body != null
															? body.getChanges()
															: new ArrayList<>());
										},
										error);
							}

							@Override
							public void onFailure(
									@NonNull Call<MrChanges> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void loadNextPage(Context ctx) {
		if (isMrChanges || isLoadingMore || isLastPage) return;
		isLoadingMore = true;
		currentPage++;
		fetch(ctx, currentPage);
	}

	private void fetch(Context ctx, int page) {
		Call<List<Diff>> call =
				RetrofitClient.getApiInterface(ctx)
						.getCommitDiffs(projectId, sha, resultLimit, page);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Diff>> c, @NonNull Response<List<Diff>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<Diff> body = r.body();
									List<Diff> current =
											(page == 1)
													? new ArrayList<>()
													: diffsList.getValue() != null
															? new ArrayList<>(diffsList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									diffsList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<Diff>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) diffsList.setValue(new ArrayList<>());
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
