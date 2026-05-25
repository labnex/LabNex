package com.labnex.app.models.merge_requests;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class CrudeMergeRequest implements Serializable {

	@SerializedName("title")
	private String title;

	@SerializedName("description")
	private String description;

	@SerializedName("source_branch")
	private String sourceBranch;

	@SerializedName("target_branch")
	private String targetBranch;

	@SerializedName("should_remove_source_branch")
	private boolean shouldRemoveSourceBranch;

	@SerializedName("squash")
	private boolean squash;

	@SerializedName("state_event")
	private String stateEvent;

	@SerializedName("labels")
	private List<String> labels;

	@SerializedName("milestone_id")
	private Long milestoneId;

	@SerializedName("remove_source_branch")
	private boolean removeSourceBranch;

	@SerializedName("target_project_id")
	private Long targetProjectId;

	@SerializedName("discussion_locked")
	private Boolean discussionLocked;

	public void setDiscussionLocked(Boolean discussionLocked) {
		this.discussionLocked = discussionLocked;
	}

	public void setTargetProjectId(Long targetProjectId) {
		this.targetProjectId = targetProjectId;
	}

	public CrudeMergeRequest title(String title) {
		this.title = title;
		return this;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public CrudeMergeRequest description(String description) {
		this.description = description;
		return this;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CrudeMergeRequest sourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
		return this;
	}

	public void setSourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
	}

	public CrudeMergeRequest targetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
		return this;
	}

	public void setTargetBranch(String targetBranch) {
		this.targetBranch = targetBranch;
	}

	public CrudeMergeRequest shouldRemoveSourceBranch(boolean shouldRemoveSourceBranch) {
		this.shouldRemoveSourceBranch = shouldRemoveSourceBranch;
		return this;
	}

	public void setShouldRemoveSourceBranch(boolean shouldRemoveSourceBranch) {
		this.shouldRemoveSourceBranch = shouldRemoveSourceBranch;
	}

	public CrudeMergeRequest squash(boolean squash) {
		this.squash = squash;
		return this;
	}

	public void setSquash(boolean squash) {
		this.squash = squash;
	}

	public CrudeMergeRequest stateEvent(String stateEvent) {
		this.stateEvent = stateEvent;
		return this;
	}

	public void setStateEvent(String stateEvent) {
		this.stateEvent = stateEvent;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public void setMilestoneId(Long milestoneId) {
		this.milestoneId = milestoneId;
	}

	public void setRemoveSourceBranch(boolean removeSourceBranch) {
		this.removeSourceBranch = removeSourceBranch;
	}
}
