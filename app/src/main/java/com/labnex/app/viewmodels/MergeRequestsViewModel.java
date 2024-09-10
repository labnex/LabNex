package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.labnex.app.R;
import com.labnex.app.adapters.MergeRequestsAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.merge_requests.MergeRequests;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MergeRequestsViewModel extends ViewModel {

	private MutableLiveData<List<MergeRequests>> mutableList;

	public LiveData<List<MergeRequests>> getMergeRequests(
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

		Call<List<MergeRequests>> call;

		if (source.equalsIgnoreCase("mr")) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectMergeRequests(id, state, resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getMergeRequests(scope, state, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<MergeRequests>> call,
							@NonNull Response<List<MergeRequests>> response) {

						if (response.isSuccessful()) {
							mutableList.postValue(response.body());
						} else if (response.code() == 401) {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomAppBar, ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomAppBar,
									ctx.getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomAppBar, ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<MergeRequests>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
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
			MergeRequestsAdapter adapter,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<MergeRequests>> call;

		if (source.equalsIgnoreCase("mr")) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectMergeRequests(id, state, resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getMergeRequests(scope, state, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<MergeRequests>> call,
							@NonNull Response<List<MergeRequests>> response) {

						if (response.isSuccessful()) {

							List<MergeRequests> list = mutableList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
						} else {
							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomAppBar,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<MergeRequests>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
