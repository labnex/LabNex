package com.labnex.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.AppSettingsActivity;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.activities.GroupsActivity;
import com.labnex.app.activities.IssueDetailActivity;
import com.labnex.app.activities.IssuesActivity;
import com.labnex.app.activities.MergeRequestDetailActivity;
import com.labnex.app.activities.MergeRequestsActivity;
import com.labnex.app.activities.NotesActivity;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.activities.ProjectDetailActivity;
import com.labnex.app.activities.ProjectsActivity;
import com.labnex.app.activities.SnippetsActivity;
import com.labnex.app.adapters.MostVisitedAdapter;
import com.labnex.app.adapters.TodoAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.IssueContext;
import com.labnex.app.contexts.MergeRequestContext;
import com.labnex.app.contexts.ProjectsContext;
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
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.todo.ToDoItem;
import com.labnex.app.models.user.User;
import com.labnex.app.viewmodels.TodoViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class HomeFragment extends Fragment {

	private TodoViewModel todoViewModel;
	private FragmentHomeBinding binding;
	private Context ctx;
	private int currentActiveAccountId;
	private NotesApi notesApi;
	private ProjectsApi projectsApi;
	private List<Projects> projectsList;
	private MostVisitedAdapter mostVisitedAdapter;
	private int refreshSuccessCounter;
	private boolean isUserRefresh;
	private List<ToDoItem> todoList;
	private TodoAdapter todoAdapter;
	private boolean isExpanded = false;

	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentHomeBinding.inflate(inflater, container, false);
		View root = binding.getRoot();
		ctx = requireContext();

		todoViewModel = new ViewModelProvider(requireActivity()).get(TodoViewModel.class);

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
		getTodos();

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

											todoList.clear();
											todoAdapter.updateList(todoList);

											binding.sectionTodo.todoEmptyState.setVisibility(
													View.GONE);
											binding.sectionTodo.todoRecyclerView.setVisibility(
													View.VISIBLE);
											binding.sectionMostVisited.nothingFoundFrame
													.setVisibility(View.GONE);

											getBroadcastMessage();
											getUserInfo();
											getMostVisitedProjects();
											getTodos();
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

		todoList = new ArrayList<>();
		todoAdapter =
				new TodoAdapter(
						ctx,
						todoList,
						true,
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

		binding.sectionTodo.todoRecyclerView.setHasFixedSize(true);
		binding.sectionTodo.todoRecyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));
		binding.sectionTodo.todoRecyclerView.setAdapter(todoAdapter);

		setupTodoObservers();

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();

		refreshMostVisitedProjects();
		updateNotesBadge();
		updateClearButtonVisibility();
		refreshTodos();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void getTodos() {
		fetchTodos(true);
	}

	private void refreshTodos() {
		fetchTodos(false);
	}

	private void setupTodoObservers() {
		todoViewModel
				.getRemovedTodoId()
				.observe(
						getViewLifecycleOwner(),
						todoId -> {
							if (todoId != null && todoId > 0) {
								fetchTodos(false);
							}
						});
	}

	private void fetchTodos(boolean incrementCounter) {
		Call<List<ToDoItem>> call = RetrofitClient.getApiInterface(ctx).getAllTodos();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<ToDoItem>> call,
							@NonNull Response<List<ToDoItem>> response) {
						if (response.isSuccessful() && response.body() != null) {
							List<ToDoItem> allTodos = response.body();
							int itemsToShow = Math.min(allTodos.size(), 5);

							todoList.clear();
							for (int i = 0; i < itemsToShow; i++) {
								todoList.add(allTodos.get(i));
							}

							todoAdapter.updateList(todoList);
							todoViewModel.setTodoList(allTodos);
						}

						if (incrementCounter) {
							refreshSuccessCounter++;
							checkRefreshComplete();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<ToDoItem>> call, @NonNull Throwable t) {
						if (incrementCounter) {
							refreshSuccessCounter++;
							checkRefreshComplete();
						}
					}
				});
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
							fetchTodos(false);

							Snackbar.info(
									requireActivity(),
									requireActivity().findViewById(R.id.nav_view),
									getString(R.string.todo_marked_done));
						}
					}

					@Override
					public void onFailure(@NonNull Call<ToDoItem> call, @NonNull Throwable t) {}
				});
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

	private void setupBroadcastToggle() {
		TextView messageView = binding.message;
		ImageView toggleIcon = binding.toggleIcon;

		isExpanded = false;
		messageView.setMaxLines(2);
		toggleIcon.setRotation(0f);

		View.OnClickListener toggleListener =
				v -> {
					isExpanded = !isExpanded;
					messageView.setMaxLines(isExpanded ? Integer.MAX_VALUE : 2);
					toggleIcon.setRotation(isExpanded ? 180f : 0f);
				};

		toggleIcon.setOnClickListener(toggleListener);

		binding.broadcastMessage.setOnClickListener(
				v ->
						messageView.post(
								() -> {
									if (messageView.getLayout() != null
											&& messageView.getLineCount() > 2) {
										toggleListener.onClick(v);
									}
								}));

		String text = messageView.getText().toString();
		if (text.length() > 100) {
			toggleIcon.setVisibility(View.VISIBLE);
		} else {
			messageView.post(
					() -> {
						if (messageView.getLayout() != null) {
							int lineCount = messageView.getLineCount();
							toggleIcon.setVisibility(lineCount > 2 ? View.VISIBLE : View.GONE);
						}
					});
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
										setupBroadcastToggle();
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

						if (!isAdded() || getContext() == null) {
							return;
						}

						User userDetails = response.body();
						if (response.isSuccessful() && response.code() == 200) {
							assert userDetails != null;

							try {
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
											Intent intent =
													new Intent(getContext(), ProfileActivity.class);
											intent.putExtra("source", "home");
											intent.putExtra("userId", userDetails.getId());
											startActivity(intent);
										});
								binding.userAvatar.setEnabled(true);
								updateWelcomeText(userDetails);
							} catch (IllegalStateException e) {
								return;
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
						if (isAdded() && getContext() != null) {
							Snackbar.info(
									requireActivity(),
									requireActivity().findViewById(R.id.nav_view),
									getString(R.string.generic_server_response_error));
						}
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
		if (refreshSuccessCounter == 4 && isAdded()) {
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
			refreshTodos();

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
			showErrorAndFallback(String.valueOf(R.string.project_info_not_available));
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
							showErrorAndFallback(
									String.valueOf(R.string.generic_server_response_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<com.labnex.app.models.projects.Projects> call,
							@NonNull Throwable t) {
						showErrorAndFallback(
								String.valueOf(R.string.generic_server_response_error));
					}
				});
	}

	private void fetchProjectAndOpenIssue(ToDoItem todo) {
		if (todo.getProject() == null) {
			showErrorAndFallback(String.valueOf(R.string.project_info_not_available));
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
							showErrorAndFallback(
									String.valueOf(R.string.generic_server_response_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<com.labnex.app.models.projects.Projects> call,
							@NonNull Throwable t) {
						showErrorAndFallback(
								String.valueOf(R.string.generic_server_response_error));
					}
				});
	}

	private void fetchAndOpenSpecificMergeRequest(
			com.labnex.app.models.projects.Projects project, ToDoItem todo) {
		if (todo.getTarget() == null) {
			showErrorAndFallback(String.valueOf(R.string.mr_detail_not_available));
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
			showErrorAndFallback(String.valueOf(R.string.issue_detail_not_available));
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

	private void showErrorAndFallback(String errorMessage) {
		Snackbar.info(
				requireActivity(), requireActivity().findViewById(R.id.nav_view), errorMessage);
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
