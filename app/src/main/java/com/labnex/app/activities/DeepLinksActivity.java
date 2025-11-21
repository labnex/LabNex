package com.labnex.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.IssueContext;
import com.labnex.app.contexts.MergeRequestContext;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.core.CoreApplication;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ActivityDeeplinksBinding;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.models.user.User;
import com.labnex.app.views.LoadingDotsTextView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author @mmarif
 */
public class DeepLinksActivity extends BaseActivity {

	private ActivityDeeplinksBinding viewBinding;
	private boolean accountFound = false;
	private Intent mainIntent;
	private Uri data;
	private LinearLayout loadingContainer;
	private LoadingDotsTextView fetchDataText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		viewBinding = ActivityDeeplinksBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		mainIntent = new Intent(ctx, MainActivity.class);

		loadingContainer = viewBinding.loadingContainer;
		fetchDataText = viewBinding.fetchData;

		Intent intent = getIntent();
		data = intent.getData();
		if (data == null) {
			startActivity(mainIntent);
			finish();
			return;
		}

		if ("labnex".equals(data.getScheme())) {
			StringBuilder httpsUrl = getStringBuilder();
			data = Uri.parse(httpsUrl.toString());
		}

		// check for login
		if (sharedPrefDB.getInt("currentActiveAccountId", -1) <= -1) {
			Intent loginIntent = new Intent(ctx, SignInActivity.class);
			loginIntent.putExtra("instanceUrl", data.getHost());
			ctx.startActivity(loginIntent);
			finish();
			return;
		}

		// Check if account exists
		checkAccountExists();
	}

	private void showLoading() {
		runOnUiThread(
				() -> {
					loadingContainer.setVisibility(View.VISIBLE);
					fetchDataText.startAnimation();
					fetchDataText.setAnimationDelay(300);
				});
	}

	private void hideLoading() {
		runOnUiThread(
				() -> {
					loadingContainer.setVisibility(View.GONE);
					fetchDataText.stopAnimation();
				});
	}

	private void checkAccountExists() {
		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
		assert userAccountsApi != null;
		List<UserAccount> userAccounts = userAccountsApi.usersAccounts();
		int accountsCount = userAccountsApi.getCount();

		for (UserAccount userAccount : userAccounts) {
			String hostUri = userAccount.getInstanceUrl();
			String hostExternal = data.getHost();
			int portExternal = data.getPort();

			String hostUrlExternal;
			if (portExternal > 0) {
				hostUrlExternal = hostExternal + ":" + portExternal;
			} else {
				hostUrlExternal = hostExternal;
			}

			if (hostUrlExternal == null) {
				hostUrlExternal = "";
			}

			if (hostUri.toLowerCase().contains(hostUrlExternal.toLowerCase())) {
				accountFound = true;
				Utils.switchToAccount(ctx, userAccount, false);
				break;
			}
		}

		if (accountFound) {
			processDeepLink();
		} else {
			showAccountNotFoundUI(accountsCount);
		}
	}

	private void processDeepLink() {
		List<String> pathSegments = data.getPathSegments();

		if (pathSegments.size() >= 5) {
			String sectionType = pathSegments.get(3);

			String userOrGroupName = pathSegments.get(0);
			String projectName = pathSegments.get(1);
			String projectPathWithNamespace = userOrGroupName + "/" + projectName;

			if ("issues".equals(sectionType)) {
				String issueIdStr = pathSegments.get(4);
				if (issueIdStr != null && issueIdStr.matches("\\d+")) {
					int issueIid = Integer.parseInt(issueIdStr);
					goToSpecificIssue(projectPathWithNamespace, issueIid);
				} else {
					goToProjectWithSection(projectPathWithNamespace, "issue");
				}
			} else if ("merge_requests".equals(sectionType)) {
				String mrIdStr = pathSegments.get(4);
				if (mrIdStr != null && mrIdStr.matches("\\d+")) {
					int mergeRequestIid = Integer.parseInt(mrIdStr);
					goToSpecificMergeRequest(projectPathWithNamespace, mergeRequestIid);
				} else {
					goToProjectWithSection(projectPathWithNamespace, "merge_request");
				}
			} else {
				goToProject(projectPathWithNamespace);
			}
		} else if (pathSegments.size() >= 2) {
			String userOrGroupName = pathSegments.get(0);
			String projectName = pathSegments.get(1);
			String projectPathWithNamespace = userOrGroupName + "/" + projectName;
			goToProject(projectPathWithNamespace);
		} else {
			startActivity(mainIntent);
			finish();
		}
	}

	private void goToSpecificIssue(String projectPathWithNamespace, int issueIid) {
		fetchProjectAndExecute(
				projectPathWithNamespace, project -> fetchAndOpenSpecificIssue(project, issueIid));
	}

	private void fetchAndOpenSpecificIssue(Projects project, int issueIid) {
		Call<Issues> call = RetrofitClient.getApiInterface(ctx).getIssue(project.getId(), issueIid);
		viewBinding.fetchData.setVisibility(View.VISIBLE);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Issues> call,
							@NonNull retrofit2.Response<Issues> response) {
						if (response.isSuccessful() && response.body() != null) {
							Issues issue = response.body();
							loadUserInfoAndOpenIssueDirectly(project, issue);
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

	private void goToSpecificMergeRequest(String projectPathWithNamespace, int mergeRequestIid) {
		fetchProjectAndExecute(
				projectPathWithNamespace,
				project -> fetchAndOpenSpecificMergeRequest(project, mergeRequestIid));
	}

	private void fetchAndOpenSpecificMergeRequest(Projects project, int mergeRequestIid) {
		Call<MergeRequests> call =
				RetrofitClient.getApiInterface(ctx)
						.getMergeRequest(project.getId(), mergeRequestIid);
		viewBinding.fetchData.setVisibility(View.VISIBLE);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<MergeRequests> call,
							@NonNull retrofit2.Response<MergeRequests> response) {
						if (response.isSuccessful() && response.body() != null) {
							MergeRequests mergeRequest = response.body();
							loadUserInfoAndOpenMergeRequestDirectly(project, mergeRequest);
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

	private void goToProject(String projectPathWithNamespace) {
		fetchProjectAndExecute(projectPathWithNamespace, this::goToProject);
	}

	private void goToProjectWithSection(String projectPathWithNamespace, String sectionType) {
		fetchProjectAndExecute(
				projectPathWithNamespace, project -> goToProjectWithSection(project, sectionType));
	}

	private void loadUserInfoAndOpenIssueDirectly(Projects project, Issues issue) {
		loadUserInfo(() -> createBackStackAndOpenIssue(project, issue));
	}

	private void loadUserInfoAndOpenMergeRequestDirectly(
			Projects project, MergeRequests mergeRequest) {
		loadUserInfo(() -> createBackStackAndOpenMergeRequest(project, mergeRequest));
	}

	private void goToProject(Projects project) {
		loadUserInfo(
				() -> {
					ProjectsContext projectContext = new ProjectsContext(project, ctx);
					projectContext.saveToDB(ctx);

					Intent projectIntent =
							projectContext.getIntent(ctx, ProjectDetailActivity.class);
					ctx.startActivity(projectIntent);
					finish();
				});
	}

	private void goToProjectWithSection(Projects project, String sectionType) {
		loadUserInfo(
				() -> {
					ProjectsContext projectContext = new ProjectsContext(project, ctx);
					projectContext.saveToDB(ctx);

					Intent projectIntent =
							projectContext.getIntent(ctx, ProjectDetailActivity.class);
					projectIntent.putExtra("goToSection", "yes");
					projectIntent.putExtra("goToSectionType", sectionType);
					ctx.startActivity(projectIntent);
					finish();
				});
	}

	private void createBackStackAndOpenIssue(Projects project, Issues issue) {
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

		finish();
	}

	private void createBackStackAndOpenMergeRequest(Projects project, MergeRequests mergeRequest) {
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

		finish();
	}

	private void fetchProjectAndExecute(String projectPathWithNamespace, ProjectAction action) {

		showLoading();

		Call<Projects> call =
				RetrofitClient.getApiInterface(ctx).getProjectByPath(projectPathWithNamespace);
		viewBinding.fetchData.setVisibility(View.VISIBLE);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Projects> call,
							@NonNull retrofit2.Response<Projects> response) {
						if (response.isSuccessful() && response.body() != null) {
							Projects project = response.body();
							action.execute(project);
						} else {
							hideLoading();
							fallbackToMainActivity();
						}
					}

					@Override
					public void onFailure(@NonNull Call<Projects> call, @NonNull Throwable t) {
						hideLoading();
						fallbackToMainActivity();
					}
				});
	}

	private void loadUserInfo(Runnable onSuccess) {
		Call<User> call = RetrofitClient.getApiInterface(ctx).getCurrentUser();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {
						if (response.isSuccessful()
								&& response.code() == 200
								&& response.body() != null) {
							User userDetails = response.body();
							CoreApplication app = (CoreApplication) getApplication();
							if (app.currentAccount != null) {
								app.currentAccount.setUserInfo(userDetails);
								onSuccess.run();
							} else {
								fallbackToMainActivity();
							}
						} else {
							fallbackToMainActivity();
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						fallbackToMainActivity();
					}
				});
	}

	private void fallbackToMainActivity() {
		startActivity(mainIntent);
		finish();
	}

	private void showAccountNotFoundUI(int accountsCount) {
		viewBinding.progressBar.setVisibility(View.GONE);
		viewBinding.addNewAccountFrame.setVisibility(View.VISIBLE);
		viewBinding.othersFrame.setVisibility(View.GONE);

		if (accountsCount > 0) {
			viewBinding.goToApp.setVisibility(View.VISIBLE);
			viewBinding.goToApp.setOnClickListener(
					goToApp -> {
						startActivity(mainIntent);
						finish();
					});
		}

		viewBinding.addAccountText.setText(
				String.format(
						getResources().getString(R.string.account_does_not_exist), data.getHost()));

		viewBinding.addNewAccount.setOnClickListener(
				addNewAccount -> {
					Intent accountIntent = new Intent(ctx, SignInActivity.class);
					accountIntent.putExtra("instanceUrl", data.getHost());
					accountIntent.putExtra("source", "add_account");
					startActivity(accountIntent);
					finish();
				});
	}

	@NonNull private StringBuilder getStringBuilder() {
		String originalHost = data.getHost();
		String originalPath = data.getPath();
		String query = data.getQuery();
		String fragment = data.getFragment();

		StringBuilder httpsUrl = new StringBuilder("https://");
		httpsUrl.append(originalHost);

		if (originalPath != null) {
			httpsUrl.append(originalPath);
		}

		if (query != null) {
			httpsUrl.append("?").append(query);
		}

		if (fragment != null) {
			httpsUrl.append("#").append(fragment);
		}
		return httpsUrl;
	}

	private interface ProjectAction {
		void execute(Projects project);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (fetchDataText != null) {
			fetchDataText.stopAnimation();
		}
	}
}
