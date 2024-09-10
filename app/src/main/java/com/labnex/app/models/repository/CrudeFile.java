package com.labnex.app.models.repository;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class CrudeFile implements Serializable {

	@SerializedName("branch")
	private String branch;

	@SerializedName("commit_message")
	private String commitMessage;

	@SerializedName("content")
	private String content;

	public CrudeFile branch(String branch) {
		this.branch = branch;
		return this;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public CrudeFile commitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
		return this;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

	public CrudeFile content(String content) {
		this.content = content;
		return this;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
