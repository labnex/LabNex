package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.models.groups.CreateGroup;
import com.labnex.app.models.groups.GroupsItem;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class GroupsViewModel extends ViewModel {

	private final MutableLiveData<List<GroupsItem>> groupsList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<GroupsItem> groupDetail = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isDetailLoading = new MutableLiveData<>(false);

	public LiveData<List<GroupsItem>> getGroupsList() {
		return groupsList;
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

	public LiveData<Boolean> getActionSuccess() {
		return actionSuccess;
	}

	public LiveData<GroupsItem> getGroupDetail() {
		return groupDetail;
	}

	public LiveData<Boolean> getIsDetailLoading() {
		return isDetailLoading;
	}

	private int currentPage = 1;
	private int resultLimit;
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public void clearActionSuccess() {
		actionSuccess.setValue(false);
	}

	public void setResultLimit(int limit) {
		this.resultLimit = limit;
	}

	public void loadGroups(Context ctx) {
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

	public void loadGroupDetail(Context ctx, long groupId) {
		isDetailLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getGroup(groupId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<GroupsItem> c, @NonNull Response<GroupsItem> r) {
								isDetailLoading.setValue(false);
								if (r.isSuccessful() && r.body() != null) {
									groupDetail.setValue(r.body());
								} else {
									error.setValue("generic_error");
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<GroupsItem> c, @NonNull Throwable t) {
								isDetailLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	private void fetch(Context ctx, int page) {
		Call<List<GroupsItem>> call =
				RetrofitClient.getApiInterface(ctx).getGroups(true, "id", "asc", resultLimit, page);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<GroupsItem>> call,
							@NonNull Response<List<GroupsItem>> response) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (response.isSuccessful()) {
							String totalHeader = response.headers().get("x-total");
							List<GroupsItem> body = response.body();
							List<GroupsItem> current =
									(page == 1)
											? new ArrayList<>()
											: groupsList.getValue() != null
													? new ArrayList<>(groupsList.getValue())
													: new ArrayList<>();
							if (body != null) current.addAll(body);
							groupsList.setValue(current);
							checkLastPage(
									body != null ? body.size() : 0, totalHeader, current.size());
						} else {
							if (page == 1) groupsList.setValue(new ArrayList<>());
							if (response.code() == 401) error.setValue("auth_error");
							else if (response.code() == 403) error.setValue("access_forbidden_403");
							else error.setValue("generic_error");
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<GroupsItem>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) groupsList.setValue(new ArrayList<>());
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

	public void createGroup(Context ctx, CreateGroup group) {
		isActionLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.createGroup(group)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<GroupsItem> c, @NonNull Response<GroupsItem> r) {
								isActionLoading.setValue(false);
								if (r.code() == 201) {
									actionSuccess.setValue(true);
								} else if (r.code() == 401) {
									error.setValue("auth_error");
								} else if (r.code() == 403) {
									error.setValue("access_forbidden_403");
								} else {
									error.setValue("generic_error");
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<GroupsItem> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void updateGroup(Context ctx, long groupId, CreateGroup group) {
		isActionLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.updateGroup(groupId, group)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<GroupsItem> c, @NonNull Response<GroupsItem> r) {
								isActionLoading.setValue(false);
								if (r.code() == 200) {
									actionSuccess.setValue(true);
								} else if (r.code() == 401) {
									error.setValue("auth_error");
								} else if (r.code() == 403) {
									error.setValue("access_forbidden_403");
								} else {
									error.setValue("generic_error");
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<GroupsItem> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}
}
