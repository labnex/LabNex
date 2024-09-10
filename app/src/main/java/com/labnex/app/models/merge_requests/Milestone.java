package com.labnex.app.models.merge_requests;

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
	private int iid;

	@SerializedName("group_id")
	private int groupId;

	@SerializedName("due_date")
	private String dueDate;

	@SerializedName("description")
	private String description;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("id")
	private int id;

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

	public int getIid() {
		return iid;
	}

	public int getGroupId() {
		return groupId;
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

	public int getId() {
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
