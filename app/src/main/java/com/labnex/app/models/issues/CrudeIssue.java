package com.labnex.app.models.issues;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

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
