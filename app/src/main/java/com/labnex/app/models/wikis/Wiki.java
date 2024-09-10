package com.labnex.app.models.wikis;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Wiki implements Serializable {

	@SerializedName("front_matter")
	private FrontMatter frontMatter;

	@SerializedName("format")
	private String format;

	@SerializedName("title")
	private String title;

	@SerializedName("encoding")
	private String encoding;

	@SerializedName("slug")
	private String slug;

	@SerializedName("content")
	private String content;

	public FrontMatter getFrontMatter() {
		return frontMatter;
	}

	public String getFormat() {
		return format;
	}

	public String getTitle() {
		return title;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getSlug() {
		return slug;
	}

	public String getContent() {
		return content;
	}
}
