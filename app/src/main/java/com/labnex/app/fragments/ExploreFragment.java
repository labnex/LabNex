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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.activities.MergeRequestDetailActivity;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.adapters.IssuesAdapter;
import com.labnex.app.adapters.MembersAdapter;
import com.labnex.app.adapters.MergeRequestsAdapter;
import com.labnex.app.adapters.ProjectsAdapter;
import com.labnex.app.databinding.BottomsheetExploreSearchBinding;
import com.labnex.app.databinding.FragmentExploreBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.models.user.User;
import com.labnex.app.viewmodels.ExploreViewModel;
import com.labnex.app.viewmodels.MergeRequestsViewModel;
import java.util.List;
import java.util.Objects;

/**
 * @author mmarif
 */
public class ExploreFragment extends Fragment {

	private FragmentExploreBinding binding;
	private Context ctx;
	private ExploreViewModel viewModel;
	private MergeRequestsViewModel mergeRequestsViewModel;
	private EndlessRecyclerViewScrollListener scrollListener;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.recyclerView, null, null);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentExploreBinding.inflate(inflater, container, false);
		ctx = requireContext();
		viewModel = new ViewModelProvider(this).get(ExploreViewModel.class);
		mergeRequestsViewModel = new ViewModelProvider(this).get(MergeRequestsViewModel.class);

		int resultLimit = ((BaseActivity) requireActivity()).getAccount().getMaxPageLimit();
		viewModel.setResultLimit(resultLimit);

		setupRecyclerView();
		observeViewModel();

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
		binding.recyclerView.setLayoutManager(layoutManager);
		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(ctx);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.progressBar.setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {
								binding.progressBar.setVisibility(View.GONE);
							}
						});

		viewModel
				.getSearchResult()
				.observe(
						getViewLifecycleOwner(),
						result -> {
							Boolean loading = viewModel.getIsLoading().getValue();
							if (Boolean.TRUE.equals(loading) && result.data.isEmpty()) {
								return;
							}

							if (result.data.isEmpty()) {
								if (Boolean.FALSE.equals(loading)) {
									binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
									binding.recyclerView.setVisibility(View.GONE);
								}
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								renderResults(result.scope, result.data);
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg != null) {
								Toasty.show(ctx, errorMsg);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}
						});

		mergeRequestsViewModel
				.getNavigateToMr()
				.observe(
						getViewLifecycleOwner(),
						mrCtx -> {
							if (mrCtx != null) {
								startActivity(
										mrCtx.getIntent(ctx, MergeRequestDetailActivity.class));
								mergeRequestsViewModel.clearNavigation();
							}
						});
	}

	@SuppressWarnings("unchecked")
	private void renderResults(String scope, List<?> data) {
		scrollListener.resetState();

		switch (scope) {
			case ExploreViewModel.SCOPE_PROJECTS:
				binding.recyclerView.setAdapter(
						new ProjectsAdapter(ctx, (List<Projects>) data, "search"));
				break;
			case ExploreViewModel.SCOPE_ISSUES:
				binding.recyclerView.setAdapter(new IssuesAdapter(ctx, (List<Issues>) data));
				break;
			case ExploreViewModel.SCOPE_MERGE_REQUESTS:
				binding.recyclerView.setAdapter(
						new MergeRequestsAdapter(
								ctx,
								(List<MergeRequests>) data,
								new MergeRequestsAdapter.OnMrClickListener() {
									@Override
									public void onMrClick(MergeRequests mr) {
										mergeRequestsViewModel.fetchAndNavigateMr(ctx, mr);
									}

									@Override
									public void onAuthorClick(MergeRequests mr) {
										if (mr.getAuthor() != null) {
											Intent intent = new Intent(ctx, ProfileActivity.class);
											intent.putExtra("source", "mr");
											intent.putExtra("userId", mr.getAuthor().getId());
											startActivity(intent);
										}
									}
								}));
				break;
			case ExploreViewModel.SCOPE_USERS:
				binding.recyclerView.setAdapter(new MembersAdapter(ctx, (List<User>) data));
				break;
		}
	}

	private void showSearchBottomSheet() {
		BottomSheetDialog dialog = new BottomSheetDialog(ctx);
		BottomsheetExploreSearchBinding sheetBinding =
				BottomsheetExploreSearchBinding.inflate(getLayoutInflater());
		dialog.setContentView(sheetBinding.getRoot());
		UIHelper.applySheetStyle(dialog, true);

		String currentScope = viewModel.getScope();
		sheetBinding.chipIssues.setChecked(ExploreViewModel.SCOPE_ISSUES.equals(currentScope));
		sheetBinding.chipMr.setChecked(ExploreViewModel.SCOPE_MERGE_REQUESTS.equals(currentScope));
		sheetBinding.chipUsers.setChecked(ExploreViewModel.SCOPE_USERS.equals(currentScope));
		sheetBinding.chipProjects.setChecked(ExploreViewModel.SCOPE_PROJECTS.equals(currentScope));

		sheetBinding.btnSearch.setOnClickListener(
				v -> {
					String query =
							Objects.requireNonNull(sheetBinding.searchQueryInput.getText())
									.toString()
									.trim();
					if (!query.isEmpty()) {
						String scope = ExploreViewModel.SCOPE_PROJECTS;
						if (sheetBinding.chipIssues.isChecked())
							scope = ExploreViewModel.SCOPE_ISSUES;
						else if (sheetBinding.chipMr.isChecked())
							scope = ExploreViewModel.SCOPE_MERGE_REQUESTS;
						else if (sheetBinding.chipUsers.isChecked())
							scope = ExploreViewModel.SCOPE_USERS;

						viewModel.setScope(scope);
						viewModel.search(ctx, query);
						dialog.dismiss();
					}
				});

		dialog.show();
	}

	public void openContextMenu() {
		showSearchBottomSheet();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
