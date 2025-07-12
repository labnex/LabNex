package com.labnex.app.bottomsheets;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.NotesApi;
import com.labnex.app.database.models.Notes;
import com.labnex.app.databinding.BottomSheetNotesBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.interfaces.BottomSheetListener;
import com.vdurmont.emoji.EmojiParser;
import java.time.Instant;
import java.util.Objects;

/**
 * @author mmarif
 */
public class NotesBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetListener bottomSheetListener;
	private String source;
	private NotesApi notesApi;
	private int noteId;
	private Notes notes;
	private BottomSheetNotesBinding bottomSheetNotesBinding;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetNotesBinding = BottomSheetNotesBinding.inflate(inflater, container, false);

		notesApi = BaseApi.getInstance(requireContext(), NotesApi.class);

		Bundle bundle = getArguments();
		assert bundle != null;

		if (bundle.getString("source") != null) {
			source = bundle.getString("source");
		} else {
			source = "";
		}

		bottomSheetNotesBinding.closeBs.setOnClickListener(close -> dismiss());

		bottomSheetNotesBinding.view.setOnClickListener(
				view -> {
					bottomSheetNotesBinding.contentsLayout.setVisibility(View.GONE);
					bottomSheetNotesBinding.renderContents.setVisibility(View.VISIBLE);

					bottomSheetNotesBinding.view.setVisibility(View.GONE);
					bottomSheetNotesBinding.edit.setVisibility(View.VISIBLE);

					Markdown.render(
							requireContext(),
							EmojiParser.parseToUnicode(
									Objects.requireNonNull(
													bottomSheetNotesBinding.contents.getText())
											.toString()),
							bottomSheetNotesBinding.renderContents);
				});

		bottomSheetNotesBinding.edit.setOnClickListener(
				edit -> {
					bottomSheetNotesBinding.contentsLayout.setVisibility(View.VISIBLE);
					bottomSheetNotesBinding.renderContents.setVisibility(View.GONE);

					bottomSheetNotesBinding.view.setVisibility(View.VISIBLE);
					bottomSheetNotesBinding.edit.setVisibility(View.GONE);
				});

		if (source.equalsIgnoreCase("edit")) {

			noteId = bundle.getInt("noteId");
			notes = notesApi.fetchNoteById(noteId);
			bottomSheetNotesBinding.contents.setText(notes.getContent());

			assert notes.getContent() != null;
			bottomSheetNotesBinding.contents.setSelection(notes.getContent().length());

			bottomSheetNotesBinding.contents.addTextChangedListener(
					new TextWatcher() {

						@Override
						public void afterTextChanged(Editable s) {

							String text =
									Objects.requireNonNull(
													bottomSheetNotesBinding.contents.getText())
											.toString();

							if (!text.isEmpty()) {

								updateNote(text, noteId);
							}
						}

						@Override
						public void beforeTextChanged(
								CharSequence s, int start, int count, int after) {}

						@Override
						public void onTextChanged(
								CharSequence s, int start, int before, int count) {}
					});
		} else if (source.equalsIgnoreCase("new")) {

			bottomSheetNotesBinding.contents.addTextChangedListener(
					new TextWatcher() {

						@Override
						public void afterTextChanged(Editable s) {

							String text =
									Objects.requireNonNull(
													bottomSheetNotesBinding.contents.getText())
											.toString();

							if (!text.isEmpty() && text.length() > 4) {

								if (noteId > 0) {
									updateNote(text, noteId);
								} else {
									noteId =
											(int)
													notesApi.insertNote(
															text,
															(int) Instant.now().getEpochSecond());
								}
							}
						}

						@Override
						public void beforeTextChanged(
								CharSequence s, int start, int count, int after) {}

						@Override
						public void onTextChanged(
								CharSequence s, int start, int before, int count) {}
					});
		}

		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) requireContext())
				.getWindowManager()
				.getDefaultDisplay()
				.getMetrics(displayMetrics);
		int height = displayMetrics.heightPixels;
		bottomSheetNotesBinding.contents.setMinHeight(height);
		bottomSheetNotesBinding.renderContents.setMinHeight(height);

		return bottomSheetNotesBinding.getRoot();
	}

	private void updateNote(String content, int noteId) {
		notesApi.updateNote(content, Instant.now().getEpochSecond(), noteId);
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_notes);

		dialog.setOnShowListener(
				dialogInterface -> {
					BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
					View bottomSheet =
							bottomSheetDialog.findViewById(
									com.google.android.material.R.id.design_bottom_sheet);

					if (bottomSheet != null) {

						BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
						behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
						behavior.setPeekHeight(bottomSheet.getHeight());
						behavior.setHideable(false);
					}
				});

		if (dialog.getWindow() != null) {

			WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
			params.height = WindowManager.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setAttributes(params);
		}

		return dialog;
	}

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		try {
			bottomSheetListener = (BottomSheetListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}
}
