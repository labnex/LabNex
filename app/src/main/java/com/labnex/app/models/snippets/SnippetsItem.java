package com.labnex.app.models.snippets;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class SnippetsItem implements Serializable {

	@SerializedName("ssh_url_to_repo")
	private String sshUrlToRepo;

	@SerializedName("visibility")
	private String visibility;

	@SerializedName("author")
	private Author author;

	@SerializedName("file_name")
	private String fileName;

	@SerializedName("imported_from")
	private String importedFrom;

	@SerializedName("description")
	private String description;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("title")
	private String title;

	@SerializedName("http_url_to_repo")
	private String httpUrlToRepo;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("project_id")
	private int projectId;

	@SerializedName("imported")
	private boolean imported;

	@SerializedName("files")
	private List<FilesItem> files;

	@SerializedName("id")
	private int id;

	@SerializedName("raw_url")
	private String rawUrl;

	public String getSshUrlToRepo() {
		return sshUrlToRepo;
	}

	public String getVisibility() {
		return visibility;
	}

	public Author getAuthor() {
		return author;
	}

	public String getFileName() {
		return fileName;
	}

	public String getImportedFrom() {
		return importedFrom;
	}

	public String getDescription() {
		return description;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getTitle() {
		return title;
	}

	public String getHttpUrlToRepo() {
		return httpUrlToRepo;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public int getProjectId() {
		return projectId;
	}

	public boolean isImported() {
		return imported;
	}

	public List<FilesItem> getFiles() {
		return files;
	}

	public int getId() {
		return id;
	}

	public String getRawUrl() {
		return rawUrl;
	}
}
