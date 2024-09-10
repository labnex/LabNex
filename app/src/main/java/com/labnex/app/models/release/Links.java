package com.labnex.app.models.release;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Links implements Serializable {

	@SerializedName("closed_issues_url")
	private String closedIssuesUrl;

	@SerializedName("opened_issues_url")
	private String openedIssuesUrl;

	@SerializedName("merged_merge_requests_url")
	private String mergedMergeRequestsUrl;

	@SerializedName("edit_url")
	private String editUrl;

	@SerializedName("self")
	private String self;

	@SerializedName("closed_merge_requests_url")
	private String closedMergeRequestsUrl;

	@SerializedName("opened_merge_requests_url")
	private String openedMergeRequestsUrl;

	public String getClosedIssuesUrl() {
		return closedIssuesUrl;
	}

	public String getOpenedIssuesUrl() {
		return openedIssuesUrl;
	}

	public String getMergedMergeRequestsUrl() {
		return mergedMergeRequestsUrl;
	}

	public String getEditUrl() {
		return editUrl;
	}

	public String getSelf() {
		return self;
	}

	public String getClosedMergeRequestsUrl() {
		return closedMergeRequestsUrl;
	}

	public String getOpenedMergeRequestsUrl() {
		return openedMergeRequestsUrl;
	}
}
