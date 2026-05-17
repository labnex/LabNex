package com.labnex.app.models.issues;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class CrudeIssue implements Serializable {

	@SerializedName("title")
	private String title;

	@SerializedName("description")
	private String description;

	@SerializedName("state_event")
	private String stateEvent;

	@SerializedName("confidential")
	private boolean confidential;

	@SerializedName("due_date")
	private String dueDate;

	@SerializedName("labels")
	private List<String> labels;

	@SerializedName("milestone_id")
	private Long milestoneId;

	@SerializedName("weight")
	private Integer weight;

	public void setConfidential(boolean confidential) {
		this.confidential = confidential;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public void setMilestoneId(Long milestoneId) {
		this.milestoneId = milestoneId;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public CrudeIssue title(String title) {
		this.title = title;
		return this;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public CrudeIssue description(String description) {
		this.description = description;
		return this;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CrudeIssue stateEvent(String stateEvent) {
		this.stateEvent = stateEvent;
		return this;
	}

	public void setStateEvent(String stateEvent) {
		this.stateEvent = stateEvent;
	}
}
