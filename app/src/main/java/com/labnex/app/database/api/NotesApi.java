package com.labnex.app.database.api;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.labnex.app.database.dao.NotesDao;
import com.labnex.app.database.models.Notes;
import java.util.List;

/**
 * @author mmarif
 */
public class NotesApi extends BaseApi {

	private final NotesDao notesDao;

	NotesApi(Context context) {
		super(context);
		notesDao = labnexDatabase.notesDao();
	}

	public long insertNote(String content, Integer datetime) {

		Notes notes = new Notes();
		notes.setContent(content);
		notes.setDatetime(datetime);

		return insertNoteAsyncTask(notes);
	}

	public long insertNoteAsyncTask(Notes notes) {
		return notesDao.insertNote(notes);
	}

	public LiveData<List<Notes>> fetchAllNotes() {
		return notesDao.fetchAllNotes();
	}

	public Integer getCount() {
		return notesDao.getCount();
	}

	public Notes fetchNoteById(int noteId) {
		return notesDao.fetchNoteById(noteId);
	}

	public void updateNote(final String content, final long modified, int noteId) {
		executorService.execute(() -> notesDao.updateNote(content, modified, noteId));
	}

	public void deleteAllNotes() {
		executorService.execute(notesDao::deleteAllNotes);
	}

	public void deleteNote(final int noteId) {
		final Notes note = notesDao.fetchNoteById(noteId);

		if (note != null) {
			executorService.execute(() -> notesDao.deleteNote(noteId));
		}
	}

	public LiveData<List<Notes>> searchNotes(String content) {
		return notesDao.searchNotes(content);
	}
}
