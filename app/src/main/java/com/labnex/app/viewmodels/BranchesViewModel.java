package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.R;
import com.labnex.app.adapters.BranchesAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetBranchesBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.branches.Branches;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class BranchesViewModel extends ViewModel {

	private MutableLiveData<List<Branches>> mutableList;

	public LiveData<List<Branches>> getBranches(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetBranchesBinding binding) {

		mutableList = new MutableLiveData<>();
		loadInitialList(ctx, id, resultLimit, page, activity, binding);

		return mutableList;
	}

	public void loadInitialList(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetBranchesBinding binding) {

		Call<List<Branches>> call =
				RetrofitClient.getApiInterface(ctx).getProjectBranches(id, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Branches>> call,
							@NonNull Response<List<Branches>> response) {

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
					public void onFailure(
							@NonNull Call<List<Branches>> call, @NonNull Throwable t) {

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
			BranchesAdapter adapter,
			Activity activity,
			BottomSheetBranchesBinding binding) {

		Call<List<Branches>> call =
				RetrofitClient.getApiInterface(ctx).getProjectBranches(id, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Branches>> call,
							@NonNull Response<List<Branches>> response) {

						if (response.isSuccessful()) {
							List<Branches> list = mutableList.getValue();
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
					public void onFailure(
							@NonNull Call<List<Branches>> call, @NonNull Throwable t) {

						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								binding.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
