package com.labnex.app.models.release;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class SourcesItem implements Serializable {

	@SerializedName("format")
	private String format;

	@SerializedName("url")
	private String url;

	public String getFormat() {
		return format;
	}

	public String getUrl() {
		return url;
	}
}
