package com.labnex.app.activities;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.TimelineAdapter;
import com.labnex.app.bottomsheets.CommitDiffsBottomSheet;
import com.labnex.app.bottomsheets.CreateMergeRequestBottomSheet;
import com.labnex.app.bottomsheets.GenericMenuBottomSheet;
import com.labnex.app.bottomsheets.MergeMrBottomSheet;
import com.labnex.app.contexts.MergeRequestContext;
import com.labnex.app.databinding.ActivityMrDetailBinding;
import com.labnex.app.helpers.*;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.models.approvals.ApprovedBy;
import com.labnex.app.models.labels.Labels;
import com.labnex.app.models.merge_requests.AssigneesItem;
import com.labnex.app.models.merge_requests.Author;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.viewmodels.MrDetailViewModel;
import com.labnex.app.viewmodels.ReactionsViewModel;
import com.labnex.app.viewmodels.TimelineViewModel;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class MergeRequestDetailActivity extends BaseActivity {

	private ActivityMrDetailBinding binding;
	private MrDetailViewModel viewModel;
	private TimelineViewModel timelineViewModel;
	private TimelineAdapter timelineAdapter;
	private ReactionsViewModel reactionsViewModel;

	private MergeRequestContext mrContext;
	private long mrIid;
	private long projectId;
	private boolean timelineInitialized = false;
	private boolean labelsObserverSet = false;
	private List<String> currentLabelNames = new ArrayList<>();
	private boolean hasUserApproved = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMrDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, binding.scrollView, null, null);

		viewModel = new ViewModelProvider(this).get(MrDetailViewModel.class);

		mrContext = MergeRequestContext.fromIntent(getIntent());

		if (mrContext == null || mrContext.getProjects() == null) {
			Toasty.show(ctx, getString(R.string.cannot_find_mr));
			finish();
			return;
		}

		mrIid = mrContext.getMergeRequestIndex();
		projectId = mrContext.getProjects().getProjectId();

		if (mrIid <= 0 || projectId <= 0) {
			Toasty.show(ctx, getString(R.string.cannot_find_mr));
			finish();
			return;
		}

		setupDock();
		observeViewModel();
		setupCommentBox();

		binding.getRoot()
				.getViewTreeObserver()
				.addOnGlobalLayoutListener(
						() -> {
							Rect r = new Rect();
							binding.getRoot().getWindowVisibleDisplayFrame(r);
							int screenHeight = binding.getRoot().getRootView().getHeight();
							int keypadHeight = screenHeight - r.bottom;

							ViewGroup.MarginLayoutParams params =
									(ViewGroup.MarginLayoutParams)
											binding.commentBox.getRoot().getLayoutParams();
							if (keypadHeight > screenHeight * 0.15) {
								params.bottomMargin = keypadHeight;
							} else {
								params.bottomMargin =
										(int) (36 * getResources().getDisplayMetrics().density);
							}
							binding.commentBox.getRoot().setLayoutParams(params);
						});

		viewModel.loadMergeRequest(ctx, projectId, mrIid);
	}

	private void setupDock() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnComment.setOnClickListener(v -> toggleCommentBox());
		binding.btnMenu.setOnClickListener(v -> showMrMenu());
	}

	private void updateCommentButton(MergeRequests mrData) {
		boolean isLocked = mrData.getDiscussionLocked();
		boolean isArchived = mrContext.getProjects().getProject().isArchived();
		boolean isMerged = "merged".equalsIgnoreCase(mrData.getState());

		if (isArchived || isMerged) {
			binding.btnComment.setVisibility(View.GONE);
			return;
		}

		if (isLocked) {
			int accessLevel = AccessLevel.getUserAccessLevel(mrContext.getProjects().getProject());
			boolean isMember = accessLevel >= AccessLevel.REPORTER;
			binding.btnComment.setEnabled(isMember);
			binding.btnComment.setAlpha(isMember ? 1.0f : 0.4f);
		} else {
			binding.btnComment.setEnabled(true);
			binding.btnComment.setAlpha(1.0f);
		}
	}

	private void showMrMenu() {
		MergeRequests mrData = viewModel.getMrData().getValue();
		if (mrData == null) return;

		List<GenericMenuItemModel> items = new ArrayList<>();
		boolean isClosed = "closed".equalsIgnoreCase(mrData.getState());
		boolean isMerged = "merged".equalsIgnoreCase(mrData.getState());
		boolean isLocked = mrData.getDiscussionLocked();
		int accessLevel = AccessLevel.getUserAccessLevel(mrContext.getProjects().getProject());
		boolean canModify = accessLevel >= AccessLevel.MAINTAINER;

		items.add(
				new GenericMenuItemModel(
						"commits",
						R.string.commits,
						R.drawable.ic_commits,
						com.google.android.material.R.attr.colorPrimaryContainer,
						com.google.android.material.R.attr.colorOnPrimaryContainer));
		items.add(
				new GenericMenuItemModel(
						"files",
						R.string.files,
						R.drawable.ic_files_code,
						com.google.android.material.R.attr.colorPrimaryContainer,
						com.google.android.material.R.attr.colorOnPrimaryContainer));

		if (!isMerged) {
			if (canModify
					&& !isClosed
					&& "can_be_merged".equalsIgnoreCase(mrData.getMergeStatus())) {
				items.add(
						new GenericMenuItemModel(
								"merge",
								R.string.merge,
								R.drawable.ic_merge,
								com.google.android.material.R.attr.colorPrimaryContainer,
								com.google.android.material.R.attr.colorOnPrimaryContainer));
			}

			if (canModify) {
				if (isClosed && !isLocked) {
					items.add(
							new GenericMenuItemModel(
									"reopen",
									R.string.reopen,
									R.drawable.ic_refresh,
									com.google.android.material.R.attr.colorPrimaryContainer,
									com.google.android.material.R.attr.colorOnPrimaryContainer));
				} else if (!isClosed) {
					items.add(
							new GenericMenuItemModel(
									"close",
									R.string.close,
									R.drawable.ic_close,
									com.google.android.material.R.attr.colorErrorContainer,
									com.google.android.material.R.attr.colorOnErrorContainer));
				}

				if (isLocked) {
					items.add(
							new GenericMenuItemModel(
									"unlock",
									R.string.unlock_discussion,
									R.drawable.ic_unlock,
									com.google.android.material.R.attr.colorPrimaryContainer,
									com.google.android.material.R.attr.colorOnPrimaryContainer));
				} else {
					items.add(
							new GenericMenuItemModel(
									"lock",
									R.string.lock_discussion,
									R.drawable.ic_lock,
									com.google.android.material.R.attr.colorPrimaryContainer,
									com.google.android.material.R.attr.colorOnPrimaryContainer));
				}
			}
		}

		if (canModify && !isMerged) {
			boolean isDraft = mrData.isDraft() || mrData.isWorkInProgress();

			if (isDraft) {
				items.add(
						new GenericMenuItemModel(
								"mark_ready",
								R.string.mark_ready,
								R.drawable.ic_draft,
								com.google.android.material.R.attr.colorPrimaryContainer,
								com.google.android.material.R.attr.colorOnPrimaryContainer));
			} else {
				items.add(
						new GenericMenuItemModel(
								"mark_draft",
								R.string.mark_draft,
								R.drawable.ic_draft,
								com.google.android.material.R.attr.colorPrimaryContainer,
								com.google.android.material.R.attr.colorOnPrimaryContainer));
			}
		}

		items.add(
				new GenericMenuItemModel(
						"copy_url",
						R.string.copy_url,
						R.drawable.ic_link,
						com.google.android.material.R.attr.colorPrimaryContainer,
						com.google.android.material.R.attr.colorOnPrimaryContainer));
		items.add(
				new GenericMenuItemModel(
						"open_browser",
						R.string.open_in_browser,
						R.drawable.ic_browser,
						com.google.android.material.R.attr.colorPrimaryContainer,
						com.google.android.material.R.attr.colorOnPrimaryContainer));

		GenericMenuBottomSheet sheet =
				GenericMenuBottomSheet.newInstance(mrData.getTitle(), "!" + mrIid, items);
		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "merge":
							MergeMrBottomSheet.newInstance(projectId, mrIid, viewModel)
									.show(getSupportFragmentManager(), "mergeMrSheet");
							break;
						case "commits":
							Intent commitsIntent =
									mrContext.getProjects().getIntent(ctx, CommitsActivity.class);
							commitsIntent.putExtra("source", "mr");
							commitsIntent.putExtra("mergeRequestIid", mrIid);
							commitsIntent.putExtra("projectId", projectId);
							startActivity(commitsIntent);
							break;
						case "files":
							Bundle bundle = new Bundle();
							bundle.putString("source", "mr");
							bundle.putLong("projectId", projectId);
							bundle.putLong("mrIid", mrIid);
							CommitDiffsBottomSheet bottomSheet = new CommitDiffsBottomSheet();
							bottomSheet.setArguments(bundle);
							bottomSheet.show(
									((FragmentActivity) ctx).getSupportFragmentManager(),
									"mrDiffsBottomSheet");
							break;
						case "close":
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.close_mr)
									.setMessage(R.string.close_mr_confirmation)
									.setPositiveButton(
											R.string.close,
											(d, w) ->
													viewModel.toggleMrState(
															ctx, projectId, mrIid, "close"))
									.setNegativeButton(R.string.cancel, null)
									.show();
							break;
						case "reopen":
							viewModel.toggleMrState(ctx, projectId, mrIid, "reopen");
							break;
						case "lock":
							viewModel.toggleDiscussionLock(ctx, projectId, mrIid, true);
							break;
						case "unlock":
							viewModel.toggleDiscussionLock(ctx, projectId, mrIid, false);
							break;
						case "mark_ready":
							viewModel.toggleDraft(ctx, projectId, mrIid, mrData.getTitle(), false);
							break;
						case "mark_draft":
							viewModel.toggleDraft(ctx, projectId, mrIid, mrData.getTitle(), true);
							break;
						case "copy_url":
							Utils.copyToClipboard(
									ctx,
									mrData.getWebUrl(),
									getString(R.string.copied_to_clipboard));
							break;
						case "open_browser":
							Utils.openUrlInBrowser(this, mrData.getWebUrl());
							break;
					}
				});
		sheet.show(getSupportFragmentManager(), "mrMenuSheet");
	}

	private void setupHeader(MergeRequests mrData) {
		boolean isDraft = mrData.isDraft() || mrData.isWorkInProgress();
		binding.draftIcon.setVisibility(isDraft ? View.VISIBLE : View.GONE);
		binding.draftIcon.setOnClickListener(v -> Toasty.show(ctx, R.string.draft));

		String state = mrData.getState();
		int stateColor;
		String stateLabel;
		if ("merged".equalsIgnoreCase(state)) {
			stateColor =
					ctx.getResources().getColor(R.color.alert_important_border, ctx.getTheme());
			stateLabel = getString(R.string.merged);
		} else if ("closed".equalsIgnoreCase(state)) {
			stateColor = ctx.getResources().getColor(R.color.label_default_color, ctx.getTheme());
			stateLabel = getString(R.string.closed);
		} else {
			stateColor = ctx.getResources().getColor(R.color.green, ctx.getTheme());
			stateLabel = getString(R.string.open);
		}
		Drawable stateBadge = AvatarGenerator.getLabelDrawable(ctx, stateLabel, stateColor, 22);
		binding.stateBadge.setImageDrawable(stateBadge);
		binding.stateBadge.setVisibility(View.VISIBLE);

		binding.lockIcon.setVisibility(mrData.getDiscussionLocked() ? View.VISIBLE : View.GONE);
		if (mrData.getDiscussionLocked()) {
			binding.lockIcon.setOnClickListener(
					v -> Toasty.show(ctx, getString(R.string.discussion_locked_message)));
		}

		int accessLevel = AccessLevel.getUserAccessLevel(mrContext.getProjects().getProject());
		boolean canModify = accessLevel >= AccessLevel.MAINTAINER;
		boolean isMerged = "merged".equalsIgnoreCase(state);
		binding.btnEdit.setVisibility(canModify && !isMerged ? View.VISIBLE : View.GONE);
		binding.btnEdit.setOnClickListener(
				v -> {
					CreateMergeRequestBottomSheet.newInstance(
									"project", projectId, canModify, false, null, null, mrData,
									null)
							.show(getSupportFragmentManager(), "editMrSheet");
				});

		setupMrHeaderTitle(mrData.getTitle(), mrIid);

		Author author = mrData.getAuthor();
		if (author != null) {
			if (author.getAvatarUrl() != null) {
				Glide.with(ctx)
						.load(author.getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(binding.authorAvatar);
			} else {
				binding.authorAvatar.setImageDrawable(
						AvatarGenerator.getLetterAvatar(ctx, author.getName(), 36));
			}
			String displayName = author.getName() != null ? author.getName() : author.getUsername();
			binding.authorName.setText(displayName);
			binding.authorAvatar.setOnClickListener(v -> openProfile(author.getId()));
		}

		String createdAt = mrData.getCreatedAt();
		if (createdAt != null) {
			Date date = TimeHelper.parseIso8601(createdAt);
			binding.createdTime.setText(TimeHelper.formatTime(date));
			binding.createdTime.setOnClickListener(
					v -> Toasty.show(ctx, TimeHelper.getFullDateTime(date, Locale.getDefault())));
		}

		String description = mrData.getDescription();
		if (description != null && !description.isEmpty()) {
			Markdown.render(
					ctx,
					EmojiParser.parseToUnicode(description.trim()),
					binding.mrDescription,
					mrContext.getProjects());
		} else {
			binding.mrDescription.setVisibility(View.GONE);
		}
	}

	private void setupMrHeaderTitle(String titleText, long mrIid) {
		if (titleText == null) titleText = "";
		String numberText = " " + getString(R.string.mr_number, mrIid);

		TypedValue typedValue = new TypedValue();
		getTheme()
				.resolveAttribute(
						com.google.android.material.R.attr.colorOnSurface, typedValue, true);
		int baseColor = typedValue.data;
		int alphaColor = (baseColor & 0x00FFFFFF) | (0x99 << 24);

		Spanned titleSpanned =
				Markdown.renderToSpanned(ctx, EmojiParser.parseToUnicode(titleText.trim()));

		SpannableStringBuilder builder = new SpannableStringBuilder();
		builder.append(titleSpanned);

		int startPos = builder.length();
		builder.append(numberText);
		builder.setSpan(
				new ForegroundColorSpan(alphaColor),
				startPos,
				builder.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		binding.mrTitleAndNumber.setText(builder);
	}

	@SuppressLint("SetTextI18n")
	private void setupInfoSection(MergeRequests mrData) {
		setupLabels(mrData.getLabels());
		setupAssignees(mrData.getAssignees());

		if (mrData.getMilestone() != null) {
			binding.infoMilestone.setVisibility(View.VISIBLE);
			binding.infoMilestoneText.setText(mrData.getMilestone().getTitle());
		}

		String sourceBranch = mrData.getSourceBranch();
		String targetBranch = mrData.getTargetBranch();
		if (sourceBranch != null && targetBranch != null) {
			binding.infoBranches.setVisibility(View.VISIBLE);
			binding.infoBranchesText.setText(targetBranch + " ← " + sourceBranch);
		}
	}

	private void setupApprovals(MergeRequests mrData) {
		viewModel.fetchApprovals(ctx, projectId, mrIid);

		viewModel
				.getApprovals()
				.observe(
						this,
						approvals -> {
							if (approvals == null) return;

							binding.approvalSection.setVisibility(View.VISIBLE);
							binding.approvalDivider.setVisibility(View.VISIBLE);

							int approvedCount =
									approvals.getApprovedBy() != null
											? approvals.getApprovedBy().size()
											: 0;
							long requiredCount = approvals.getApprovalsRequired();

							binding.approvalCount.setText(
									getString(
											R.string.activity_mr_approvals,
											approvedCount,
											requiredCount));

							hasUserApproved = false;
							long myUserId =
									getAccount().getUserInfo() != null
											? getAccount().getUserInfo().getId()
											: 0;
							if (approvals.getApprovedBy() != null) {
								for (ApprovedBy ab : approvals.getApprovedBy()) {
									if (ab.getUser().getId() == myUserId) {
										hasUserApproved = true;
										break;
									}
								}
							}

							boolean isMerged = "merged".equalsIgnoreCase(mrData.getState());
							binding.btnApprove.setEnabled(!isMerged);
							binding.btnApprove.setText(
									hasUserApproved ? R.string.revoke_approval : R.string.approve);
							binding.btnApprove.setOnClickListener(
									v -> {
										if (hasUserApproved) {
											viewModel.revokeApproval(ctx, projectId, mrIid);
										} else {
											viewModel.approveMr(ctx, projectId, mrIid);
										}
									});
						});
	}

	private void setupLabels(List<String> labelNames) {
		this.currentLabelNames = labelNames != null ? labelNames : new ArrayList<>();
		if (currentLabelNames.isEmpty()) {
			binding.labelsScrollView.setVisibility(View.GONE);
			return;
		}
		binding.labelsScrollView.setVisibility(View.VISIBLE);
		binding.labelsContainer.removeAllViews();

		for (String name : currentLabelNames) {
			viewModel.fetchLabel(ctx, projectId, name);
		}

		if (!labelsObserverSet) {
			labelsObserverSet = true;
			viewModel
					.getLabelCache()
					.observe(
							this,
							cache -> {
								LabelStylingHelper styler = LabelStylingHelper.getInstance(ctx);
								binding.labelsContainer.removeAllViews();
								for (String name : currentLabelNames) {
									Labels label = cache.get(name);
									if (label != null) addLabelView(label, styler);
								}
							});
		}
	}

	private void addLabelView(Labels label, LabelStylingHelper styler) {
		if (LabelStylingHelper.isScopedLabel(label.getName())) {
			LinearLayout container = new LinearLayout(ctx);
			container.setOrientation(LinearLayout.HORIZONTAL);
			container.setGravity(Gravity.CENTER_VERTICAL);
			TextView labelName = new TextView(ctx);
			TextView labelValue = new TextView(ctx);
			styler.styleScopedLabel(
					label.getName(),
					label.getColor(),
					label.getTextColor(),
					labelName,
					labelValue,
					13,
					4,
					10);
			container.addView(labelName);
			container.addView(labelValue);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
			int margin = (int) (8 * ctx.getResources().getDisplayMetrics().density);
			params.setMargins(0, 0, margin, 0);
			container.setLayoutParams(params);
			binding.labelsContainer.addView(container);
		} else {
			TextView labelName = new TextView(ctx);
			styler.styleRegularLabel(
					label.getName(), label.getColor(), label.getTextColor(), labelName, 13, 4, 10);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
			int margin = (int) (8 * ctx.getResources().getDisplayMetrics().density);
			params.setMargins(0, 0, margin, 0);
			labelName.setLayoutParams(params);
			binding.labelsContainer.addView(labelName);
		}
	}

	private void setupAssignees(List<AssigneesItem> assignees) {
		if (assignees == null || assignees.isEmpty()) {
			binding.infoAssignees.setVisibility(View.GONE);
			return;
		}
		binding.infoAssignees.setVisibility(View.VISIBLE);
		binding.assigneesContainer.removeAllViews();

		int sizeDp = 24;
		int marginDp = 4;
		for (AssigneesItem a : assignees) {
			ImageView iv = new ImageView(ctx);
			int sizePx = (int) (sizeDp * getResources().getDisplayMetrics().density);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
			params.setMargins(
					0, 0, (int) (marginDp * getResources().getDisplayMetrics().density), 0);
			iv.setLayoutParams(params);
			Glide.with(ctx)
					.load(a.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.ic_spinner)
					.centerCrop()
					.transform(new RoundedCorners(14))
					.into(iv);
			iv.setOnClickListener(v -> openProfile(a.getId()));
			binding.assigneesContainer.addView(iv);
		}
	}

	private void initTimeline() {
		if (timelineInitialized) return;
		timelineInitialized = true;

		timelineViewModel = new ViewModelProvider(this).get(TimelineViewModel.class);

		long myUserId = getAccount().getUserInfo() != null ? getAccount().getUserInfo().getId() : 0;
		timelineAdapter =
				new TimelineAdapter(
						ctx,
						new ArrayList<>(),
						mrContext.getProjects(),
						getSupportFragmentManager(),
						myUserId,
						"mr");

		LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(timelineAdapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						timelineViewModel.loadNextPage(ctx);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		timelineViewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (Boolean.TRUE.equals(loading)
									&& timelineAdapter.getItemCount() == 0) {
								binding.progressBar.setVisibility(View.VISIBLE);
							} else {
								binding.progressBar.setVisibility(View.GONE);
							}
						});

		timelineViewModel
				.getTimelineList()
				.observe(
						this,
						list -> {
							if (list != null && !list.isEmpty()) {
								binding.recyclerView.setVisibility(View.VISIBLE);
								timelineAdapter.updateList(list);
							}
						});

		timelineViewModel
				.getIsSubmitting()
				.observe(
						this,
						isSubmitting -> {
							if (Boolean.TRUE.equals(isSubmitting)) {
								binding.commentBox.btnQuickSend.setVisibility(View.GONE);
								binding.commentBox.commentLoader.setVisibility(View.VISIBLE);
							} else {
								binding.commentBox.commentLoader.setVisibility(View.GONE);
								binding.commentBox.btnQuickSend.setVisibility(View.VISIBLE);
							}
						});

		timelineViewModel
				.getSubmittedComment()
				.observe(
						this,
						comment -> {
							if (comment != null) {
								Toasty.show(ctx, getString(R.string.comment_posted));
								hideCommentBox();
								timelineViewModel.clearSubmittedComment();
								refreshTimeline();
								binding.recyclerView.postDelayed(
										this::scrollTimelineToBottom, 1500);
							}
						});

		timelineViewModel
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
									break;
								case "generic_error":
									Toasty.show(ctx, getString(R.string.generic_error));
									break;
								default:
									Toasty.show(ctx, errorMsg);
									break;
							}
							timelineViewModel.clearError();
						});
	}

	private void refreshTimeline() {
		timelineViewModel.loadTimeline(ctx, projectId, mrIid, "mr");
	}

	private void loadTimeline() {
		initTimeline();
		refreshTimeline();
	}

	private void toggleCommentSectionSpace(boolean expand) {
		float density = getResources().getDisplayMetrics().density;
		int targetHeight = expand ? (int) (190 * density) : 0;
		ValueAnimator animator =
				ValueAnimator.ofInt(binding.scrollSpacer.getLayoutParams().height, targetHeight);
		animator.setDuration(300);
		animator.addUpdateListener(
				animation -> {
					binding.scrollSpacer.getLayoutParams().height =
							(int) animation.getAnimatedValue();
					binding.scrollSpacer.requestLayout();
				});
		animator.start();
	}

	private void toggleCommentBox() {
		if (binding.commentBox.getRoot().getVisibility() == View.VISIBLE) {
			hideCommentBox();
		} else {
			showCommentBox();
		}
	}

	private void showCommentBox() {
		MergeRequests mrData = viewModel.getMrData().getValue();
		if (mrData != null && mrData.getDiscussionLocked()) {
			int accessLevel = AccessLevel.getUserAccessLevel(mrContext.getProjects().getProject());
			boolean isMember = accessLevel >= AccessLevel.REPORTER;
			if (!isMember) {
				Toasty.show(ctx, getString(R.string.discussion_locked));
				return;
			}
		}
		toggleCommentSectionSpace(true);
		binding.commentBox.getRoot().setVisibility(View.VISIBLE);
		binding.commentBox.getRoot().setAlpha(0f);
		binding.commentBox.getRoot().setTranslationY(100f);
		binding.commentBox
				.getRoot()
				.animate()
				.alpha(1f)
				.translationY(0f)
				.setDuration(250)
				.withEndAction(() -> binding.commentBox.etQuickComment.requestFocus())
				.start();
	}

	private void hideCommentBox() {
		toggleCommentSectionSpace(false);
		Utils.hideKeyboard(this);
		binding.commentBox.etQuickComment.setText("");
		binding.commentBox
				.getRoot()
				.animate()
				.alpha(0f)
				.translationY(100f)
				.setDuration(200)
				.withEndAction(() -> binding.commentBox.getRoot().setVisibility(View.GONE))
				.start();
	}

	private void setupCommentBox() {
		binding.commentBox.etQuickComment.setOnFocusChangeListener(
				(v, hasFocus) -> {
					if (hasFocus) Utils.showKeyboard(this, binding.commentBox.etQuickComment);
				});
		binding.commentBox.btnCloseReply.setOnClickListener(v -> hideCommentBox());
		binding.commentBox.btnQuickSend.setOnClickListener(v -> submitComment());
		binding.commentBox.etQuickComment.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == MotionEvent.ACTION_UP
							|| event.getAction() == MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});
	}

	private void submitComment() {
		String body =
				binding.commentBox.etQuickComment.getText() != null
						? binding.commentBox.etQuickComment.getText().toString().trim()
						: "";
		if (body.isEmpty()) return;
		timelineViewModel.addComment(ctx, projectId, mrIid, body);
	}

	private void scrollTimelineToBottom() {
		binding.scrollView.post(
				() ->
						binding.scrollView.smoothScrollTo(
								0, binding.scrollView.getChildAt(0).getHeight()));
	}

	private void setupReactions() {
		if (reactionsViewModel == null) {
			reactionsViewModel = new ViewModelProvider(this).get(ReactionsViewModel.class);
		}
		long myUserId = getAccount().getUserInfo() != null ? getAccount().getUserInfo().getId() : 0;
		binding.reactionsView.configure(
				projectId, "mr", mrIid, null, getSupportFragmentManager(), myUserId);
		binding.reactionsView.loadReactions(reactionsViewModel);
	}

	private void openProfile(long userId) {
		Intent intent = new Intent(ctx, ProfileActivity.class);
		intent.putExtra("userId", String.valueOf(userId));
		startActivity(intent);
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (Boolean.TRUE.equals(loading)) {
								binding.progressBar.setVisibility(View.VISIBLE);
							} else {
								binding.progressBar.setVisibility(View.GONE);
								binding.scrollView.setVisibility(View.VISIBLE);
							}
						});

		viewModel
				.getMrData()
				.observe(
						this,
						mrData -> {
							if (mrData == null) return;
							mrContext.setMergeRequest(mrData);
							setupHeader(mrData);
							updateCommentButton(mrData);
							setupInfoSection(mrData);
							setupApprovals(mrData);
							setupReactions();
							loadTimeline();
						});

		viewModel
				.getCloseSuccess()
				.observe(
						this,
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(ctx, getString(R.string.mr_closed));
								AppUIStateManager.refreshData();
								viewModel.clearCloseSuccess();
							}
						});

		viewModel
				.getReopenSuccess()
				.observe(
						this,
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(ctx, getString(R.string.mr_reopened));
								AppUIStateManager.refreshData();
								viewModel.clearReopenSuccess();
							}
						});

		viewModel
				.getMergeSuccess()
				.observe(
						this,
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(ctx, getString(R.string.merge_request_merged_text));
								AppUIStateManager.refreshData();
								viewModel.clearMergeSuccess();
							}
						});

		viewModel
				.getApproveSuccess()
				.observe(
						this,
						success -> {
							if (Boolean.TRUE.equals(success)) {
								viewModel.clearApproveSuccess();
								viewModel.fetchApprovals(ctx, projectId, mrIid);
							}
						});

		viewModel
				.getLockSuccess()
				.observe(
						this,
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(ctx, getString(R.string.discussion_locked_success));
								viewModel.clearLockSuccess();
							}
						});

		viewModel
				.getUnlockSuccess()
				.observe(
						this,
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(ctx, getString(R.string.discussion_unlocked_success));
								viewModel.clearUnlockSuccess();
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
									break;
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

	@Override
	protected void onGlobalRefresh() {
		viewModel.loadMergeRequest(ctx, projectId, mrIid);
	}
}
