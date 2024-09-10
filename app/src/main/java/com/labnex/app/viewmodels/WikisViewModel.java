package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.R;
import com.labnex.app.adapters.WikisAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetProjectWikisBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.wikis.Wiki;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class WikisViewModel extends ViewModel {

	private MutableLiveData<List<Wiki>> wikisList;

	public LiveData<List<Wiki>> getWikis(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectWikisBinding binding) {

		wikisList = new MutableLiveData<>();
		loadList(ctx, id, resultLimit, page, activity, binding);

		return wikisList;
	}

	public void loadList(
			Context ctx,
			int id,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectWikisBinding binding) {

		Call<List<Wiki>> call =
				RetrofitClient.getApiInterface(ctx).getProjectWikis(id, 1, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Wiki>> call,
							@NonNull Response<List<Wiki>> response) {

						if (response.isSuccessful()) {
							wikisList.postValue(response.body());
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
					public void onFailure(@NonNull Call<List<Wiki>> call, @NonNull Throwable t) {
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
			WikisAdapter adapter,
			Activity activity,
			BottomSheetProjectWikisBinding binding) {

		Call<List<Wiki>> call =
				RetrofitClient.getApiInterface(ctx).getProjectWikis(id, 1, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Wiki>> call,
							@NonNull Response<List<Wiki>> response) {

						if (response.isSuccessful()) {

							List<Wiki> list = wikisList.getValue();
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
					public void onFailure(@NonNull Call<List<Wiki>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								binding.getRoot(),
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
