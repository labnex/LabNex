package com.labnex.app.models.milestone;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class CrudeMilestone implements Serializable {

	@SerializedName("title")
	private String title;

	@SerializedName("description")
	private String description;

	@SerializedName("due_date")
	private String due_date;

	@SerializedName("start_date")
	private String start_date;

	public CrudeMilestone name(String title) {
		this.title = title;
		return this;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public CrudeMilestone description(String description) {
		this.description = description;
		return this;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CrudeMilestone due_date(String due_date) {
		this.due_date = due_date;
		return this;
	}

	public void setDueDate(String due_date) {
		this.due_date = due_date;
	}

	public CrudeMilestone start_date(String start_date) {
		this.start_date = start_date;
		return this;
	}

	public void setStartDate(String start_date) {
		this.start_date = start_date;
	}
}
