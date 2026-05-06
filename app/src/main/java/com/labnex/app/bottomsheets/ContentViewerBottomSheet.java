package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.BottomsheetContentViewerBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mmarif
 */
public class ContentViewerBottomSheet extends BottomSheetDialogFragment {

	public enum Feature {
		MARKDOWN_PREVIEW,
		START_IN_MARKDOWN,
		ALLOW_COPY,
		ALLOW_SHARE,
		SYNTAX_HIGHLIGHT,
		SHOW_TITLE,
		IMAGE_PREVIEW
	}

	private static final String ARG_CONTENT = "content";
	private static final String ARG_TITLE = "title";
	private static final String ARG_PROJECTS_CONTEXT = "project_context";
	private static final String ARG_FEATURES = "features";
	private static final String ARG_FILE_EXTENSION = "file_extension";
	private static final String ARG_IS_IMAGE = "is_image";

	private BottomsheetContentViewerBinding binding;
	private final Set<Feature> enabledFeatures = new HashSet<>();
	private ProjectsContext projectsContext;
	private String rawContent;
	private String title;
	private String fileExtension;
	private byte[] imageBytes;
	private boolean isMarkdownMode = false;

	public static ContentViewerBottomSheet newInstance(
			@Nullable String content,
			@Nullable byte[] imageData,
			@Nullable String title,
			@Nullable ProjectsContext projectsContext,
			@Nullable String fileExtension,
			Feature... features) {
		ContentViewerBottomSheet fragment = new ContentViewerBottomSheet();
		Bundle args = new Bundle();
		if (imageData != null) {
			args.putByteArray(ARG_CONTENT, imageData);
			args.putBoolean(ARG_IS_IMAGE, true);
		} else {
			args.putString(ARG_CONTENT, content != null ? content : "");
			args.putBoolean(ARG_IS_IMAGE, false);
		}
		if (title != null) args.putString(ARG_TITLE, title);
		if (projectsContext != null) args.putSerializable(ARG_PROJECTS_CONTEXT, projectsContext);
		if (fileExtension != null) args.putString(ARG_FILE_EXTENSION, fileExtension);
		args.putStringArrayList(ARG_FEATURES, featureNamesToList(features));
		fragment.setArguments(args);
		return fragment;
	}

	private static ArrayList<String> featureNamesToList(Feature... features) {
		ArrayList<String> names = new ArrayList<>();
		for (Feature f : features) {
			names.add(f.name());
		}
		return names;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			boolean isImage = getArguments().getBoolean(ARG_IS_IMAGE, false);

			if (isImage) {
				imageBytes = getArguments().getByteArray(ARG_CONTENT);
			} else {
				rawContent = getArguments().getString(ARG_CONTENT, "");
			}

			title = getArguments().getString(ARG_TITLE);
			projectsContext =
					(ProjectsContext) getArguments().getSerializable(ARG_PROJECTS_CONTEXT);
			fileExtension = getArguments().getString(ARG_FILE_EXTENSION);

			ArrayList<String> featureNames = getArguments().getStringArrayList(ARG_FEATURES);
			if (featureNames != null) {
				for (String name : featureNames) {
					try {
						enabledFeatures.add(Feature.valueOf(name));
					} catch (IllegalArgumentException ignored) {
					}
				}
			}
		}
		isMarkdownMode = enabledFeatures.contains(Feature.START_IN_MARKDOWN);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetContentViewerBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setupUI();
		renderContent();
	}

	private void setupUI() {
		binding.btnClose.setOnClickListener(v -> dismiss());

		if (enabledFeatures.contains(Feature.ALLOW_COPY)) {
			binding.btnCopy.setVisibility(View.VISIBLE);
			binding.btnCopy.setOnClickListener(v -> copyContent());
		}

		if (enabledFeatures.contains(Feature.ALLOW_SHARE)) {
			binding.btnShare.setVisibility(View.VISIBLE);
			binding.btnShare.setOnClickListener(v -> shareContent());
		}

		if (enabledFeatures.contains(Feature.MARKDOWN_PREVIEW)) {
			binding.btnMarkdown.setVisibility(View.VISIBLE);
			binding.btnMarkdown.setOnClickListener(v -> toggleMarkdownMode());
			updateMarkdownIcon();
		}

		if (enabledFeatures.contains(Feature.SHOW_TITLE) && title != null) {
			binding.viewerTitle.setText(title);
			binding.viewerTitle.setVisibility(View.VISIBLE);
		}
	}

	private void renderContent() {
		if (enabledFeatures.contains(Feature.IMAGE_PREVIEW) && imageBytes != null) {
			binding.rawContentScroll.setVisibility(View.GONE);
			binding.syntaxHighlightedCodeScroll.setVisibility(View.GONE);
			binding.markdownPreviewScroll.setVisibility(View.GONE);
			binding.photoView.setVisibility(View.VISIBLE);

			Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
			if (bitmap != null) {
				binding.photoView.setImageBitmap(bitmap);
			} else {
				Toasty.show(requireContext(), R.string.image_load_error);
			}
			return;
		}

		String content = rawContent != null ? rawContent : "";

		if (enabledFeatures.contains(Feature.SYNTAX_HIGHLIGHT)) {
			binding.rawContentScroll.setVisibility(View.GONE);
			binding.syntaxHighlightedCodeScroll.setVisibility(View.VISIBLE);
			binding.markdownPreviewScroll.setVisibility(View.GONE);
			binding.photoView.setVisibility(View.GONE);
			binding.syntaxHighlightedCode.setContent(content, fileExtension);
		} else if (isMarkdownMode) {
			binding.rawContentScroll.setVisibility(View.GONE);
			binding.syntaxHighlightedCodeScroll.setVisibility(View.GONE);
			binding.markdownPreviewScroll.setVisibility(View.VISIBLE);
			binding.photoView.setVisibility(View.GONE);

			if (projectsContext != null) {
				binding.markdownPreview.setVisibility(View.VISIBLE);
				binding.markdownPreviewText.setVisibility(View.GONE);
				Markdown.render(
						requireContext(), content, binding.markdownPreview, projectsContext);
			} else {
				binding.markdownPreview.setVisibility(View.GONE);
				binding.markdownPreviewText.setVisibility(View.VISIBLE);
				Markdown.render(requireContext(), content, binding.markdownPreviewText);
			}
		} else {
			binding.rawContentScroll.setVisibility(View.VISIBLE);
			binding.syntaxHighlightedCodeScroll.setVisibility(View.GONE);
			binding.markdownPreviewScroll.setVisibility(View.GONE);
			binding.photoView.setVisibility(View.GONE);
			binding.rawContentText.setText(content);
		}
	}

	private void toggleMarkdownMode() {
		isMarkdownMode = !isMarkdownMode;
		updateMarkdownIcon();
		renderContent();
	}

	private void updateMarkdownIcon() {
		binding.btnMarkdown.setIconResource(
				isMarkdownMode ? R.drawable.ic_edit : R.drawable.ic_markdown);
	}

	private void copyContent() {
		Utils.copyToClipboard(
				requireContext(), rawContent, getString(R.string.copied_to_clipboard));
	}

	private void shareContent() {
		Utils.share(requireContext(), rawContent);
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
