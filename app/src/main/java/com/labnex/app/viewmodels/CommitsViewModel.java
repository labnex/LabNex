package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.labnex.app.R;
import com.labnex.app.adapters.CommitsAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.commits.Commits;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CommitsViewModel extends ViewModel {

	private MutableLiveData<List<Commits>> mutableList;

	public LiveData<List<Commits>> getCommits(
			Context ctx,
			String source,
			int id,
			int mergeRequestIid,
			String branch,
			int resultLimit,
			int page,
			Activity activity,
			BottomAppBar bottomAppBar) {

		mutableList = new MutableLiveData<>();
		loadInitialList(
				ctx,
				source,
				id,
				mergeRequestIid,
				branch,
				resultLimit,
				page,
				activity,
				bottomAppBar);

		return mutableList;
	}

	public void loadInitialList(
			Context ctx,
			String source,
			int id,
			int mergeRequestIid,
			String branch,
			int resultLimit,
			int page,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<Commits>> call;
		if (source.equalsIgnoreCase("mr")) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getMergeRequestCommits(id, mergeRequestIid, resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectCommits(id, branch, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Commits>> call,
							@NonNull Response<List<Commits>> response) {

						if (response.isSuccessful()) {
							mutableList.postValue(response.body());
						} else if (response.code() == 401) {

							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomAppBar,
									ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomAppBar,
									ctx.getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomAppBar,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {
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
			int mergeRequestIid,
			String branch,
			int resultLimit,
			int page,
			CommitsAdapter adapter,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<Commits>> call;
		if (source.equalsIgnoreCase("mr")) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getMergeRequestCommits(id, mergeRequestIid, resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectCommits(id, branch, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Commits>> call,
							@NonNull Response<List<Commits>> response) {

						if (response.isSuccessful()) {

							List<Commits> list = mutableList.getValue();
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
					public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
