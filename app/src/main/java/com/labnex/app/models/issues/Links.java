package com.labnex.app.models.issues;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Links implements Serializable {

	@SerializedName("notes")
	private String notes;

	@SerializedName("self")
	private String self;

	@SerializedName("award_emoji")
	private String awardEmoji;

	@SerializedName("project")
	private String project;

	@SerializedName("closed_as_duplicate_of")
	private Object closedAsDuplicateOf;

	public String getNotes() {
		return notes;
	}

	public String getSelf() {
		return self;
	}

	public String getAwardEmoji() {
		return awardEmoji;
	}

	public String getProject() {
		return project;
	}

	public Object getClosedAsDuplicateOf() {
		return closedAsDuplicateOf;
	}
}
