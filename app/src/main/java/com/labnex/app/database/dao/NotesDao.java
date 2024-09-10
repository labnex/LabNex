package com.labnex.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.labnex.app.database.models.Notes;
import java.util.List;

/**
 * @author mmarif
 */
@Dao
public interface NotesDao {

	@Insert
	long insertNote(Notes notes);

	@Query("SELECT * FROM Notes ORDER BY modified DESC, noteId DESC")
	LiveData<List<Notes>> fetchAllNotes();

	@Query("SELECT * FROM Notes WHERE noteId = :noteId")
	Notes fetchNoteById(int noteId);

	@Query("UPDATE Notes SET content = :content, modified = :modified WHERE noteId = :noteId")
	void updateNote(String content, long modified, int noteId);

	@Query("DELETE FROM Notes")
	void deleteAllNotes();

	@Query("DELETE FROM Notes WHERE noteId = :noteId")
	void deleteNote(int noteId);

	@Query("SELECT COUNT(noteId) FROM Notes")
	Integer getCount();

	@Query(
			"SELECT * FROM Notes WHERE content LIKE '%' || :content || '%' ORDER BY modified DESC, noteId DESC")
	LiveData<List<Notes>> searchNotes(String content);
}
