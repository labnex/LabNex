package com.labnex.app.models.notes;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class CreateNote implements Serializable {

	@SerializedName("body")
	private String body;

	public CreateNote body(String body) {
		this.body = body;
		return this;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
