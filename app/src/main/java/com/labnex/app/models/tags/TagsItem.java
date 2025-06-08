package com.labnex.app.models.tags;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class TagsItem implements Serializable {

	@SerializedName("protected")
	private boolean jsonMemberProtected;

	@SerializedName("release")
	private Release release;

	@SerializedName("commit")
	private Commit commit;

	@SerializedName("name")
	private String name;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("message")
	private Object message;

	@SerializedName("target")
	private String target;

	public boolean isJsonMemberProtected() {
		return jsonMemberProtected;
	}

	public Release getRelease() {
		return release;
	}

	public Commit getCommit() {
		return commit;
	}

	public String getName() {
		return name;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public Object getMessage() {
		return message;
	}

	public String getTarget() {
		return target;
	}
}
