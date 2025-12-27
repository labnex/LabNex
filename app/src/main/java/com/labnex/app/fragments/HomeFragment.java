package com.labnex.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
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
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.Projects;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.FragmentHomeBinding;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.broadcast_messages.Messages;
import com.labnex.app.models.user.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
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
	private int refreshSuccessCounter;
	private boolean isUserRefresh;

	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentHomeBinding.inflate(inflater, container, false);
		View root = binding.getRoot();
		ctx = requireContext();

		binding.progressBar.setVisibility(View.VISIBLE);
		binding.userAvatar.setEnabled(false);
		binding.sectionMostVisited.recyclerViewMostVisited.setEnabled(false);
		binding.sectionWork.groupsFrame.setEnabled(false);
		binding.sectionWork.projectsFrame.setEnabled(false);
		binding.sectionWork.starredFrame.setEnabled(false);
		binding.sectionWork.snippetFrame.setEnabled(false);
		binding.sectionWork.issuesFrame.setEnabled(false);
		binding.sectionWork.mergeRequestsFrame.setEnabled(false);
		binding.sectionAppSettings.notesFrame.setEnabled(false);
		binding.sectionAppSettings.settingsFrame.setEnabled(false);
		binding.settingsViewTop.setEnabled(false);
		binding.sectionMostVisited.clearMostVisited.setEnabled(false);
		binding.refreshHomeScreen.setEnabled(false);

		currentActiveAccountId =
				SharedPrefDB.getInstance(requireContext()).getInt("currentActiveAccountId");

		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
		UserAccount account =
				userAccountsApi != null
						? userAccountsApi.getAccountById(currentActiveAccountId)
						: null;

		notesApi = BaseApi.getInstance(ctx, NotesApi.class);
		projectsApi = BaseApi.getInstance(ctx, ProjectsApi.class);

		projectsList = new ArrayList<>();
		mostVisitedAdapter = new MostVisitedAdapter(ctx, projectsList);

		binding.sectionMostVisited.recyclerViewMostVisited.setHasFixedSize(true);
		binding.sectionMostVisited.recyclerViewMostVisited.setLayoutManager(
				new LinearLayoutManager(requireContext()));
		binding.sectionMostVisited.recyclerViewMostVisited.setAdapter(mostVisitedAdapter);

		getBroadcastMessage();
		getUserInfo();
		getMostVisitedProjects();

		binding.refreshHomeScreen.setOnClickListener(
				ref ->
						requireActivity()
								.runOnUiThread(
										() -> {
											refreshSuccessCounter = 0;
											isUserRefresh = true;
											binding.progressBar.setVisibility(View.VISIBLE);
											binding.userAvatar.setEnabled(false);
											binding.sectionMostVisited.recyclerViewMostVisited
													.setEnabled(false);
											binding.sectionWork.groupsFrame.setEnabled(false);
											binding.sectionWork.projectsFrame.setEnabled(false);
											binding.sectionWork.starredFrame.setEnabled(false);
											binding.sectionWork.snippetFrame.setEnabled(false);
											binding.sectionWork.issuesFrame.setEnabled(false);
											binding.sectionWork.mergeRequestsFrame.setEnabled(
													false);
											binding.sectionAppSettings.notesFrame.setEnabled(false);
											binding.sectionAppSettings.settingsFrame.setEnabled(
													false);
											binding.settingsViewTop.setEnabled(false);
											binding.sectionMostVisited.clearMostVisited.setEnabled(
													false);
											binding.refreshHomeScreen.setEnabled(false);
											projectsList.clear();
											mostVisitedAdapter.notifyDataChanged();
											getBroadcastMessage();
											getUserInfo();
											getMostVisitedProjects();
										}));

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

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();

		refreshMostVisitedProjects();
		updateNotesBadge();
		updateClearButtonVisibility();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void refreshMostVisitedProjects() {

		LiveData<List<Projects>> liveData =
				projectsApi.fetchMostVisitedWithLimit(currentActiveAccountId, 5);
		liveData.observe(
				getViewLifecycleOwner(),
				mostVisited -> {
					if (mostVisited != null) {
						projectsList.clear();
						if (!mostVisited.isEmpty()) {
							binding.sectionMostVisited.nothingFoundFrame.setVisibility(View.GONE);
							projectsList.addAll(mostVisited);
							mostVisitedAdapter.notifyDataChanged();
							binding.sectionMostVisited.clearMostVisited.setVisibility(View.VISIBLE);
						} else {
							binding.sectionMostVisited.nothingFoundFrame.setVisibility(
									View.VISIBLE);
							binding.sectionMostVisited.clearMostVisited.setVisibility(View.GONE);
						}
					}
				});
	}

	private void updateNotesBadge() {
		if (notesApi.getCount() > 0) {
			if (notesApi.getCount() > 9) {
				binding.sectionAppSettings.notesBadge.setPadding(16, 0, 16, 0);
			}
			binding.sectionAppSettings.notesBadge.setVisibility(View.VISIBLE);
			binding.sectionAppSettings.notesBadge.setText(String.valueOf(notesApi.getCount()));
		} else {
			binding.sectionAppSettings.notesBadge.setVisibility(View.GONE);
		}
	}

	private void updateClearButtonVisibility() {
		if (projectsApi.getCount() > 0) {
			binding.sectionMostVisited.clearMostVisited.setVisibility(View.VISIBLE);
		} else {
			binding.sectionMostVisited.clearMostVisited.setVisibility(View.GONE);
		}
	}

	private void getBroadcastMessage() {

		Call<List<Messages>> call = RetrofitClient.getApiInterface(ctx).getBroadcastMessage();

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Messages>> call,
							@NonNull retrofit2.Response<List<Messages>> response) {

						if (response.code() == 200 && isAdded() && ctx != null) {

							List<Messages> messages = response.body();
							binding.broadcastMessage.setVisibility(View.GONE);

							if (messages != null) {
								for (Messages message : messages) {
									if (message.isActive()) {
										binding.broadcastMessage.setVisibility(View.VISIBLE);
										binding.message.setText(message.getMessage());
										break;
									}
								}
							}
							refreshSuccessCounter++;
							checkRefreshComplete();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Messages>> call, @NonNull Throwable t) {
						refreshSuccessCounter++;
						checkRefreshComplete();
					}
				});
	}

	private void getMostVisitedProjects() {

		refreshSuccessCounter++;
		checkRefreshComplete();
	}

	private void getUserInfo() {

		Call<User> call = RetrofitClient.getApiInterface(ctx).getCurrentUser();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

						User userDetails = response.body();
						if (response.isSuccessful() && response.code() == 200) {

							assert userDetails != null;

							if (isAdded() && ctx != null) {
								((BaseActivity) requireActivity())
										.getAccount()
										.setUserInfo(userDetails);
								Glide.with(requireContext())
										.load(userDetails.getAvatarUrl())
										.diskCacheStrategy(DiskCacheStrategy.ALL)
										.placeholder(R.drawable.ic_spinner)
										.centerCrop()
										.into(binding.userAvatar);
								binding.userAvatar.setOnClickListener(
										profile -> {
											Intent intent = new Intent(ctx, ProfileActivity.class);
											intent.putExtra("source", "home");
											intent.putExtra("userId", userDetails.getId());
											ctx.startActivity(intent);
										});
								binding.userAvatar.setEnabled(true);
								updateWelcomeText(userDetails);
							}
						} else {
							Snackbar.info(
									requireActivity(),
									requireActivity().findViewById(R.id.nav_view),
									getString(R.string.generic_server_response_error));
						}
						refreshSuccessCounter++;
						checkRefreshComplete();
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						Snackbar.info(
								requireActivity(),
								requireActivity().findViewById(R.id.nav_view),
								getString(R.string.generic_server_response_error));
						refreshSuccessCounter++;
						checkRefreshComplete();
					}
				});
	}

	private void updateWelcomeText(User user) {
		if (user == null) return;

		String displayName = getDisplayName(user);
		binding.hiText.setText(String.format(getString(R.string.hi_username), displayName));

		updateTextViewWithData(binding.jobTitleText, user.getJobTitle());

		String locationOrg = buildLocationOrganizationString(user);
		updateTextViewWithData(binding.locationOrgText, locationOrg);

		String additionalInfo = getAdditionalInfoString(user);
		updateTextViewWithData(binding.userAdditionalInfo, additionalInfo);
	}

	private String getDisplayName(User user) {
		if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
			return user.getFullName();
		} else if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
			return user.getUsername();
		}
		return getString(R.string.there);
	}

	private String buildLocationOrganizationString(User user) {
		StringBuilder sb = new StringBuilder();

		boolean hasLocation = user.getLocation() != null && !user.getLocation().trim().isEmpty();
		boolean hasOrganization =
				user.getOrganization() != null && !user.getOrganization().trim().isEmpty();

		if (hasLocation) {
			sb.append(user.getLocation());
		}

		if (hasLocation && hasOrganization) {
			sb.append(" ").append(getString(R.string.separator_dot)).append(" ");
		}

		if (hasOrganization) {
			sb.append(user.getOrganization());
		}

		return sb.toString();
	}

	private void updateTextViewWithData(TextView textView, String data) {
		if (data != null && !data.trim().isEmpty()) {
			textView.setText(data);
			textView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
		}
	}

	private String getAdditionalInfoString(User user) {
		List<String> infoItems = new ArrayList<>();

		String lastActive = getLastActiveText(user.getLastActivityOn());
		if (!lastActive.isEmpty()) {
			infoItems.add(lastActive);
		}

		String memberSince = getMemberSinceText(user.getCreatedAt());
		if (!memberSince.isEmpty() && infoItems.size() < 2) {
			infoItems.add(memberSince);
		}

		if (user.getPlan() != null && !user.getPlan().isEmpty() && infoItems.size() < 2) {
			infoItems.add(user.getPlan() + " " + getString(R.string.plan));
		}

		String separator = " " + getString(R.string.separator_dot) + " ";
		return TextUtils.join(separator, infoItems);
	}

	private String getLastActiveText(String lastActivityDate) {
		if (TextUtils.isEmpty(lastActivityDate)) {
			return "";
		}

		String datePart = lastActivityDate.split("T")[0];

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			Date date = sdf.parse(datePart);

			if (date == null) {
				return "";
			}

			Calendar activityCal = Calendar.getInstance();
			activityCal.setTime(date);
			Calendar todayCal = Calendar.getInstance();

			long diffMillis = todayCal.getTimeInMillis() - activityCal.getTimeInMillis();
			long days = TimeUnit.MILLISECONDS.toDays(diffMillis);

			Resources res = getResources();

			if (days == 0) return res.getString(R.string.active_today);
			if (days == 1) return res.getString(R.string.active_yesterday);
			if (days < 7)
				return res.getQuantityString(R.plurals.active_days_ago, (int) days, (int) days);
			if (days < 30) {
				int weeks = (int) (days / 7);
				return res.getQuantityString(R.plurals.active_weeks_ago, weeks, weeks);
			}

			int months = (int) (days / 30);
			return res.getQuantityString(R.plurals.active_months_ago, months, months);

		} catch (Exception e) {
			return "";
		}
	}

	private String getMemberSinceText(String createdAt) {
		if (TextUtils.isEmpty(createdAt)) {
			return "";
		}

		try {
			String datePart = createdAt.split("T")[0];
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			Date date = sdf.parse(datePart);

			if (date == null) {
				return "";
			}

			SimpleDateFormat outputFormat = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
			return String.format(getString(R.string.member_since), outputFormat.format(date));

		} catch (Exception e) {
			return "";
		}
	}

	private void checkRefreshComplete() {
		if (refreshSuccessCounter == 3 && isAdded()) {
			binding.progressBar.setVisibility(View.GONE);
			binding.userAvatar.setEnabled(true);
			binding.sectionMostVisited.recyclerViewMostVisited.setEnabled(true);
			binding.sectionWork.groupsFrame.setEnabled(true);
			binding.sectionWork.projectsFrame.setEnabled(true);
			binding.sectionWork.starredFrame.setEnabled(true);
			binding.sectionWork.snippetFrame.setEnabled(true);
			binding.sectionWork.issuesFrame.setEnabled(true);
			binding.sectionWork.mergeRequestsFrame.setEnabled(true);
			binding.sectionAppSettings.notesFrame.setEnabled(true);
			binding.sectionAppSettings.settingsFrame.setEnabled(true);
			binding.settingsViewTop.setEnabled(true);
			binding.sectionMostVisited.clearMostVisited.setEnabled(true);
			binding.refreshHomeScreen.setEnabled(true);

			refreshMostVisitedProjects();

			if (isUserRefresh) {
				Snackbar.info(
						requireActivity(),
						requireActivity().findViewById(R.id.nav_view),
						getString(R.string.refreshed));
				isUserRefresh = false;
			}
			refreshSuccessCounter = 0;
		}
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

								binding.sectionMostVisited.nothingFoundFrame.setVisibility(
										View.VISIBLE);

								binding.sectionMostVisited.clearMostVisited.setVisibility(
										View.GONE);

								dialog.dismiss();
							})
					.setNeutralButton(R.string.cancel, null)
					.show();
		}
	}
}
