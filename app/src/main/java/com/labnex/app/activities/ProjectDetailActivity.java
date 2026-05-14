package com.labnex.app.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.*;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityProjectDetailBinding;
import com.labnex.app.databinding.ItemProjectActionCardBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.app.CardColors;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.viewmodels.ProjectDetailViewModel;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author mmarif
 */
public class ProjectDetailActivity extends BaseActivity
		implements BranchesBottomSheet.OnBranchSelectedListener {

	private ActivityProjectDetailBinding binding;
	private ProjectDetailViewModel viewModel;
	private ProjectsContext projectsContext;
	private int projectId;
	private String branch;
	private String readmePath;
	private Map<String, Integer> languageColors;

	private static final CardColors COLOR_CODE =
			new CardColors(
					com.google.android.material.R.attr.colorPrimaryContainer,
					com.google.android.material.R.attr.colorOnPrimaryContainer);

	private static final CardColors COLOR_ISSUE_MR =
			new CardColors(
					com.google.android.material.R.attr.colorSurfaceContainer,
					com.google.android.material.R.attr.colorOnSurface);

	private static final CardColors COLOR_META =
			new CardColors(
					com.google.android.material.R.attr.colorTertiaryContainer,
					com.google.android.material.R.attr.colorOnTertiaryContainer);

	private static final CardColors COLOR_OTHER =
			new CardColors(
					com.google.android.material.R.attr.colorSurfaceContainerHighest,
					com.google.android.material.R.attr.colorOnSurface);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityProjectDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, binding.scrollView, null, null);

		viewModel = new ViewModelProvider(this).get(ProjectDetailViewModel.class);

		projectsContext = ProjectsContext.fromIntent(getIntent());
		projectId = projectsContext.getProjectId();
		loadLanguageColors();

		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnStar.setOnClickListener(v -> viewModel.toggleStar(ctx, projectId));
		binding.btnMenu.setOnClickListener(v -> showProjectMenu());

		setupActionCards();
		observeViewModel();

		viewModel.loadProject(ctx, projectId);
	}

	@Override
	protected void onGlobalRefresh() {
		viewModel.loadProject(ctx, projectId);
		Log.e("ProjectDetail", "called");
	}

	@Override
	public void onBranchSelected(String branch) {
		this.branch = branch;
		ItemProjectActionCardBinding card =
				ItemProjectActionCardBinding.bind(binding.sectionActions.cardBranch.getRoot());
		card.actionTitle.setText(branch);
		if (readmePath != null) {
			viewModel.loadReadme(ctx, projectId, branch, readmePath);
		}
	}

	private void setupActionCards() {
		setupCard(
				binding.sectionActions.cardBranch.getRoot(),
				R.drawable.ic_branch,
				0,
				COLOR_CODE,
				v ->
						BranchesBottomSheet.newInstance(projectId)
								.show(getSupportFragmentManager(), "branchesSheet"));

		setupCard(
				binding.sectionActions.cardFiles.getRoot(),
				R.drawable.ic_files_code,
				R.string.files,
				COLOR_CODE,
				v -> navigateTo(FilesBrowserActivity.class, "project"));

		setupCard(
				binding.sectionActions.cardIssues.getRoot(),
				R.drawable.ic_issues,
				R.string.issues,
				COLOR_ISSUE_MR,
				v -> navigateTo(IssuesActivity.class, "project"));

		setupCard(
				binding.sectionActions.cardMergeRequests.getRoot(),
				R.drawable.ic_merge_request,
				R.string.merge_requests,
				COLOR_ISSUE_MR,
				v -> navigateTo(MergeRequestsActivity.class, "mr"));

		setupCard(
				binding.sectionActions.cardCommits.getRoot(),
				R.drawable.ic_commits,
				R.string.commits,
				COLOR_CODE,
				v -> navigateTo(CommitsActivity.class, "project"));

		setupCard(
				binding.sectionActions.cardReleases.getRoot(),
				R.drawable.ic_releases,
				R.string.releases,
				COLOR_META,
				v -> showSheet(new ProjectReleasesBottomSheet(), "projectReleasesBottomSheet"));

		setupCard(
				binding.sectionActions.cardMilestones.getRoot(),
				R.drawable.ic_milestones,
				R.string.milestones,
				COLOR_META,
				v -> showSheet(new ProjectMilestonesBottomSheet(), "projectMilestonesBottomSheet"));

		setupCard(
				binding.sectionActions.cardTags.getRoot(),
				R.drawable.ic_tags,
				R.string.tags,
				COLOR_META,
				v -> showSheet(new ProjectTagsBottomSheet(), "projectTagsBottomSheet"));

		setupCard(
				binding.sectionActions.cardLabels.getRoot(),
				R.drawable.ic_labels,
				R.string.labels,
				COLOR_META,
				v ->
						LabelsBottomSheet.newInstance("project", projectId)
								.show(getSupportFragmentManager(), "labelsSheet"));

		setupCard(
				binding.sectionActions.cardWiki.getRoot(),
				R.drawable.ic_wiki,
				R.string.wiki,
				COLOR_OTHER,
				v -> showSheet(new ProjectWikisBottomSheet(), "projectWikisBottomSheet"));

		setupCard(
				binding.sectionActions.cardMembers.getRoot(),
				R.drawable.ic_users,
				R.string.members,
				COLOR_OTHER,
				v ->
						MembersBottomSheet.newInstance("project", projectId)
								.show(getSupportFragmentManager(), "membersSheet"));

		setupCard(
				binding.sectionActions.cardCopyInfo.getRoot(),
				R.drawable.ic_copy,
				R.string.copy_info,
				COLOR_OTHER,
				this::showCopyInfoSheet);
	}

	private void setupCard(
			View cardView,
			int iconRes,
			int defaultTextRes,
			CardColors colors,
			View.OnClickListener listener) {
		ItemProjectActionCardBinding card = ItemProjectActionCardBinding.bind(cardView);
		card.actionIcon.setImageResource(iconRes);

		TypedValue bgValue = new TypedValue();
		ctx.getTheme().resolveAttribute(colors.backgroundAttr, bgValue, true);
		card.actionCard.setCardBackgroundColor(bgValue.data);

		TypedValue tintValue = new TypedValue();
		ctx.getTheme().resolveAttribute(colors.iconTextAttr, tintValue, true);
		card.actionIcon.setImageTintList(ColorStateList.valueOf(tintValue.data));
		card.actionTitle.setTextColor(tintValue.data);
		card.actionChip.setTextColor(tintValue.data);
		card.actionChip.setChipBackgroundColor(
				ColorStateList.valueOf(
						android.graphics.Color.argb(
								30,
								android.graphics.Color.red(tintValue.data),
								android.graphics.Color.green(tintValue.data),
								android.graphics.Color.blue(tintValue.data))));

		if (defaultTextRes != 0) card.actionTitle.setText(defaultTextRes);
		cardView.setOnClickListener(listener);
	}

	private <T extends BaseActivity> void navigateTo(Class<T> target, String source) {
		ProjectsContext pc = new ProjectsContext(projectsContext.getProject(), ctx);
		Intent intent = pc.getIntent(ctx, target);
		intent.putExtra("source", source);
		intent.putExtra("projectName", projectsContext.getProjectName());
		intent.putExtra("path", projectsContext.getPath());
		intent.putExtra("projectId", projectId);
		intent.putExtra("id", projectId);
		if (branch != null) intent.putExtra("branch", branch);
		startActivity(intent);
	}

	private void showSheet(BottomSheetDialogFragment sheet, String tag) {
		Bundle args = new Bundle();
		args.putInt("projectId", projectId);
		sheet.setArguments(args);
		sheet.show(getSupportFragmentManager(), tag);
	}

	private void showProjectMenu() {
		List<GenericMenuItemModel> items = new ArrayList<>();
		items.add(
				new GenericMenuItemModel(
						"create_issue",
						R.string.create_issue,
						R.drawable.ic_add,
						com.google.android.material.R.attr.colorSecondaryContainer,
						com.google.android.material.R.attr.colorOnSecondaryContainer));
		items.add(
				new GenericMenuItemModel(
						"create_mr",
						R.string.create_mr,
						R.drawable.ic_add,
						com.google.android.material.R.attr.colorSecondaryContainer,
						com.google.android.material.R.attr.colorOnSecondaryContainer));
		items.add(
				new GenericMenuItemModel(
						"create_branch",
						R.string.create_branch,
						R.drawable.ic_add,
						com.google.android.material.R.attr.colorSecondaryContainer,
						com.google.android.material.R.attr.colorOnSecondaryContainer));
		items.add(
				new GenericMenuItemModel(
						"create_milestone",
						R.string.create_milestone,
						R.drawable.ic_add,
						com.google.android.material.R.attr.colorSecondaryContainer,
						com.google.android.material.R.attr.colorOnSecondaryContainer));
		items.add(
				new GenericMenuItemModel(
						"create_label",
						R.string.create_new_label,
						R.drawable.ic_add,
						com.google.android.material.R.attr.colorSecondaryContainer,
						com.google.android.material.R.attr.colorOnSecondaryContainer));
		items.add(
				new GenericMenuItemModel(
						"fork_project",
						R.string.fork,
						R.drawable.ic_forks,
						com.google.android.material.R.attr.colorTertiaryContainer,
						com.google.android.material.R.attr.colorOnTertiaryContainer));

		GenericMenuBottomSheet sheet =
				GenericMenuBottomSheet.newInstance(
						projectsContext.getProjectName(),
						projectsContext.getProject().getNameWithNamespace(),
						items);
		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "create_issue":
							startActivity(
									new ProjectsContext(
													projectsContext.getProjectName(),
													projectsContext.getPath(),
													projectId,
													ctx)
											.getIntent(ctx, CreateIssueActivity.class));
							break;
						case "create_mr":
							startActivity(
									new ProjectsContext(
													projectsContext.getProjectName(),
													projectsContext.getPath(),
													projectId,
													ctx)
											.getIntent(ctx, CreateMergeRequestActivity.class));
							break;
						case "create_branch":
							CreateBranchBottomSheet.newInstance(projectId, branch)
									.show(getSupportFragmentManager(), "createBranchSheet");
							break;
						case "create_milestone":
							// milestone sheet
							break;
						case "create_label":
							CreateLabelBottomSheet.newInstance("project", projectId)
									.show(getSupportFragmentManager(), "createLabelSheet");
							break;
						case "fork_project":
							// fork a project
							break;
					}
				});
		sheet.show(getSupportFragmentManager(), "projectMenuSheet");
	}

	private void showCopyInfoSheet(View v) {
		Projects project = viewModel.getProjectInfo().getValue();
		if (project == null) return;

		List<GenericMenuItemModel> items = new ArrayList<>();
		items.add(
				new GenericMenuItemModel(
						"copy_id",
						R.string.copy_project_id,
						R.drawable.ic_info,
						com.google.android.material.R.attr.colorPrimaryContainer,
						com.google.android.material.R.attr.colorOnPrimaryContainer));
		items.add(
				new GenericMenuItemModel(
						"copy_url",
						R.string.copy_web_url,
						R.drawable.ic_browser,
						com.google.android.material.R.attr.colorSecondaryContainer,
						com.google.android.material.R.attr.colorOnSecondaryContainer));
		items.add(
				new GenericMenuItemModel(
						"copy_https",
						R.string.copy_clone_https_url,
						R.drawable.ic_copy,
						com.google.android.material.R.attr.colorTertiaryContainer,
						com.google.android.material.R.attr.colorOnTertiaryContainer));
		items.add(
				new GenericMenuItemModel(
						"copy_ssh",
						R.string.copy_ssh_url,
						R.drawable.ic_copy,
						com.google.android.material.R.attr.colorPrimaryContainer,
						com.google.android.material.R.attr.colorOnPrimaryContainer));

		GenericMenuBottomSheet sheet =
				GenericMenuBottomSheet.newInstance(getString(R.string.copy_info), null, items);
		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "copy_id":
							Utils.copyToClipboard(
									ctx,
									String.valueOf(project.getId()),
									getString(R.string.copied_to_clipboard));
							break;
						case "copy_url":
							Utils.copyToClipboard(
									ctx,
									project.getWebUrl(),
									getString(R.string.copied_to_clipboard));
							break;
						case "copy_https":
							Utils.copyToClipboard(
									ctx,
									project.getHttpUrlToRepo(),
									getString(R.string.copied_to_clipboard));
							break;
						case "copy_ssh":
							Utils.copyToClipboard(
									ctx,
									project.getSshUrlToRepo(),
									getString(R.string.copied_to_clipboard));
							break;
					}
				});
		sheet.show(getSupportFragmentManager(), "copyInfoSheet");
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							binding.progressBar.setVisibility(
									Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getProjectInfo()
				.observe(
						this,
						project -> {
							if (project == null) return;
							populateHeader(project);
							viewModel.loadLanguageStats(ctx, projectId);
							viewModel.loadMrCount(ctx, projectId);
							viewModel.checkStarStatus(
									ctx, getAccount().getUserInfo().getId(), projectId);

							branch = project.getDefaultBranch();
							ItemProjectActionCardBinding branchCard =
									ItemProjectActionCardBinding.bind(
											binding.sectionActions.cardBranch.getRoot());
							branchCard.actionTitle.setText(branch);

							if (project.getReadmeUrl() != null) {
								readmePath =
										project.getReadmeUrl()
												.substring(project.getReadmeUrl().length() - 9);
								viewModel.loadReadme(ctx, projectId, branch, readmePath);
							}
						});

		viewModel.getLanguageStats().observe(this, this::displayLanguageStats);

		viewModel
				.getReadmeContent()
				.observe(
						this,
						content -> {
							if (content != null && !content.isEmpty()) {
								binding.sectionReadme.getRoot().setVisibility(View.VISIBLE);
								Markdown.render(
										ctx,
										EmojiParser.parseToUnicode(content),
										binding.sectionReadme.readme,
										projectsContext);
							}
						});

		viewModel
				.getIsStarred()
				.observe(
						this,
						starred -> {
							binding.btnStar.setIconResource(
									Boolean.TRUE.equals(starred)
											? R.drawable.ic_star_filled
											: R.drawable.ic_star);
						});

		viewModel
				.getStarCount()
				.observe(
						this,
						count -> {
							if (count != null && count >= 0) {
								binding.sectionHeader.projectStars.setText(
										Utils.numberFormatter(count));
							}
						});

		viewModel
				.getActionSuccess()
				.observe(
						this,
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Log.e(
										"ActionSuccess",
										String.valueOf(viewModel.getIsStarred().getValue()));
								Boolean starred = viewModel.getIsStarred().getValue();
								Toasty.show(
										ctx,
										getString(
												Boolean.TRUE.equals(starred)
														? R.string.project_starred
														: R.string.project_unstarred));
								AppUIStateManager.refreshData();
								viewModel.clearActionSuccess();
							}
						});

		viewModel
				.getMrCount()
				.observe(
						this,
						count -> {
							if (count != null && count >= 0) {
								ItemProjectActionCardBinding mrCard =
										ItemProjectActionCardBinding.bind(
												binding.sectionActions.cardMergeRequests.getRoot());
								mrCard.actionChip.setText(Utils.numberFormatter(count));
							}
						});

		viewModel
				.getError()
				.observe(
						this,
						errorMsg -> {
							if (errorMsg == null) return;
							switch (errorMsg) {
								case "auth_error":
									Toasty.show(ctx, getString(R.string.not_authorized));
									break;
								case "access_forbidden_403":
									Toasty.show(ctx, getString(R.string.access_forbidden_403));
									break;
								case "not_found":
									Toasty.show(ctx, getString(R.string.not_found));
								case "generic_error":
									Toasty.show(ctx, getString(R.string.generic_error));
									break;
								default:
									Toasty.show(ctx, errorMsg);
									break;
							}
							viewModel.clearError();
						});
	}

	private void populateHeader(Projects project) {
		binding.sectionHeader.basicInfoFrame.setVisibility(View.VISIBLE);

		if (project.isArchived()) {
			binding.archivedProjectFrame.setVisibility(View.VISIBLE);
			binding.btnStar.setEnabled(false);
			binding.btnStar.setAlpha(0.4f);
			binding.dockContainer.removeView(binding.btnMenu);
		}

		if (project.getAvatarUrl() != null && "public".equalsIgnoreCase(project.getVisibility())) {
			Glide.with(ctx)
					.load(project.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.ic_spinner)
					.centerCrop()
					.into(binding.sectionHeader.projectAvatar);
		} else {
			binding.sectionHeader.projectAvatar.setImageDrawable(
					AvatarGenerator.getLetterAvatar(ctx, project.getName(), 48));
		}

		if ("private".equalsIgnoreCase(project.getVisibility())) {
			binding.sectionHeader.visibilityIcon.setVisibility(View.VISIBLE);
		}

		binding.sectionHeader.projectName.setText(project.getName());
		binding.sectionHeader.projectPath.setText(project.getPathWithNamespace());

		if (project.getDescription() != null && !project.getDescription().isEmpty()) {
			binding.sectionHeader.projectDescription.setVisibility(View.VISIBLE);
			binding.sectionHeader.projectDescription.setText(project.getDescription());
		}

		binding.sectionHeader.statsStars.setOnClickListener(
				v -> {
					MembersBottomSheet.newInstance("starrers", projectId)
							.show(getSupportFragmentManager(), "membersSheet");
				});

		binding.sectionHeader.statsForks.setOnClickListener(
				v -> {
					ProjectsContext pc = new ProjectsContext(projectsContext.getProject(), ctx);
					Intent intent = pc.getIntent(ctx, ProjectsActivity.class);
					intent.putExtra("source", "forks");
					intent.putExtra("projectId", projectId);
					startActivity(intent);
				});

		binding.sectionHeader.projectStars.setText(Utils.numberFormatter(project.getStarCount()));
		binding.sectionHeader.projectForks.setText(Utils.numberFormatter(project.getForksCount()));

		ItemProjectActionCardBinding issuesCard =
				ItemProjectActionCardBinding.bind(binding.sectionActions.cardIssues.getRoot());
		issuesCard.actionChip.setVisibility(View.VISIBLE);
		issuesCard.actionChip.setText(Utils.numberFormatter(project.getOpenIssuesCount()));

		ItemProjectActionCardBinding mrCard =
				ItemProjectActionCardBinding.bind(
						binding.sectionActions.cardMergeRequests.getRoot());
		mrCard.actionChip.setVisibility(View.VISIBLE);
	}

	private void displayLanguageStats(Map<String, Float> languages) {
		if (languages == null || languages.isEmpty()) return;

		binding.sectionHeader.languageBarContainer.setVisibility(View.VISIBLE);
		binding.sectionHeader.languageLabel.setVisibility(View.VISIBLE);
		binding.sectionHeader.languageBarContainer.removeAllViews();

		List<Map.Entry<String, Float>> sorted = new ArrayList<>(languages.entrySet());
		sorted.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

		StringBuilder label = new StringBuilder();
		int maxDisplay = Math.min(3, sorted.size());
		for (int i = 0; i < maxDisplay; i++) {
			String lang = sorted.get(i).getKey();
			float pct = sorted.get(i).getValue();
			if (i > 0) label.append(" · ");
			label.append(String.format(Locale.US, "%s %.1f%%", lang, pct));
		}
		binding.sectionHeader.languageLabel.setText(label.toString());

		binding.sectionHeader.languageBarContainer.post(
				() -> {
					float dp8 =
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_DIP,
									8,
									getResources().getDisplayMetrics());
					int totalWidth = binding.sectionHeader.languageBarContainer.getWidth();
					if (totalWidth <= 0) return;
					float total = 0;
					for (int i = 0; i < maxDisplay; i++) total += sorted.get(i).getValue();
					for (int i = 0; i < maxDisplay; i++) {
						float pct = sorted.get(i).getValue() / total;
						int color = getLanguageColor(sorted.get(i).getKey());

						View bar = new View(this);
						int width = (int) (totalWidth * pct);
						int height = getResources().getDimensionPixelSize(R.dimen.dimen8dp);

						float[] radii;
						if (maxDisplay == 1) {
							radii = new float[] {dp8, dp8, dp8, dp8, dp8, dp8, dp8, dp8};
						} else if (i == 0) {
							radii = new float[] {dp8, dp8, 0, 0, 0, 0, dp8, dp8};
						} else if (i == maxDisplay - 1) {
							radii = new float[] {0, 0, dp8, dp8, dp8, dp8, 0, 0};
						} else {
							radii = new float[] {0, 0, 0, 0, 0, 0, 0, 0};
						}

						GradientDrawable drawable = new GradientDrawable();
						drawable.setColor(color);
						drawable.setCornerRadii(radii);

						bar.setBackground(drawable);
						bar.setLayoutParams(new LinearLayout.LayoutParams(width, height));
						binding.sectionHeader.languageBarContainer.addView(bar);
					}
				});
	}

	private void loadLanguageColors() {
		languageColors = new HashMap<>();
		String[] names = getResources().getStringArray(R.array.language_names);
		String[] hexColors = getResources().getStringArray(R.array.language_colors);
		if (names.length == hexColors.length) {
			for (int i = 0; i < names.length; i++) {
				languageColors.put(names[i], android.graphics.Color.parseColor(hexColors[i]));
			}
		}
	}

	private int getLanguageColor(String lang) {
		String key = lang.toLowerCase().replace(" ", "_").replace("-", "_");
		Integer color = languageColors.get(key);
		if (color == null) color = languageColors.get("default");
		return color != null ? color : android.graphics.Color.parseColor("#49da39");
	}
}
