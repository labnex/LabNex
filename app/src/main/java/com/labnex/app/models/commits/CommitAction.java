package com.labnex.app.models.commits;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class CommitAction implements Serializable {

	@SerializedName("action")
	private String action;

	@SerializedName("file_path")
	private String filePath;

	@SerializedName("content")
	private String content;

	@SerializedName("encoding")
	private String encoding;

	public CommitAction(String action, String filePath, String content, String encoding) {
		this.action = action;
		this.filePath = filePath;
		this.content = content;
		this.encoding = encoding;
	}

	public String getAction() {
		return action;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getContent() {
		return content;
	}

	public String getEncoding() {
		return encoding;
	}
}
