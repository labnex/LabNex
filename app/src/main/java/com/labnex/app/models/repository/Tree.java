package com.labnex.app.models.repository;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Tree implements Serializable {

	@SerializedName("mode")
	private String mode;

	@SerializedName("path")
	private String path;

	@SerializedName("name")
	private String name;

	@SerializedName("id")
	private String id;

	@SerializedName("type")
	private String type;

	public String getMode() {
		return mode;
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}
}
