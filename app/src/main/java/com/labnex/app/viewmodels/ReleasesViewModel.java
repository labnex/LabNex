package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.R;
import com.labnex.app.adapters.ProjectReleasesAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetProjectReleasesBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.release.Releases;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ReleasesViewModel extends ViewModel {

	private MutableLiveData<List<Releases>> mainList;

	public LiveData<List<Releases>> getReleases(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectReleasesBinding binding) {

		mainList = new MutableLiveData<>();
		loadList(ctx, id, resultLimit, page, activity, binding);

		return mainList;
	}

	public void loadList(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectReleasesBinding binding) {

		Call<List<Releases>> call =
				RetrofitClient.getApiInterface(ctx).getProjectReleases(id, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Releases>> call,
							@NonNull Response<List<Releases>> response) {

						if (response.isSuccessful()) {
							mainList.postValue(response.body());
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
					public void onFailure(
							@NonNull Call<List<Releases>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								binding.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMore(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			ProjectReleasesAdapter adapter,
			Activity activity,
			BottomSheetProjectReleasesBinding binding) {

		Call<List<Releases>> call =
				RetrofitClient.getApiInterface(ctx).getProjectReleases(id, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Releases>> call,
							@NonNull Response<List<Releases>> response) {

						if (response.isSuccessful()) {

							List<Releases> list = mainList.getValue();
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
									binding.getRoot(),
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Releases>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								binding.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
