package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.R;
import com.labnex.app.adapters.ProjectTagsAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetProjectTagsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.tags.TagsItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class TagsViewModel extends ViewModel {

	private MutableLiveData<List<TagsItem>> tagsList;

	public LiveData<List<TagsItem>> getProjectTags(
			Context context,
			int projectId,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectTagsBinding binding) {
		tagsList = new MutableLiveData<>();
		loadInitialTags(context, projectId, resultLimit, page, activity, binding);
		return tagsList;
	}

	private void loadInitialTags(
			Context context,
			int projectId,
			int resultLimit,
			int page,
			Activity activity,
			BottomSheetProjectTagsBinding binding) {
		Call<List<TagsItem>> call =
				RetrofitClient.getApiInterface(context)
						.getProjectTags(projectId, resultLimit, page);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<TagsItem>> call,
							@NonNull Response<List<TagsItem>> response) {
						if (response.isSuccessful()) {
							tagsList.postValue(response.body());
						} else {
							handleError(context, response.code(), binding);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<TagsItem>> call, @NonNull Throwable t) {
						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								context,
								binding.getRoot(),
								context.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMoreTags(
			Context context,
			int projectId,
			int resultLimit,
			int page,
			ProjectTagsAdapter adapter,
			Activity activity,
			BottomSheetProjectTagsBinding binding) {
		Call<List<TagsItem>> call =
				RetrofitClient.getApiInterface(context)
						.getProjectTags(projectId, resultLimit, page);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<TagsItem>> call,
							@NonNull Response<List<TagsItem>> response) {
						if (response.isSuccessful() && response.body() != null) {
							List<TagsItem> currentList = tagsList.getValue();
							if (currentList != null) {
								if (!response.body().isEmpty()) {
									currentList.addAll(response.body());
									adapter.updateList(currentList);
								} else {
									adapter.setMoreDataAvailable(false);
								}
							}
						} else {
							handleError(context, response.code(), binding);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<TagsItem>> call, @NonNull Throwable t) {
						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								context,
								binding.getRoot(),
								context.getString(R.string.generic_server_response_error));
					}
				});
	}

	private void handleError(Context context, int code, BottomSheetProjectTagsBinding binding) {
		binding.progressBar.setVisibility(View.GONE);
		int message =
				switch (code) {
					case 401 -> R.string.not_authorized;
					case 403 -> R.string.access_forbidden_403;
					default -> R.string.generic_error;
				};
		Snackbar.info(context, binding.getRoot(), context.getString(message));
	}
}
