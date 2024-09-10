package com.labnex.app.models.repository;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class FileContents implements Serializable {

	@SerializedName("file_path")
	private String filePath;

	@SerializedName("ref")
	private String ref;

	@SerializedName("size")
	private int size;

	@SerializedName("content_sha256")
	private String contentSha256;

	@SerializedName("blob_id")
	private String blobId;

	@SerializedName("file_name")
	private String fileName;

	@SerializedName("encoding")
	private String encoding;

	@SerializedName("commit_id")
	private String commitId;

	@SerializedName("last_commit_id")
	private String lastCommitId;

	@SerializedName("content")
	private String content;

	@SerializedName("execute_filemode")
	private boolean executeFilemode;

	public String getFilePath() {
		return filePath;
	}

	public String getRef() {
		return ref;
	}

	public int getSize() {
		return size;
	}

	public String getContentSha256() {
		return contentSha256;
	}

	public String getBlobId() {
		return blobId;
	}

	public String getFileName() {
		return fileName;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getCommitId() {
		return commitId;
	}

	public String getLastCommitId() {
		return lastCommitId;
	}

	public String getContent() {
		return content;
	}

	public boolean isExecuteFilemode() {
		return executeFilemode;
	}
}
