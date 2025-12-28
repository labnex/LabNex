package com.labnex.app.models.todo;

import com.google.gson.annotations.SerializedName;

/**
 * @author mmarif
 */
public class Milestone {

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("iid")
	private long iid;

	@SerializedName("project_id")
	private long projectId;

	@SerializedName("due_date")
	private Object dueDate;

	@SerializedName("description")
	private String description;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("id")
	private long id;

	@SerializedName("state")
	private String state;

	@SerializedName("title")
	private String title;

	public String getUpdatedAt() {
		return updatedAt;
	}

	public long getIid() {
		return iid;
	}

	public long getProjectId() {
		return projectId;
	}

	public Object getDueDate() {
		return dueDate;
	}

	public String getDescription() {
		return description;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public long getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public String getTitle() {
		return title;
	}
}
