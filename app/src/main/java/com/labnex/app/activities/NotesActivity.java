package com.labnex.app.activities;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.NotesAdapter;
import com.labnex.app.bottomsheets.NotesBottomSheet;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.NotesApi;
import com.labnex.app.database.models.Notes;
import com.labnex.app.databinding.ActivityNotesBinding;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import java.util.ArrayList;

/**
 * @author mmarif
 */
public class NotesActivity extends BaseActivity implements NotesAdapter.OnNoteClickListener {

	private ActivityNotesBinding binding;
	private NotesApi notesApi;
	private NotesAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityNotesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		notesApi = BaseApi.getInstance(ctx, NotesApi.class);

		binding.btnBack.setOnClickListener(v -> finish());

		binding.btnDeleteAll.setOnClickListener(
				v -> {
					if (adapter.getItemCount() == 0) {
						Toasty.show(ctx, getString(R.string.all_good));
						return;
					}
					new MaterialAlertDialogBuilder(ctx)
							.setMessage(R.string.delete_all_notes_dialog_message)
							.setPositiveButton(
									R.string.delete,
									(dialog, which) -> {
										if (notesApi != null) notesApi.deleteAllNotes();
										adapter.updateList(new ArrayList<>());
										showEmptyState();
										Toasty.show(
												ctx,
												getResources()
														.getQuantityString(
																R.plurals.note_delete_message, 2));
									})
							.setNeutralButton(R.string.cancel, null)
							.show();
				});

		binding.btnNewNote.setOnClickListener(
				v -> {
					NotesBottomSheet sheet = new NotesBottomSheet();
					Bundle args = new Bundle();
					args.putString("source", "new");
					sheet.setArguments(args);
					sheet.show(getSupportFragmentManager(), "notesBottomSheet");
				});

		adapter = new NotesAdapter(ctx, new ArrayList<>(), this);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		binding.recyclerView.setAdapter(adapter);

		binding.pullToRefresh.setOnRefreshListener(this::loadNotes);
		loadNotes();
	}

	@Override
	protected void onGlobalRefresh() {
		loadNotes();
	}

	private void loadNotes() {
		binding.progressBar.setVisibility(View.VISIBLE);

		if (notesApi != null) {
			notesApi.fetchAllNotes()
					.observe(
							this,
							notes -> {
								binding.progressBar.setVisibility(View.GONE);
								binding.pullToRefresh.setRefreshing(false);

								if (notes != null && !notes.isEmpty()) {
									adapter.updateList(notes);
									binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
									binding.recyclerView.setVisibility(View.VISIBLE);
								} else {
									showEmptyState();
								}
							});
		}
	}

	private void showEmptyState() {
		if (adapter.getItemCount() == 0) {
			binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onNoteClick(Notes note) {
		NotesBottomSheet sheet = new NotesBottomSheet();
		Bundle args = new Bundle();
		args.putString("source", "edit");
		args.putInt("noteId", note.getNoteId());
		sheet.setArguments(args);
		sheet.show(getSupportFragmentManager(), "notesBottomSheet");
	}

	@Override
	public void onNoteDelete(Notes note, int position) {
		new MaterialAlertDialogBuilder(ctx)
				.setMessage(R.string.delete_note_dialog_message)
				.setPositiveButton(
						R.string.delete,
						(dialog, which) -> {
							if (notesApi != null) notesApi.deleteNote(note.getNoteId());
							adapter.removeItem(position);
							showEmptyState();
							Toasty.show(
									ctx,
									getResources()
											.getQuantityString(R.plurals.note_delete_message, 1));
						})
				.setNeutralButton(R.string.cancel, null)
				.show();
	}
}
