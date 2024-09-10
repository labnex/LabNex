package com.labnex.app.models.projects;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Links implements Serializable {

	@SerializedName("merge_requests")
	private String mergeRequests;

	@SerializedName("cluster_agents")
	private String clusterAgents;

	@SerializedName("members")
	private String members;

	@SerializedName("self")
	private String self;

	@SerializedName("repo_branches")
	private String repoBranches;

	@SerializedName("issues")
	private String issues;

	@SerializedName("events")
	private String events;

	@SerializedName("labels")
	private String labels;

	public String getMergeRequests() {
		return mergeRequests;
	}

	public String getClusterAgents() {
		return clusterAgents;
	}

	public String getMembers() {
		return members;
	}

	public String getSelf() {
		return self;
	}

	public String getRepoBranches() {
		return repoBranches;
	}

	public String getIssues() {
		return issues;
	}

	public String getEvents() {
		return events;
	}

	public String getLabels() {
		return labels;
	}
}
