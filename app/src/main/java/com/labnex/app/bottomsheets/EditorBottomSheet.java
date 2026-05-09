package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.CodeView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.core.MainGrammarLocator;
import com.labnex.app.databinding.BottomsheetEditorBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.codeeditor.CustomCodeViewAdapter;
import com.labnex.app.helpers.codeeditor.SourcePositionListener;
import com.labnex.app.helpers.codeeditor.languages.Language;
import com.labnex.app.helpers.codeeditor.theme.Theme;
import com.vdurmont.emoji.EmojiParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mmarif
 */
public class EditorBottomSheet extends BottomSheetDialogFragment {

	public enum EditorMode {
		STANDARD, // Mentions enabled, no syntax highlighting, regular font
		MARKDOWN, // Mentions enabled, Markdown preview available, regular font
		CODE // No mentions, no Markdown preview, monospace font, syntax highlighting
	}

	private static final String CONTENT = "content";
	private static final String PROJECT_CONTEXT = "project_context";
	private static final String EDITOR_MODE = "editor_mode";
	private static final String FILE_EXTENSION = "file_extension";

	private BottomsheetEditorBinding binding;
	private ProjectsContext projectsContext;
	private EditorMode editorMode = EditorMode.STANDARD;
	private String fileExtension;
	private boolean isMarkdownMode = false;
	private EditorListener listener;

	public interface EditorListener {
		void onContentChanged(String newContent);
	}

	public static EditorBottomSheet newInstance(
			String content,
			ProjectsContext projectsContext,
			EditorMode mode,
			@Nullable String fileExtension) {
		EditorBottomSheet fragment = new EditorBottomSheet();
		Bundle args = new Bundle();
		args.putString(CONTENT, content);
		args.putSerializable(PROJECT_CONTEXT, projectsContext);
		args.putString(EDITOR_MODE, mode.name());
		if (fileExtension != null) {
			args.putString(FILE_EXTENSION, fileExtension);
		}
		fragment.setArguments(args);
		return fragment;
	}

	public static EditorBottomSheet newInstance(
			String content, ProjectsContext projectsContext, boolean showNotes, boolean showMd) {
		EditorMode mode = showMd ? EditorMode.MARKDOWN : EditorMode.STANDARD;
		return newInstance(content, projectsContext, mode, null);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			projectsContext = (ProjectsContext) getArguments().getSerializable(PROJECT_CONTEXT);
			String modeStr = getArguments().getString(EDITOR_MODE);
			if (modeStr != null) {
				editorMode = EditorMode.valueOf(modeStr);
			}
			fileExtension = getArguments().getString(FILE_EXTENSION);
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetEditorBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		String content = getArguments() != null ? getArguments().getString(CONTENT) : "";
		if (content == null) content = "";

		setupByMode(content);
		setupListeners();
	}

	private void setupByMode(String content) {
		switch (editorMode) {
			case CODE:
				binding.fullscreenEditor.setVisibility(View.GONE);
				binding.fullscreenCodeView.setVisibility(View.VISIBLE);
				binding.fullscreenBtnNotes.setVisibility(View.GONE);
				binding.fullscreenBtnMarkdown.setVisibility(View.GONE);
				binding.fullscreenBtnClear.setVisibility(View.GONE);
				binding.editorInfoLayout.setVisibility(View.VISIBLE);
				binding.editorCursorPosition.setVisibility(View.VISIBLE);

				if (fileExtension != null) {
					binding.editorFileExt.setText(fileExtension);
				}

				setupCodeView(content);
				break;

			case MARKDOWN:
				binding.fullscreenEditor.setVisibility(View.VISIBLE);
				binding.fullscreenCodeView.setVisibility(View.GONE);
				binding.fullscreenBtnNotes.setVisibility(View.VISIBLE);
				binding.fullscreenBtnMarkdown.setVisibility(View.VISIBLE);
				binding.fullscreenBtnClear.setVisibility(View.VISIBLE);
				binding.editorInfoLayout.setVisibility(View.GONE);

				binding.fullscreenEditor.setText(content);
				binding.fullscreenEditor.setSelection(content.length());
				break;

			case STANDARD:
			default:
				binding.fullscreenEditor.setVisibility(View.VISIBLE);
				binding.fullscreenCodeView.setVisibility(View.GONE);
				binding.fullscreenBtnNotes.setVisibility(View.VISIBLE);
				binding.fullscreenBtnMarkdown.setVisibility(View.GONE);
				binding.fullscreenBtnClear.setVisibility(View.VISIBLE);
				binding.editorInfoLayout.setVisibility(View.GONE);

				binding.fullscreenEditor.setText(content);
				binding.fullscreenEditor.setSelection(content.length());
				break;
		}
	}

	private void setupCodeView(String content) {
		CodeView codeView = binding.fullscreenCodeView;
		codeView.setTypeface(android.graphics.Typeface.MONOSPACE);

		String language =
				fileExtension != null ? MainGrammarLocator.fromExtension(fileExtension) : "text";
		Language lang = Language.fromName(language);

		lang.applyTheme(requireContext(), codeView, Theme.getDefaultTheme(requireContext()));

		TypedValue bgValue = new TypedValue();
		requireContext()
				.getTheme()
				.resolveAttribute(
						com.google.android.material.R.attr.colorSurfaceContainerLow, bgValue, true);
		codeView.setBackgroundColor(bgValue.data);

		TypedValue textValue = new TypedValue();
		requireContext()
				.getTheme()
				.resolveAttribute(
						com.google.android.material.R.attr.colorOnSurface, textValue, true);
		codeView.setTextColor(textValue.data);

		TypedValue gutterValue = new TypedValue();
		requireContext()
				.getTheme()
				.resolveAttribute(
						com.google.android.material.R.attr.colorSurfaceContainerHigh,
						gutterValue,
						true);

		setupIndentation(codeView, lang);

		Map<Character, Character> pairCompleteMap = new HashMap<>();
		pairCompleteMap.put('{', '}');
		pairCompleteMap.put('[', ']');
		pairCompleteMap.put('(', ')');
		pairCompleteMap.put('<', '>');
		pairCompleteMap.put('"', '"');
		pairCompleteMap.put('\'', '\'');
		codeView.setPairCompleteMap(pairCompleteMap);
		codeView.enablePairComplete(true);
		codeView.enablePairCompleteCenterCursor(true);

		List<Code> codeList = lang.getCodeList();
		if (codeList != null && !codeList.isEmpty()) {
			CustomCodeViewAdapter adapter = new CustomCodeViewAdapter(requireContext(), codeList);
			codeView.setAdapter(adapter);
		}

		setupCursorPositionTracker();

		if (content != null && !content.isEmpty()) {
			codeView.setText(content);
		}
	}

	private void setupIndentation(CodeView codeView, Language lang) {
		codeView.setIndentationStarts(lang.getIndentationStarts());
		codeView.setIndentationEnds(lang.getIndentationEnds());

		int tabWidth = 4;
		codeView.setTabLength(tabWidth);
	}

	private void setupCursorPositionTracker() {
		binding.editorCursorPosition.setVisibility(View.VISIBLE);
		SourcePositionListener sourcePositionListener =
				new SourcePositionListener(binding.fullscreenCodeView);
		sourcePositionListener.setOnPositionChanged(
				(line, column) -> {
					binding.editorCursorPosition.setText(
							getString(R.string.cursor_position_format, line, column));
				});
	}

	private void setupListeners() {
		binding.fullscreenBtnCollapse.setOnClickListener(
				v -> {
					String newContent;

					if (editorMode == EditorMode.CODE) {
						newContent = binding.fullscreenCodeView.getText().toString();
					} else {
						newContent =
								binding.fullscreenEditor.getText() != null
										? binding.fullscreenEditor.getText().toString()
										: "";
					}

					if (listener != null) {
						listener.onContentChanged(newContent);
					}
					dismiss();
				});

		binding.fullscreenBtnClear.setOnClickListener(
				v -> {
					binding.fullscreenEditor.setText("");
					if (isMarkdownMode) {
						binding.fullscreenMarkdownPreview.setAdapter(null);
					}
				});

		binding.fullscreenBtnNotes.setOnClickListener(
				v -> {
					NotesPickerBottomSheet notesPicker = NotesPickerBottomSheet.newInstance();
					notesPicker.setOnNoteSelectedListener(
							noteContent -> {
								int start = binding.fullscreenEditor.getSelectionStart();
								binding.fullscreenEditor.getText().insert(start, noteContent);
							});
					notesPicker.show(getChildFragmentManager(), "NOTES_PICKER");
				});

		binding.fullscreenBtnMarkdown.setOnClickListener(
				v -> {
					isMarkdownMode = !isMarkdownMode;
					binding.fullscreenBtnMarkdown.setIconResource(
							isMarkdownMode ? R.drawable.ic_edit : R.drawable.ic_markdown);

					if (isMarkdownMode) {
						binding.fullscreenEditor.setVisibility(View.GONE);
						String editorContent =
								binding.fullscreenEditor.getText() != null
										? binding.fullscreenEditor.getText().toString()
										: "";

						if (projectsContext != null) {
							binding.fullscreenMarkdownScroll.setVisibility(View.VISIBLE);
							binding.fullscreenMarkdownScrollText.setVisibility(View.GONE);
							Markdown.render(
									requireContext(),
									EmojiParser.parseToUnicode(editorContent),
									binding.fullscreenMarkdownPreview,
									projectsContext);
						} else {
							binding.fullscreenMarkdownScroll.setVisibility(View.GONE);
							binding.fullscreenMarkdownScrollText.setVisibility(View.VISIBLE);
							Markdown.render(
									requireContext(),
									EmojiParser.parseToUnicode(editorContent),
									binding.fullscreenMarkdownPreviewText);
						}
					} else {
						binding.fullscreenMarkdownScroll.setVisibility(View.GONE);
						binding.fullscreenMarkdownScrollText.setVisibility(View.GONE);
						binding.fullscreenEditor.setVisibility(View.VISIBLE);
						binding.fullscreenEditor.requestFocus();
						String currentText =
								binding.fullscreenEditor.getText() != null
										? binding.fullscreenEditor.getText().toString()
										: "";
						binding.fullscreenEditor.setSelection(currentText.length());
					}
				});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	public void setEditorListener(EditorListener listener) {
		this.listener = listener;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
