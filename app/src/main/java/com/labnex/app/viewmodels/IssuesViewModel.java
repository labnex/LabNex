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
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssuesViewModel extends ViewModel {

	private MutableLiveData<List<Issues>> mutableList;

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

		Call<List<Issues>> call;

		if (source.equalsIgnoreCase("project")) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectIssues(id, state, resultLimit, page);
		} else {
			call = RetrofitClient.getApiInterface(ctx).getIssues(scope, state, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Issues>> call,
							@NonNull Response<List<Issues>> response) {

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
					public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
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
			IssuesAdapter adapter,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<Issues>> call;

		if (source.equalsIgnoreCase("project")) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getProjectIssues(id, state, resultLimit, page);
		} else {
			call = RetrofitClient.getApiInterface(ctx).getIssues(scope, state, resultLimit, page);
			RetrofitClient.getApiInterface(ctx).getIssues(scope, state, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Issues>> call,
							@NonNull Response<List<Issues>> response) {

						if (response.isSuccessful()) {

							List<Issues> list = mutableList.getValue();
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
					public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
