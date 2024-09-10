package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.R;
import com.labnex.app.adapters.CommitDiffsAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetCommitDiffsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.commits.Diff;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CommitDiffsViewModel extends ViewModel {

	private MutableLiveData<List<Diff>> mutableList;

	public LiveData<List<Diff>> getCommitDiffs(
			Context ctx,
			String source,
			int id,
			String sha,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetCommitDiffsBinding binding) {

		mutableList = new MutableLiveData<>();
		loadInitialList(ctx, source, id, sha, resultLimit, page, activity, binding);

		return mutableList;
	}

	public void loadInitialList(
			Context ctx,
			String source,
			int id,
			String sha,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetCommitDiffsBinding binding) {

		Call<List<Diff>> call =
				RetrofitClient.getApiInterface(ctx).getCommitDiffs(id, sha, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Diff>> call,
							@NonNull Response<List<Diff>> response) {

						if (response.isSuccessful()) {
							mutableList.postValue(response.body());
						} else if (response.code() == 401) {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx, binding.getRoot(), ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.getRoot(),
									ctx.getString(R.string.access_forbidden_403));
						} else {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx, binding.getRoot(), ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Diff>> call, @NonNull Throwable t) {

						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								binding.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMore(
			Context ctx,
			String source,
			int id,
			String sha,
			int resultLimit,
			int page,
			CommitDiffsAdapter adapter,
			Activity activity,
			BottomSheetCommitDiffsBinding binding) {

		Call<List<Diff>> call =
				RetrofitClient.getApiInterface(ctx).getCommitDiffs(id, sha, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Diff>> call,
							@NonNull Response<List<Diff>> response) {

						if (response.isSuccessful()) {
							List<Diff> list = mutableList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
						} else if (response.code() == 401) {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx, binding.getRoot(), ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.getRoot(),
									ctx.getString(R.string.access_forbidden_403));
						} else {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx, binding.getRoot(), ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Diff>> call, @NonNull Throwable t) {

						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								binding.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
