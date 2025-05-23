package com.labnex.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.CodeEditorActivity;
import com.labnex.app.databinding.FragmentSnippetFileBinding;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;

/**
 * @author mmarif
 */
public class SnippetFileFragment extends Fragment {

	private FragmentSnippetFileBinding binding;
	private String fileName = "";
	private String fileContent = "";
	private static final String ARG_FILE_NAME = "file_name";
	private static final String ARG_FILE_CONTENT = "file_content";
	private static final String ARG_POSITION = "position";
	private OnFileNameChangedListener listener;
	private int position;

	private final ActivityResultLauncher<Intent> codeEditorLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK
								&& result.getData() != null) {
							String updatedContent =
									result.getData().getStringExtra("fileContentFromActivity");
							if (!TextUtils.isEmpty(updatedContent)) {
								fileContent = updatedContent;
								if (binding != null) {
									binding.fileContent.setText(updatedContent);
								}
							}
						}
					});

	public interface OnFileNameChangedListener {
		void onFileNameChanged(int position, String newFileName);
	}

	public static SnippetFileFragment newInstance(
			String fileName, String fileContent, int position) {
		SnippetFileFragment fragment = new SnippetFileFragment();
		Bundle args = new Bundle();
		args.putString(ARG_FILE_NAME, fileName);
		args.putString(ARG_FILE_CONTENT, fileContent);
		args.putInt(ARG_POSITION, position);
		fragment.setArguments(args);
		return fragment;
	}

	public void setOnFileNameChangedListener(OnFileNameChangedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			fileName = args.getString(ARG_FILE_NAME, "file1.txt");
			fileContent = args.getString(ARG_FILE_CONTENT, "");
			position = args.getInt(ARG_POSITION, 0);
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentSnippetFileBinding.inflate(inflater, container, false);

		binding.fileName.setText(fileName);
		binding.fileContent.setText(fileContent);

		binding.fileName.addTextChangedListener(
				new TextWatcher() {
					@Override
					public void beforeTextChanged(
							CharSequence s, int start, int count, int after) {}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {}

					@Override
					public void afterTextChanged(Editable s) {
						String newFileName = s.toString().trim();
						if (!newFileName.isEmpty() && listener != null) {
							listener.onFileNameChanged(position, newFileName);
							fileName = newFileName;
						}
						binding.fileName.requestFocus();
					}
				});

		binding.openCodeEditor.setOnClickListener(
				v -> {
					String currentFileName =
							Objects.requireNonNull(binding.fileName.getText()).toString().trim();
					String currentFileContent =
							Objects.requireNonNull(binding.fileContent.getText()).toString();
					if (currentFileName.isEmpty()) {
						new MaterialAlertDialogBuilder(requireContext())
								.setMessage(R.string.ce_no_filename_filled)
								.setPositiveButton(
										R.string.ignore,
										(dialog, which) -> launchCodeEditor(currentFileContent, ""))
								.setNeutralButton(R.string.cancel, null)
								.show();
					} else {
						launchCodeEditor(
								currentFileContent, FilenameUtils.getExtension(currentFileName));
					}
				});

		return binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	public String getFileName() {
		if (binding == null) {
			return fileName;
		}
		String name = Objects.requireNonNull(binding.fileName.getText()).toString().trim();
		fileName = name;
		return name;
	}

	public String getFileContent() {
		if (binding == null) {
			return fileContent;
		}
		String content = Objects.requireNonNull(binding.fileContent.getText()).toString();
		fileContent = content;
		return content;
	}

	private void launchCodeEditor(String fileContent, String fileExtension) {
		Intent intent = new Intent(requireContext(), CodeEditorActivity.class);
		intent.putExtra("fileExtension", fileExtension);
		intent.putExtra("fileContent", fileContent);
		codeEditorLauncher.launch(intent);
	}
}
