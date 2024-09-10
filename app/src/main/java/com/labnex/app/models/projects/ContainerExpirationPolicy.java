package com.labnex.app.models.projects;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class ContainerExpirationPolicy implements Serializable {

	@SerializedName("keep_n")
	private int keepN;

	@SerializedName("next_run_at")
	private String nextRunAt;

	@SerializedName("name_regex_keep")
	private Object nameRegexKeep;

	@SerializedName("older_than")
	private String olderThan;

	@SerializedName("name_regex")
	private String nameRegex;

	@SerializedName("cadence")
	private String cadence;

	@SerializedName("enabled")
	private boolean enabled;

	public int getKeepN() {
		return keepN;
	}

	public String getNextRunAt() {
		return nextRunAt;
	}

	public Object getNameRegexKeep() {
		return nameRegexKeep;
	}

	public String getOlderThan() {
		return olderThan;
	}

	public String getNameRegex() {
		return nameRegex;
	}

	public String getCadence() {
		return cadence;
	}

	public boolean isEnabled() {
		return enabled;
	}
}
