package com.labnex.app.models.events;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class PushData implements Serializable {

	@SerializedName("commit_count")
	private long commitCount;

	@SerializedName("ref")
	private String ref;

	@SerializedName("commit_title")
	private String commitTitle;

	@SerializedName("commit_from")
	private String commitFrom;

	@SerializedName("action")
	private String action;

	@SerializedName("ref_type")
	private String refType;

	@SerializedName("ref_count")
	private Object refCount;

	@SerializedName("commit_to")
	private String commitTo;

	public long getCommitCount() {
		return commitCount;
	}

	public String getRef() {
		return ref;
	}

	public String getCommitTitle() {
		return commitTitle;
	}

	public String getCommitFrom() {
		return commitFrom;
	}

	public String getAction() {
		return action;
	}

	public String getRefType() {
		return refType;
	}

	public Object getRefCount() {
		return refCount;
	}

	public String getCommitTo() {
		return commitTo;
	}
}
