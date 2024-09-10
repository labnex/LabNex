package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.labnex.app.R;
import com.labnex.app.adapters.ProjectsAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.projects.Projects;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ProjectsViewModel extends ViewModel {

	private MutableLiveData<List<Projects>> projectsList;

	public LiveData<List<Projects>> getProjects(
			Context ctx,
			String source,
			String mode,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomAppBar bottomAppBar) {

		projectsList = new MutableLiveData<>();
		loadProjectsList(ctx, source, mode, id, resultLimit, page, activity, bottomAppBar);

		return projectsList;
	}

	public void loadProjectsList(
			Context ctx,
			String source,
			String mode,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<Projects>> call;

		if (source.equalsIgnoreCase("group") && mode.equalsIgnoreCase("single")) {
			call = RetrofitClient.getApiInterface(ctx).getGroupProjects(id, resultLimit, page);
		} else if (source.equalsIgnoreCase("starred")) {
			call = RetrofitClient.getApiInterface(ctx).getStarredProjects(id, resultLimit, page);
		} else {
			call = RetrofitClient.getApiInterface(ctx).getProjects(id, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Projects>> call,
							@NonNull Response<List<Projects>> response) {

						if (response.isSuccessful()) {
							projectsList.postValue(response.body());
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
					public void onFailure(
							@NonNull Call<List<Projects>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMoreProjects(
			Context ctx,
			String source,
			String mode,
			int id,
			int resultLimit,
			int page,
			ProjectsAdapter adapter,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<Projects>> call;

		if (source.equalsIgnoreCase("starred")) {
			call = RetrofitClient.getApiInterface(ctx).getStarredProjects(id, resultLimit, page);
		} else {
			call = RetrofitClient.getApiInterface(ctx).getProjects(id, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Projects>> call,
							@NonNull Response<List<Projects>> response) {

						if (response.isSuccessful()) {

							List<Projects> list = projectsList.getValue();
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
					public void onFailure(
							@NonNull Call<List<Projects>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
