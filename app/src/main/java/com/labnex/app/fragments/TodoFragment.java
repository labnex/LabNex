package com.labnex.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.labnex.app.R;
import com.labnex.app.activities.IssueDetailActivity;
import com.labnex.app.activities.IssuesActivity;
import com.labnex.app.activities.MergeRequestDetailActivity;
import com.labnex.app.activities.MergeRequestsActivity;
import com.labnex.app.activities.ProjectDetailActivity;
import com.labnex.app.adapters.TodoAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.IssueContext;
import com.labnex.app.contexts.MergeRequestContext;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.BottomsheetTodoFilterBinding;
import com.labnex.app.databinding.FragmentTodoBinding;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.models.todo.ToDoItem;
import com.labnex.app.viewmodels.TodoViewModel;
import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class TodoFragment extends Fragment {

	private FragmentTodoBinding binding;
	private Context ctx;
	private TodoViewModel viewModel;
	private TodoAdapter adapter;
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
		binding = FragmentTodoBinding.inflate(inflater, container, false);
		ctx = requireContext();
		viewModel = new ViewModelProvider(this).get(TodoViewModel.class);

		setupRecyclerView();
		setupPullToRefresh();
		observeViewModel();

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
		viewModel.loadTodos(ctx);
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
				new TodoAdapter(
						ctx,
						new ArrayList<>(),
						new TodoAdapter.OnTodoClickListener() {
							@Override
							public void onTodoClick(ToDoItem todo) {
								navigateToTodoTarget(todo);
							}

							@Override
							public void onTodoMarkAsDone(ToDoItem todo) {
								viewModel.markAsDone(ctx, todo.getId());
							}
						});
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		binding.recyclerView.setAdapter(adapter);
	}

	private void setupPullToRefresh() {
		binding.pullToRefresh.setOnRefreshListener(() -> viewModel.loadTodos(ctx));
	}

	private void showFilterBottomSheet() {
		BottomSheetDialog dialog = new BottomSheetDialog(ctx);
		BottomsheetTodoFilterBinding fb = BottomsheetTodoFilterBinding.inflate(getLayoutInflater());
		dialog.setContentView(fb.getRoot());
		UIHelper.applySheetStyle(dialog, true);

		String current = viewModel.getFilter();
		fb.chipAll.setChecked(TodoViewModel.FILTER_ALL.equals(current));
		fb.chipIssues.setChecked(TodoViewModel.FILTER_ISSUES.equals(current));
		fb.chipMr.setChecked(TodoViewModel.FILTER_MERGE_REQUESTS.equals(current));

		fb.chipAll.setOnClickListener(
				v -> {
					viewModel.setFilter(TodoViewModel.FILTER_ALL);
					dialog.dismiss();
				});
		fb.chipIssues.setOnClickListener(
				v -> {
					viewModel.setFilter(TodoViewModel.FILTER_ISSUES);
					dialog.dismiss();
				});
		fb.chipMr.setOnClickListener(
				v -> {
					viewModel.setFilter(TodoViewModel.FILTER_MERGE_REQUESTS);
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
				.getFilteredTodos()
				.observe(
						getViewLifecycleOwner(),
						todos -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;

							if (todos == null) {
								return;
							}

							if (todos.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								adapter.updateList(todos);
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg == null) return;
							switch (errorMsg) {
								case "marked_done":
									Toasty.show(ctx, getString(R.string.todo_marked_done));
									break;
								case "generic_error":
									Toasty.show(
											ctx, getString(R.string.generic_server_response_error));
									break;
								default:
									Toasty.show(ctx, errorMsg);
									break;
							}
							viewModel.clearError();
						});
	}

	private void navigateToTodoTarget(ToDoItem todo) {
		String type = todo.getTargetType();
		if ("MergeRequest".equalsIgnoreCase(type) && todo.getTarget() != null)
			navigateToMergeRequest(todo);
		else if ("Issue".equalsIgnoreCase(type) && todo.getTarget() != null) navigateToIssue(todo);
		else if (todo.getTargetUrl() != null)
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(todo.getTargetUrl())));
	}

	private void navigateToMergeRequest(ToDoItem todo) {
		if (todo.getProject() == null) {
			showError();
			return;
		}
		RetrofitClient.getApiInterface(ctx)
				.getProjectInfo(todo.getProject().getId())
				.enqueue(
						new ProjectCallback() {
							@Override
							void onSuccess(Projects p) {
								fetchAndOpenMergeRequest(p, todo);
							}
						});
	}

	private void navigateToIssue(ToDoItem todo) {
		if (todo.getProject() == null) {
			showError();
			return;
		}
		RetrofitClient.getApiInterface(ctx)
				.getProjectInfo(todo.getProject().getId())
				.enqueue(
						new ProjectCallback() {
							@Override
							void onSuccess(Projects p) {
								fetchAndOpenIssue(p, todo);
							}
						});
	}

	private void fetchAndOpenMergeRequest(Projects project, ToDoItem todo) {
		if (todo.getTarget() == null) {
			showError();
			return;
		}
		RetrofitClient.getApiInterface(ctx)
				.getMergeRequest(project.getId(), todo.getTarget().getIid())
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MergeRequests> c,
									@NonNull Response<MergeRequests> r) {
								if (r.isSuccessful() && r.body() != null)
									openMergeRequest(project, r.body());
								else goToProjectSection(project, "merge_request");
							}

							@Override
							public void onFailure(
									@NonNull Call<MergeRequests> c, @NonNull Throwable t) {
								goToProjectSection(project, "merge_request");
							}
						});
	}

	private void fetchAndOpenIssue(Projects project, ToDoItem todo) {
		if (todo.getTarget() == null) {
			showError();
			return;
		}
		RetrofitClient.getApiInterface(ctx)
				.getIssue(project.getId(), todo.getTarget().getIid())
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Issues> c, @NonNull Response<Issues> r) {
								if (r.isSuccessful() && r.body() != null)
									openIssue(project, r.body());
								else goToProjectSection(project, "issue");
							}

							@Override
							public void onFailure(@NonNull Call<Issues> c, @NonNull Throwable t) {
								goToProjectSection(project, "issue");
							}
						});
	}

	private void openMergeRequest(Projects project, MergeRequests mr) {
		ProjectsContext pc = new ProjectsContext(project, ctx);
		pc.saveToDB(ctx);
		ctx.startActivity(
				pc.getIntent(ctx, ProjectDetailActivity.class)
						.setFlags(
								Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
		Intent list = new Intent(ctx, MergeRequestsActivity.class);
		list.putExtra("project", pc);
		list.putExtra("source", "mr");
		list.putExtra("projectId", pc.getProjectId());
		ctx.startActivity(list);
		ctx.startActivity(
				new MergeRequestContext(mr, pc).getIntent(ctx, MergeRequestDetailActivity.class));
	}

	private void openIssue(Projects project, Issues issue) {
		ProjectsContext pc = new ProjectsContext(project, ctx);
		pc.saveToDB(ctx);
		ctx.startActivity(
				pc.getIntent(ctx, ProjectDetailActivity.class)
						.setFlags(
								Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
		Intent list = new Intent(ctx, IssuesActivity.class);
		list.putExtra("project", pc);
		list.putExtra("source", "project");
		list.putExtra("id", pc.getProjectId());
		ctx.startActivity(list);
		ctx.startActivity(new IssueContext(issue, pc).getIntent(ctx, IssueDetailActivity.class));
	}

	private void goToProjectSection(Projects project, String section) {
		ProjectsContext pc = new ProjectsContext(project, ctx);
		pc.saveToDB(ctx);
		Intent i = pc.getIntent(ctx, ProjectDetailActivity.class);
		i.putExtra("goToSection", "yes");
		i.putExtra("goToSectionType", section);
		ctx.startActivity(i);
	}

	private void showError() {
		Toasty.show(ctx, getString(R.string.generic_server_response_error));
	}

	private abstract class ProjectCallback implements Callback<Projects> {
		@Override
		public void onResponse(@NonNull Call<Projects> c, @NonNull Response<Projects> r) {
			if (r.isSuccessful() && r.body() != null) onSuccess(r.body());
			else showError();
		}

		@Override
		public void onFailure(@NonNull Call<Projects> c, @NonNull Throwable t) {
			showError();
		}

		abstract void onSuccess(Projects project);
	}
}
