package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.adapters.NotesAdapter;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.NotesApi;
import com.labnex.app.database.models.Notes;
import com.labnex.app.databinding.BottomsheetNotesPickerBinding;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class NotesPickerBottomSheet extends BottomSheetDialogFragment {

	public interface OnNoteSelectedListener {
		void onNoteSelected(String noteContent);
	}

	private BottomsheetNotesPickerBinding binding;
	private OnNoteSelectedListener listener;
	private NotesAdapter adapter;
	private NotesApi notesApi;
	private final List<Notes> notesList = new ArrayList<>();

	public static NotesPickerBottomSheet newInstance() {
		return new NotesPickerBottomSheet();
	}

	public void setOnNoteSelectedListener(OnNoteSelectedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notesApi = BaseApi.getInstance(requireContext(), NotesApi.class);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetNotesPickerBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setupRecyclerView();

		if (notesApi.getCount() > 0) {
			fetchNotes();
		} else {
			Toasty.show(requireContext(), getString(R.string.no_notes));
			dismiss();
		}
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);

		adapter = new NotesAdapter(requireContext(), notesList, null);
		adapter.setSheetStyle(true);
		adapter.setOnNoteInsertListener(
				noteContent -> {
					if (listener != null) {
						listener.onNoteSelected(noteContent);
					}
					dismiss();
				});
		binding.recyclerView.setAdapter(adapter);
	}

	private void fetchNotes() {
		notesApi.fetchAllNotes()
				.observe(
						getViewLifecycleOwner(),
						allNotes -> {
							if (allNotes != null && !allNotes.isEmpty()) {
								notesList.clear();
								notesList.addAll(allNotes);
								adapter.updateList(allNotes);
								binding.recyclerView.setVisibility(View.VISIBLE);
							} else {
								adapter.updateList(new ArrayList<>());
								binding.recyclerView.setVisibility(View.GONE);
							}
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
