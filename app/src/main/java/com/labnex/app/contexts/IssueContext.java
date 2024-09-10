package com.labnex.app.contexts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.projects.Projects;
import java.io.Serializable;

/**
 * @author qwerty287
 * @author mmarif
 */
public class IssueContext implements Serializable {

	public static final String INTENT_EXTRA = "issue";
	private final ProjectsContext projectsContext;
	private Issues issues;
	private int issueIndex = 0;
	private String issueType;

	public IssueContext(ProjectsContext projectsContext, int issueIndex, String issueType) {
		this.projectsContext = projectsContext;
		this.issueIndex = issueIndex;
		this.issueType = issueType;
	}

	/*public IssueContext(Issues issues, ProjectsContext projectsContext) {
		this.issues = issues;
		this.issueType = issues.getIssueType() == null ? "ISSUE" : "Pull";
		//this.pullRequest = pullRequest;
		this.projectsContext = projectsContext;
	}*/

	public IssueContext(ProjectsContext projectsContext) {
		this.issueType = "Pull";
		// this.pullRequest = pullRequest;
		this.projectsContext = projectsContext;
	}

	public IssueContext(Issues issues, ProjectsContext projectsContext) {
		this.issues = issues;
		this.issueType = issues.getIssueType() == null ? "ISSUE" : "Pull";

		this.projectsContext = projectsContext;
	}

	/*public IssueContext(
			Issues issues, Projects projects, Context context) {
		this.issues = issues;
		this.issueType = issues.getIssueType() == null ? "ISSUE" : "Pull";
		//this.pullRequest = pullRequest;

		this.projectsContext = new ProjectsContext(projects, context);
	}*/

	public IssueContext(Issues issues, Projects projects, Context context) {
		this.issues = issues;
		this.issueType = issues.getIssueType() == null ? "ISSUE" : "Pull";
		this.projectsContext = new ProjectsContext(projects, context);
	}

	public static IssueContext fromIntent(Intent intent) {
		return (IssueContext) intent.getSerializableExtra(INTENT_EXTRA);
	}

	public static IssueContext fromBundle(Bundle bundle) {
		return (IssueContext) bundle.getSerializable(INTENT_EXTRA);
	}

	public Issues getIssue() {

		return issues;
	}

	public void setIssue(Issues issues) {
		this.issues = issues;
		if (issues != null) {
			this.issueType = issues.getIssueType() == null ? "ISSUE" : "Pull";
		}
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

	public ProjectsContext getProjects() {

		return projectsContext;
	}

	public int getIssueIndex() {

		/*return Math.toIntExact(
		issueIndex != 0
				? issueIndex
				: issues != null ? issues.getNumber() : pullRequest.getNumber());*/
		return Math.toIntExact(
				issueIndex != 0 ? issueIndex : issues != null ? issues.getIid() : issueIndex);
	}

	public String getIssueType() {

		return issueType;
	}
}
