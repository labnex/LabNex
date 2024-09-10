package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.labnex.app.R;
import com.labnex.app.adapters.ActivitiesAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.FragmentActivitiesBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.events.Events;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ActivitiesViewModel extends ViewModel {

	private MutableLiveData<List<Events>> events;

	public LiveData<List<Events>> getEvents(
			Context ctx,
			String source,
			int resultLimit,
			int page,
			FragmentActivitiesBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		events = new MutableLiveData<>();
		loadInitialList(ctx, source, resultLimit, page, binding, activity, bottomNavigationView);

		return events;
	}

	public void loadInitialList(
			Context ctx,
			String source,
			int resultLimit,
			int page,
			FragmentActivitiesBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<Events>> call = RetrofitClient.getApiInterface(ctx).getEvents(resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Events>> call,
							@NonNull Response<List<Events>> response) {

						if (response.isSuccessful()) {
							events.postValue(response.body());
						} else if (response.code() == 401) {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomNavigationView,
									ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomNavigationView,
									ctx.getString(R.string.access_forbidden_403));
						} else {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomNavigationView,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Events>> call, @NonNull Throwable t) {

						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMore(
			Context ctx,
			String source,
			int resultLimit,
			int page,
			ActivitiesAdapter adapter,
			FragmentActivitiesBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<Events>> call = RetrofitClient.getApiInterface(ctx).getEvents(resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Events>> call,
							@NonNull Response<List<Events>> response) {

						if (response.isSuccessful()) {

							List<Events> list = events.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
						} else {

							binding.progressBar.setVisibility(View.GONE);
							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomNavigationView,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Events>> call, @NonNull Throwable t) {

						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
