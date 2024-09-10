package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.R;
import com.labnex.app.adapters.MembersAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetGroupDetailBinding;
import com.labnex.app.databinding.BottomSheetProjectMembersBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.user.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MembersViewModel extends ViewModel {

	private MutableLiveData<List<User>> listData;

	public LiveData<List<User>> getMembers(
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

	public LiveData<List<User>> getProjectMembers(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectMembersBinding binding) {

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

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx).getGroupMembers(id, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.code() == 200) {
							listData.postValue(response.body());
						} else if (response.code() == 401) {

							binding.bottomSheetGroupMembers.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupMembers.getRoot(),
									ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							binding.bottomSheetGroupMembers.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupMembers.getRoot(),
									ctx.getString(R.string.access_forbidden_403));
						} else {

							binding.bottomSheetGroupMembers.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupMembers.getRoot(),
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						binding.bottomSheetGroupMembers.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								binding.bottomSheetGroupMembers.getRoot(),
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
			BottomSheetProjectMembersBinding binding) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx).getProjectMembers(id, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.code() == 200) {
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
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

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
			MembersAdapter adapter,
			Activity activity,
			BottomSheetGroupDetailBinding binding) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx).getGroupMembers(id, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.code() == 200) {
							List<User> list = listData.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
						} else if (response.code() == 401) {

							binding.bottomSheetGroupMembers.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupMembers.getRoot(),
									ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							binding.bottomSheetGroupMembers.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupMembers.getRoot(),
									ctx.getString(R.string.access_forbidden_403));
						} else {

							binding.bottomSheetGroupMembers.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									binding.bottomSheetGroupMembers.getRoot(),
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						binding.bottomSheetGroupMembers.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								binding.bottomSheetGroupMembers.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMoreProjectMembers(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			MembersAdapter adapter,
			Activity activity,
			BottomSheetProjectMembersBinding binding) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx).getProjectMembers(id, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.code() == 200) {
							List<User> list = listData.getValue();
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
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								binding.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
