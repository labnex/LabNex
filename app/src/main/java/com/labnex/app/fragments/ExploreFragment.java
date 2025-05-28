package com.labnex.app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.activities.MainActivity;
import com.labnex.app.adapters.IssuesAdapter;
import com.labnex.app.adapters.MembersAdapter;
import com.labnex.app.adapters.MergeRequestsAdapter;
import com.labnex.app.adapters.ProjectsAdapter;
import com.labnex.app.databinding.FragmentExploreBinding;
import com.labnex.app.viewmodels.ExploreViewModel;
import java.util.Objects;

/**
 * @author mmarif
 */
public class ExploreFragment extends Fragment {

	private Context ctx;
	private FragmentExploreBinding binding;
	private ExploreViewModel exploreViewModel;
	private ProjectsAdapter projectsAdapter;
	private IssuesAdapter issuesAdapter;
	private MergeRequestsAdapter mergeRequestsAdapter;
	private MembersAdapter membersAdapter;
	private int page = 1;
	private int resultLimit;
	private String scope = "projects";
	private int scopeSelection = 0;
	private BottomNavigationView bottomNavigationView;

	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		ctx = requireContext();
		exploreViewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
		binding = FragmentExploreBinding.inflate(inflater, container, false);

		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();
		bottomNavigationView = ((MainActivity) requireContext()).findViewById(R.id.nav_view);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		MaterialAlertDialogBuilder materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						ctx,
						com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert);
		CharSequence[] scopes = {"Projects", "Issues", "Merge requests", "Users"};

		binding.scope.setOnClickListener(
				filter -> {
					materialAlertDialogBuilder.setSingleChoiceItems(
							scopes,
							scopeSelection,
							(dialog, which) -> {
								page = 1;
								if (scopes[which] == "Projects") {
									scope = "projects";
									scopeSelection = 0;
									dialog.dismiss();
								} else if (scopes[which] == "Issues") {
									scope = "issues";
									scopeSelection = 1;
									dialog.dismiss();
								} else if (scopes[which] == "Merge requests") {
									scope = "merge_requests";
									scopeSelection = 2;
									dialog.dismiss();
								} else if (scopes[which] == "Users") {
									scope = "users";
									scopeSelection = 3;
									dialog.dismiss();
								}
							});
					materialAlertDialogBuilder.create().show();
				});

		binding.exploreLayout.setEndIconOnClickListener(
				v -> {
					page = 1;
					String query = Objects.requireNonNull(binding.search.getText()).toString();
					if (scope.equalsIgnoreCase("projects")) {
						searchProjects(query);
					} else if (scope.equalsIgnoreCase("issues")) {
						searchIssues(query);
					} else if (scope.equalsIgnoreCase("merge_requests")) {
						searchMergeRequests(query);
					} else if (scope.equalsIgnoreCase("users")) {
						searchUsers(query);
					}
				});

		binding.search.setOnEditorActionListener(
				(v, actionId, event) -> {
					if (scope.equalsIgnoreCase("projects")) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							searchProjects(
									Objects.requireNonNull(binding.search.getText()).toString());
						}
					} else if (scope.equalsIgnoreCase("issues")) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							searchIssues(
									Objects.requireNonNull(binding.search.getText()).toString());
						}
					} else if (scope.equalsIgnoreCase("merge_requests")) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							searchMergeRequests(
									Objects.requireNonNull(binding.search.getText()).toString());
						}
					} else if (scope.equalsIgnoreCase("users")) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							searchUsers(
									Objects.requireNonNull(binding.search.getText()).toString());
						}
					}

					return false;
				});

		return binding.getRoot();
	}

	private void searchProjects(String search) {

		binding.progressBar.setVisibility(View.VISIBLE);
		clearAdapters();

		exploreViewModel
				.searchProjects(
						ctx,
						"projects",
						search,
						resultLimit,
						page,
						binding,
						requireActivity(),
						bottomNavigationView)
				.observe(
						requireActivity(),
						mainList -> {
							projectsAdapter = new ProjectsAdapter(ctx, mainList, "search");
							projectsAdapter.setLoadMoreListener(
									new ProjectsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											exploreViewModel.loadMoreProjects(
													ctx,
													"projects",
													search,
													resultLimit,
													page,
													binding,
													projectsAdapter,
													requireActivity(),
													bottomNavigationView);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (projectsAdapter.getItemCount() > 0) {

								binding.recyclerView.setAdapter(projectsAdapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {

								projectsAdapter.notifyDataChanged();
								binding.recyclerView.setAdapter(projectsAdapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	private void searchIssues(String search) {

		binding.progressBar.setVisibility(View.VISIBLE);
		clearAdapters();

		exploreViewModel
				.searchIssues(
						ctx,
						"issues",
						search,
						resultLimit,
						page,
						binding,
						requireActivity(),
						bottomNavigationView)
				.observe(
						requireActivity(),
						mainList -> {
							issuesAdapter = new IssuesAdapter(ctx, mainList);
							issuesAdapter.setLoadMoreListener(
									new IssuesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											exploreViewModel.loadMoreIssues(
													ctx,
													"issues",
													search,
													resultLimit,
													page,
													binding,
													issuesAdapter,
													requireActivity(),
													bottomNavigationView);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (issuesAdapter.getItemCount() > 0) {

								binding.recyclerView.setAdapter(issuesAdapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {

								issuesAdapter.notifyDataChanged();
								binding.recyclerView.setAdapter(issuesAdapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	private void searchMergeRequests(String search) {

		binding.progressBar.setVisibility(View.VISIBLE);
		clearAdapters();

		exploreViewModel
				.searchMergeRequests(
						ctx,
						"merge_requests",
						search,
						resultLimit,
						page,
						binding,
						requireActivity(),
						bottomNavigationView)
				.observe(
						requireActivity(),
						mainList -> {
							mergeRequestsAdapter = new MergeRequestsAdapter(ctx, mainList);
							mergeRequestsAdapter.setLoadMoreListener(
									new MergeRequestsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											exploreViewModel.loadMoreMergeRequests(
													ctx,
													"merge_requests",
													search,
													resultLimit,
													page,
													binding,
													mergeRequestsAdapter,
													requireActivity(),
													bottomNavigationView);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (mergeRequestsAdapter.getItemCount() > 0) {

								binding.recyclerView.setAdapter(mergeRequestsAdapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {

								mergeRequestsAdapter.notifyDataChanged();
								binding.recyclerView.setAdapter(mergeRequestsAdapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	private void searchUsers(String search) {

		binding.progressBar.setVisibility(View.VISIBLE);
		clearAdapters();

		exploreViewModel
				.searchUsers(
						ctx,
						"users",
						search,
						resultLimit,
						page,
						binding,
						requireActivity(),
						bottomNavigationView)
				.observe(
						requireActivity(),
						listMain -> {
							membersAdapter = new MembersAdapter(getContext(), listMain);
							membersAdapter.setLoadMoreListener(
									new MembersAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											exploreViewModel.loadMoreUsers(
													ctx,
													"users",
													search,
													resultLimit,
													page,
													binding,
													membersAdapter,
													requireActivity(),
													bottomNavigationView);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (membersAdapter.getItemCount() > 0) {

								binding.recyclerView.setAdapter(membersAdapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {

								membersAdapter.notifyDataChanged();
								binding.recyclerView.setAdapter(membersAdapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	private void clearAdapters() {
		if (projectsAdapter != null) {
			projectsAdapter.clearAdapter();
			projectsAdapter.notifyDataChanged();
		}
		if (issuesAdapter != null) {
			issuesAdapter.clearAdapter();
			issuesAdapter.notifyDataChanged();
		}
		if (mergeRequestsAdapter != null) {
			mergeRequestsAdapter.clearAdapter();
			mergeRequestsAdapter.notifyDataChanged();
		}
		if (membersAdapter != null) {
			membersAdapter.clearAdapter();
			membersAdapter.notifyDataChanged();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
