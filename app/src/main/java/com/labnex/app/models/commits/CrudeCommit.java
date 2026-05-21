package com.labnex.app.models.commits;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class CrudeCommit implements Serializable {

	@SerializedName("branch")
	private String branch;

	@SerializedName("start_branch")
	private String startBranch;

	@SerializedName("commit_message")
	private String commitMessage;

	@SerializedName("actions")
	private List<CommitAction> actions;

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public String getStartBranch() {
		return startBranch;
	}

	public void setStartBranch(String startBranch) {
		this.startBranch = startBranch;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

	public List<CommitAction> getActions() {
		return actions;
	}

	public void setActions(List<CommitAction> actions) {
		this.actions = actions;
	}
}
