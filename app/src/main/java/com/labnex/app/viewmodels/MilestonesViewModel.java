package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.R;
import com.labnex.app.adapters.ProjectMilestonesAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetProjectMilestonesBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.milestone.Milestones;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MilestonesViewModel extends ViewModel {

	private MutableLiveData<List<Milestones>> mainList;

	public LiveData<List<Milestones>> getMilestones(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectMilestonesBinding binding) {

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
			BottomSheetProjectMilestonesBinding binding) {

		Call<List<Milestones>> call =
				RetrofitClient.getApiInterface(ctx)
						.getProjectMilestones(id, "active", resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Milestones>> call,
							@NonNull Response<List<Milestones>> response) {

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
							@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {
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
			ProjectMilestonesAdapter adapter,
			Activity activity,
			BottomSheetProjectMilestonesBinding binding) {

		Call<List<Milestones>> call =
				RetrofitClient.getApiInterface(ctx)
						.getProjectMilestones(id, "active", resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Milestones>> call,
							@NonNull Response<List<Milestones>> response) {

						if (response.isSuccessful()) {

							List<Milestones> list = mainList.getValue();
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
							@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								binding.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
