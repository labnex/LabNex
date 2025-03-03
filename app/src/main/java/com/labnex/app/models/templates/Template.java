package com.labnex.app.models.templates;

import com.google.gson.annotations.SerializedName;

public class Template {

	@SerializedName("name")
	private String name;

	@SerializedName("content")
	private String content;

	public String getName() {
		return name;
	}

	public String getContent() {
		return content;
	}
}
