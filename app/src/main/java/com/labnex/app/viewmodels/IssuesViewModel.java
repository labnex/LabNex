package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.labnex.app.R;
import com.labnex.app.adapters.IssuesAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.issues.Issues;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssuesViewModel extends ViewModel {

	private MutableLiveData<List<Issues>> mutableList;
	private Call<List<Issues>> currentCall;

	public LiveData<List<Issues>> getIssues(
			Context ctx,
			String source,
			int id,
			String scope,
			String state,
			int resultLimit,
			int page,
			Activity activity,
			BottomAppBar bottomAppBar) {

		mutableList = new MutableLiveData<>();
		loadInitialList(ctx, source, id, scope, state, resultLimit, page, activity, bottomAppBar);

		return mutableList;
	}

	public void loadInitialList(
			Context ctx,
			String source,
			int id,
			String scope,
			String state,
			int resultLimit,
			int page,
			Activity activity,
			BottomAppBar bottomAppBar) {

		if (currentCall != null && !currentCall.isCanceled()) {
			currentCall.cancel();
		}

		if (source.equalsIgnoreCase("project")) {
			currentCall =
					RetrofitClient.getApiInterface(ctx)
							.getProjectIssues(id, state, resultLimit, page);
		} else {
			currentCall =
					RetrofitClient.getApiInterface(ctx).getIssues(scope, state, resultLimit, page);
		}

		currentCall.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issues>> call,
							@NonNull Response<List<Issues>> response) {
						if (!call.isCanceled()) {
							if (response.isSuccessful() && response.body() != null) {
								mutableList.postValue(response.body());
							} else if (response.code() == 401) {
								Snackbar.info(
										ctx, activity.findViewById(android.R.id.content),
										bottomAppBar, ctx.getString(R.string.not_authorized));
							} else if (response.code() == 403) {
								Snackbar.info(
										ctx, activity.findViewById(android.R.id.content),
										bottomAppBar, ctx.getString(R.string.access_forbidden_403));
							} else {
								Snackbar.info(
										ctx, activity.findViewById(android.R.id.content),
										bottomAppBar, ctx.getString(R.string.generic_error));
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
						if (!call.isCanceled()) {
							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomAppBar,
											ctx.getString(R.string.generic_server_response_error));
						}
					}
				});
	}

	public void loadMore(
			Context ctx,
			String source,
			int id,
			String scope,
			String state,
			int resultLimit,
			int page,
			IssuesAdapter adapter,
			Activity activity,
			BottomAppBar bottomAppBar) {

		if (currentCall != null && !currentCall.isCanceled()) {
			currentCall.cancel();
		}

		if (source.equalsIgnoreCase("project")) {
			currentCall =
					RetrofitClient.getApiInterface(ctx)
							.getProjectIssues(id, state, resultLimit, page);
		} else {
			currentCall =
					RetrofitClient.getApiInterface(ctx).getIssues(scope, state, resultLimit, page);
		}

		currentCall.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issues>> call,
							@NonNull Response<List<Issues>> response) {
						if (!call.isCanceled()) {
							if (response.isSuccessful() && response.body() != null) {
								List<Issues> currentList = mutableList.getValue();
								if (currentList == null) {
									currentList = new ArrayList<>();
								}
								if (!response.body().isEmpty()) {
									currentList.addAll(response.body());
									adapter.updateList(currentList);
									mutableList.postValue(currentList);
								} else {
									adapter.setMoreDataAvailable(false);
								}
							} else {
								Snackbar.info(
										ctx, activity.findViewById(android.R.id.content),
										bottomAppBar, ctx.getString(R.string.generic_error));
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
						if (!call.isCanceled()) {
							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomAppBar,
											ctx.getString(R.string.generic_server_response_error));
						}
					}
				});
	}

	@Override
	protected void onCleared() {
		if (currentCall != null && !currentCall.isCanceled()) {
			currentCall.cancel();
		}
		super.onCleared();
	}
}
