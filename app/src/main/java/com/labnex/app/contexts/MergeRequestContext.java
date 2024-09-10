package com.labnex.app.contexts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.Projects;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class MergeRequestContext implements Serializable {

	public static final String INTENT_EXTRA = "mr";
	private final ProjectsContext projectsContext;
	private MergeRequests mergeRequests;
	private int mergeRequestIndex = 0;

	public MergeRequestContext(ProjectsContext projectsContext, int mergeRequestIndex) {
		this.projectsContext = projectsContext;
		this.mergeRequestIndex = mergeRequestIndex;
	}

	public MergeRequestContext(ProjectsContext projectsContext) {
		this.projectsContext = projectsContext;
	}

	public MergeRequestContext(MergeRequests mergeRequests, ProjectsContext projectsContext) {
		this.mergeRequests = mergeRequests;
		this.projectsContext = projectsContext;
	}

	public MergeRequestContext(MergeRequests mergeRequests, Projects projects, Context context) {
		this.mergeRequests = mergeRequests;

		this.projectsContext = new ProjectsContext(projects, context);
	}

	public static MergeRequestContext fromIntent(Intent intent) {
		return (MergeRequestContext) intent.getSerializableExtra(INTENT_EXTRA);
	}

	public static MergeRequestContext fromBundle(Bundle bundle) {
		return (MergeRequestContext) bundle.getSerializable(INTENT_EXTRA);
	}

	public MergeRequests getMergeRequest() {

		return mergeRequests;
	}

	public void setMergeRequest(MergeRequests mergeRequests) {
		this.mergeRequests = mergeRequests;
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

	public int getMergeRequestIndex() {

		return Math.toIntExact(
				mergeRequestIndex != 0
						? mergeRequestIndex
						: mergeRequests != null ? mergeRequests.getIid() : mergeRequestIndex);
	}
}
