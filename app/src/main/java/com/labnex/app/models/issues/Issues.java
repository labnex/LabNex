package com.labnex.app.models.issues;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Issues implements Serializable {

	@SerializedName("discussion_locked")
	private boolean discussionLocked;

	@SerializedName("upvotes")
	private int upvotes;

	@SerializedName("references")
	private References references;

	@SerializedName("iid")
	private int iid;

	@SerializedName("merge_requests_count")
	private int mergeRequestsCount;

	@SerializedName("_links")
	private Links links;

	@SerializedName("description")
	private String description;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("assignees")
	private List<AssigneesItem> assignees;

	@SerializedName("title")
	private String title;

	@SerializedName("type")
	private String type;

	@SerializedName("closed_by")
	private ClosedBy closedBy;

	@SerializedName("has_tasks")
	private boolean hasTasks;

	@SerializedName("service_desk_reply_to")
	private Object serviceDeskReplyTo;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("project_id")
	private int projectId;

	@SerializedName("imported")
	private boolean imported;

	@SerializedName("epic_iid")
	private Object epicIid;

	@SerializedName("time_stats")
	private TimeStats timeStats;

	@SerializedName("iteration")
	private Object iteration;

	@SerializedName("id")
	private int id;

	@SerializedName("state")
	private String state;

	@SerializedName("health_status")
	private Object healthStatus;

	@SerializedName("confidential")
	private boolean confidential;

	@SerializedName("severity")
	private String severity;

	@SerializedName("task_status")
	private String taskStatus;

	@SerializedName("closed_at")
	private String closedAt;

	@SerializedName("author")
	private Author author;

	@SerializedName("due_date")
	private Object dueDate;

	@SerializedName("issue_type")
	private String issueType;

	@SerializedName("imported_from")
	private String importedFrom;

	@SerializedName("weight")
	private Object weight;

	@SerializedName("epic")
	private Object epic;

	@SerializedName("downvotes")
	private int downvotes;

	@SerializedName("blocking_issues_count")
	private int blockingIssuesCount;

	@SerializedName("labels")
	private List<Object> labels;

	@SerializedName("moved_to_id")
	private Object movedToId;

	@SerializedName("milestone")
	private Milestone milestone;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("user_notes_count")
	private int userNotesCount;

	@SerializedName("assignee")
	private Assignee assignee;

	@SerializedName("task_completion_status")
	private TaskCompletionStatus taskCompletionStatus;

	public boolean getDiscussionLocked() {
		return discussionLocked;
	}

	public int getUpvotes() {
		return upvotes;
	}

	public References getReferences() {
		return references;
	}

	public int getIid() {
		return iid;
	}

	public int getMergeRequestsCount() {
		return mergeRequestsCount;
	}

	public Links getLinks() {
		return links;
	}

	public String getDescription() {
		return description;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public List<AssigneesItem> getAssignees() {
		return assignees;
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return type;
	}

	public ClosedBy getClosedBy() {
		return closedBy;
	}

	public boolean isHasTasks() {
		return hasTasks;
	}

	public Object getServiceDeskReplyTo() {
		return serviceDeskReplyTo;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public int getProjectId() {
		return projectId;
	}

	public boolean isImported() {
		return imported;
	}

	public Object getEpicIid() {
		return epicIid;
	}

	public TimeStats getTimeStats() {
		return timeStats;
	}

	public Object getIteration() {
		return iteration;
	}

	public int getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public Object getHealthStatus() {
		return healthStatus;
	}

	public boolean isConfidential() {
		return confidential;
	}

	public String getSeverity() {
		return severity;
	}

	public String getTaskStatus() {
		return taskStatus;
	}

	public String getClosedAt() {
		return closedAt;
	}

	public Author getAuthor() {
		return author;
	}

	public Object getDueDate() {
		return dueDate;
	}

	public String getIssueType() {
		return issueType;
	}

	public String getImportedFrom() {
		return importedFrom;
	}

	public Object getWeight() {
		return weight;
	}

	public Object getEpic() {
		return epic;
	}

	public int getDownvotes() {
		return downvotes;
	}

	public int getBlockingIssuesCount() {
		return blockingIssuesCount;
	}

	public List<Object> getLabels() {
		return labels;
	}

	public Object getMovedToId() {
		return movedToId;
	}

	public Milestone getMilestone() {
		return milestone;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public int getUserNotesCount() {
		return userNotesCount;
	}

	public Assignee getAssignee() {
		return assignee;
	}

	public TaskCompletionStatus getTaskCompletionStatus() {
		return taskCompletionStatus;
	}
}
