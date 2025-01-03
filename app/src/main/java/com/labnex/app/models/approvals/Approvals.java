package com.labnex.app.models.approvals;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author lululujojo123
 */
public class Approvals implements Serializable {

	@SerializedName("id")
	private int id;

	@SerializedName("iid")
	private int iid;

	@SerializedName("project_id")
	private int projectId;

	@SerializedName("title")
	private String title;

	@SerializedName("description")
	private String description;

	@SerializedName("state")
	private String state;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("merge_status")
	private String mergeStatus;

	@SerializedName("approvals_required")
	private int approvalsRequired;

	@SerializedName("approvals_left")
	private int approvalsLeft;

	@SerializedName("approved_by")
	private List<ApprovedBy> approvedBy;

	public int getId() {
		return id;
	}

	public int getIid() {
		return iid;
	}

	public int getProjectId() {
		return projectId;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getState() {
		return state;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getMergeStatus() {
		return mergeStatus;
	}

	public int getApprovalsRequired() {
		return approvalsRequired;
	}

	public int getApprovalsLeft() {
		return approvalsLeft;
	}

	public List<ApprovedBy> getApprovedBy() {
		return approvedBy;
	}
}
