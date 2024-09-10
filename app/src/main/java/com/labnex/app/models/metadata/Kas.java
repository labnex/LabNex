package com.labnex.app.models.metadata;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Kas implements Serializable {

	@SerializedName("externalUrl")
	private String externalUrl;

	@SerializedName("version")
	private String version;

	@SerializedName("enabled")
	private boolean enabled;

	public String getExternalUrl() {
		return externalUrl;
	}

	public String getVersion() {
		return version;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
