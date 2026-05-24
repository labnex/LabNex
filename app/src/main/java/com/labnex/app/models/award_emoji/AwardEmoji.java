package com.labnex.app.models.award_emoji;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class AwardEmoji implements Serializable {

	@SerializedName("id")
	private long id;

	@SerializedName("name")
	private String name;

	@SerializedName("user")
	private AwardEmojiUser user;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("awardable_id")
	private long awardableId;

	@SerializedName("awardable_type")
	private String awardableType;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public AwardEmojiUser getUser() {
		return user;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public long getAwardableId() {
		return awardableId;
	}

	public String getAwardableType() {
		return awardableType;
	}
}
