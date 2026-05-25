package com.labnex.app.activities;

import android.animation.ValueAnimator;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.TimelineAdapter;
import com.labnex.app.bottomsheets.CreateIssueBottomSheet;
import com.labnex.app.bottomsheets.GenericMenuBottomSheet;
import com.labnex.app.contexts.IssueContext;
import com.labnex.app.databinding.ActivityIssueDetailBinding;
import com.labnex.app.helpers.AccessLevel;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.LabelStylingHelper;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.models.issues.AssigneesItem;
import com.labnex.app.models.issues.Author;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.labels.Labels;
import com.labnex.app.viewmodels.IssueDetailViewModel;
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
public class IssueDetailActivity extends BaseActivity {

	private ActivityIssueDetailBinding binding;
	private IssueDetailViewModel viewModel;
	private TimelineViewModel timelineViewModel;
	private TimelineAdapter timelineAdapter;
	private ReactionsViewModel reactionsViewModel;

	private IssueContext issue;
	private long issueIid;
	private long projectId;
	private boolean timelineInitialized = false;
	private boolean labelsObserverSet = false;
	private List<String> currentLabelNames = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityIssueDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, binding.scrollView, null, null);

		viewModel = new ViewModelProvider(this).get(IssueDetailViewModel.class);

		issue = IssueContext.fromIntent(getIntent());

		if (issue == null || issue.getProjects() == null) {
			Toasty.show(ctx, getString(R.string.cannot_find_issue));
			finish();
			return;
		}

		issueIid = issue.getIssueIndex();
		projectId = issue.getProjects().getProjectId();

		if (issueIid <= 0 || projectId <= 0) {
			Toasty.show(ctx, getString(R.string.cannot_find_issue));
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

		viewModel.loadIssue(ctx, projectId, issueIid);
	}

	private void setupDock() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnComment.setOnClickListener(v -> toggleCommentBox());
		binding.btnMenu.setOnClickListener(v -> showIssueMenu());
	}

	private void updateCommentButton(Issues issueData) {
		boolean isLocked = issueData.getDiscussionLocked();
		boolean isArchived = issue.getProjects().getProject().isArchived();

		if (isArchived) {
			binding.btnComment.setVisibility(View.GONE);
			return;
		}

		if (isLocked) {
			int accessLevel = AccessLevel.getUserAccessLevel(issue.getProjects().getProject());
			boolean isMember = accessLevel >= AccessLevel.REPORTER;
			binding.btnComment.setEnabled(isMember);
			binding.btnComment.setAlpha(isMember ? 1.0f : 0.4f);
		} else {
			binding.btnComment.setEnabled(true);
			binding.btnComment.setAlpha(1.0f);
		}
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
		Issues issueData = viewModel.getIssueData().getValue();
		if (issueData != null && issueData.getDiscussionLocked()) {
			int accessLevel = AccessLevel.getUserAccessLevel(issue.getProjects().getProject());
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
					if (hasFocus) {
						Utils.showKeyboard(this, binding.commentBox.etQuickComment);
					}
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

		timelineViewModel.addComment(ctx, projectId, issueIid, body);
	}

	private void scrollTimelineToBottom() {
		binding.scrollView.post(
				() ->
						binding.scrollView.smoothScrollTo(
								0, binding.scrollView.getChildAt(0).getHeight()));
	}

	private void showIssueMenu() {
		Issues issueData = issue.getIssue();
		if (issueData == null) return;

		List<GenericMenuItemModel> items = new ArrayList<>();
		boolean isClosed = "closed".equalsIgnoreCase(issueData.getState());
		boolean isLocked = issueData.getDiscussionLocked();
		int accessLevel = AccessLevel.getUserAccessLevel(issue.getProjects().getProject());
		boolean canModify = accessLevel >= AccessLevel.MAINTAINER;

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
				GenericMenuBottomSheet.newInstance(issueData.getTitle(), "#" + issueIid, items);
		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "close":
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.close_issue)
									.setMessage(R.string.close_issue_confirmation)
									.setPositiveButton(
											R.string.close,
											(d, w) ->
													viewModel.toggleIssueState(
															ctx, projectId, issueIid, "close"))
									.setNegativeButton(R.string.cancel, null)
									.show();
							break;
						case "reopen":
							viewModel.toggleIssueState(ctx, projectId, issueIid, "reopen");
							break;
						case "lock":
							viewModel.toggleDiscussionLock(ctx, projectId, issueIid, true);
							break;
						case "unlock":
							viewModel.toggleDiscussionLock(ctx, projectId, issueIid, false);
							break;
						case "copy_url":
							Utils.copyToClipboard(
									ctx,
									issueData.getWebUrl(),
									getString(R.string.copied_to_clipboard));
							break;
						case "open_browser":
							Utils.openUrlInBrowser(this, issueData.getWebUrl());
							break;
					}
				});
		sheet.show(getSupportFragmentManager(), "issueMenuSheet");
	}

	private void setupHeader(Issues issueData) {
		String state = issueData.getState();
		String stateLabel =
				state != null ? state.substring(0, 1).toUpperCase() + state.substring(1) : "";
		int stateColor;
		if ("closed".equalsIgnoreCase(state)) {
			stateColor = ctx.getResources().getColor(R.color.label_default_color, ctx.getTheme());
		} else {
			stateColor = ctx.getResources().getColor(R.color.green, ctx.getTheme());
			stateLabel = getString(R.string.open);
		}

		Drawable stateBadge = AvatarGenerator.getLabelDrawable(ctx, stateLabel, stateColor, 22);
		binding.stateBadge.setImageDrawable(stateBadge);

		binding.lockIcon.setVisibility(issueData.getDiscussionLocked() ? View.VISIBLE : View.GONE);
		if (issueData.getDiscussionLocked()) {
			binding.lockIcon.setOnClickListener(
					v -> Toasty.show(ctx, getString(R.string.discussion_locked_message)));
		}

		if (issueData.isConfidential()) {
			int confidentialColor =
					ctx.getResources().getColor(R.color.alert_warning_border, ctx.getTheme());
			Drawable confidentialBadge =
					AvatarGenerator.getLabelDrawable(
							ctx, getString(R.string.confidential), confidentialColor, 22);
			binding.confidentialBadge.setImageDrawable(confidentialBadge);
			binding.confidentialBadge.setVisibility(View.VISIBLE);
		} else {
			binding.confidentialBadge.setVisibility(View.GONE);
		}

		int accessLevel = AccessLevel.getUserAccessLevel(issue.getProjects().getProject());
		boolean canModify = accessLevel >= AccessLevel.MAINTAINER;
		binding.btnEdit.setVisibility(canModify ? View.VISIBLE : View.GONE);
		binding.btnEdit.setOnClickListener(
				v -> {
					CreateIssueBottomSheet.newInstance("project", projectId, canModify, issueData)
							.show(getSupportFragmentManager(), "editIssueSheet");
				});

		setupIssueHeaderTitle(issueData.getTitle(), issueIid);

		Author author = issueData.getAuthor();
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

		String createdAt = issueData.getCreatedAt();
		if (createdAt != null) {
			Date date = TimeHelper.parseIso8601(createdAt);
			binding.createdTime.setText(TimeHelper.formatTime(date));
			binding.createdTime.setOnClickListener(
					v -> Toasty.show(ctx, TimeHelper.getFullDateTime(date, Locale.getDefault())));
		}

		String description = issueData.getDescription();
		if (description != null && !description.isEmpty()) {
			Markdown.render(
					ctx,
					EmojiParser.parseToUnicode(description.trim()),
					binding.issueDescription,
					issue.getProjects());
		} else {
			binding.issueDescription.setVisibility(View.GONE);
		}
	}

	private void setupIssueHeaderTitle(String titleText, long issueIid) {
		if (titleText == null) titleText = "";

		String numberText = " " + getString(R.string.issue_number, issueIid);

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

		binding.issueTitleAndNumber.setText(builder);
	}

	private void setupInfoSection(Issues issueData) {
		setupLabels(issueData.getLabels());
		setupAssignees(issueData.getAssignees());

		if (issueData.getMilestone() != null) {
			binding.infoMilestone.setVisibility(View.VISIBLE);
			binding.infoMilestoneText.setText(issueData.getMilestone().getTitle());
		}

		if (issueData.getDueDate() != null) {
			binding.infoDueDate.setVisibility(View.VISIBLE);
			binding.infoDueDateText.setText(issueData.getDueDate());
		}
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
									if (label != null) {
										addLabelView(label, styler);
									}
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
						issue.getProjects(),
						getSupportFragmentManager(),
						myUserId,
						"issue");

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
							if (errorMsg != null) {
								Toasty.show(ctx, errorMsg);
								timelineViewModel.clearError();
							}
						});
	}

	private void refreshTimeline() {
		timelineViewModel.loadTimeline(ctx, projectId, issueIid, "issue");
	}

	private void loadTimeline() {
		initTimeline();
		refreshTimeline();
	}

	private void openProfile(long userId) {
		Intent intent = new Intent(ctx, ProfileActivity.class);
		intent.putExtra("userId", String.valueOf(userId));
		startActivity(intent);
	}

	private void setupReactions() {
		if (reactionsViewModel == null) {
			reactionsViewModel = new ViewModelProvider(this).get(ReactionsViewModel.class);
		}
		long myUserId = getAccount().getUserInfo() != null ? getAccount().getUserInfo().getId() : 0;
		binding.reactionsView.configure(
				projectId, "issue", issueIid, null, getSupportFragmentManager(), myUserId);
		binding.reactionsView.loadReactions(reactionsViewModel);
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
				.getIssueData()
				.observe(
						this,
						issueData -> {
							if (issueData == null) return;
							issue.setIssue(issueData);
							setupHeader(issueData);
							updateCommentButton(issueData);
							setupInfoSection(issueData);
							setupReactions();
							loadTimeline();
						});

		viewModel
				.getCloseSuccess()
				.observe(
						this,
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(ctx, getString(R.string.issue_closed));
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
								Toasty.show(ctx, getString(R.string.issue_reopened));
								AppUIStateManager.refreshData();
								viewModel.clearReopenSuccess();
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
		viewModel.loadIssue(ctx, projectId, issueIid);
	}
}
