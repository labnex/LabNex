package com.labnex.app.models.todo;

import com.google.gson.annotations.SerializedName;

/**
 * @author mmarif
 */
public class Project {

	@SerializedName("path")
	private String path;

	@SerializedName("path_with_namespace")
	private String pathWithNamespace;

	@SerializedName("name")
	private String name;

	@SerializedName("id")
	private long id;

	@SerializedName("name_with_namespace")
	private String nameWithNamespace;

	public String getPath() {
		return path;
	}

	public String getPathWithNamespace() {
		return pathWithNamespace;
	}

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}

	public String getNameWithNamespace() {
		return nameWithNamespace;
	}
}
