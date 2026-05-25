package com.labnex.app.helpers.attachments;

import android.net.Uri;

/**
 * @author mmarif
 */
public class AttachmentsModel {

	private Uri uri;
	private String fileName;
	private long fileSize;

	public AttachmentsModel(String fileName, Uri uri) {
		this.fileName = fileName;
		this.uri = uri;
	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
}
