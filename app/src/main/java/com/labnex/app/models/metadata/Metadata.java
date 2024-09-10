package com.labnex.app.models.metadata;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Metadata implements Serializable {

	@SerializedName("enterprise")
	private boolean enterprise;

	@SerializedName("kas")
	private Kas kas;

	@SerializedName("version")
	private String version;

	@SerializedName("revision")
	private String revision;

	public boolean isEnterprise() {
		return enterprise;
	}

	public Kas getKas() {
		return kas;
	}

	public String getVersion() {
		return version;
	}

	public String getRevision() {
		return revision;
	}
}
