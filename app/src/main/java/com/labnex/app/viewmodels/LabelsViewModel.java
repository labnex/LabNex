package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.labels.CrudeLabel;
import com.labnex.app.models.labels.Labels;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class LabelsViewModel extends ViewModel {

	private final MutableLiveData<List<Labels>> labelList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> editSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> deleteSuccess = new MutableLiveData<>(false);

	public LiveData<Boolean> getCreateSuccess() {
		return createSuccess;
	}

	public LiveData<Boolean> getEditSuccess() {
		return editSuccess;
	}

	public LiveData<Boolean> getDeleteSuccess() {
		return deleteSuccess;
	}

	public LiveData<List<Labels>> getLabelList() {
		return labelList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public LiveData<Boolean> getActionSuccess() {
		return actionSuccess;
	}

	public LiveData<String> getError() {
		return error;
	}

	private String currentType;
	private long currentId;
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public void clearActionSuccess() {
		actionSuccess.setValue(false);
	}

	public void clearCreateSuccess() {
		createSuccess.setValue(false);
	}

	public void clearEditSuccess() {
		editSuccess.setValue(false);
	}

	public void clearDeleteSuccess() {
		deleteSuccess.setValue(false);
	}

	public void loadLabels(Context ctx, String type, long id) {
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
		Call<List<Labels>> call;
		if ("project".equals(currentType)) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectLabels(currentId, true, resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getGroupLabels(currentId, true, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Labels>> c, @NonNull Response<List<Labels>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<Labels> body = r.body();
									List<Labels> current =
											(page == 1)
													? new ArrayList<>()
													: labelList.getValue() != null
															? new ArrayList<>(labelList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									labelList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<Labels>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) labelList.setValue(new ArrayList<>());
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

	public void createLabel(Context ctx, String type, long id, CrudeLabel label) {
		isActionLoading.setValue(true);
		Call<Labels> call =
				"project".equals(type)
						? RetrofitClient.getApiInterface(ctx).createProjectLabel(id, label)
						: RetrofitClient.getApiInterface(ctx).createGroupLabel(id, label);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(@NonNull Call<Labels> c, @NonNull Response<Labels> r) {
						ApiResponseHandler.handleAction(r, isActionLoading, actionSuccess, error);
						if (r.isSuccessful()) {
							createSuccess.setValue(true);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Labels> c, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}

	public void updateLabel(Context ctx, String type, long id, long labelId, CrudeLabel label) {
		isActionLoading.setValue(true);
		Call<Labels> call =
				"project".equals(type)
						? RetrofitClient.getApiInterface(ctx).updateProjectLabel(id, labelId, label)
						: RetrofitClient.getApiInterface(ctx).updateGroupLabel(id, labelId, label);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(@NonNull Call<Labels> c, @NonNull Response<Labels> r) {
						ApiResponseHandler.handleAction(r, isActionLoading, actionSuccess, error);
						if (r.isSuccessful()) {
							editSuccess.setValue(true);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Labels> c, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}

	public void deleteLabel(Context ctx, String type, long id, long labelId) {
		isActionLoading.setValue(true);
		Call<Void> call =
				"project".equals(type)
						? RetrofitClient.getApiInterface(ctx).deleteProjectLabel(id, labelId)
						: RetrofitClient.getApiInterface(ctx).deleteGroupLabel(id, labelId);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(@NonNull Call<Void> c, @NonNull Response<Void> r) {
						ApiResponseHandler.handleAction(r, isActionLoading, actionSuccess, error);
						if (r.isSuccessful()) {
							deleteSuccess.setValue(true);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> c, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}

	public void clearError() {
		error.setValue(null);
	}
}
