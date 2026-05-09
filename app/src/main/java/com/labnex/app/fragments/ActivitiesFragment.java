package com.labnex.app.fragments;

import android.content.Context;
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
import com.labnex.app.activities.ProjectDetailActivity;
import com.labnex.app.adapters.ActivitiesAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.BottomsheetActivitiesFilterBinding;
import com.labnex.app.databinding.FragmentActivitiesBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.viewmodels.ActivitiesViewModel;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ActivitiesFragment extends Fragment {

	private FragmentActivitiesBinding binding;
	private Context ctx;
	private ActivitiesViewModel viewModel;
	private ActivitiesAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private boolean isFirstLoad = true;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.recyclerView, binding.pullToRefresh, null);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentActivitiesBinding.inflate(inflater, container, false);
		ctx = requireContext();
		viewModel = new ViewModelProvider(this).get(ActivitiesViewModel.class);

		setupRecyclerView();
		setupPullToRefresh();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad) lazyLoad();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad) lazyLoad();
	}

	private void lazyLoad() {
		isFirstLoad = false;
		viewModel.loadEvents(ctx);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	public void openContextMenu() {
		showFilterBottomSheet();
	}

	private void setupRecyclerView() {
		adapter =
				new ActivitiesAdapter(
						ctx,
						new ArrayList<>(),
						event -> {
							if (event.getProjectId() > 0) {
								RetrofitClient.getApiInterface(ctx)
										.getProjectInfo(event.getProjectId())
										.enqueue(
												new Callback<>() {
													@Override
													public void onResponse(
															@NonNull Call<Projects> c,
															@NonNull Response<Projects> r) {
														if (r.isSuccessful() && r.body() != null) {
															ProjectsContext pc =
																	new ProjectsContext(
																			r.body(), ctx);
															pc.saveToDB(ctx);
															ctx.startActivity(
																	pc.getIntent(
																			ctx,
																			ProjectDetailActivity
																					.class));
														}
													}

													@Override
													public void onFailure(
															@NonNull Call<Projects> c,
															@NonNull Throwable t) {}
												});
							}
						});
		LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(ctx);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupPullToRefresh() {
		binding.pullToRefresh.setOnRefreshListener(() -> viewModel.loadEvents(ctx));
	}

	private void showFilterBottomSheet() {
		BottomSheetDialog dialog = new BottomSheetDialog(ctx);
		BottomsheetActivitiesFilterBinding fb =
				BottomsheetActivitiesFilterBinding.inflate(getLayoutInflater());
		dialog.setContentView(fb.getRoot());
		UIHelper.applySheetStyle(dialog, true);

		String current = viewModel.getFilter();
		fb.chipAll.setChecked(ActivitiesViewModel.FILTER_ALL.equals(current));
		fb.chipIssue.setChecked(ActivitiesViewModel.FILTER_ISSUE.equals(current));
		fb.chipNote.setChecked(ActivitiesViewModel.FILTER_NOTE.equals(current));
		fb.chipProject.setChecked(ActivitiesViewModel.FILTER_PROJECT.equals(current));
		fb.chipMr.setChecked(ActivitiesViewModel.FILTER_MERGE_REQUEST.equals(current));
		fb.chipMilestone.setChecked(ActivitiesViewModel.FILTER_MILESTONE.equals(current));
		fb.chipSnippet.setChecked(ActivitiesViewModel.FILTER_SNIPPET.equals(current));
		fb.chipEpic.setChecked(ActivitiesViewModel.FILTER_EPIC.equals(current));
		fb.chipUser.setChecked(ActivitiesViewModel.FILTER_USER.equals(current));

		fb.chipAll.setOnClickListener(
				v -> {
					viewModel.setFilter(ActivitiesViewModel.FILTER_ALL);
					viewModel.loadEvents(ctx);
					dialog.dismiss();
				});
		fb.chipIssue.setOnClickListener(
				v -> {
					viewModel.setFilter(ActivitiesViewModel.FILTER_ISSUE);
					viewModel.loadEvents(ctx);
					dialog.dismiss();
				});
		fb.chipNote.setOnClickListener(
				v -> {
					viewModel.setFilter(ActivitiesViewModel.FILTER_NOTE);
					viewModel.loadEvents(ctx);
					dialog.dismiss();
				});
		fb.chipProject.setOnClickListener(
				v -> {
					viewModel.setFilter(ActivitiesViewModel.FILTER_PROJECT);
					viewModel.loadEvents(ctx);
					dialog.dismiss();
				});
		fb.chipMr.setOnClickListener(
				v -> {
					viewModel.setFilter(ActivitiesViewModel.FILTER_MERGE_REQUEST);
					viewModel.loadEvents(ctx);
					dialog.dismiss();
				});
		fb.chipMilestone.setOnClickListener(
				v -> {
					viewModel.setFilter(ActivitiesViewModel.FILTER_MILESTONE);
					viewModel.loadEvents(ctx);
					dialog.dismiss();
				});
		fb.chipSnippet.setOnClickListener(
				v -> {
					viewModel.setFilter(ActivitiesViewModel.FILTER_SNIPPET);
					viewModel.loadEvents(ctx);
					dialog.dismiss();
				});
		fb.chipEpic.setOnClickListener(
				v -> {
					viewModel.setFilter(ActivitiesViewModel.FILTER_EPIC);
					viewModel.loadEvents(ctx);
					dialog.dismiss();
				});
		fb.chipUser.setOnClickListener(
				v -> {
					viewModel.setFilter(ActivitiesViewModel.FILTER_USER);
					viewModel.loadEvents(ctx);
					dialog.dismiss();
				});

		dialog.show();
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
								binding.pullToRefresh.setRefreshing(false);
							}
						});

		viewModel
				.getEvents()
				.observe(
						getViewLifecycleOwner(),
						events -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;

							if (events == null) return;

							if (events.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								adapter.updateList(events);
								scrollListener.resetState();
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg == null) return;
							switch (errorMsg) {
								case "auth_error":
									Toasty.show(ctx, getString(R.string.not_authorized));
									break;
								case "access_forbidden_403":
									Toasty.show(ctx, getString(R.string.access_forbidden_403));
									break;
								case "generic_error":
									Toasty.show(ctx, getString(R.string.generic_error));
									break;
								default:
									Toasty.show(ctx, errorMsg);
									break;
							}
							viewModel.clearError();
						});
	}
}
