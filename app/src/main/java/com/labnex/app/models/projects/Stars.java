package com.labnex.app.models.projects;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Stars implements Serializable {

	@SerializedName("starred_since")
	private String starredSince;

	@SerializedName("user")
	private User user;

	public String getStarredSince() {
		return starredSince;
	}

	public User getUser() {
		return user;
	}
}
