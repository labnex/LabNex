package com.labnex.app.models.events;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Events implements Serializable {

	@SerializedName("note")
	private Note note;

	@SerializedName("author")
	private Author author;

	@SerializedName("push_data")
	private PushData pushData;

	@SerializedName("target_type")
	private String targetType;

	@SerializedName("imported_from")
	private String importedFrom;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("target_id")
	private String targetId;

	@SerializedName("target_iid")
	private String targetIid;

	@SerializedName("project_id")
	private long projectId;

	@SerializedName("action_name")
	private String actionName;

	@SerializedName("target_title")
	private String targetTitle;

	@SerializedName("imported")
	private boolean imported;

	@SerializedName("id")
	private long id;

	@SerializedName("author_id")
	private int authorId;

	@SerializedName("author_username")
	private String authorUsername;

	public Note getNote() {
		return note;
	}

	public Author getAuthor() {
		return author;
	}

	public PushData getPushData() {
		return pushData;
	}

	public String getTargetType() {
		return targetType;
	}

	public String getImportedFrom() {
		return importedFrom;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getTargetId() {
		return targetId;
	}

	public String getTargetIid() {
		return targetIid;
	}

	public long getProjectId() {
		return projectId;
	}

	public String getActionName() {
		return actionName;
	}

	public String getTargetTitle() {
		return targetTitle;
	}

	public boolean isImported() {
		return imported;
	}

	public long getId() {
		return id;
	}

	public int getAuthorId() {
		return authorId;
	}

	public String getAuthorUsername() {
		return authorUsername;
	}
}
