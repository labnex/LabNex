package com.labnex.app.models.labels;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class CrudeLabel implements Serializable {

	@SerializedName("name")
	private String name;

	@SerializedName("new_name")
	private String new_name;

	@SerializedName("description")
	private String description;

	@SerializedName("color")
	private String color;

	public CrudeLabel name(String name) {
		this.name = name;
		return this;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CrudeLabel new_name(String new_name) {
		this.new_name = new_name;
		return this;
	}

	public void setNew_name(String new_name) {
		this.new_name = new_name;
	}

	public CrudeLabel description(String description) {
		this.description = description;
		return this;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CrudeLabel color(String color) {
		this.color = color;
		return this;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
