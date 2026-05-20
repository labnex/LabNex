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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.IssueDetailActivity;
import com.labnex.app.activities.MainActivity;
import com.labnex.app.activities.MergeRequestDetailActivity;
import com.labnex.app.adapters.TodoAdapter;
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
import java.util.List;

/**
 * @author mmarif
 */
public class TodoFragment extends Fragment {

	private FragmentTodoBinding binding;
	private Context ctx;
	private TodoViewModel viewModel;
	private TodoAdapter adapter;
	private boolean isFirstLoad = true;

	private ToDoItem pendingMrTodo;
	private ToDoItem pendingIssueTodo;

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
		if (viewModel == null) {
			viewModel = new ViewModelProvider(this).get(TodoViewModel.class);
		}
		if (!isHidden()) {
			if (isFirstLoad || viewModel.needsDataLoad()) {
				lazyLoad();
			} else {
				restoreUIState();
			}
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			if (viewModel == null) {
				viewModel = new ViewModelProvider(this).get(TodoViewModel.class);
			}
			if (isFirstLoad || viewModel.needsDataLoad()) {
				lazyLoad();
			} else {
				restoreUIState();
			}
		}
	}

	private void lazyLoad() {
		isFirstLoad = false;
		if (viewModel != null) {
			viewModel.loadTodos(ctx);
		}
	}

	private void restoreUIState() {
		if (viewModel == null) return;

		List<ToDoItem> currentTodos = viewModel.getTodoList().getValue();
		if (currentTodos != null && !currentTodos.isEmpty()) {
			binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.VISIBLE);
			binding.progressBar.setVisibility(View.GONE);
		} else {
			binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
			binding.progressBar.setVisibility(View.GONE);
		}
	}

	public void openContextMenu() {
		if (viewModel != null) {
			showFilterBottomSheet();
		}
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

		String type = viewModel.getCurrentType();
		fb.chipAll.setChecked(TodoViewModel.FILTER_ALL.equals(type));
		fb.chipIssues.setChecked(TodoViewModel.FILTER_ISSUES.equals(type));
		fb.chipMr.setChecked(TodoViewModel.FILTER_MERGE_REQUESTS.equals(type));

		String state = viewModel.getCurrentState();
		fb.chipPending.setChecked(TodoViewModel.STATE_PENDING.equals(state));
		fb.chipDone.setChecked(TodoViewModel.STATE_DONE.equals(state));

		String action = viewModel.getCurrentAction();
		fb.chipAssigned.setChecked("assigned".equals(action));
		fb.chipMentioned.setChecked("mentioned".equals(action));
		fb.chipMarked.setChecked("marked".equals(action));
		fb.chipBuildFailed.setChecked("build_failed".equals(action));
		fb.chipApprovalRequired.setChecked("approval_required".equals(action));

		fb.btnMarkAllDone.setOnClickListener(
				v -> {
					new MaterialAlertDialogBuilder(ctx)
							.setTitle(R.string.mark_all_as_done)
							.setMessage(R.string.mark_all_as_done_confirm)
							.setPositiveButton(
									R.string.confirm,
									(dialog1, which) -> {
										viewModel.markAllAsDone(ctx);
										dialog.dismiss();
									})
							.setNegativeButton(R.string.cancel, null)
							.show();
				});

		fb.btnApply.setOnClickListener(
				v -> {
					if (fb.chipIssues.isChecked())
						viewModel.setCurrentType(TodoViewModel.FILTER_ISSUES);
					else if (fb.chipMr.isChecked())
						viewModel.setCurrentType(TodoViewModel.FILTER_MERGE_REQUESTS);
					else viewModel.setCurrentType(TodoViewModel.FILTER_ALL);

					viewModel.setCurrentState(
							fb.chipDone.isChecked()
									? TodoViewModel.STATE_DONE
									: TodoViewModel.STATE_PENDING);

					String selectedAction = null;
					if (fb.chipAssigned.isChecked()) selectedAction = "assigned";
					else if (fb.chipMentioned.isChecked()) selectedAction = "mentioned";
					else if (fb.chipMarked.isChecked()) selectedAction = "marked";
					else if (fb.chipBuildFailed.isChecked()) selectedAction = "build_failed";
					else if (fb.chipApprovalRequired.isChecked())
						selectedAction = "approval_required";
					viewModel.setCurrentAction(selectedAction);

					viewModel.loadTodos(ctx);
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
				.getTodoList()
				.observe(
						getViewLifecycleOwner(),
						todos -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;
							if (todos == null) return;

							adapter.setCurrentState(viewModel.getCurrentState());

							if (todos.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
								binding.progressBar.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								binding.progressBar.setVisibility(View.GONE);
								adapter.updateList(todos);
							}
						});

		viewModel
				.getFetchedProject()
				.observe(
						getViewLifecycleOwner(),
						project -> {
							if (project == null) return;
							if (pendingMrTodo != null) {
								viewModel.fetchMrForTodo(
										ctx, project.getId(), pendingMrTodo.getTarget().getIid());
								pendingMrTodo = null;
							} else if (pendingIssueTodo != null) {
								viewModel.fetchIssueForTodo(
										ctx,
										project.getId(),
										pendingIssueTodo.getTarget().getIid());
								pendingIssueTodo = null;
							}
						});

		viewModel
				.getFetchedMr()
				.observe(
						getViewLifecycleOwner(),
						mr -> {
							if (mr == null) return;
							Projects project = viewModel.getFetchedProject().getValue();
							if (project != null) {
								navigateToMergeRequestDetail(project, mr);
							}
							viewModel.clearFetchedData();
						});

		viewModel
				.getFetchedIssue()
				.observe(
						getViewLifecycleOwner(),
						issue -> {
							if (issue == null) return;
							Projects project = viewModel.getFetchedProject().getValue();
							if (project != null) {
								navigateToIssueDetail(project, issue);
							}
							viewModel.clearFetchedData();
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
									if (getActivity() instanceof MainActivity) {
										((MainActivity) getActivity()).refreshTodoBadge();
									}
									break;
								case "all_marked_done":
									Toasty.show(ctx, getString(R.string.todo_all_marked_done));
									if (getActivity() instanceof MainActivity) {
										((MainActivity) getActivity()).refreshTodoBadge();
									}
									break;
								case "auth_error":
									Toasty.show(ctx, getString(R.string.not_authorized));
									break;
								case "access_forbidden_403":
									Toasty.show(ctx, getString(R.string.access_forbidden_403));
									break;
								case "not_found":
									Toasty.show(ctx, getString(R.string.not_found));
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
		if ("MergeRequest".equalsIgnoreCase(type) && todo.getTarget() != null) {
			navigateToMergeRequest(todo);
		} else if ("Issue".equalsIgnoreCase(type) && todo.getTarget() != null) {
			navigateToIssue(todo);
		} else if (todo.getTargetUrl() != null) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(todo.getTargetUrl())));
		}
	}

	private void navigateToMergeRequestDetail(Projects project, MergeRequests mr) {
		ProjectsContext pc = new ProjectsContext(project, ctx);
		pc.saveToDB(ctx);

		Intent intent =
				new MergeRequestContext(mr, pc).getIntent(ctx, MergeRequestDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	private void navigateToIssueDetail(Projects project, Issues issue) {
		ProjectsContext pc = new ProjectsContext(project, ctx);
		pc.saveToDB(ctx);

		Intent intent = new IssueContext(issue, pc).getIntent(ctx, IssueDetailActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	private void navigateToMergeRequest(ToDoItem todo) {
		if (todo.getProject() == null) {
			showError();
			return;
		}
		pendingMrTodo = todo;
		pendingIssueTodo = null;
		viewModel.fetchProjectForTodo(ctx, todo.getProject().getId());
	}

	private void navigateToIssue(ToDoItem todo) {
		if (todo.getProject() == null) {
			showError();
			return;
		}
		pendingIssueTodo = todo;
		pendingMrTodo = null;
		viewModel.fetchProjectForTodo(ctx, todo.getProject().getId());
	}

	private void showError() {
		Toasty.show(ctx, getString(R.string.generic_server_response_error));
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
