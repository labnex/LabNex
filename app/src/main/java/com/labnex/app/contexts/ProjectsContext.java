package com.labnex.app.contexts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.ProjectsApi;
import com.labnex.app.database.models.Projects;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Utils;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author qwerty287
 * @author mmarif
 */
public class ProjectsContext implements Serializable {

	public static final String INTENT_EXTRA = "project";
	private final AccountContext account;
	private int projectId;
	private final String projectName;
	private final String path;
	private com.labnex.app.models.projects.Projects project;
	private State issueState = State.OPEN;
	private State prState = State.OPEN;
	private State milestoneState = State.OPEN;
	private String branchRef;
	private String issueMilestoneFilterName;
	private boolean starred = false;
	private boolean watched = false;
	private Projects projectModel = null;

	public ProjectsContext(com.labnex.app.models.projects.Projects project, Context context) {
		this.account = ((BaseActivity) context).getAccount();
		this.project = project;
		this.projectId = project.getId();
		this.projectName = project.getName();
		this.path = project.getPath();
	}

	public ProjectsContext(String projectName, String path, int projectId, Context context) {
		this.account = ((BaseActivity) context).getAccount();
		this.projectName = projectName;
		this.path = path;
		this.projectId = projectId;
	}

	public static ProjectsContext fromIntent(Intent intent) {
		return (ProjectsContext) intent.getSerializableExtra(INTENT_EXTRA);
	}

	public static ProjectsContext fromBundle(Bundle bundle) {
		return (ProjectsContext) bundle.getSerializable(INTENT_EXTRA);
	}

	public State getIssueState() {
		return issueState;
	}

	public void setIssueState(State issueState) {
		this.issueState = issueState;
	}

	public State getMilestoneState() {

		return milestoneState;
	}

	public void setMilestoneState(State milestoneState) {

		this.milestoneState = milestoneState;
	}

	public State getPrState() {

		return prState;
	}

	public void setPrState(State prState) {

		this.prState = prState;
	}

	public com.labnex.app.models.projects.Projects getProject() {

		return project;
	}

	public void setProject(com.labnex.app.models.projects.Projects project) {
		this.project = project;
	}

	public String getBranchRef() {

		return branchRef;
	}

	public void setBranchRef(String branchRef) {

		this.branchRef = branchRef;
	}

	public <T extends BaseActivity> Intent getIntent(Context context, Class<T> clazz) {
		Intent intent = new Intent(context, clazz);
		intent.putExtra(INTENT_EXTRA, this);
		return intent;
	}

	public Bundle getBundle() {
		Bundle bundle = new Bundle();
		bundle.putSerializable(INTENT_EXTRA, this);
		return bundle;
	}

	public String getIssueMilestoneFilterName() {

		return issueMilestoneFilterName;
	}

	public void setIssueMilestoneFilterName(String issueMilestoneFilterName) {

		this.issueMilestoneFilterName = issueMilestoneFilterName;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {

		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getPath() {
		return path;
	}

	public boolean isStarred() {

		return starred;
	}

	public void setStarred(boolean starred) {

		this.starred = starred;
	}

	public boolean isWatched() {

		return watched;
	}

	public void setWatched(boolean watched) {

		this.watched = watched;
	}

	public Projects getProjectModel() {

		return projectModel;
	}

	public void setProjectModel(Projects projectModel) {

		this.projectModel = projectModel;
	}

	public Projects loadProjectModel(Context context) {
		projectModel =
				Objects.requireNonNull(BaseApi.getInstance(context, ProjectsApi.class))
						.fetchByProjectId(projectId);
		return projectModel;
	}

	public void checkAccountSwitch(Context context) {
		if (((BaseActivity) context).getAccount().getAccount().getAccountId()
						!= account.getAccount().getAccountId()
				&& account.getAccount().getAccountId()
						== SharedPrefDB.getInstance(context).getInt("currentActiveAccountId")) {
			// user changed account using a deep link or a submodule
			Utils.switchToAccount(context, account.getAccount());
		}
	}

	public int saveToDB(Context context) {
		int currentActiveAccountId =
				SharedPrefDB.getInstance(context).getInt("currentActiveAccountId");
		ProjectsApi projectData = BaseApi.getInstance(context, ProjectsApi.class);

		assert projectData != null;
		Projects getMostVisitedValue =
				projectData.getProject(currentActiveAccountId, getProjectId());

		if (getMostVisitedValue == null) {
			long id =
					projectData.insertProject(
							currentActiveAccountId, getProjectId(), getProjectName(), getPath(), 1);
			Projects data = projectData.fetchProjectById((int) id);
			setProjectId(data.getProjectId());
			return data.getProjectId();
		} else {
			Projects data = projectData.getProject(currentActiveAccountId, getProjectId());
			setProjectId(data.getProjectId());
			projectData.updateProjectMostVisited(
					getMostVisitedValue.getMostVisited() + 1, data.getProjectId());
			return data.getProjectId();
		}
	}

	public void removeProject() {

		project = null;
	}

	public enum State {
		OPEN,
		CLOSED;

		@NonNull @Override
		public String toString() {
			if (this == OPEN) {
				return "open";
			}
			return "closed";
		}
	}
}
