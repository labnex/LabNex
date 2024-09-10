package com.labnex.app.models.groups;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class DefaultBranchProtectionDefaults implements Serializable {

	@SerializedName("allowed_to_push")
	private List<AllowedToPushItem> allowedToPush;

	@SerializedName("allowed_to_merge")
	private List<AllowedToMergeItem> allowedToMerge;

	@SerializedName("allow_force_push")
	private boolean allowForcePush;

	public List<AllowedToPushItem> getAllowedToPush() {
		return allowedToPush;
	}

	public List<AllowedToMergeItem> getAllowedToMerge() {
		return allowedToMerge;
	}

	public boolean isAllowForcePush() {
		return allowForcePush;
	}
}
