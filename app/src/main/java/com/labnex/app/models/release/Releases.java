package com.labnex.app.models.release;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Releases implements Serializable {

	@SerializedName("_links")
	private Links links;

	@SerializedName("tag_name")
	private String tagName;

	@SerializedName("author")
	private Author author;

	@SerializedName("commit")
	private Commit commit;

	@SerializedName("description")
	private String description;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("released_at")
	private String releasedAt;

	@SerializedName("commit_path")
	private String commitPath;

	@SerializedName("upcoming_release")
	private boolean upcomingRelease;

	@SerializedName("assets")
	private Assets assets;

	@SerializedName("name")
	private String name;

	@SerializedName("evidences")
	private List<EvidencesItem> evidences;

	@SerializedName("tag_path")
	private String tagPath;

	public Links getLinks() {
		return links;
	}

	public String getTagName() {
		return tagName;
	}

	public Author getAuthor() {
		return author;
	}

	public Commit getCommit() {
		return commit;
	}

	public String getDescription() {
		return description;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getReleasedAt() {
		return releasedAt;
	}

	public String getCommitPath() {
		return commitPath;
	}

	public boolean isUpcomingRelease() {
		return upcomingRelease;
	}

	public Assets getAssets() {
		return assets;
	}

	public String getName() {
		return name;
	}

	public List<EvidencesItem> getEvidences() {
		return evidences;
	}

	public String getTagPath() {
		return tagPath;
	}
}
