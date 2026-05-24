package com.labnex.app.models.award_emoji;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class AwardEmojiUser implements Serializable {

	@SerializedName("id")
	private long id;

	@SerializedName("name")
	private String name;

	@SerializedName("username")
	private String username;

	@SerializedName("avatar_url")
	private String avatarUrl;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getUsername() {
		return username;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}
}
