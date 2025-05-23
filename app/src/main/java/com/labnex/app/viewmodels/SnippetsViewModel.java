package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.labnex.app.R;
import com.labnex.app.adapters.SnippetsAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.ActivitySnippetsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.snippets.SnippetsItem;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class SnippetsViewModel extends ViewModel {

	private MutableLiveData<List<SnippetsItem>> mutableList;
	private Call<List<SnippetsItem>> currentCall;

	public LiveData<List<SnippetsItem>> getSnippets(
			Context ctx,
			int resultLimit,
			int page,
			Activity activity,
			ActivitySnippetsBinding binding) {

		mutableList = new MutableLiveData<>(null);
		loadInitialList(ctx, resultLimit, page, activity, binding);
		return mutableList;
	}

	private void loadInitialList(
			Context ctx,
			int resultLimit,
			int page,
			Activity activity,
			ActivitySnippetsBinding binding) {

		if (currentCall != null && !currentCall.isCanceled()) {
			currentCall.cancel();
		}

		currentCall = RetrofitClient.getApiInterface(ctx).getSnippets(resultLimit, page);

		currentCall.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<SnippetsItem>> call,
							@NonNull Response<List<SnippetsItem>> response) {
						if (!call.isCanceled()) {
							if (response.isSuccessful() && response.body() != null) {
								List<SnippetsItem> snippetsItems = response.body();
								mutableList.postValue(snippetsItems);
							} else {
								mutableList.postValue(new ArrayList<>());
								handleError(ctx, response.code(), activity, binding.bottomAppBar);
							}
						}
						binding.progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onFailure(
							@NonNull Call<List<SnippetsItem>> call, @NonNull Throwable t) {
						if (!call.isCanceled()) {
							mutableList.postValue(new ArrayList<>());
							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									binding.bottomAppBar,
									ctx.getString(R.string.generic_server_response_error));
						}
						binding.progressBar.setVisibility(View.GONE);
					}
				});
	}

	public void loadMore(
			Context ctx,
			int resultLimit,
			int page,
			SnippetsAdapter adapter,
			Activity activity,
			ActivitySnippetsBinding binding) {

		if (currentCall != null && !currentCall.isCanceled()) {
			currentCall.cancel();
		}

		currentCall = RetrofitClient.getApiInterface(ctx).getSnippets(resultLimit, page);

		currentCall.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<SnippetsItem>> call,
							@NonNull Response<List<SnippetsItem>> response) {
						if (!call.isCanceled()) {
							if (response.isSuccessful() && response.body() != null) {
								List<SnippetsItem> newSnippets = response.body();
								List<SnippetsItem> currentList = mutableList.getValue();
								if (currentList == null) {
									currentList = new ArrayList<>();
								}
								if (!newSnippets.isEmpty()) {
									currentList.addAll(newSnippets);
									adapter.updateList(currentList);
									mutableList.postValue(currentList);
								} else {
									adapter.setMoreDataAvailable(false);
								}
							} else {
								handleError(ctx, response.code(), activity, binding.bottomAppBar);
							}
						}
						binding.progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onFailure(
							@NonNull Call<List<SnippetsItem>> call, @NonNull Throwable t) {
						if (!call.isCanceled()) {
							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									binding.bottomAppBar,
									ctx.getString(R.string.generic_server_response_error));
						}
						binding.progressBar.setVisibility(View.GONE);
					}
				});
	}

	private void handleError(
			Context ctx, int responseCode, Activity activity, BottomAppBar bottomAppBar) {
		String message;
		if (responseCode == 401) {
			message = ctx.getString(R.string.not_authorized);
		} else if (responseCode == 403) {
			message = ctx.getString(R.string.access_forbidden_403);
		} else {
			message = ctx.getString(R.string.generic_error);
		}
		Snackbar.info(ctx, activity.findViewById(android.R.id.content), bottomAppBar, message);
	}

	@Override
	protected void onCleared() {
		if (currentCall != null && !currentCall.isCanceled()) {
			currentCall.cancel();
		}
		super.onCleared();
	}
}
