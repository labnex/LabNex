package com.labnex.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.NotesAdapter;
import com.labnex.app.bottomsheets.NotesBottomSheet;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.NotesApi;
import com.labnex.app.database.models.Notes;
import com.labnex.app.databinding.ActivityNotesBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class NotesActivity extends BaseActivity implements BottomSheetListener {

	private ActivityNotesBinding binding;
	private NotesApi notesApi;
	private List<Notes> notesList;
	private List<Notes> notesListSearch;
	private NotesAdapter adapter;
	private NotesAdapter adapterSearch;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityNotesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		notesList = new ArrayList<>();
		notesApi = BaseApi.getInstance(ctx, NotesApi.class);

		notesListSearch = new ArrayList<>();
		adapterSearch = new NotesAdapter(ctx, notesListSearch, "", "search", binding.bottomAppBar);

		binding.recyclerView.setHasFixedSize(true);

		adapter = new NotesAdapter(ctx, notesList, "", "", binding.bottomAppBar);

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											notesList.clear();
											binding.pullToRefresh.setRefreshing(false);
											binding.progressBar.setVisibility(View.VISIBLE);
											getNotes();
										},
										350));

		getNotes();

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					if (menuItem.getItemId() == R.id.delete_all_notes) {

						if (notesList.isEmpty()) {
							Snackbar.info(
									ctx,
									findViewById(android.R.id.content),
									binding.bottomAppBar,
									ctx.getResources().getString(R.string.all_good));
						} else {
							new MaterialAlertDialogBuilder(ctx)
									.setMessage(R.string.delete_all_notes_dialog_message)
									.setPositiveButton(
											R.string.delete,
											(dialog, which) -> {
												deleteAllNotes();
												dialog.dismiss();
											})
									.setNeutralButton(R.string.cancel, null)
									.show();
						}
					}
					return false;
				});

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void beforeTextChanged(
									CharSequence charSequence, int i, int i1, int i2) {}

							@Override
							public void onTextChanged(
									CharSequence charSequence, int i, int i1, int i2) {
								String searchText = charSequence.toString();
								if (searchText.length() >= 2) {
									updateSearchResult(searchText);
								} else {
									notesListSearch.clear();
									adapterSearch.notifyDataChanged();
								}
							}

							@Override
							public void afterTextChanged(Editable editable) {}
						});

		Bundle bsBundle = new Bundle();
		binding.newNote.setOnClickListener(
				newNote -> {
					bsBundle.putString("source", "new");
					NotesBottomSheet bottomSheet = new NotesBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "notesBottomSheet");
				});
	}

	public void updateSearchResult(String text) {

		binding.recyclerViewSearch.setHasFixedSize(true);

		notesApi.searchNotes(text)
				.observe(
						this,
						allNotes -> {
							assert allNotes != null;
							if (!allNotes.isEmpty()) {
								notesListSearch.clear();
								notesListSearch.addAll(allNotes);
								adapterSearch.notifyDataChanged();
								binding.recyclerViewSearch.setAdapter(adapterSearch);
							}
						});
	}

	private void getNotes() {

		notesApi.fetchAllNotes()
				.observe(
						this,
						allNotes -> {
							binding.pullToRefresh.setRefreshing(false);
							assert allNotes != null;
							if (!allNotes.isEmpty()) {

								notesList.clear();
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								notesList.addAll(allNotes);
								adapter.notifyDataChanged();
								binding.recyclerView.setAdapter(adapter);
							} else {

								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}
							binding.progressBar.setVisibility(View.GONE);
						});
	}

	public void deleteAllNotes() {

		if (!notesList.isEmpty()) {

			notesApi.deleteAllNotes();
			notesList.clear();
			adapter.notifyDataChanged();
			Snackbar.info(
					ctx,
					findViewById(android.R.id.content),
					binding.bottomAppBar,
					ctx.getResources().getQuantityString(R.plurals.note_delete_message, 2));
		} else {
			Snackbar.info(
					ctx,
					findViewById(android.R.id.content),
					binding.bottomAppBar,
					ctx.getResources().getString(R.string.all_good));
		}
	}

	@Override
	public void onButtonClicked(String text) {}
}
