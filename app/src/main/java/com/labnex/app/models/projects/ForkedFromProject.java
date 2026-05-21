package com.labnex.app.models.projects;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class ForkedFromProject implements Serializable {

	@SerializedName("id")
	private int id;

	@SerializedName("name")
	private String name;

	@SerializedName("path")
	private String path;

	@SerializedName("path_with_namespace")
	private String pathWithNamespace;

	@SerializedName("name_with_namespace")
	private String nameWithNamespace;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("default_branch")
	private String defaultBranch;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public String getPathWithNamespace() {
		return pathWithNamespace;
	}

	public String getNameWithNamespace() {
		return nameWithNamespace;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public String getDefaultBranch() {
		return defaultBranch;
	}
}
