package com.labnex.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.activities.*;
import com.labnex.app.databinding.FragmentHomeBinding;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.user.User;
import com.labnex.app.viewmodels.HomeViewModel;
import java.util.Calendar;

/**
 * @author mmarif
 */
public class HomeFragment extends Fragment {

	private FragmentHomeBinding binding;
	private Context ctx;
	private HomeViewModel viewModel;
	private boolean isFirstLoad = true;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.nestedScrollView, binding.pullToRefresh, null);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		ctx = requireContext();

		int currentActiveAccountId = SharedPrefDB.getInstance(ctx).getInt("currentActiveAccountId");

		viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

		setupClickListeners();
		setupSwipeRefresh();
		observeViewModel();

		viewModel.loadMvp(ctx, currentActiveAccountId);

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad) {
			lazyLoad();
		}
	}

	private void lazyLoad() {
		isFirstLoad = false;
		if (Boolean.FALSE.equals(viewModel.getHasLoadedOnce().getValue())) {
			viewModel.loadAll(ctx, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					viewModel.loadAll(ctx, true);
				});
	}

	private void setupClickListeners() {
		binding.sectionProjects.groupsFrame.setOnClickListener(
				v -> startActivity(new Intent(ctx, GroupsActivity.class)));

		binding.sectionProjects.projectsFrame.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, ProjectsActivity.class);
					intent.putExtra("source", "projects");
					startActivity(intent);
				});

		binding.sectionProjects.starredFrame.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, ProjectsActivity.class);
					intent.putExtra("source", "starred");
					startActivity(intent);
				});

		binding.sectionActivity.activitiesFrame.setOnClickListener(
				v -> {
					if (requireActivity() instanceof MainActivity ma) {
						ma.switchTab(ma.activitiesFrag, R.id.btn_nav_activities);
					}
				});

		binding.sectionActivity.issuesFrame.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, IssuesActivity.class);
					intent.putExtra("source", "my_issues");
					intent.putExtra("id", 0);
					startActivity(intent);
				});

		binding.sectionActivity.mergeRequestsFrame.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, MergeRequestsActivity.class);
					intent.putExtra("source", "my_merge_requests");
					intent.putExtra("id", 0);
					startActivity(intent);
				});

		binding.sectionActivity.snippetFrame.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, SnippetsActivity.class);
					intent.putExtra("source", "snippets");
					startActivity(intent);
				});

		binding.sectionPreferences.mostVisited.setOnClickListener(
				v -> startActivity(new Intent(ctx, MostVisitedProjectsActivity.class)));

		binding.sectionPreferences.notesFrame.setOnClickListener(
				v -> startActivity(new Intent(ctx, NotesActivity.class)));

		binding.sectionPreferences.settingsFrame.setOnClickListener(
				v -> startActivity(new Intent(ctx, AppSettingsActivity.class)));
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (loading) {
								binding.progressBar.setVisibility(View.VISIBLE);
								binding.pullToRefresh.setRefreshing(false);
							} else {
								binding.progressBar.setVisibility(View.GONE);
								binding.nestedScrollView.setVisibility(View.VISIBLE);
								binding.pullToRefresh.setRefreshing(false);
							}
						});

		viewModel
				.getProjectsCount()
				.observe(
						getViewLifecycleOwner(),
						count -> {
							if (count != null && count >= 0) {
								binding.sectionProjects.projectsCount.setText(
										Utils.numberFormatter(count));
							}
						});

		viewModel
				.getGroupsCount()
				.observe(
						getViewLifecycleOwner(),
						count -> {
							if (count != null && count >= 0) {
								binding.sectionProjects.groupsCount.setText(
										Utils.numberFormatter(count));
							}
						});

		viewModel
				.getStarredCount()
				.observe(
						getViewLifecycleOwner(),
						count -> {
							if (count != null && count >= 0) {
								binding.sectionProjects.starredCount.setText(
										Utils.numberFormatter(count));
							}
						});

		viewModel
				.getIssuesCount()
				.observe(
						getViewLifecycleOwner(),
						count -> {
							if (count != null && count >= 0) {
								binding.sectionActivity.issuesChip.setText(
										Utils.numberFormatter(count));
							}
						});

		viewModel
				.getMergeRequestsCount()
				.observe(
						getViewLifecycleOwner(),
						count -> {
							if (count != null && count >= 0) {
								binding.sectionActivity.mrChip.setText(
										Utils.numberFormatter(count));
							}
						});

		viewModel
				.getSnippetsCount()
				.observe(
						getViewLifecycleOwner(),
						count -> {
							if (count != null && count >= 0) {
								binding.sectionActivity.snippetChip.setText(
										Utils.numberFormatter(count));
							}
						});

		viewModel
				.getMvpCount()
				.observe(
						getViewLifecycleOwner(),
						count -> {
							if (count != null && count >= 0) {
								binding.sectionPreferences.mvpCount.setText(
										count > 0 ? Utils.numberFormatter(count) : "0");
							}
						});

		viewModel
				.getUserInfo()
				.observe(
						getViewLifecycleOwner(),
						user -> {
							if (user == null) return;

							if (requireActivity() instanceof BaseActivity) {
								((BaseActivity) requireActivity()).getAccount().setUserInfo(user);
							}

							Glide.with(ctx)
									.load(user.getAvatarUrl())
									.diskCacheStrategy(DiskCacheStrategy.ALL)
									.placeholder(R.drawable.ic_spinner)
									.centerCrop()
									.into(binding.sectionHeader.profilePictureLayout);

							binding.sectionHeader.profilePictureLayout.setOnClickListener(
									profile -> {
										Intent intent = new Intent(ctx, ProfileActivity.class);
										intent.putExtra("source", "home");
										intent.putExtra("userId", user.getId());
										startActivity(intent);
									});
							binding.sectionHeader.profilePictureLayout.setEnabled(true);

							Calendar c = Calendar.getInstance();
							int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
							int greetingRes =
									(timeOfDay < 12)
											? R.string.good_morning
											: (timeOfDay < 16)
													? R.string.good_afternoon
													: R.string.good_evening;
							String displayName = getDisplayName(user);
							String greetings = getString(greetingRes) + " " + displayName;
							binding.sectionHeader.hiText.setText(greetings);

							binding.sectionHeader.usernameText.setText(
									user.getUsername() != null ? "@" + user.getUsername() : "");

							binding.sectionHeader.chipFollowers.setText(
									getString(
											R.string.user_followers,
											Utils.numberFormatter(user.getFollowers())));
							binding.sectionHeader.chipFollowing.setText(
									getString(
											R.string.user_following,
											Utils.numberFormatter(user.getFollowing())));
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg == null) return;
							if (!errorMsg.isEmpty()) {
								Toasty.show(ctx, errorMsg);
							}
						});
	}

	private String getDisplayName(User user) {
		if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
			return user.getFullName();
		} else if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
			return user.getUsername();
		}
		return getString(R.string.there);
	}
}
