package com.labnex.app.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;
import com.labnex.app.R;
import com.labnex.app.adapters.SnippetFilePagerAdapter;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.ActivitySnippetCreateBinding;
import com.labnex.app.databinding.ActivitySnippetViewBinding;
import com.labnex.app.fragments.SnippetFileFragment;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.SyntaxHighlightedArea;
import com.labnex.app.models.snippets.SnippetCreateModel;
import com.labnex.app.models.snippets.SnippetsItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
public class SnippetDetailActivity extends BaseActivity
		implements SnippetFileFragment.OnFileNameChangedListener {

	private ActivitySnippetViewBinding binding;
	private ActivitySnippetCreateBinding createBinding;
	private SnippetFilePagerAdapter pagerAdapter;
	private int snippetId = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String mode = getIntent().getStringExtra("MODE");
		snippetId = getIntent().getIntExtra("SNIPPET_ID", -1);

		if ("VIEW".equals(mode) && snippetId != -1) {
			binding = ActivitySnippetViewBinding.inflate(getLayoutInflater());
			setContentView(binding.getRoot());
			setupViewMode();
		} else if ("CREATE".equals(mode)) {
			createBinding = ActivitySnippetCreateBinding.inflate(getLayoutInflater());
			setContentView(createBinding.getRoot());
			setupCreateMode();
		} else {
			finish();
		}
	}

	private void setupCreateMode() {
		createBinding.bottomAppBar.setNavigationOnClickListener(v -> finish());
		updateFileCount(1); // Initial file count

		// Initialize visibility AutoCompleteTextView
		ArrayAdapter<CharSequence> adapter =
				ArrayAdapter.createFromResource(
						this,
						R.array.snippet_visibility_options,
						android.R.layout.simple_dropdown_item_1line);
		createBinding.visibility.setAdapter(adapter);
		createBinding.visibility.setText(
				getString(R.string.visibility_private), false); // Default: Private

		// Initialize ViewPager2 and TabLayout
		pagerAdapter = new SnippetFilePagerAdapter(this, this);
		createBinding.viewPager.setAdapter(pagerAdapter);
		new TabLayoutMediator(
						createBinding.tabLayout,
						createBinding.viewPager,
						(tab, position) -> {
							View tabView =
									LayoutInflater.from(this)
											.inflate(
													R.layout.fragment_tab_custom_view,
													createBinding.tabLayout,
													false);
							TextView tabText = tabView.findViewById(R.id.tab_text);
							tabText.setText(pagerAdapter.getFileName(position));
							ImageView deleteIcon = tabView.findViewById(R.id.tab_delete);
							if (pagerAdapter.getItemCount() > 1) {
								deleteIcon.setVisibility(View.VISIBLE);
								deleteIcon.setOnClickListener(
										v -> {
											new MaterialAlertDialogBuilder(this)
													.setTitle(R.string.delete_file)
													.setMessage(R.string.delete_file_confirmation)
													.setNeutralButton(R.string.cancel, null)
													.setPositiveButton(
															R.string.delete,
															(dialog, which) -> {
																pagerAdapter.removeFile(position);
																updateFileCount(
																		pagerAdapter
																				.getItemCount());
																Snackbar.info(
																		this,
																		createBinding.bottomAppBar,
																		getString(
																				R.string
																						.file_deleted));
															})
													.show();
										});
							} else {
								deleteIcon.setVisibility(View.GONE);
							}
							tab.setCustomView(tabView);
						})
				.attach();

		createBinding.newTab.setOnClickListener(
				v -> {
					if (pagerAdapter.getItemCount() < 10) {
						pagerAdapter.addFile(
								"file" + (pagerAdapter.getItemCount() + 1) + ".txt", "");
						updateFileCount(pagerAdapter.getItemCount());
					} else {
						Snackbar.info(
								this,
								createBinding.bottomAppBar,
								getString(R.string.max_files_reached));
					}
				});

		createBinding.create.setOnClickListener(v -> createSnippet());
	}

	private void createSnippet() {

		String title = Objects.requireNonNull(createBinding.title.getText()).toString().trim();
		String description =
				Objects.requireNonNull(createBinding.description.getText()).toString().trim();
		String visibilityText = createBinding.visibility.getText().toString().trim();
		String visibility = mapVisibility(visibilityText);

		if (title.isEmpty()) {
			Snackbar.info(this, createBinding.bottomAppBar, getString(R.string.title_required));
			return;
		}

		List<SnippetCreateModel.File> files = new ArrayList<>();
		Set<String> fileNames = new HashSet<>();
		for (SnippetFileFragment fragment : pagerAdapter.getFragments()) {
			String fileName = fragment.getFileName();
			if (fileName == null || fileName.isEmpty()) {
				continue;
			}
			if (!fileNames.add(fileName)) {
				Snackbar.info(
						this, createBinding.bottomAppBar, getString(R.string.duplicate_file_name));
				return;
			}
			String fileContent = fragment.getFileContent();
			if (fileContent == null || fileContent.trim().isEmpty()) {
				Snackbar.info(
						this, createBinding.bottomAppBar, getString(R.string.empty_file_content));
				return;
			}
			files.add(new SnippetCreateModel.File(fileName, fileContent));
		}

		if (files.isEmpty()) {
			Snackbar.info(this, createBinding.bottomAppBar, getString(R.string.at_least_one_file));
			return;
		}

		SnippetCreateModel model = new SnippetCreateModel(title, description, visibility, files);
		createBinding.progressBar.setVisibility(View.VISIBLE);
		createBinding.create.setEnabled(false);

		RetrofitClient.getApiInterface(this)
				.createSnippet(model)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<SnippetsItem> call,
									@NonNull Response<SnippetsItem> response) {
								createBinding.progressBar.setVisibility(View.GONE);
								createBinding.create.setEnabled(true);
								if (response.isSuccessful() && response.body() != null) {
									createBinding.create.setEnabled(false);
									com.google.android.material.snackbar.Snackbar snackbar =
											com.google.android.material.snackbar.Snackbar.make(
													createBinding.bottomAppBar,
													getString(R.string.snippet_created),
													com.google.android.material.snackbar.Snackbar
															.LENGTH_LONG);
									snackbar.setAnchorView(createBinding.bottomAppBar);
									snackbar.addCallback(
											new com.google.android.material.snackbar.Snackbar
													.Callback() {
												@Override
												public void onDismissed(
														com.google.android.material.snackbar
																		.Snackbar
																transientBottomBar,
														int event) {
													setResult(RESULT_OK);
													finish();
												}
											});
									snackbar.show();
								} else if (response.code() == 401) {
									Snackbar.info(
											SnippetDetailActivity.this,
											createBinding.bottomAppBar,
											getString(R.string.not_authorized));
								} else if (response.code() == 403) {
									Snackbar.info(
											SnippetDetailActivity.this,
											createBinding.bottomAppBar,
											getString(R.string.access_forbidden_403));
								} else {
									Snackbar.info(
											SnippetDetailActivity.this,
											createBinding.bottomAppBar,
											getString(R.string.generic_api_error, response.code()));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<SnippetsItem> call, @NonNull Throwable t) {
								createBinding.progressBar.setVisibility(View.GONE);
								createBinding.create.setEnabled(true);
								Snackbar.info(
										SnippetDetailActivity.this,
										createBinding.bottomAppBar,
										getString(R.string.generic_server_response_error));
							}
						});
	}

	@Override
	public void onFileNameChanged(int position, String newFileName) {
		pagerAdapter.updateFileName(position, newFileName);
		View tabView =
				Objects.requireNonNull(createBinding.tabLayout.getTabAt(position)).getCustomView();
		if (tabView != null) {
			TextView tabText = tabView.findViewById(R.id.tab_text);
			tabText.setText(newFileName);
		}
	}

	private String mapVisibility(String visibilityText) {
		if (visibilityText.equals(getString(R.string.visibility_internal))) {
			return "internal";
		} else if (visibilityText.equals(getString(R.string.visibility_public))) {
			return "public";
		} else {
			return "private";
		}
	}

	private void updateFileCount(int count) {
		createBinding.bottomAppBarTitle.setText(
				getString(R.string.create_snippet_with_count, count));
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
