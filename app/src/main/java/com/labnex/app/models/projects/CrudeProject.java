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

	@SerializedName("namespace_id")
	private long namespaceId;

	@SerializedName("default_branch")
	private String defaultBranch;

	@SerializedName("lfs_enabled")
	private boolean lfsEnabled;

	@SerializedName("emails_enabled")
	private boolean emailsEnabled;

	@SerializedName("path")
	private String path;

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setInitializeWithReadme(boolean initializeWithReadme) {
		this.initializeWithReadme = initializeWithReadme;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public void setNamespaceId(long namespaceId) {
		this.namespaceId = namespaceId;
	}

	public void setDefaultBranch(String defaultBranch) {
		this.defaultBranch = defaultBranch;
	}

	public void setLfsEnabled(boolean lfsEnabled) {
		this.lfsEnabled = lfsEnabled;
	}

	public void setEmailsEnabled(boolean emailsEnabled) {
		this.emailsEnabled = emailsEnabled;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
