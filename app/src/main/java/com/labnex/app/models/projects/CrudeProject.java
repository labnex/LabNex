package com.labnex.app.models.projects;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class CrudeProject implements Serializable {

	@SerializedName("name")
	private String name;

	@SerializedName("description")
	private String description;

	@SerializedName("initialize_with_readme")
	private boolean initializeWithReadme;

	@SerializedName("visibility")
	private String visibility;

	public CrudeProject name(String name) {
		this.name = name;
		return this;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CrudeProject description(String description) {
		this.description = description;
		return this;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CrudeProject initializeWithReadme(boolean initializeWithReadme) {
		this.initializeWithReadme = initializeWithReadme;
		return this;
	}

	public void setInitializeWithReadme(boolean initializeWithReadme) {
		this.initializeWithReadme = initializeWithReadme;
	}

	public CrudeProject visibility(String visibility) {
		this.visibility = visibility;
		return this;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
}
