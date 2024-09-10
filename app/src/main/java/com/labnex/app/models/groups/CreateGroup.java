package com.labnex.app.models.groups;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class CreateGroup implements Serializable {

	@SerializedName("name")
	private String name;

	@SerializedName("description")
	private String description;

	@SerializedName("path")
	private String path;

	@SerializedName("visibility")
	private String visibility;

	public CreateGroup name(String name) {
		this.name = name;
		return this;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CreateGroup description(String description) {
		this.description = description;
		return this;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CreateGroup path(String path) {
		this.path = path;
		return this;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public CreateGroup visibility(String visibility) {
		this.visibility = visibility;
		return this;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
}
