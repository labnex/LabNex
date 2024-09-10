package com.labnex.app.models.release;

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

	@SerializedName("extended_trailers")
	private ExtendedTrailers extendedTrailers;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("short_id")
	private String shortId;

	@SerializedName("parent_ids")
	private List<Object> parentIds;

	@SerializedName("title")
	private String title;

	@SerializedName("message")
	private String message;

	@SerializedName("committer_name")
	private String committerName;

	@SerializedName("trailers")
	private Trailers trailers;

	@SerializedName("committed_date")
	private String committedDate;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("author_email")
	private String authorEmail;

	@SerializedName("id")
	private String id;

	public String getAuthorName() {
		return authorName;
	}

	public String getAuthoredDate() {
		return authoredDate;
	}

	public String getCommitterEmail() {
		return committerEmail;
	}

	public ExtendedTrailers getExtendedTrailers() {
		return extendedTrailers;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getShortId() {
		return shortId;
	}

	public List<Object> getParentIds() {
		return parentIds;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public String getCommitterName() {
		return committerName;
	}

	public Trailers getTrailers() {
		return trailers;
	}

	public String getCommittedDate() {
		return committedDate;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public String getId() {
		return id;
	}
}
