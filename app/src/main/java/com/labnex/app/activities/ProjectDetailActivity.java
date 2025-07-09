package com.labnex.app.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.BranchesBottomSheet;
import com.labnex.app.bottomsheets.ProjectLabelsBottomSheet;
import com.labnex.app.bottomsheets.ProjectMembersBottomSheet;
import com.labnex.app.bottomsheets.ProjectMilestonesBottomSheet;
import com.labnex.app.bottomsheets.ProjectReleasesBottomSheet;
import com.labnex.app.bottomsheets.ProjectTagsBottomSheet;
import com.labnex.app.bottomsheets.ProjectWikisBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityProjectDetailBinding;
import com.labnex.app.databinding.BottomSheetProjectMenuBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.TextDrawable.ColorGenerator;
import com.labnex.app.helpers.TextDrawable.TextDrawable;
import com.labnex.app.helpers.Utils;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.branches.Branches;
import com.labnex.app.models.error.ErrorResponse;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.models.repository.FileContents;
import com.vdurmont.emoji.EmojiParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ProjectDetailActivity extends BaseActivity
		implements BottomSheetListener, BranchesBottomSheet.UpdateInterface {

	private ActivityProjectDetailBinding binding;
	public ProjectsContext projectsContext;
	private int projectId;
	private String branch;
	private String README;
	private String source;
	private Bundle bsBundle;
	private Map<String, Integer> languageColors;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityProjectDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		BranchesBottomSheet.setUpdateListener(ProjectDetailActivity.this);

		projectsContext = ProjectsContext.fromIntent(getIntent());
		projectId = projectsContext.getProjectId();

		loadLanguageColors();

		bsBundle = new Bundle();

		if (getIntent().getStringExtra("source") != null) {

			source = getIntent().getStringExtra("source");

			/*if (Objects.requireNonNull(source).equalsIgnoreCase("starred")) {
				binding.projectsText.setText(R.string.starred_projects);
			}*/
		} else {
			source = "";
		}

		binding.filesMainFrame.setOnClickListener(
				files -> {
					ProjectsContext project =
							new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = project.getIntent(ctx, FilesBrowserActivity.class);
					intent.putExtra("source", "project");
					intent.putExtra("projectName", projectsContext.getProjectName());
					intent.putExtra("path", projectsContext.getPath());
					intent.putExtra("projectId", projectId);
					intent.putExtra("branch", branch);
					ctx.startActivity(intent);
				});

		binding.commitsMainFrame.setOnClickListener(
				commits -> {
					ProjectsContext project =
							new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = project.getIntent(ctx, CommitsActivity.class);
					intent.putExtra("source", "project");
					intent.putExtra("projectName", projectsContext.getProjectName());
					intent.putExtra("path", projectsContext.getPath());
					intent.putExtra("projectId", projectId);
					intent.putExtra("branch", branch);
					ctx.startActivity(intent);
				});

		binding.issuesMainFrame.setOnClickListener(
				issues -> {
					ProjectsContext project =
							new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = project.getIntent(ctx, IssuesActivity.class);
					intent.putExtra("source", "project");
					intent.putExtra("projectName", projectsContext.getProjectName());
					intent.putExtra("path", projectsContext.getPath());
					intent.putExtra("id", projectId);
					ctx.startActivity(intent);
				});

		binding.mergeRequestsMainFrame.setOnClickListener(
				mr -> {
					ProjectsContext project =
							new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = project.getIntent(ctx, MergeRequestsActivity.class);
					intent.putExtra("source", "mr");
					intent.putExtra("projectId", projectId);
					intent.putExtra("projectName", projectsContext.getProjectName());
					intent.putExtra("path", projectsContext.getPath());
					ctx.startActivity(intent);
				});

		binding.releasesMainFrame.setOnClickListener(
				releases -> {
					bsBundle.putInt("projectId", projectId);
					ProjectReleasesBottomSheet bottomSheet = new ProjectReleasesBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "projectReleasesBottomSheet");
				});

		binding.tagsMainFrame.setOnClickListener(
				tags -> {
					bsBundle.putInt("projectId", projectId);
					ProjectTagsBottomSheet bottomSheet = new ProjectTagsBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "projectTagsBottomSheet");
				});

		binding.labelsMainFrame.setOnClickListener(
				labels -> {
					bsBundle.putInt("projectId", projectId);
					ProjectLabelsBottomSheet bottomSheet = new ProjectLabelsBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "projectLabelsBottomSheet");
				});

		binding.milestonesMainFrame.setOnClickListener(
				milestones -> {
					bsBundle.putInt("projectId", projectId);
					ProjectMilestonesBottomSheet bottomSheet = new ProjectMilestonesBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "projectMilestonesBottomSheet");
				});

		binding.membersMainFrame.setOnClickListener(
				members -> {
					bsBundle.putInt("projectId", projectId);
					bsBundle.putString("type", "members");
					ProjectMembersBottomSheet bottomSheet = new ProjectMembersBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "projectMembersBottomSheet");
				});

		binding.wikiMainFrame.setOnClickListener(
				wiki -> {
					bsBundle.putInt("projectId", projectId);
					ProjectWikisBottomSheet bottomSheet = new ProjectWikisBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "projectWikisBottomSheet");
				});

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					if (menuItem.getItemId() == R.id.menu) {
						showProjectMenuBottomSheet();
						return true;
					}
					return false;
				});

		binding.newIssue.setOnClickListener(
				newIssue -> {
					ProjectsContext project =
							new ProjectsContext(
									projectsContext.getProjectName(),
									projectsContext.getPath(),
									projectsContext.getProjectId(),
									ctx);
					Intent intent = project.getIntent(ctx, CreateIssueActivity.class);
					ctx.startActivity(intent);
				});

		binding.switchBranch.setOnClickListener(
				branches -> {
					bsBundle.putInt("projectId", projectId);
					bsBundle.putString("source", "project_detail");
					BranchesBottomSheet bottomSheet = new BranchesBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "branchesBottomSheet");
				});

		binding.starAProject.setOnClickListener(starAProject -> starAProject());

		binding.unstarAProject.setOnClickListener(unstarAProject -> unstarAProject());

		getProjectInfo();
		loadUserStars();
	}

	@Override
	public void updateDataListener(String str, String type) {

		branch = str;
		binding.branchTitle.setText(str);
	}

	private void showProjectMenuBottomSheet() {
		BottomSheetProjectMenuBinding sheetBinding =
				BottomSheetProjectMenuBinding.inflate(LayoutInflater.from(this), null, false);
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		LinearLayout createBranchItem = sheetBinding.createBranchItem;
		createBranchItem.setOnClickListener(
				v -> {
					showCreateBranchDialog();
					bottomSheetDialog.dismiss();
				});

		bottomSheetDialog.show();
	}

	private void showCreateBranchDialog() {

		View dialogView =
				LayoutInflater.from(this).inflate(R.layout.custom_create_branch_dialog, null);
		EditText newBranchInput = dialogView.findViewById(R.id.branch_name);
		EditText refInput = dialogView.findViewById(R.id.branch_ref);

		MaterialAlertDialogBuilder builder =
				new MaterialAlertDialogBuilder(this)
						.setTitle(R.string.create_branch)
						.setView(dialogView)
						.setPositiveButton(R.string.create, null)
						.setNeutralButton(R.string.cancel, null);

		final androidx.appcompat.app.AlertDialog dialog = builder.create();
		dialog.show();

		dialog.getButton(DialogInterface.BUTTON_POSITIVE)
				.setOnClickListener(
						v -> {
							String newBranch = newBranchInput.getText().toString().trim();
							String ref = refInput.getText().toString().trim();

							if (newBranch.isEmpty() || ref.isEmpty()) {
								Snackbar.info(
										this,
										binding.bottomAppBar,
										getString(R.string.branch_ref_required));
							} else {
								createBranch(newBranch, ref, dialog);
							}
						});
	}

	private void createBranch(
			String branch, String ref, androidx.appcompat.app.AlertDialog dialog) {

		Call<Branches> call =
				RetrofitClient.getApiInterface(this).createBranch(projectId, branch, ref);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Branches> call, @NonNull Response<Branches> response) {

						if (response.isSuccessful() && response.code() == 201) {

							Snackbar.info(
									ProjectDetailActivity.this,
									findViewById(R.id.bottom_app_bar),
									getString(R.string.branch_created, branch));
							dialog.dismiss();
						} else {

							String errorMessage;
							try (okhttp3.ResponseBody errorBody = response.errorBody()) {

								if (errorBody != null) {

									Gson gson = new Gson();
									ErrorResponse errorResponse =
											gson.fromJson(errorBody.string(), ErrorResponse.class);
									errorMessage =
											errorResponse.getMessage() != null
													? errorResponse.getMessage()
													: getString(R.string.generic_error);

								} else if (response.code() == 401) {

									errorMessage = getString(R.string.not_authorized);
								} else if (response.code() == 403) {

									errorMessage = getString(R.string.access_forbidden_403);
								} else {

									errorMessage = getString(R.string.generic_error);
								}
							} catch (IOException e) {
								errorMessage = getString(R.string.generic_error);
							}

							Snackbar.info(
									ProjectDetailActivity.this,
									findViewById(R.id.bottom_app_bar),
									errorMessage);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Branches> call, @NonNull Throwable t) {

						Snackbar.info(
								ProjectDetailActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void getProjectInfo() {

		Call<Projects> call = RetrofitClient.getApiInterface(ctx).getProjectInfo(projectId);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Projects> call, @NonNull Response<Projects> response) {

						Projects projectDetails = response.body();

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								assert projectDetails != null;

								projectsContext.setProject(projectDetails);
								projectsContext.setBranchRef(
										projectsContext.getProject().getDefaultBranch());

								binding.progressBar.setVisibility(View.GONE);
								binding.basicInfoFrame.setVisibility(View.VISIBLE);
								binding.codeSection.setVisibility(View.VISIBLE);
								binding.branchSection.setVisibility(View.VISIBLE);

								if (projectDetails.isArchived()) {
									binding.archivedProjectFrame.setVisibility(View.VISIBLE);
								}

								if (!projectDetails.isArchived()
										&& projectDetails.isIssuesEnabled()
										&& !Objects.equals(source, "most_visited")) {
									binding.newIssue.setVisibility(View.VISIBLE);
								}

								ColorGenerator generator = ColorGenerator.MATERIAL;
								int color = generator.getColor(projectDetails.getName());
								String firstCharacter =
										String.valueOf(projectDetails.getName().charAt(0));

								TextDrawable drawable =
										TextDrawable.builder()
												.beginConfig()
												.useFont(Typeface.DEFAULT)
												.fontSize(16)
												.toUpperCase()
												.width(28)
												.height(28)
												.endConfig()
												.buildRoundRect(firstCharacter, color, 8);

								if (projectDetails.getAvatarUrl() != null
										&& projectDetails
												.getVisibility()
												.equalsIgnoreCase("public")) {

									Glide.with(ctx)
											.load(projectDetails.getAvatarUrl())
											.diskCacheStrategy(DiskCacheStrategy.ALL)
											.placeholder(R.drawable.ic_spinner)
											.centerCrop()
											.into(binding.projectAvatar);
								} else {
									binding.projectAvatar.setImageDrawable(drawable);
								}

								binding.projectName.setText(projectDetails.getName());
								binding.projectPath.setText(projectDetails.getPathWithNamespace());
								if (projectDetails.getDescription() != null
										&& !projectDetails.getDescription().isEmpty()) {
									binding.projectDescription.setVisibility(View.VISIBLE);
									binding.projectDescription.setText(
											projectDetails.getDescription());
								}

								binding.projectStars.setText(
										getResources()
												.getQuantityString(
														R.plurals.project_stars,
														projectDetails.getStarCount(),
														Utils.numberFormatter(
																projectDetails.getStarCount())));
								binding.projectForks.setText(
										getResources()
												.getQuantityString(
														R.plurals.project_forks,
														projectDetails.getForksCount(),
														Utils.numberFormatter(
																projectDetails.getForksCount())));
								binding.issuesOpenCount.setText(
										Utils.numberFormatter(projectDetails.getOpenIssuesCount()));

								binding.branchTitle.setText(projectDetails.getDefaultBranch());
								branch = projectDetails.getDefaultBranch();

								binding.copyProjectUrl.setOnClickListener(
										copy -> {
											MaterialAlertDialogBuilder materialAlertDialogBuilder =
													new MaterialAlertDialogBuilder(ctx);

											View customDialogView =
													LayoutInflater.from(ctx)
															.inflate(
																	R.layout
																			.custom_copy_project_urls_dialog,
																	findViewById(
																			android.R.id.content),
																	false);

											materialAlertDialogBuilder
													.setView(customDialogView)
													.setNeutralButton(R.string.close, null)
													.show();

											MaterialButton projId =
													customDialogView.findViewById(
															R.id.copy_project_id);
											MaterialButton webUrl =
													customDialogView.findViewById(
															R.id.copy_web_url);
											MaterialButton httpsUrl =
													customDialogView.findViewById(
															R.id.copy_https_url);
											MaterialButton sshUrl =
													customDialogView.findViewById(
															R.id.copy_ssh_url);

											projId.setOnClickListener(
													projectId ->
															Utils.copyToClipboard(
																	ctx,
																	ProjectDetailActivity.this,
																	String.valueOf(
																			projectDetails.getId()),
																	getString(
																			R.string
																					.copy_url_message)));
											webUrl.setOnClickListener(
													web ->
															Utils.copyToClipboard(
																	ctx,
																	ProjectDetailActivity.this,
																	projectDetails.getWebUrl(),
																	getString(
																			R.string
																					.copy_url_message)));
											httpsUrl.setOnClickListener(
													https ->
															Utils.copyToClipboard(
																	ctx,
																	ProjectDetailActivity.this,
																	projectDetails
																			.getHttpUrlToRepo(),
																	getString(
																			R.string
																					.copy_url_message)));
											sshUrl.setOnClickListener(
													ssh ->
															Utils.copyToClipboard(
																	ctx,
																	ProjectDetailActivity.this,
																	projectDetails
																			.getSshUrlToRepo(),
																	getString(
																			R.string
																					.copy_url_message)));
										});

								setupLanguageStats();
								if (projectDetails.getReadmeUrl() != null) {
									README =
											projectDetails
													.getReadmeUrl()
													.substring(
															projectDetails.getReadmeUrl().length()
																	- 9);
									loadProjectReadme();
								}

								if (projectDetails.getStarCount() > 0) {
									binding.projectStars.setOnClickListener(
											stars -> {
												bsBundle.putInt("projectId", projectId);
												bsBundle.putString("type", "stars");
												ProjectMembersBottomSheet bottomSheet =
														new ProjectMembersBottomSheet();
												bottomSheet.setArguments(bsBundle);
												bottomSheet.show(
														getSupportFragmentManager(),
														"projectMembersBottomSheet");
											});
								}

								if (projectDetails.getForksCount() > 0) {
									binding.projectForks.setOnClickListener(
											forks -> {
												ProjectsContext project =
														new ProjectsContext(
																projectsContext.getProject(), ctx);
												Intent intent =
														project.getIntent(
																ctx, ProjectsActivity.class);
												intent.putExtra("source", "forks");
												intent.putExtra("projectId", projectId);
												ctx.startActivity(intent);
											});
								}
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<Projects> call, @NonNull Throwable t) {
						Snackbar.info(
								ProjectDetailActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void loadProjectReadme() {

		Call<FileContents> call =
				RetrofitClient.getApiInterface(ctx)
						.getProjectFileContent(
								projectId, README, projectsContext.getProject().getDefaultBranch());

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<FileContents> call,
							@NonNull Response<FileContents> response) {

						FileContents readmeFile = response.body();

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								assert readmeFile != null;
								binding.projectReadmeSection.setVisibility(View.VISIBLE);

								Markdown.render(
										ctx,
										EmojiParser.parseToUnicode(
												Utils.decodeBase64(readmeFile.getContent())),
										binding.readme,
										projectsContext);
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<FileContents> call, @NonNull Throwable t) {
						Snackbar.info(
								ProjectDetailActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void loadUserStars() {

		Call<List<Projects>> call =
				RetrofitClient.getApiInterface(ctx)
						.getStarredProjects(getAccount().getUserInfo().getId(), 100, 1);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Projects>> call,
							@NonNull Response<List<Projects>> response) {

						List<Projects> projects = response.body();

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								assert projects != null;
								List<Integer> starred = new ArrayList<>();
								for (int i = 0; i < projects.size(); i++) {
									starred.add(projects.get(i).getId());
								}

								binding.starForkProjectFrame.setVisibility(View.VISIBLE);
								if (starred.contains(projectId)) {
									binding.unstarAProject.setVisibility(View.VISIBLE);
									binding.starAProject.setVisibility(View.GONE);
								} else {
									binding.starAProject.setVisibility(View.VISIBLE);
									binding.unstarAProject.setVisibility(View.GONE);
								}
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Projects>> call, @NonNull Throwable t) {
						Snackbar.info(
								ProjectDetailActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void starAProject() {

		Call<Projects> call = RetrofitClient.getApiInterface(ctx).starProject(projectId);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Projects> call, @NonNull Response<Projects> response) {

						if (response.isSuccessful()) {

							if (response.code() == 201) {

								Snackbar.info(
										ProjectDetailActivity.this,
										findViewById(R.id.bottom_app_bar),
										getString(R.string.project_starred));

								binding.starAProject.setVisibility(View.GONE);
								binding.unstarAProject.setVisibility(View.VISIBLE);
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<Projects> call, @NonNull Throwable t) {
						Snackbar.info(
								ProjectDetailActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void unstarAProject() {

		Call<Projects> call = RetrofitClient.getApiInterface(ctx).unstarProject(projectId);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Projects> call, @NonNull Response<Projects> response) {

						if (response.isSuccessful()) {

							if (response.code() == 201) {

								Snackbar.info(
										ProjectDetailActivity.this,
										findViewById(R.id.bottom_app_bar),
										getString(R.string.project_unstarred));

								binding.starAProject.setVisibility(View.VISIBLE);
								binding.unstarAProject.setVisibility(View.GONE);
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<Projects> call, @NonNull Throwable t) {
						Snackbar.info(
								ProjectDetailActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void loadLanguageColors() {
		languageColors = new HashMap<>();
		String[] names = getResources().getStringArray(R.array.language_names);
		String[] hexColors = getResources().getStringArray(R.array.language_colors);
		if (names.length == hexColors.length) {
			for (int i = 0; i < names.length; i++) {
				languageColors.put(names[i], Color.parseColor(hexColors[i]));
			}
		}
	}

	private void setupLanguageStats() {

		Call<Map<String, Float>> call =
				RetrofitClient.getApiInterface(this).getProjectLanguages(projectId);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Map<String, Float>> call,
							@NonNull Response<Map<String, Float>> response) {
						if (response.isSuccessful() && response.code() == 200) {
							Map<String, Float> languages = response.body();
							if (languages != null && !languages.isEmpty()) {
								binding.languageStatsCard.setVisibility(View.VISIBLE);
								displayLanguageBar(languages);
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<Map<String, Float>> call, @NonNull Throwable t) {}
				});
	}

	private void displayLanguageBar(Map<String, Float> languages) {

		LinearLayout container = binding.languageBarContainer;
		container.removeAllViews();

		List<Map.Entry<String, Float>> sortedLanguages = new ArrayList<>(languages.entrySet());
		sortedLanguages.sort((e1, e2) -> Float.compare(e2.getValue(), e1.getValue()));
		int maxDisplay = Math.min(3, sortedLanguages.size());

		float totalPercent = 0;
		for (int i = 0; i < maxDisplay; i++) {
			totalPercent += sortedLanguages.get(i).getValue();
		}

		float finalTotalPercent = totalPercent;
		container.post(
				() -> {
					int totalWidth = container.getWidth();
					if (totalWidth == 0)
						totalWidth = getResources().getDisplayMetrics().widthPixels - 64;
					for (int i = 0; i < maxDisplay; i++) {
						Map.Entry<String, Float> entry = sortedLanguages.get(i);
						String lang = entry.getKey();
						float percent = (entry.getValue() / finalTotalPercent) * 100;
						int sectionWidth = (int) ((totalWidth * percent) / 100);

						View section = new View(this);
						section.setLayoutParams(
								new LinearLayout.LayoutParams(
										sectionWidth,
										getResources().getDimensionPixelSize(R.dimen.dimen12dp)));
						int color = getLanguageColor(lang);
						section.setBackgroundColor(color);
						container.addView(section);
					}

					binding.languageStatsCard.setOnClickListener(v -> showLanguageDialog());
				});
	}

	private int getLanguageColor(String lang) {
		String normalizedLang = lang.toLowerCase().replace(" ", "_").replace("-", "_");
		Integer color = languageColors.get(normalizedLang);
		if (color == null) {
			color = languageColors.get("default");
		}
		return color != null ? color : Color.parseColor("#49da39");
	}

	private void showLanguageDialog() {

		Call<Map<String, Float>> call =
				RetrofitClient.getApiInterface(this).getProjectLanguages(projectId);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Map<String, Float>> call,
							@NonNull Response<Map<String, Float>> response) {
						if (response.isSuccessful() && response.code() == 200) {
							Map<String, Float> languages = response.body();
							if (languages != null && !languages.isEmpty()) {
								showLanguageDialogInternal(languages);
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<Map<String, Float>> call, @NonNull Throwable t) {}
				});
	}

	private void showLanguageDialogInternal(Map<String, Float> languages) {

		LinearLayout dialogLayout = new LinearLayout(this);
		dialogLayout.setOrientation(LinearLayout.VERTICAL);
		dialogLayout.setPadding(16, 32, 16, 16);

		for (Map.Entry<String, Float> entry : languages.entrySet()) {
			String lang = entry.getKey();
			float percent = entry.getValue();

			Chip chip = new Chip(this);
			chip.setText(String.format(Locale.US, "%s (%.1f%%)", lang, percent));
			chip.setChipBackgroundColor(
					ContextCompat.getColorStateList(this, R.color.alert_tip_border));
			int color = getLanguageColor(lang);
			chip.setChipBackgroundColor(ColorStateList.valueOf(color));
			chip.setTextColor(isDarkColor(color) ? Color.WHITE : Color.BLACK);
			chip.setChipMinHeight(getResources().getDimensionPixelSize(R.dimen.dimen28dp));
			float cornerRadius = dpToPx(this);
			chip.setShapeAppearanceModel(
					chip.getShapeAppearanceModel().toBuilder()
							.setAllCornerSizes(cornerRadius)
							.build());
			chip.setPadding(12, 8, 12, 8);
			chip.setClickable(false);
			chip.setEnsureMinTouchTargetSize(false);

			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(36, 12, 36, 12);
			chip.setLayoutParams(params);

			dialogLayout.addView(chip);
		}

		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.languages)
				.setView(dialogLayout)
				.setPositiveButton(R.string.close, null)
				.show();
	}

	private boolean isDarkColor(int color) {
		double darkness =
				1
						- (0.299 * Color.red(color)
										+ 0.587 * Color.green(color)
										+ 0.114 * Color.blue(color))
								/ 255;
		return darkness >= 0.5;
	}

	private float dpToPx(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return 32 * (metrics.densityDpi / 160f);
	}

	@Override
	public void onButtonClicked(String text) {}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
