package com.labnex.app.models.issues;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class References implements Serializable {

	@SerializedName("short")
	private String jsonMemberShort;

	@SerializedName("relative")
	private String relative;

	@SerializedName("full")
	private String full;

	public String getJsonMemberShort() {
		return jsonMemberShort;
	}

	public String getRelative() {
		return relative;
	}

	public String getFull() {
		return full;
	}
}
