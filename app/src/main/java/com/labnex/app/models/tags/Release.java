package com.labnex.app.models.tags;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Release implements Serializable {

	@SerializedName("tag_name")
	private String tagName;

	@SerializedName("description")
	private String description;

	public String getTagName() {
		return tagName;
	}

	public String getDescription() {
		return description;
	}
}
