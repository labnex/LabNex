package com.labnex.app.models.broadcast_messages;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Messages implements Serializable {

	@SerializedName("starts_at")
	private String startsAt;

	@SerializedName("dismissable")
	private boolean dismissable;

	@SerializedName("active")
	private boolean active;

	@SerializedName("ends_at")
	private String endsAt;

	@SerializedName("id")
	private int id;

	@SerializedName("message")
	private String message;

	@SerializedName("target_path")
	private String targetPath;

	@SerializedName("font")
	private String font;

	@SerializedName("target_access_levels")
	private List<Integer> targetAccessLevels;

	@SerializedName("broadcast_type")
	private String broadcastType;

	public String getStartsAt() {
		return startsAt;
	}

	public boolean isDismissable() {
		return dismissable;
	}

	public boolean isActive() {
		return active;
	}

	public String getEndsAt() {
		return endsAt;
	}

	public int getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public String getTargetPath() {
		return targetPath;
	}

	public String getFont() {
		return font;
	}

	public List<Integer> getTargetAccessLevels() {
		return targetAccessLevels;
	}

	public String getBroadcastType() {
		return broadcastType;
	}
}
