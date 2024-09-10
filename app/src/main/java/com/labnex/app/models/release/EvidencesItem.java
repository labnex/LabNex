package com.labnex.app.models.release;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class EvidencesItem implements Serializable {

	@SerializedName("filepath")
	private String filepath;

	@SerializedName("sha")
	private String sha;

	@SerializedName("collected_at")
	private String collectedAt;

	public String getFilepath() {
		return filepath;
	}

	public String getSha() {
		return sha;
	}

	public String getCollectedAt() {
		return collectedAt;
	}
}
