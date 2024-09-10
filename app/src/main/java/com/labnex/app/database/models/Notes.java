package com.labnex.app.database.models;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * @author mmarif
 */
@Entity(tableName = "Notes")
public class Notes implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private int noteId;

	@Nullable private String content;
	private Integer datetime;
	private Integer modified;

	public int getNoteId() {
		return noteId;
	}

	public void setNoteId(int noteId) {
		this.noteId = noteId;
	}

	@Nullable public String getContent() {
		return content;
	}

	public void setContent(@Nullable String content) {
		this.content = content;
	}

	public Integer getDatetime() {
		return datetime;
	}

	public void setDatetime(Integer datetime) {
		this.datetime = datetime;
	}

	public Integer getModified() {
		return modified;
	}

	public void setModified(Integer modified) {
		this.modified = modified;
	}
}
