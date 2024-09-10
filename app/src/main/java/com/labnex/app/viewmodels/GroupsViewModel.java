package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.R;
import com.labnex.app.adapters.GroupsAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.ActivityGroupsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.groups.GroupsItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class GroupsViewModel extends ViewModel {

	private MutableLiveData<List<GroupsItem>> groupsList;

	public LiveData<List<GroupsItem>> getGroups(
			Context ctx,
			int resultLimit,
			int page,
			Activity activity,
			ActivityGroupsBinding binding) {

		groupsList = new MutableLiveData<>();
		loadGroupsList(ctx, resultLimit, page, activity, binding);

		return groupsList;
	}

	public void loadGroupsList(
			Context ctx,
			int resultLimit,
			int page,
			Activity activity,
			ActivityGroupsBinding binding) {

		Call<List<GroupsItem>> call =
				RetrofitClient.getApiInterface(ctx).getGroups(true, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<GroupsItem>> call,
							@NonNull Response<List<GroupsItem>> response) {

						if (response.isSuccessful()) {
							groupsList.postValue(response.body());
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
							@NonNull Call<List<GroupsItem>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								binding.getRoot().findViewById(R.id.bottom_app_bar),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMoreGroups(
			Context ctx,
			int resultLimit,
			int page,
			GroupsAdapter adapter,
			Activity activity,
			ActivityGroupsBinding binding) {

		Call<List<GroupsItem>> call =
				RetrofitClient.getApiInterface(ctx).getGroups(true, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<GroupsItem>> call,
							@NonNull Response<List<GroupsItem>> response) {

						if (response.isSuccessful()) {
							List<GroupsItem> list = groupsList.getValue();
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
									binding.getRoot().findViewById(R.id.bottom_app_bar),
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<GroupsItem>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								binding.getRoot().findViewById(R.id.bottom_app_bar),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
