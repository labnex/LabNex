package com.labnex.app.bottomsheets;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.NotesApi;
import com.labnex.app.database.models.Notes;
import com.labnex.app.databinding.BottomsheetNotesBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.vdurmont.emoji.EmojiParser;
import java.time.Instant;
import java.util.Objects;

/**
 * @author mmarif
 */
public class NotesBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetNotesBinding binding;
	private NotesApi notesApi;
	private String source;
	private int noteId = -1;
	private boolean autoSaveEnabled = true;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetNotesBinding.inflate(inflater, container, false);
		notesApi = BaseApi.getInstance(requireContext(), NotesApi.class);

		Bundle args = getArguments();
		if (args != null) {
			source = args.getString("source", "new");
			noteId = args.getInt("noteId", -1);
		}

		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.btnToggleView.setOnClickListener(v -> togglePreview(true));
		binding.btnToggleEdit.setOnClickListener(v -> togglePreview(false));

		if ("edit".equalsIgnoreCase(source) && noteId > 0) {
			Notes note = notesApi != null ? notesApi.fetchNoteById(noteId) : null;
			if (note != null && note.getContent() != null) {
				binding.contents.setText(note.getContent());
				binding.contents.setSelection(note.getContent().length());
			}
		}

		binding.contents.requestFocus();

		binding.contents.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void afterTextChanged(Editable s) {
						if (!autoSaveEnabled) return;
						String text = s != null ? s.toString().trim() : "";
						if ("edit".equalsIgnoreCase(source) && noteId > 0) {
							if (!text.isEmpty()) updateNote(text);
						} else if (text.length() > 4) {
							if (noteId > 0) {
								updateNote(text);
							} else {
								noteId =
										(int)
												(notesApi != null
														? notesApi.insertNote(
																text,
																(int)
																		Instant.now()
																				.getEpochSecond())
														: -1);
								source = "edit";
							}
						}
					}

					@Override
					public void beforeTextChanged(
							CharSequence s, int start, int count, int after) {}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {}
				});

		return binding.getRoot();
	}

	private void togglePreview(boolean showPreview) {
		autoSaveEnabled = !showPreview;

		if (showPreview) {
			String text = Objects.requireNonNull(binding.contents.getText()).toString();
			Markdown.render(
					requireContext(), EmojiParser.parseToUnicode(text), binding.renderContents);
			binding.contents.setVisibility(View.GONE);
			binding.previewScroll.setVisibility(View.VISIBLE);
			binding.btnToggleView.setVisibility(View.GONE);
			binding.btnToggleEdit.setVisibility(View.VISIBLE);
		} else {
			binding.contents.setVisibility(View.VISIBLE);
			binding.previewScroll.setVisibility(View.GONE);
			binding.btnToggleView.setVisibility(View.VISIBLE);
			binding.btnToggleEdit.setVisibility(View.GONE);
			binding.contents.requestFocus();
		}
	}

	private void updateNote(String content) {
		if (notesApi != null) {
			notesApi.updateNote(content, Instant.now().getEpochSecond(), noteId);
			AppUIStateManager.refreshData();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getDialog() instanceof BottomSheetDialog) {
			UIHelper.applyFullScreenSheetStyle((BottomSheetDialog) getDialog(), false);
		}
		Utils.showKeyboard(requireActivity(), binding.contents);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
