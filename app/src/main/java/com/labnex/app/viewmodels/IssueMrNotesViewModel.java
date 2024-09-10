package com.labnex.app.viewmodels;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.labnex.app.R;
import com.labnex.app.adapters.IssueNotesAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.notes.Notes;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssueMrNotesViewModel extends ViewModel {

	private MutableLiveData<List<Notes>> mutableList;

	public LiveData<List<Notes>> getNotes(
			Context ctx,
			int id,
			int iid,
			String type,
			int resultLimit,
			int page,
			Activity activity,
			BottomAppBar bottomAppBar) {

		mutableList = new MutableLiveData<>();
		loadInitialList(ctx, id, iid, type, resultLimit, page, activity, bottomAppBar);

		return mutableList;
	}

	public void loadInitialList(
			Context ctx,
			int id,
			int iid,
			String type,
			int resultLimit,
			int page,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<Notes>> call;
		if (type.equalsIgnoreCase("issue")) {
			call = RetrofitClient.getApiInterface(ctx).getIssueNotes(id, iid, resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getMergeRequestNotes(id, iid, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Notes>> call,
							@NonNull Response<List<Notes>> response) {

						if (response.code() == 200) {
							mutableList.postValue(response.body());
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
					public void onFailure(@NonNull Call<List<Notes>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMore(
			Context ctx,
			int id,
			int iid,
			String type,
			int resultLimit,
			int page,
			IssueNotesAdapter adapter,
			Activity activity,
			BottomAppBar bottomAppBar) {

		Call<List<Notes>> call;
		if (type.equalsIgnoreCase("issue")) {
			call = RetrofitClient.getApiInterface(ctx).getIssueNotes(id, iid, resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getMergeRequestNotes(id, iid, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Notes>> call,
							@NonNull Response<List<Notes>> response) {

						if (response.isSuccessful()) {
							mutableList.postValue(response.body());
						} else {
							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomAppBar,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Notes>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
}
