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

	@SerializedName("author_email")
	private String authorEmail;

	@SerializedName("author_name")
	private String authorName;

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

	public CrudeFile authorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
		return this;
	}

	public void setAuthorEmail(String authorEmail) {
		this.authorEmail = authorEmail;
	}

	public CrudeFile authorName(String authorName) {
		this.authorName = authorName;
		return this;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}
}
