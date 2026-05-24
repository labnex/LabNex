package com.labnex.app.models.issues;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Milestone implements Serializable {

	@SerializedName("expired")
	private boolean expired;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("iid")
	private long iid;

	@SerializedName("project_id")
	private long projectId;

	@SerializedName("due_date")
	private String dueDate;

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

	@SerializedName("start_date")
	private String startDate;

	public boolean isExpired() {
		return expired;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public long getIid() {
		return iid;
	}

	public long getProjectId() {
		return projectId;
	}

	public String getDueDate() {
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

	public String getStartDate() {
		return startDate;
	}
}
