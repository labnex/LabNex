package com.labnex.app.models.templates;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Templates implements Serializable {

	@SerializedName("name")
	private String name;

	@SerializedName("key")
	private String key;

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}
}
