package com.labnex.app.models.release;

import com.google.gson.annotations.SerializedName;

/**
 * @author mmarif
 */
public class CrudeRelease {
	@SerializedName("tag_name")
	private String tagName;

	@SerializedName("name")
	private String name;

	@SerializedName("ref")
	private String ref;

	@SerializedName("description")
	private String description;

	@SerializedName("released_at")
	private String releasedAt;

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public void setReleasedAt(String releasedAt) {
		this.releasedAt = releasedAt;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setRef(String ref) {
		this.ref = ref;
	}

	public void setName(String name) {
		this.name = name;
	}
}
