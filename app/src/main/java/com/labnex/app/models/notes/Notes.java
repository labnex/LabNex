package com.labnex.app.models.notes;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Notes implements Serializable {

	@SerializedName("noteable_id")
	private Long noteableId;

	@SerializedName("internal")
	private boolean internal;

	@SerializedName("author")
	private Author author;

	@SerializedName("imported_from")
	private String importedFrom;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("type")
	private String type;

	@SerializedName("body")
	private String body;

	@SerializedName("commands_changes")
	private CommandsChanges commandsChanges;

	@SerializedName("noteable_iid")
	private Long noteableIid;

	@SerializedName("system")
	private boolean system;

	@SerializedName("attachment")
	private String attachment;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("project_id")
	private int projectId;

	@SerializedName("resolvable")
	private boolean resolvable;

	@SerializedName("noteable_type")
	private String noteableType;

	@SerializedName("imported")
	private boolean imported;

	@SerializedName("id")
	private Long id;

	@SerializedName("confidential")
	private boolean confidential;

	public long getNoteableId() {
		return noteableId;
	}

	public boolean isInternal() {
		return internal;
	}

	public Author getAuthor() {
		return author;
	}

	public String getImportedFrom() {
		return importedFrom;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getType() {
		return type;
	}

	public String getBody() {
		return body;
	}

	public CommandsChanges getCommandsChanges() {
		return commandsChanges;
	}

	public long getNoteableIid() {
		return noteableIid;
	}

	public boolean isSystem() {
		return system;
	}

	public String getAttachment() {
		return attachment;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public int getProjectId() {
		return projectId;
	}

	public boolean isResolvable() {
		return resolvable;
	}

	public String getNoteableType() {
		return noteableType;
	}

	public boolean isImported() {
		return imported;
	}

	public Long getId() {
		return id;
	}

	public boolean isConfidential() {
		return confidential;
	}
}
