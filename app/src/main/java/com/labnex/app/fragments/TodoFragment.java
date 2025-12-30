package com.labnex.app.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.chip.Chip;
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
import com.labnex.app.databinding.FragmentTodoBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.todo.ToDoItem;
import com.labnex.app.viewmodels.TodoViewModel;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class TodoFragment extends Fragment {

	private TodoViewModel todoViewModel;
	private Context ctx;
	private FragmentTodoBinding binding;
	private TodoAdapter adapter;
	private final List<ToDoItem> todoList = new ArrayList<>();
	private final String[] filterTypes = {"All", "Issues", "Merge Requests"};
	private String selectedFilter = "All";

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ctx = requireContext();
		binding = FragmentTodoBinding.inflate(inflater, container, false);

		setupRecyclerView();
		setupChipFilters();
		setupPullToRefresh();

		todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

		setupObservers();
		loadTodos();

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter =
				new TodoAdapter(
						ctx,
						todoList,
						false,
						new TodoAdapter.OnTodoClickListener() {
							@Override
							public void onTodoClick(ToDoItem todo) {
								navigateToTodoTarget(todo);
							}

							@Override
							public void onTodoMarkAsDone(ToDoItem todo) {
								markTodoAsDone(todo.getId());
							}
						});

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		binding.recyclerView.setAdapter(adapter);
	}

	@SuppressLint("NotifyDataSetChanged")
	private void setupChipFilters() {
		LayoutInflater inflater = LayoutInflater.from(ctx);

		for (String filterType : filterTypes) {
			Chip chip = (Chip) inflater.inflate(R.layout.chip_item, binding.filterChips, false);
			chip.setText(filterType);
			chip.setCheckable(true);
			chip.setClickable(true);

			if ("All".equals(filterType)) {
				chip.setChecked(true);
			}

			chip.setOnClickListener(
					v -> {
						selectedFilter = filterType;
						todoList.clear();
						adapter.notifyDataSetChanged();
						loadTodos();
						binding.progressBar.setVisibility(View.VISIBLE);
					});

			binding.filterChips.addView(chip);
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	private void setupPullToRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											todoList.clear();
											adapter.notifyDataSetChanged();
											loadTodos();
											binding.pullToRefresh.setRefreshing(false);
										},
										250));
	}

	@SuppressLint("NotifyDataSetChanged")
	private void setupObservers() {
		todoViewModel
				.getTodoList()
				.observe(
						getViewLifecycleOwner(),
						allTodos -> {
							if (allTodos != null && !allTodos.isEmpty()) {
								List<ToDoItem> filteredItems = applyFilter(allTodos);
								todoList.clear();
								todoList.addAll(filteredItems);
								adapter.notifyDataSetChanged();

								if (todoList.isEmpty()) {
									binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
									binding.recyclerView.setVisibility(View.GONE);
								} else {
									binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
									binding.recyclerView.setVisibility(View.VISIBLE);
								}
							}
						});

		todoViewModel
				.getRemovedTodoId()
				.observe(
						getViewLifecycleOwner(),
						todoId -> {
							if (todoId != null && todoId > 0) {
								for (int i = 0; i < todoList.size(); i++) {
									if (todoList.get(i).getId() == todoId) {
										todoList.remove(i);
										adapter.notifyItemRemoved(i);
										break;
									}
								}

								todoViewModel.clearRemovedTodo();

								if (todoList.isEmpty()) {
									binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
									binding.recyclerView.setVisibility(View.GONE);
								}
							}
						});
	}

	private void loadTodos() {
		binding.progressBar.setVisibility(View.VISIBLE);

		Call<List<ToDoItem>> call = RetrofitClient.getApiInterface(ctx).getAllTodos();
		call.enqueue(
				new Callback<>() {
					@SuppressLint("NotifyDataSetChanged")
					@Override
					public void onResponse(
							@NonNull Call<List<ToDoItem>> call,
							@NonNull Response<List<ToDoItem>> response) {
						binding.progressBar.setVisibility(View.GONE);

						if (response.isSuccessful() && response.body() != null) {
							List<ToDoItem> newItems = response.body();
							todoViewModel.setTodoList(newItems);
						} else {
							showError(getString(R.string.generic_server_response_error));
							binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							binding.recyclerView.setVisibility(View.GONE);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<ToDoItem>> call, @NonNull Throwable t) {
						binding.progressBar.setVisibility(View.GONE);
						showError(getString(R.string.generic_server_response_error));
						binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
						binding.recyclerView.setVisibility(View.GONE);
					}
				});
	}

	private List<ToDoItem> applyFilter(List<ToDoItem> items) {
		if ("All".equals(selectedFilter)) {
			return items;
		} else if ("Issues".equals(selectedFilter)) {
			List<ToDoItem> filtered = new ArrayList<>();
			for (ToDoItem item : items) {
				if ("Issue".equalsIgnoreCase(item.getTargetType())) {
					filtered.add(item);
				}
			}
			return filtered;
		} else if ("Merge Requests".equals(selectedFilter)) {
			List<ToDoItem> filtered = new ArrayList<>();
			for (ToDoItem item : items) {
				if ("MergeRequest".equalsIgnoreCase(item.getTargetType())) {
					filtered.add(item);
				}
			}
			return filtered;
		}
		return items;
	}

	private void markTodoAsDone(long todoId) {
		Call<ToDoItem> call = RetrofitClient.getApiInterface(ctx).markTodoAsDone((int) todoId);
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ToDoItem> call, @NonNull Response<ToDoItem> response) {
						if (response.isSuccessful()) {
							todoViewModel.removeTodo(todoId);

							Snackbar.info(
									requireActivity(),
									requireActivity().findViewById(R.id.nav_view),
									getString(R.string.todo_marked_done));
						} else {
							showError(getString(R.string.generic_server_response_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<ToDoItem> call, @NonNull Throwable t) {
						showError(getString(R.string.generic_server_response_error));
					}
				});
	}

	private void navigateToTodoTarget(ToDoItem todo) {
		if (todo.getTargetType().equalsIgnoreCase("MergeRequest") && todo.getTarget() != null) {
			navigateToMergeRequest(todo);
		} else if (todo.getTargetType().equalsIgnoreCase("Issue") && todo.getTarget() != null) {
			navigateToIssue(todo);
		} else {
			if (todo.getTargetUrl() != null) {
				Intent browserIntent =
						new Intent(Intent.ACTION_VIEW, Uri.parse(todo.getTargetUrl()));
				startActivity(browserIntent);
			}
		}
	}

	private void navigateToMergeRequest(ToDoItem todo) {
		fetchProjectAndOpenMergeRequest(todo);
	}

	private void navigateToIssue(ToDoItem todo) {
		fetchProjectAndOpenIssue(todo);
	}

	private void fetchProjectAndOpenMergeRequest(ToDoItem todo) {
		if (todo.getProject() == null) {
			showError(getString(R.string.project_info_not_available));
			return;
		}

		Call<com.labnex.app.models.projects.Projects> call =
				RetrofitClient.getApiInterface(ctx).getProjectInfo(todo.getProject().getId());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<com.labnex.app.models.projects.Projects> call,
							@NonNull Response<com.labnex.app.models.projects.Projects> response) {
						if (response.isSuccessful() && response.body() != null) {
							com.labnex.app.models.projects.Projects project = response.body();
							fetchAndOpenSpecificMergeRequest(project, todo);
						} else {
							showError(getString(R.string.generic_server_response_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<com.labnex.app.models.projects.Projects> call,
							@NonNull Throwable t) {
						showError(getString(R.string.generic_server_response_error));
					}
				});
	}

	private void fetchProjectAndOpenIssue(ToDoItem todo) {
		if (todo.getProject() == null) {
			showError(getString(R.string.project_info_not_available));
			return;
		}

		Call<com.labnex.app.models.projects.Projects> call =
				RetrofitClient.getApiInterface(ctx).getProjectInfo(todo.getProject().getId());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<com.labnex.app.models.projects.Projects> call,
							@NonNull Response<com.labnex.app.models.projects.Projects> response) {
						if (response.isSuccessful() && response.body() != null) {
							com.labnex.app.models.projects.Projects project = response.body();
							fetchAndOpenSpecificIssue(project, todo);
						} else {
							showError(getString(R.string.generic_server_response_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<com.labnex.app.models.projects.Projects> call,
							@NonNull Throwable t) {
						showError(getString(R.string.generic_server_response_error));
					}
				});
	}

	private void fetchAndOpenSpecificMergeRequest(
			com.labnex.app.models.projects.Projects project, ToDoItem todo) {
		if (todo.getTarget() == null) {
			showError(getString(R.string.mr_detail_not_available));
			return;
		}

		Call<MergeRequests> call =
				RetrofitClient.getApiInterface(ctx)
						.getMergeRequest(project.getId(), todo.getTarget().getIid());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<MergeRequests> call,
							@NonNull Response<MergeRequests> response) {
						if (response.isSuccessful() && response.body() != null) {
							MergeRequests mergeRequest = response.body();
							createBackStackAndOpenMergeRequest(project, mergeRequest);
						} else {
							goToProjectWithSection(project, "merge_request");
						}
					}

					@Override
					public void onFailure(@NonNull Call<MergeRequests> call, @NonNull Throwable t) {
						goToProjectWithSection(project, "merge_request");
					}
				});
	}

	private void fetchAndOpenSpecificIssue(
			com.labnex.app.models.projects.Projects project, ToDoItem todo) {
		if (todo.getTarget() == null) {
			showError(getString(R.string.issue_detail_not_available));
			return;
		}

		Call<Issues> call =
				RetrofitClient.getApiInterface(ctx)
						.getIssue(project.getId(), todo.getTarget().getIid());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Issues> call, @NonNull Response<Issues> response) {
						if (response.isSuccessful() && response.body() != null) {
							Issues issue = response.body();
							createBackStackAndOpenIssue(project, issue);
						} else {
							goToProjectWithSection(project, "issue");
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {
						goToProjectWithSection(project, "issue");
					}
				});
	}

	private void createBackStackAndOpenIssue(
			com.labnex.app.models.projects.Projects project, Issues issue) {
		ProjectsContext projectContext = new ProjectsContext(project, ctx);
		projectContext.saveToDB(ctx);

		Intent projectIntent = projectContext.getIntent(ctx, ProjectDetailActivity.class);
		projectIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		ctx.startActivity(projectIntent);

		Intent issuesIntent = new Intent(ctx, IssuesActivity.class);
		issuesIntent.putExtra("project", projectContext);
		issuesIntent.putExtra("source", "project");
		issuesIntent.putExtra("id", projectContext.getProjectId());
		ctx.startActivity(issuesIntent);

		IssueContext issueContext = new IssueContext(issue, projectContext);
		Intent issueDetailIntent = issueContext.getIntent(ctx, IssueDetailActivity.class);
		ctx.startActivity(issueDetailIntent);
	}

	private void createBackStackAndOpenMergeRequest(
			com.labnex.app.models.projects.Projects project, MergeRequests mergeRequest) {
		ProjectsContext projectContext = new ProjectsContext(project, ctx);
		projectContext.saveToDB(ctx);

		Intent projectIntent = projectContext.getIntent(ctx, ProjectDetailActivity.class);
		projectIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		ctx.startActivity(projectIntent);

		Intent mrIntent = new Intent(ctx, MergeRequestsActivity.class);
		mrIntent.putExtra("project", projectContext);
		mrIntent.putExtra("source", "mr");
		mrIntent.putExtra("projectId", projectContext.getProjectId());
		ctx.startActivity(mrIntent);

		MergeRequestContext mrContext = new MergeRequestContext(mergeRequest, projectContext);
		Intent mrDetailIntent = mrContext.getIntent(ctx, MergeRequestDetailActivity.class);
		ctx.startActivity(mrDetailIntent);
	}

	private void goToProjectWithSection(
			com.labnex.app.models.projects.Projects project, String sectionType) {
		ProjectsContext projectContext = new ProjectsContext(project, ctx);
		projectContext.saveToDB(ctx);

		Intent projectIntent = projectContext.getIntent(ctx, ProjectDetailActivity.class);
		projectIntent.putExtra("goToSection", "yes");
		projectIntent.putExtra("goToSectionType", sectionType);
		ctx.startActivity(projectIntent);
	}

	private void showError(String message) {
		Snackbar.info(requireActivity(), requireActivity().findViewById(R.id.nav_view), message);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
