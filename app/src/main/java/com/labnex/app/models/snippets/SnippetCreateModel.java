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

		@SerializedName("action")
		private String action;

		@SerializedName("previous_path")
		private String previousPath;

		public File(String filePath, String content) {
			this.filePath = filePath;
			this.content = content;
		}

		public File(String action, String filePath, String content, String previousPath) {
			this.action = action;
			this.filePath = filePath;
			this.content = content;
			this.previousPath = previousPath;
		}

		public String getFilePath() {
			return filePath;
		}

		public String getContent() {
			return content;
		}

		public String getAction() {
			return action;
		}

		public String getPreviousPath() {
			return previousPath;
		}
	}
}
