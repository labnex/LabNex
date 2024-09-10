package com.labnex.app.models.commits;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Diff implements Serializable {

	@SerializedName("generated_file")
	private Object generatedFile;

	@SerializedName("new_path")
	private String newPath;

	@SerializedName("renamed_file")
	private boolean renamedFile;

	@SerializedName("a_mode")
	private String aMode;

	@SerializedName("deleted_file")
	private boolean deletedFile;

	@SerializedName("b_mode")
	private String bMode;

	@SerializedName("new_file")
	private boolean newFile;

	@SerializedName("diff")
	private String diff;

	@SerializedName("old_path")
	private String oldPath;

	public Object getGeneratedFile() {
		return generatedFile;
	}

	public String getNewPath() {
		return newPath;
	}

	public boolean isRenamedFile() {
		return renamedFile;
	}

	public String getAMode() {
		return aMode;
	}

	public boolean isDeletedFile() {
		return deletedFile;
	}

	public String getBMode() {
		return bMode;
	}

	public boolean isNewFile() {
		return newFile;
	}

	public String getDiff() {
		return diff;
	}

	public String getOldPath() {
		return oldPath;
	}
}
