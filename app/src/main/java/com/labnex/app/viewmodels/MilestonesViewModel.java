package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.milestone.CrudeMilestone;
import com.labnex.app.models.milestone.Milestones;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MilestonesViewModel extends ViewModel {

	private final MutableLiveData<List<Milestones>> milestoneList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> editSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>(false);

	public LiveData<Boolean> getDeleteSuccess() {
		return deleteSuccess;
	}

	public LiveData<Boolean> getActionSuccess() {
		return actionSuccess;
	}

	public LiveData<List<Milestones>> getMilestoneList() {
		return milestoneList;
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

	public LiveData<Boolean> getCreateSuccess() {
		return createSuccess;
	}

	public LiveData<Boolean> getEditSuccess() {
		return editSuccess;
	}

	public void clearCreateSuccess() {
		createSuccess.setValue(false);
	}

	public void clearEditSuccess() {
		editSuccess.setValue(false);
	}

	public void clearActionSuccess() {
		actionSuccess.setValue(false);
	}

	public void clearDeleteSuccess() {
		deleteSuccess.setValue(false);
	}

	private String currentType;
	private long currentId;
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public void loadMilestones(Context ctx, String type, long id) {
		this.currentType = type;
		this.currentId = id;
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
		Call<List<Milestones>> call;
		if ("group".equals(currentType)) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectMilestones(currentId, "active", resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectMilestones(currentId, "active", resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Milestones>> c,
							@NonNull Response<List<Milestones>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<Milestones> body = r.body();
									List<Milestones> current =
											(page == 1)
													? new ArrayList<>()
													: milestoneList.getValue() != null
															? new ArrayList<>(
																	milestoneList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									milestoneList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<Milestones>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) milestoneList.setValue(new ArrayList<>());
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

	public void createMilestone(
			Context ctx, String type, long projectId, CrudeMilestone milestone) {
		isActionLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.createMilestone(projectId, milestone)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Milestones> c, @NonNull Response<Milestones> r) {
								ApiResponseHandler.handleAction(
										r, isActionLoading, actionSuccess, error);
								if (r.isSuccessful()) createSuccess.setValue(true);
							}

							@Override
							public void onFailure(
									@NonNull Call<Milestones> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void updateMilestone(
			Context ctx, String type, long projectId, long milestoneId, CrudeMilestone milestone) {
		isActionLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.updateMilestone(projectId, milestoneId, milestone)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Milestones> c, @NonNull Response<Milestones> r) {
								ApiResponseHandler.handleAction(
										r, isActionLoading, actionSuccess, error);
								if (r.isSuccessful()) editSuccess.setValue(true);
							}

							@Override
							public void onFailure(
									@NonNull Call<Milestones> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void deleteMilestone(Context ctx, String type, long projectId, long milestoneId) {
		isActionLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.deleteMilestone(projectId, milestoneId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> c, @NonNull Response<Void> r) {
								ApiResponseHandler.handleAction(
										r, isActionLoading, actionSuccess, error);
								if (r.isSuccessful()) deleteSuccess.setValue(true);
							}

							@Override
							public void onFailure(@NonNull Call<Void> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}
}
