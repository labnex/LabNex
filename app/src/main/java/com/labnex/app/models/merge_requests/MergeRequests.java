package com.labnex.app.models.merge_requests;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class MergeRequests implements Serializable {

	@SerializedName("discussion_locked")
	private boolean discussionLocked;

	@SerializedName("upvotes")
	private int upvotes;

	@SerializedName("merge_when_pipeline_succeeds")
	private boolean mergeWhenPipelineSucceeds;

	@SerializedName("references")
	private References references;

	@SerializedName("merge_user")
	private Object mergeUser;

	@SerializedName("assignees")
	private List<AssigneesItem> assignees;

	@SerializedName("reference")
	private String reference;

	@SerializedName("squash")
	private boolean squash;

	@SerializedName("project_id")
	private int projectId;

	@SerializedName("draft")
	private boolean draft;

	@SerializedName("time_stats")
	private TimeStats timeStats;

	@SerializedName("id")
	private int id;

	@SerializedName("state")
	private String state;

	@SerializedName("closed_at")
	private Object closedAt;

	@SerializedName("work_in_progress")
	private boolean workInProgress;

	@SerializedName("squash_on_merge")
	private boolean squashOnMerge;

	@SerializedName("author")
	private Author author;

	@SerializedName("merged_at")
	private Object mergedAt;

	@SerializedName("imported_from")
	private String importedFrom;

	@SerializedName("target_branch")
	private String targetBranch;

	@SerializedName("downvotes")
	private int downvotes;

	@SerializedName("should_remove_source_branch")
	private Object shouldRemoveSourceBranch;

	@SerializedName("sha")
	private String sha;

	@SerializedName("labels")
	private List<String> labels;

	@SerializedName("user_notes_count")
	private int userNotesCount;

	@SerializedName("squash_commit_sha")
	private Object squashCommitSha;

	@SerializedName("assignee")
	private Assignee assignee;

	@SerializedName("prepared_at")
	private String preparedAt;

	@SerializedName("task_completion_status")
	private TaskCompletionStatus taskCompletionStatus;

	@SerializedName("approvals_before_merge")
	private Object approvalsBeforeMerge;

	@SerializedName("target_project_id")
	private int targetProjectId;

	@SerializedName("force_remove_source_branch")
	private boolean forceRemoveSourceBranch;

	@SerializedName("has_conflicts")
	private boolean hasConflicts;

	@SerializedName("detailed_merge_status")
	private String detailedMergeStatus;

	@SerializedName("iid")
	private int iid;

	@SerializedName("description")
	private String description;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("merged_by")
	private Object mergedBy;

	@SerializedName("title")
	private String title;

	@SerializedName("closed_by")
	private Object closedBy;

	@SerializedName("source_branch")
	private String sourceBranch;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("imported")
	private boolean imported;

	@SerializedName("merge_commit_sha")
	private Object mergeCommitSha;

	@SerializedName("blocking_discussions_resolved")
	private boolean blockingDiscussionsResolved;

	@SerializedName("source_project_id")
	private int sourceProjectId;

	@SerializedName("reviewers")
	private List<ReviewersItem> reviewers;

	@SerializedName("milestone")
	private Milestone milestone;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("merge_status")
	private String mergeStatus;

	public boolean getDiscussionLocked() {
		return discussionLocked;
	}

	public int getUpvotes() {
		return upvotes;
	}

	public boolean isMergeWhenPipelineSucceeds() {
		return mergeWhenPipelineSucceeds;
	}

	public References getReferences() {
		return references;
	}

	public Object getMergeUser() {
		return mergeUser;
	}

	public List<AssigneesItem> getAssignees() {
		return assignees;
	}

	public String getReference() {
		return reference;
	}

	public boolean isSquash() {
		return squash;
	}

	public int getProjectId() {
		return projectId;
	}

	public boolean isDraft() {
		return draft;
	}

	public TimeStats getTimeStats() {
		return timeStats;
	}

	public int getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public Object getClosedAt() {
		return closedAt;
	}

	public boolean isWorkInProgress() {
		return workInProgress;
	}

	public boolean isSquashOnMerge() {
		return squashOnMerge;
	}

	public Author getAuthor() {
		return author;
	}

	public Object getMergedAt() {
		return mergedAt;
	}

	public String getImportedFrom() {
		return importedFrom;
	}

	public String getTargetBranch() {
		return targetBranch;
	}

	public int getDownvotes() {
		return downvotes;
	}

	public Object getShouldRemoveSourceBranch() {
		return shouldRemoveSourceBranch;
	}

	public String getSha() {
		return sha;
	}

	public List<String> getLabels() {
		return labels;
	}

	public int getUserNotesCount() {
		return userNotesCount;
	}

	public Object getSquashCommitSha() {
		return squashCommitSha;
	}

	public Assignee getAssignee() {
		return assignee;
	}

	public String getPreparedAt() {
		return preparedAt;
	}

	public TaskCompletionStatus getTaskCompletionStatus() {
		return taskCompletionStatus;
	}

	public Object getApprovalsBeforeMerge() {
		return approvalsBeforeMerge;
	}

	public int getTargetProjectId() {
		return targetProjectId;
	}

	public boolean isForceRemoveSourceBranch() {
		return forceRemoveSourceBranch;
	}

	public boolean isHasConflicts() {
		return hasConflicts;
	}

	public String getDetailedMergeStatus() {
		return detailedMergeStatus;
	}

	public int getIid() {
		return iid;
	}

	public String getDescription() {
		return description;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public Object getMergedBy() {
		return mergedBy;
	}

	public String getTitle() {
		return title;
	}

	public Object getClosedBy() {
		return closedBy;
	}

	public String getSourceBranch() {
		return sourceBranch;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public boolean isImported() {
		return imported;
	}

	public Object getMergeCommitSha() {
		return mergeCommitSha;
	}

	public boolean isBlockingDiscussionsResolved() {
		return blockingDiscussionsResolved;
	}

	public int getSourceProjectId() {
		return sourceProjectId;
	}

	public List<ReviewersItem> getReviewers() {
		return reviewers;
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public String getMergeStatus() {
		return mergeStatus;
	}
}
