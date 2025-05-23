package com.labnex.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.AppSettingsActivity;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.activities.GroupsActivity;
import com.labnex.app.activities.IssuesActivity;
import com.labnex.app.activities.MergeRequestsActivity;
import com.labnex.app.activities.NotesActivity;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.activities.ProjectsActivity;
import com.labnex.app.activities.SnippetsActivity;
import com.labnex.app.adapters.MostVisitedAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.NotesApi;
import com.labnex.app.database.api.ProjectsApi;
import com.labnex.app.database.models.Projects;
import com.labnex.app.databinding.FragmentHomeBinding;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.broadcast_messages.Messages;
import com.labnex.app.models.user.User;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class HomeFragment extends Fragment {

	private FragmentHomeBinding binding;
	private Context ctx;
	private int currentActiveAccountId;
	private NotesApi notesApi;
	private ProjectsApi projectsApi;
	private List<Projects> projectsList;
	private MostVisitedAdapter mostVisitedAdapter;

	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentHomeBinding.inflate(inflater, container, false);
		View root = binding.getRoot();
		ctx = requireContext();

		currentActiveAccountId =
				SharedPrefDB.getInstance(requireContext()).getInt("currentActiveAccountId");

		notesApi = BaseApi.getInstance(ctx, NotesApi.class);
		projectsApi = BaseApi.getInstance(ctx, ProjectsApi.class);

		projectsList = new ArrayList<>();
		mostVisitedAdapter = new MostVisitedAdapter(ctx, projectsList);

		binding.sectionMostVisited.recyclerViewMostVisited.setHasFixedSize(true);
		binding.sectionMostVisited.recyclerViewMostVisited.setLayoutManager(
				new LinearLayoutManager(requireContext()));

		// Snackbar.info(ctx, requireActivity().findViewById(android.R.id.content),
		// requireActivity().findViewById(R.id.nav_view), "Hello world");

		binding.sectionWork.groupsFrame.setOnClickListener(
				view -> startActivity(new Intent(ctx, GroupsActivity.class)));

		binding.sectionWork.projectsFrame.setOnClickListener(
				view -> {
					Intent intent = new Intent(requireContext(), ProjectsActivity.class);
					intent.putExtra("source", "projects");
					requireContext().startActivity(intent);
				});

		binding.sectionWork.starredFrame.setOnClickListener(
				view -> {
					Intent intent = new Intent(requireContext(), ProjectsActivity.class);
					intent.putExtra("source", "starred");
					requireContext().startActivity(intent);
				});

		binding.sectionWork.snippetFrame.setOnClickListener(
				view -> {
					Intent intent = new Intent(requireContext(), SnippetsActivity.class);
					intent.putExtra("source", "snippets");
					requireContext().startActivity(intent);
				});

		binding.settingsViewTop.setOnClickListener(
				view -> startActivity(new Intent(ctx, AppSettingsActivity.class)));
		binding.sectionAppSettings.settingsFrame.setOnClickListener(
				view -> startActivity(new Intent(ctx, AppSettingsActivity.class)));

		binding.sectionAppSettings.notesFrame.setOnClickListener(
				view -> startActivity(new Intent(ctx, NotesActivity.class)));

		binding.sectionWork.issuesFrame.setOnClickListener(
				view -> {
					Intent intent = new Intent(ctx, IssuesActivity.class);
					intent.putExtra("source", "my_issues");
					intent.putExtra("id", 0);
					ctx.startActivity(intent);
				});

		binding.sectionWork.mergeRequestsFrame.setOnClickListener(
				view -> {
					Intent intent = new Intent(ctx, MergeRequestsActivity.class);
					intent.putExtra("source", "my_merge_requests");
					intent.putExtra("id", 0);
					ctx.startActivity(intent);
				});

		binding.sectionMostVisited.clearMostVisited.setOnClickListener(view -> clearMostVisited());

		getBroadcastMessage();
		getUserInfo();
		getMostVisitedProjects();

		binding.userAvatar.setOnLongClickListener(
				ref -> {
					requireActivity()
							.runOnUiThread(
									() -> {
										getBroadcastMessage();
										getUserInfo();
										getMostVisitedProjects();
										Snackbar.info(
												requireActivity(),
												requireActivity().findViewById(R.id.nav_view),
												getString(R.string.refreshed));
									});
					return true;
				});

		return root;
	}

	@Override
	public void onResume() {

		if (notesApi.getCount() > 0) {

			if (notesApi.getCount() > 9) {
				binding.sectionAppSettings.notesBadge.setPadding(16, 0, 16, 0);
			}
			binding.sectionAppSettings.notesBadge.setVisibility(View.VISIBLE);
			binding.sectionAppSettings.notesBadge.setText(
					String.format(notesApi.getCount().toString()));
		} else {
			binding.sectionAppSettings.notesBadge.setVisibility(View.GONE);
		}

		if (projectsApi.getCount() > 0) {
			binding.sectionMostVisited.clearMostVisited.setVisibility(View.VISIBLE);
		}

		super.onResume();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void getBroadcastMessage() {

		Call<Messages> call = RetrofitClient.getApiInterface(ctx).getBroadcastMessage();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Messages> call,
							@NonNull retrofit2.Response<Messages> response) {

						Messages messageDetails = response.body();

						if (response.code() == 200) {

							assert messageDetails != null;

							if (isAdded() && ctx != null) {

								if (messageDetails.isActive()) {
									binding.broadcastMessage.setVisibility(View.VISIBLE);
									binding.message.setText(messageDetails.getMessage());
								} else {
									binding.broadcastMessage.setVisibility(View.GONE);
								}
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<Messages> call, @NonNull Throwable t) {}
				});
	}

	private void getMostVisitedProjects() {

		projectsApi
				.fetchMostVisitedWithLimit(currentActiveAccountId, 5)
				.observe(
						requireActivity(),
						mostVisited -> {
							assert mostVisited != null;
							if (!mostVisited.isEmpty()) {

								projectsList.clear();
								binding.sectionMostVisited.nothingFoundFrame.setVisibility(
										View.GONE);
								projectsList.addAll(mostVisited);
								mostVisitedAdapter.notifyDataChanged();
								binding.sectionMostVisited.recyclerViewMostVisited.setAdapter(
										mostVisitedAdapter);
							} else {

								binding.sectionMostVisited.nothingFoundFrame.setVisibility(
										View.VISIBLE);
							}
						});
	}

	private void getUserInfo() {

		Call<User> call = RetrofitClient.getApiInterface(ctx).getCurrentUser();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

						User userDetails = response.body();

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								assert userDetails != null;

								if (isAdded() && ctx != null) {
									((BaseActivity) requireActivity())
											.getAccount()
											.setUserInfo(userDetails);

									binding.welcomeTextHi.setText(
											String.format(
													getString(R.string.hi_username),
													userDetails.getFullName()));

									Glide.with(requireContext())
											.load(userDetails.getAvatarUrl())
											.diskCacheStrategy(DiskCacheStrategy.ALL)
											.placeholder(R.drawable.ic_spinner)
											.centerCrop()
											.into(binding.userAvatar);

									binding.userAvatar.setOnClickListener(
											profile -> {
												Intent intent =
														new Intent(ctx, ProfileActivity.class);
												intent.putExtra("source", "home");
												intent.putExtra("userId", userDetails.getId());
												ctx.startActivity(intent);
											});
								}
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						Snackbar.info(
								requireActivity(),
								requireActivity().findViewById(R.id.nav_view),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void clearMostVisited() {

		if (!projectsList.isEmpty()) {
			new MaterialAlertDialogBuilder(ctx)
					.setMessage(R.string.delete_all_most_visited_dialog_message)
					.setPositiveButton(
							R.string.clear,
							(dialog, which) -> {
								projectsApi.deleteAllProjects();
								projectsList.clear();
								mostVisitedAdapter.notifyDataChanged();
								binding.sectionMostVisited.clearMostVisited.setVisibility(
										View.GONE);
								dialog.dismiss();
							})
					.setNeutralButton(R.string.cancel, null)
					.show();
		}
	}
}
