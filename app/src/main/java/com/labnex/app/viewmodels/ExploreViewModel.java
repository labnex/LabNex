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
import com.labnex.app.adapters.IssuesAdapter;
import com.labnex.app.adapters.MembersAdapter;
import com.labnex.app.adapters.MergeRequestsAdapter;
import com.labnex.app.adapters.ProjectsAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.FragmentExploreBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.models.user.User;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ExploreViewModel extends ViewModel {

	private MutableLiveData<List<Projects>> projects;
	private MutableLiveData<List<Issues>> issues;
	private MutableLiveData<List<MergeRequests>> mergeRequests;
	private MutableLiveData<List<User>> users;

	// search projects
	public LiveData<List<Projects>> searchProjects(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		projects = new MutableLiveData<>();
		loadProjects(
				ctx, scope, search, resultLimit, page, binding, activity, bottomNavigationView);

		return projects;
	}

	public void loadProjects(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<Projects>> call =
				RetrofitClient.getApiInterface(ctx).searchProjects(search, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Projects>> call,
							@NonNull Response<List<Projects>> response) {

						if (response.isSuccessful()) {
							projects.postValue(response.body());
						} else if (response.code() == 401) {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomNavigationView, ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomNavigationView,
									ctx.getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomNavigationView, ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Projects>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMoreProjects(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			ProjectsAdapter adapter,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<Projects>> call =
				RetrofitClient.getApiInterface(ctx).searchProjects(search, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Projects>> call,
							@NonNull Response<List<Projects>> response) {

						if (response.isSuccessful()) {

							List<Projects> list = projects.getValue();
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
									bottomNavigationView,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Projects>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	// search projects

	// search issues
	public LiveData<List<Issues>> searchIssues(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		issues = new MutableLiveData<>();
		loadIssues(ctx, scope, search, resultLimit, page, binding, activity, bottomNavigationView);

		return issues;
	}

	public void loadIssues(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<Issues>> call =
				RetrofitClient.getApiInterface(ctx).searchIssues(search, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Issues>> call,
							@NonNull Response<List<Issues>> response) {

						if (response.isSuccessful()) {
							issues.postValue(response.body());
						} else if (response.code() == 401) {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomNavigationView, ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomNavigationView,
									ctx.getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomNavigationView, ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMoreIssues(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			IssuesAdapter adapter,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<Issues>> call =
				RetrofitClient.getApiInterface(ctx).searchIssues(search, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Issues>> call,
							@NonNull Response<List<Issues>> response) {

						if (response.isSuccessful()) {

							List<Issues> list = issues.getValue();
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
									bottomNavigationView,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	// search issues

	// search mr
	public LiveData<List<MergeRequests>> searchMergeRequests(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		mergeRequests = new MutableLiveData<>();
		loadMergeRequests(
				ctx, scope, search, resultLimit, page, binding, activity, bottomNavigationView);

		return mergeRequests;
	}

	public void loadMergeRequests(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<MergeRequests>> call =
				RetrofitClient.getApiInterface(ctx).searchMergeRequests(search, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<MergeRequests>> call,
							@NonNull Response<List<MergeRequests>> response) {

						if (response.isSuccessful()) {
							mergeRequests.postValue(response.body());
						} else if (response.code() == 401) {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomNavigationView, ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomNavigationView,
									ctx.getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomNavigationView, ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<MergeRequests>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMoreMergeRequests(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			MergeRequestsAdapter adapter,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<MergeRequests>> call =
				RetrofitClient.getApiInterface(ctx).searchMergeRequests(search, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<MergeRequests>> call,
							@NonNull Response<List<MergeRequests>> response) {

						if (response.isSuccessful()) {

							List<MergeRequests> list = mergeRequests.getValue();
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
									bottomNavigationView,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<MergeRequests>> call, @NonNull Throwable t) {
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	// search mr

	// search users
	public LiveData<List<User>> searchUsers(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		users = new MutableLiveData<>();
		loadUsers(ctx, scope, search, resultLimit, page, binding, activity, bottomNavigationView);

		return users;
	}

	public void loadUsers(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx).searchUsers(search, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.code() == 200) {
							users.postValue(response.body());
						} else if (response.code() == 401) {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomNavigationView, ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									ctx,
									activity.findViewById(android.R.id.content),
									bottomNavigationView,
									ctx.getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									ctx, activity.findViewById(android.R.id.content),
									bottomNavigationView, ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	public void loadMoreUsers(
			Context ctx,
			String scope,
			String search,
			int resultLimit,
			int page,
			FragmentExploreBinding binding,
			MembersAdapter adapter,
			Activity activity,
			BottomNavigationView bottomNavigationView) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx).searchUsers(search, resultLimit, page);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.isSuccessful()) {

							List<User> list = users.getValue();
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
									bottomNavigationView,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								activity.findViewById(android.R.id.content),
								bottomNavigationView,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}
	// search users
}
