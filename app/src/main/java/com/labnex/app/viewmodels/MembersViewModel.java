package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.projects.Stars;
import com.labnex.app.models.user.User;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MembersViewModel extends ViewModel {

	private final MutableLiveData<List<User>> memberList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();

	private String currentType;
	private long currentId;
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public LiveData<List<User>> getMemberList() {
		return memberList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public void loadMembers(Context ctx, String type, long id) {
		this.currentType = type;
		this.currentId = id;
		currentPage = 1;
		isLastPage = false;
		isLoadingMore = false;
		memberList.setValue(null);
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
		if ("starrers".equals(currentType)) {
			fetchStarrers(ctx, page);
		} else {
			fetchMembers(ctx, page);
		}
	}

	private void fetchMembers(Context ctx, int page) {
		Call<List<User>> call =
				"project".equals(currentType)
						? RetrofitClient.getApiInterface(ctx)
								.getProjectMembers(currentId, resultLimit, page)
						: RetrofitClient.getApiInterface(ctx)
								.getGroupMembers(currentId, resultLimit, page);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<User>> c, @NonNull Response<List<User>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<User> body = r.body();
									List<User> current =
											(page == 1)
													? new ArrayList<>()
													: memberList.getValue() != null
															? new ArrayList<>(memberList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									memberList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) memberList.setValue(new ArrayList<>());
						error.setValue(t.getMessage());
					}
				});
	}

	private void fetchStarrers(Context ctx, int page) {
		Call<List<Stars>> call =
				RetrofitClient.getApiInterface(ctx)
						.getProjectStarrers(currentId, resultLimit, page);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Stars>> c, @NonNull Response<List<Stars>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									List<User> users = new ArrayList<>();
									if (r.body() != null) {
										for (Stars s : r.body()) {
											if (s.getUser() != null) {
												com.labnex.app.models.projects.User pu =
														s.getUser();
												com.labnex.app.models.user.User u =
														new com.labnex.app.models.user.User();
												u.setId(pu.getId());
												u.setUsername(pu.getUsername());
												u.setFullName(pu.getName());
												u.setAvatarUrl(pu.getAvatarUrl());
												u.setWebUrl(pu.getWebUrl());
												u.setState(pu.getState());
												u.setLocked(pu.isLocked());
												users.add(u);
											}
										}
									}
									String totalHeader = r.headers().get("x-total");
									List<User> current =
											(page == 1)
													? new ArrayList<>()
													: memberList.getValue() != null
															? new ArrayList<>(memberList.getValue())
															: new ArrayList<>();
									current.addAll(users);
									memberList.setValue(current);
									checkLastPage(users.size(), totalHeader, current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<Stars>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) memberList.setValue(new ArrayList<>());
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
