package com.labnex.app.models.approvals;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author lululujojo123
 */
public class Branch implements Serializable {

	@SerializedName("id")
	private int id;

	@SerializedName("name")
	private String name;

	@SerializedName("push_access_levels")
	private List<AccessLevel> pushAccessLevels;

	@SerializedName("merge_access_levels")
	private List<AccessLevel> mergeAccessLevels;

	@SerializedName("unprotect_access_levels")
	private List<AccessLevel> unprotectAccessLevels;

	@SerializedName("code_owner_approval_required")
	private String codeOwnerApprovalRequired;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<AccessLevel> getPushAccessLevels() {
		return pushAccessLevels;
	}

	public List<AccessLevel> getMergeAccessLevels() {
		return mergeAccessLevels;
	}

	public List<AccessLevel> getUnprotectAccessLevels() {
		return unprotectAccessLevels;
	}

	public String getCodeOwnerApprovalRequired() {
		return codeOwnerApprovalRequired;
	}
}
