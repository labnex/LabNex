package com.labnex.app.models.todo;

import com.google.gson.annotations.SerializedName;

/**
 * @author mmarif
 */
public class Assignee {

	@SerializedName("avatar_url")
	private String avatarUrl;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("name")
	private String name;

	@SerializedName("id")
	private int id;

	@SerializedName("state")
	private String state;

	@SerializedName("username")
	private String username;

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public String getUsername() {
		return username;
	}
}
