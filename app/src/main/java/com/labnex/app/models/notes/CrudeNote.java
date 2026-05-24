package com.labnex.app.models.notes;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class CrudeNote implements Serializable {

	@SerializedName("body")
	private String body;

	public CrudeNote body(String body) {
		this.body = body;
		return this;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
