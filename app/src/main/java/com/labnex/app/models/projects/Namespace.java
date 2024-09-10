package com.labnex.app.models.projects;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Namespace implements Serializable {

	@SerializedName("path")
	private String path;

	@SerializedName("avatar_url")
	private Object avatarUrl;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("kind")
	private String kind;

	@SerializedName("parent_id")
	private Object parentId;

	@SerializedName("name")
	private String name;

	@SerializedName("id")
	private int id;

	@SerializedName("full_path")
	private String fullPath;

	public String getPath() {
		return path;
	}

	public Object getAvatarUrl() {
		return avatarUrl;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public String getKind() {
		return kind;
	}

	public Object getParentId() {
		return parentId;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public String getFullPath() {
		return fullPath;
	}
}
