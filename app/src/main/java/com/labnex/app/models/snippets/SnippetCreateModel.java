package com.labnex.app.models.snippets;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author mmarif
 */
public class SnippetCreateModel {

	@SerializedName("title")
	private String title;

	@SerializedName("description")
	private String description;

	@SerializedName("visibility")
	private String visibility; // "internal", "private", "public"

	@SerializedName("files")
	private List<File> files;

	public SnippetCreateModel(
			String title, String description, String visibility, List<File> files) {
		this.title = title;
		this.description = description;
		this.visibility = visibility;
		this.files = files;
	}

	public static class File {
		@SerializedName("file_path")
		private String filePath;

		@SerializedName("content")
		private String content;

		public File(String filePath, String content) {
			this.filePath = filePath;
			this.content = content;
		}
	}
}
