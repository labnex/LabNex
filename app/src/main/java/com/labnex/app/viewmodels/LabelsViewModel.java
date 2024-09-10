package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.R;
import com.labnex.app.adapters.LabelsAdapter;
import com.labnex.app.adapters.ProjectLabelsAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetGroupDetailBinding;
import com.labnex.app.databinding.BottomSheetProjectLabelsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.labels.Labels;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class LabelsViewModel extends ViewModel {

	private MutableLiveData<List<Labels>> listData;

	public LiveData<List<Labels>> getLabels(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetGroupDetailBinding binding) {

		listData = new MutableLiveData<>();
		loadInitialList(ctx, id, resultLimit, page, activity, binding);

		return listData;
	}

	public LiveData<List<Labels>> getProjectLabels(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectLabelsBinding binding) {

		listData = new MutableLiveData<>();
		loadProjectInitialList(ctx, id, resultLimit, page, activity, binding);

		return listData;
	}

	public void loadInitialList(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetGroupDetailBinding binding) {

		Call<List<Labels>> call =
				RetrofitClient.getApiInterface(ctx).getGroupLabels(id, true, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Labels>> call,
							@NonNull Response<List<Labels>> response) {

						if (response.isSuccessful()) {
							listData.postValue(response.body());
						} else if (response.code() == 401) {

							binding.bottomSheetGroupLabels.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupLabels.getRoot(),
									ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							binding.bottomSheetGroupLabels.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupLabels.getRoot(),
									ctx.getString(R.string.access_forbidden_403));
						} else {

							binding.bottomSheetGroupLabels.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupLabels.getRoot(),
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

						binding.bottomSheetGroupLabels.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								binding.bottomSheetGroupLabels.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadProjectInitialList(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectLabelsBinding binding) {

		Call<List<Labels>> call =
				RetrofitClient.getApiInterface(ctx).getProjectLabels(id, true, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Labels>> call,
							@NonNull Response<List<Labels>> response) {

						if (response.isSuccessful()) {
							listData.postValue(response.body());
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
					public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

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
			int id,
			int resultLimit,
			int page,
			LabelsAdapter adapter,
			Activity activity,
			BottomSheetGroupDetailBinding binding) {

		Call<List<Labels>> call =
				RetrofitClient.getApiInterface(ctx).getGroupLabels(id, true, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Labels>> call,
							@NonNull Response<List<Labels>> response) {

						if (response.isSuccessful()) {
							List<Labels> list = listData.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
						} else if (response.code() == 401) {

							binding.bottomSheetGroupLabels.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupLabels.getRoot(),
									ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							binding.bottomSheetGroupLabels.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupLabels.getRoot(),
									ctx.getString(R.string.access_forbidden_403));
						} else {

							binding.bottomSheetGroupLabels.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupLabels.getRoot(),
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

						binding.bottomSheetGroupLabels.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								binding.bottomSheetGroupLabels.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMoreProjectLabels(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			ProjectLabelsAdapter adapter,
			Activity activity,
			BottomSheetProjectLabelsBinding binding) {

		Call<List<Labels>> call =
				RetrofitClient.getApiInterface(ctx).getProjectLabels(id, true, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Labels>> call,
							@NonNull Response<List<Labels>> response) {

						if (response.isSuccessful()) {
							List<Labels> list = listData.getValue();
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
					public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								binding.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
