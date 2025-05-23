package com.labnex.app.models.snippets;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Author implements Serializable {

	@SerializedName("avatar_url")
	private String avatarUrl;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("name")
	private String name;

	@SerializedName("id")
	private int id;

	@SerializedName("public_email")
	private String publicEmail;

	@SerializedName("state")
	private String state;

	@SerializedName("locked")
	private boolean locked;

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

	public String getPublicEmail() {
		return publicEmail;
	}

	public String getState() {
		return state;
	}

	public boolean isLocked() {
		return locked;
	}

	public String getUsername() {
		return username;
	}
}
