package com.labnex.app.models.wikis;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class CrudeWiki implements Serializable {

	@SerializedName("title")
	private String title;

	@SerializedName("content")
	private String content;

	@SerializedName("format")
	private String format;

	public CrudeWiki name(String title) {
		this.title = title;
		return this;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public CrudeWiki content(String content) {
		this.content = content;
		return this;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public CrudeWiki format(String format) {
		this.format = format;
		return this;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
