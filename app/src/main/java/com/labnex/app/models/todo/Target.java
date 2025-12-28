package com.labnex.app.models.todo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author mmarif
 */
public class Target {

	@SerializedName("work_in_progress")
	private boolean workInProgress;

	@SerializedName("upvotes")
	private int upvotes;

	@SerializedName("merge_when_pipeline_succeeds")
	private boolean mergeWhenPipelineSucceeds;

	@SerializedName("iid")
	private int iid;

	@SerializedName("author")
	private Author author;

	@SerializedName("description")
	private String description;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("target_branch")
	private String targetBranch;

	@SerializedName("source_project_id")
	private int sourceProjectId;

	@SerializedName("title")
	private String title;

	@SerializedName("downvotes")
	private int downvotes;

	@SerializedName("source_branch")
	private String sourceBranch;

	@SerializedName("labels")
	private List<Object> labels;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("milestone")
	private Milestone milestone;

	@SerializedName("project_id")
	private long projectId;

	@SerializedName("merge_status")
	private String mergeStatus;

	@SerializedName("draft")
	private boolean draft;

	@SerializedName("user_notes_count")
	private int userNotesCount;

	@SerializedName("id")
	private long id;

	@SerializedName("state")
	private String state;

	@SerializedName("assignee")
	private Assignee assignee;

	@SerializedName("target_project_id")
	private long targetProjectId;

	public boolean isWorkInProgress() {
		return workInProgress;
	}

	public int getUpvotes() {
		return upvotes;
	}

	public boolean isMergeWhenPipelineSucceeds() {
		return mergeWhenPipelineSucceeds;
	}

	public int getIid() {
		return iid;
	}

	public Author getAuthor() {
		return author;
	}

	public String getDescription() {
		return description;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getTargetBranch() {
		return targetBranch;
	}

	public int getSourceProjectId() {
		return sourceProjectId;
	}

	public String getTitle() {
		return title;
	}

	public int getDownvotes() {
		return downvotes;
	}

	public String getSourceBranch() {
		return sourceBranch;
	}

	public List<Object> getLabels() {
		return labels;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public long getProjectId() {
		return projectId;
	}

	public String getMergeStatus() {
		return mergeStatus;
	}

	public boolean isDraft() {
		return draft;
	}

	public int getUserNotesCount() {
		return userNotesCount;
	}

	public long getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public Assignee getAssignee() {
		return assignee;
	}

	public long getTargetProjectId() {
		return targetProjectId;
	}
}
