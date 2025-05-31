package com.labnex.app.models.tags;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Commit implements Serializable {

	@SerializedName("author_name")
	private String authorName;

	@SerializedName("authored_date")
	private String authoredDate;

	@SerializedName("committer_email")
	private String committerEmail;

	@SerializedName("committed_date")
	private String committedDate;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("author_email")
	private String authorEmail;

	@SerializedName("id")
	private String id;

	@SerializedName("short_id")
	private String shortId;

	@SerializedName("title")
	private String title;

	@SerializedName("parent_ids")
	private List<String> parentIds;

	@SerializedName("message")
	private String message;

	@SerializedName("committer_name")
	private String committerName;

	public String getAuthorName() {
		return authorName;
	}

	public String getAuthoredDate() {
		return authoredDate;
	}

	public String getCommitterEmail() {
		return committerEmail;
	}

	public String getCommittedDate() {
		return committedDate;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public String getId() {
		return id;
	}

	public String getShortId() {
		return shortId;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getParentIds() {
		return parentIds;
	}

	public String getMessage() {
		return message;
	}

	public String getCommitterName() {
		return committerName;
	}
}
