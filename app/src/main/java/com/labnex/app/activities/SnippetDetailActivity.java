package com.labnex.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.ActivitySnippetViewBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.SyntaxHighlightedArea;
import com.labnex.app.models.snippets.SnippetsItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.ResponseBody;
import org.apache.commons.io.FilenameUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class SnippetDetailActivity extends BaseActivity {

	private ActivitySnippetViewBinding binding;
	private int snippetId = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String mode = getIntent().getStringExtra("MODE");
		snippetId = getIntent().getIntExtra("SNIPPET_ID", -1);

		binding = ActivitySnippetViewBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		if ("VIEW".equals(mode) && snippetId != -1) {
			setupViewMode();
		} else {
			finish();
		}
	}

	private void setupViewMode() {
		binding.bottomAppBar.setNavigationOnClickListener(v -> finish());
		binding.progressBar.setVisibility(View.VISIBLE);

		RetrofitClient.getApiInterface(this)
				.getSnippet(snippetId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<SnippetsItem> call,
									@NonNull Response<SnippetsItem> response) {
								if (response.isSuccessful() && response.body() != null) {
									binding.bottomAppBarTitle.setText(response.body().getTitle());
									loadFileContents(response.body());
								} else {
									binding.progressBar.setVisibility(View.GONE);
									Snackbar.info(
											SnippetDetailActivity.this,
											binding.getRoot(),
											getString(R.string.generic_api_error));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<SnippetsItem> call, @NonNull Throwable t) {
								binding.progressBar.setVisibility(View.GONE);
								Snackbar.info(
										SnippetDetailActivity.this,
										binding.getRoot(),
										getString(R.string.generic_api_error));
							}
						});
	}

	private void loadFileContents(SnippetsItem snippet) {

		List<String> fileNames = new ArrayList<>();
		List<String> refs = new ArrayList<>();
		List<String> filePaths = new ArrayList<>();

		if (snippet.getFiles() != null && !snippet.getFiles().isEmpty()) {
			for (int i = 0; i < snippet.getFiles().size(); i++) {
				String fileName = snippet.getFiles().get(i).getPath();
				String rawUrl = snippet.getFiles().get(i).getRawUrl();
				fileNames.add(fileName);
				String[] refAndPath = parseRefAndPath(rawUrl, fileName);
				refs.add(refAndPath[0]);
				filePaths.add(refAndPath[1]);
			}
		} else if (snippet.getFileName() != null && !snippet.getFileName().isEmpty()) {
			String fileName = snippet.getFileName();
			fileNames.add(fileName);
			refs.add("main");
			filePaths.add(fileName);
		}

		if (fileNames.isEmpty()) {
			binding.progressBar.setVisibility(View.GONE);
			Snackbar.info(this, binding.getRoot(), getString(R.string.no_files_snippet));
			return;
		}

		LinearLayout filesContainer = binding.filesContainer;
		filesContainer.removeAllViews();

		int[] loadedFiles = {0};
		int totalFiles = fileNames.size();

		for (int i = 0; i < totalFiles; i++) {

			String fileName = fileNames.get(i);
			String filePath = filePaths.get(i);
			String ref = refs.get(i);

			View fileView =
					LayoutInflater.from(this)
							.inflate(R.layout.list_snippet_file, filesContainer, false);
			TextView fileNameView = fileView.findViewById(R.id.file_name);
			SyntaxHighlightedArea fileContentView = fileView.findViewById(R.id.file_content);
			fileNameView.setText(fileName);
			fileContentView.setContent("", FilenameUtils.getExtension(fileName));
			filesContainer.addView(fileView);

			RetrofitClient.getApiInterface(this)
					.getSnippetFileContent(snippetId, ref, filePath)
					.enqueue(
							new Callback<ResponseBody>() {
								@Override
								public void onResponse(
										@NonNull Call<ResponseBody> call,
										@NonNull Response<ResponseBody> response) {
									String content = "";
									if (response.isSuccessful()) {
										try (ResponseBody body = response.body()) {
											if (body != null) {
												content = body.string();
												fileContentView.setContent(
														content,
														FilenameUtils.getExtension(fileName));
											} else {
												Snackbar.info(
														SnippetDetailActivity.this,
														binding.getRoot(),
														getString(R.string.file_content_error));
											}
										} catch (IOException e) {
											Snackbar.info(
													SnippetDetailActivity.this,
													binding.getRoot(),
													getString(R.string.file_content_error));
										}
									} else {
										Snackbar.info(
												SnippetDetailActivity.this,
												binding.getRoot(),
												getString(R.string.file_content_error));
									}
									loadedFiles[0]++;
									if (loadedFiles[0] == totalFiles) {
										binding.progressBar.setVisibility(View.GONE);
									}
								}

								@Override
								public void onFailure(
										@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

									fileContentView.setContent(
											"", FilenameUtils.getExtension(fileName));
									Snackbar.info(
											SnippetDetailActivity.this,
											binding.getRoot(),
											getString(R.string.file_content_error));
									loadedFiles[0]++;
									if (loadedFiles[0] == totalFiles) {
										binding.progressBar.setVisibility(View.GONE);
									}
								}
							});
		}
	}

	private String[] parseRefAndPath(String rawUrl, String fileName) {
		String ref = "main";
		String filePath = fileName;
		if (rawUrl != null && !rawUrl.isEmpty()) {
			Pattern pattern = Pattern.compile(".*/raw/([^/]+)/(.+)");
			Matcher matcher = pattern.matcher(rawUrl);
			if (matcher.find()) {
				ref = matcher.group(1);
				filePath = matcher.group(2);
			}
		}
		return new String[] {ref, filePath};
	}
}
