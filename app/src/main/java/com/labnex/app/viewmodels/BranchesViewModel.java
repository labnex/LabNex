package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.branches.Branches;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class BranchesViewModel extends ViewModel {

	private final MutableLiveData<List<Branches>> branchList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public LiveData<Boolean> getActionSuccess() {
		return actionSuccess;
	}

	public LiveData<List<Branches>> getBranchList() {
		return branchList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	private long projectId;
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public void loadBranches(Context ctx, long projectId) {
		this.projectId = projectId;
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

	public void clearActionSuccess() {
		actionSuccess.setValue(false);
	}

	public void createBranch(Context ctx, long projectId, String branch, String ref) {
		isActionLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.createBranch((int) projectId, branch, ref)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<com.labnex.app.models.branches.Branches> c,
									@NonNull Response<com.labnex.app.models.branches.Branches> r) {
								ApiResponseHandler.handleAction(
										r, isActionLoading, actionSuccess, error);
							}

							@Override
							public void onFailure(
									@NonNull Call<com.labnex.app.models.branches.Branches> c,
									@NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	private void fetch(Context ctx, int page) {
		Call<List<Branches>> call =
				RetrofitClient.getApiInterface(ctx)
						.getProjectBranches((int) projectId, resultLimit, page);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Branches>> c, @NonNull Response<List<Branches>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<Branches> body = r.body();
									List<Branches> current =
											(page == 1)
													? new ArrayList<>()
													: branchList.getValue() != null
															? new ArrayList<>(branchList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									branchList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<Branches>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) branchList.setValue(new ArrayList<>());
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
