package com.labnex.app.models.snippets;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class FilesItem implements Serializable {

	@SerializedName("path")
	private String path;

	@SerializedName("raw_url")
	private String rawUrl;

	public String getPath() {
		return path;
	}

	public String getRawUrl() {
		return rawUrl;
	}
}
