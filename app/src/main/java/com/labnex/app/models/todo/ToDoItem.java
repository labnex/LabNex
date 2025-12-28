package com.labnex.app.models.todo;

import com.google.gson.annotations.SerializedName;

/**
 * @author mmarif
 */
public class ToDoItem {

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("author")
	private Author author;

	@SerializedName("action_name")
	private String actionName;

	@SerializedName("target_url")
	private String targetUrl;

	@SerializedName("target_type")
	private String targetType;

	@SerializedName("project")
	private Project project;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("id")
	private long id;

	@SerializedName("state")
	private String state;

	@SerializedName("body")
	private String body;

	@SerializedName("target")
	private Target target;

	public String getUpdatedAt() {
		return updatedAt;
	}

	public Author getAuthor() {
		return author;
	}

	public String getActionName() {
		return actionName;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public String getTargetType() {
		return targetType;
	}

	public Project getProject() {
		return project;
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

	public String getBody() {
		return body;
	}

	public Target getTarget() {
		return target;
	}
}
