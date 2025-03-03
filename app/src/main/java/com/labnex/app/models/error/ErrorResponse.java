package com.labnex.app.models.error;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class ErrorResponse implements Serializable {

	@SerializedName("message")
	private String message;

	public String getMessage() {
		return message;
	}
}
