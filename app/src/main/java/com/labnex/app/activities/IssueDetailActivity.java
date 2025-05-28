package com.labnex.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.IssueNotesAdapter;
import com.labnex.app.bottomsheets.CommentOnIssueBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.IssueContext;
import com.labnex.app.databinding.ActivityIssueDetailBinding;
import com.labnex.app.databinding.BottomSheetIssueActionsBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.TextDrawable.ColorGenerator;
import com.labnex.app.helpers.TextDrawable.TextDrawable;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.helpers.Utils;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.issues.CrudeIssue;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.labels.Labels;
import com.labnex.app.viewmodels.IssueMrNotesViewModel;
import com.vdurmont.emoji.EmojiParser;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssueDetailActivity extends BaseActivity
		implements BottomSheetListener, CommentOnIssueBottomSheet.UpdateInterface {

	public IssueContext issue;
	private int issueIndex;
	private int projectId;
	private ActivityIssueDetailBinding activityIssueDetailBinding;
	private IssueMrNotesViewModel issueMrNotesViewModel;
	private IssueNotesAdapter issueNotesAdapter;
	private boolean infoCard = false;
	private int page = 1;
	private int resultLimit;
	private final String type = "issue";
	private BottomSheetIssueActionsBinding sheetBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityIssueDetailBinding = ActivityIssueDetailBinding.inflate(getLayoutInflater());
		setContentView(activityIssueDetailBinding.getRoot());

		issueMrNotesViewModel = new ViewModelProvider(this).get(IssueMrNotesViewModel.class);

		CommentOnIssueBottomSheet.setUpdateListener(this);

		Locale locale = ctx.getResources().getConfiguration().getLocales().get(0);
		issue = IssueContext.fromIntent(getIntent());

		resultLimit = getAccount().getMaxPageLimit();

		issueIndex = issue.getIssueIndex();
		if (issue.getProjects() != null) {
			projectId = issue.getProjects().getProjectId();
		}

		activityIssueDetailBinding.bottomAppBar.setNavigationOnClickListener(
				bottomAppBar -> finish());

		activityIssueDetailBinding.bottomAppBar.setOnMenuItemClickListener(
				item -> {
					if (item.getItemId() == R.id.menu) {
						showIssueActionsBottomSheet();
						return true;
					}
					return false;
				});

		if (!issue.getProjects().getProject().isArchived()) {
			activityIssueDetailBinding.newNote.setOnClickListener(
					accounts -> {
						if (issue.getIssue().getDiscussionLocked()) {
							MaterialAlertDialogBuilder materialAlertDialogBuilder =
									new MaterialAlertDialogBuilder(
											ctx,
											com.google.android.material.R.style
													.ThemeOverlay_Material3_Dialog_Alert);

							materialAlertDialogBuilder
									.setTitle(R.string.discussion_locked)
									.setMessage(R.string.discussion_locked_message)
									.setPositiveButton(
											R.string.proceed,
											(dialog, whichButton) -> {
												initComment();
											})
									.setNeutralButton(R.string.cancel, null)
									.show();
						} else {
							initComment();
						}
					});
		} else {
			activityIssueDetailBinding.newNote.setVisibility(View.GONE);
		}

		activityIssueDetailBinding.recyclerView.setHasFixedSize(true);
		activityIssueDetailBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
		activityIssueDetailBinding.recyclerView.setNestedScrollingEnabled(false);

		ColorGenerator generator = ColorGenerator.MATERIAL;
		int color = generator.getColor(issue.getIssue().getAuthor().getName());
		String firstCharacter = String.valueOf(issue.getIssue().getAuthor().getName().charAt(0));

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

		String issueId =
				"<font color='"
						+ ResourcesCompat.getColor(
								getResources(), R.color.md_theme_onBackground, null)
						+ "'>"
						+ appCtx.getResources().getString(R.string.hash)
						+ issueIndex
						+ "</font>";
		String modifiedTime =
				TimeUtils.formatTime(
						Date.from(
								OffsetDateTime.parse(issue.getIssue().getCreatedAt()).toInstant()),
						locale);

		if (issue.getIssue().getAuthor().getAvatarUrl() != null) {

			Glide.with(ctx)
					.load(issue.getIssue().getAuthor().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.ic_spinner)
					.centerCrop()
					.into(activityIssueDetailBinding.userAvatar);
		} else {
			activityIssueDetailBinding.userAvatar.setImageDrawable(drawable);
		}

		activityIssueDetailBinding.issueTitle.setText(
				HtmlCompat.fromHtml(
						EmojiParser.parseToUnicode(issue.getIssue().getTitle()) + " " + issueId,
						HtmlCompat.FROM_HTML_MODE_LEGACY));

		if (issue.getIssue().getMilestone() != null) {

			activityIssueDetailBinding.milestoneFrame.setVisibility(View.VISIBLE);
			activityIssueDetailBinding.issueMilestone.setText(
					issue.getIssue().getMilestone().getTitle());
			infoCard = true;
		}

		if (issue.getIssue().getDueDate() != null) {

			activityIssueDetailBinding.dueDateFrame.setVisibility(View.VISIBLE);
			activityIssueDetailBinding.issueDueDate.setText(
					issue.getIssue().getDueDate().toString());
			infoCard = true;
		}

		getLabels();

		if (!issue.getIssue().getDescription().isEmpty()) {
			Markdown.render(
					ctx,
					EmojiParser.parseToUnicode(issue.getIssue().getDescription().trim()),
					activityIssueDetailBinding.issueDescription,
					issue.getProjects());
		} else {
			activityIssueDetailBinding.issueDescription.setVisibility(View.GONE);
		}

		activityIssueDetailBinding.username.setText(issue.getIssue().getAuthor().getName());
		activityIssueDetailBinding.createdTime.setText(modifiedTime);
		activityIssueDetailBinding.issueThumbsUpCount.setText(
				String.valueOf(issue.getIssue().getUpvotes()));
		activityIssueDetailBinding.issueThumbsDownCount.setText(
				String.valueOf(issue.getIssue().getDownvotes()));

		activityIssueDetailBinding.userAvatar.setOnClickListener(
				profile -> {
					Intent intent = new Intent(ctx, ProfileActivity.class);
					intent.putExtra("source", "issue_detail");
					intent.putExtra("userId", issue.getIssue().getAuthor().getId());
					ctx.startActivity(intent);
				});

		if (!issue.getIssue().getAssignees().isEmpty()) {

			infoCard = true;
			LinearLayout.LayoutParams paramsAssignees = new LinearLayout.LayoutParams(64, 64);
			paramsAssignees.setMargins(0, 0, 16, 0);

			activityIssueDetailBinding.assigneesScrollView.setVisibility(View.VISIBLE);

			for (int i = 0; i < issue.getIssue().getAssignees().size(); i++) {

				ImageView assigneesView = new ImageView(ctx);

				Glide.with(ctx)
						.load(issue.getIssue().getAssignees().get(i).getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
						.into(assigneesView);

				activityIssueDetailBinding.assigneesFrame.addView(assigneesView);
				assigneesView.setLayoutParams(paramsAssignees);

				int finalI = i;
				assigneesView.setOnClickListener(
						profile -> {
							Intent intent = new Intent(ctx, ProfileActivity.class);
							intent.putExtra("source", "issue_detail");
							intent.putExtra(
									"userId", issue.getIssue().getAssignees().get(finalI).getId());
							ctx.startActivity(intent);
						});
			}
		}

		if (!infoCard) {
			activityIssueDetailBinding.issueInfoCard.setVisibility(View.GONE);
		}

		getIssueNotesData();
	}

	private void showIssueActionsBottomSheet() {

		sheetBinding =
				BottomSheetIssueActionsBinding.inflate(LayoutInflater.from(this), null, false);
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		if (issue.getIssue().getState().equalsIgnoreCase("closed")) {
			sheetBinding.closeItemCard.setVisibility(View.GONE);
		}

		sheetBinding.closeItem.setOnClickListener(
				v -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx);
					materialAlertDialogBuilder
							.setTitle(R.string.close)
							.setMessage(R.string.close_issue)
							.setPositiveButton(
									R.string.close,
									(dialog, whichButton) -> {
										closeIssue();
									})
							.setNeutralButton(R.string.cancel, null)
							.show();
					bottomSheetDialog.dismiss();
				});

		sheetBinding.urlCopyItem.setOnClickListener(
				v -> {
					Utils.copyToClipboard(
							ctx,
							IssueDetailActivity.this,
							issue.getIssue().getWebUrl(),
							getString(R.string.copy_url_message));
					bottomSheetDialog.dismiss();
				});

		sheetBinding.openInBrowserItem.setOnClickListener(
				v -> {
					Utils.openUrlInBrowser(
							this, IssueDetailActivity.this, issue.getIssue().getWebUrl());
					bottomSheetDialog.dismiss();
				});

		bottomSheetDialog.show();
	}

	@Override
	public void updateDataListener(String str) {

		if (str.equalsIgnoreCase("created")) {
			Snackbar.info(
					IssueDetailActivity.this,
					activityIssueDetailBinding.bottomAppBar,
					getString(R.string.comment_posted));
		}

		issueNotesAdapter.clearAdapter();
		page = 1;
		getIssueNotesData();
	}

	private void initComment() {

		Bundle bsBundle = new Bundle();
		bsBundle.putString("source", "comment");
		bsBundle.putInt("projectId", issue.getProjects().getProjectId());
		bsBundle.putInt("issueIid", issue.getIssueIndex());
		CommentOnIssueBottomSheet bottomSheet = new CommentOnIssueBottomSheet();
		bottomSheet.setArguments(bsBundle);
		bottomSheet.show(getSupportFragmentManager(), "commentOnIssueBottomSheet");
	}

	@Override
	public void onButtonClicked(String text) {}

	private void closeIssue() {

		CrudeIssue crudeIssue = new CrudeIssue();
		crudeIssue.setStateEvent("close");

		Call<Issues> call =
				RetrofitClient.getApiInterface(ctx).updateIssue(projectId, issueIndex, crudeIssue);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Issues> call,
							@NonNull retrofit2.Response<Issues> response) {

						if (response.code() == 200) {

							sheetBinding.closeItemCard.setVisibility(View.GONE);
							IssuesActivity.updateIssuesList = true;
							Snackbar.info(
									IssueDetailActivity.this,
									activityIssueDetailBinding.bottomAppBar,
									ctx.getString(R.string.issue_closed));
						} else if (response.code() == 401) {

							Snackbar.info(
									IssueDetailActivity.this,
									activityIssueDetailBinding.bottomAppBar,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									IssueDetailActivity.this,
									activityIssueDetailBinding.bottomAppBar,
									getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									IssueDetailActivity.this,
									activityIssueDetailBinding.bottomAppBar,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {

						Snackbar.info(
								IssueDetailActivity.this,
								activityIssueDetailBinding.bottomAppBar,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void getIssueNotesData() {

		activityIssueDetailBinding.progressBar.setVisibility(View.VISIBLE);

		issueMrNotesViewModel
				.getNotes(
						ctx,
						projectId,
						issueIndex,
						type,
						resultLimit,
						page,
						IssueDetailActivity.this,
						activityIssueDetailBinding.bottomAppBar)
				.observe(
						IssueDetailActivity.this,
						mainList -> {
							issueNotesAdapter =
									new IssueNotesAdapter(
											IssueDetailActivity.this,
											mainList,
											issue.getProjects());
							issueNotesAdapter.setLoadMoreListener(
									new IssueNotesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											issueMrNotesViewModel.loadMore(
													ctx,
													projectId,
													issueIndex,
													type,
													resultLimit,
													page,
													issueNotesAdapter,
													IssueDetailActivity.this,
													activityIssueDetailBinding.bottomAppBar);
											activityIssueDetailBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											activityIssueDetailBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							if (issueNotesAdapter.getItemCount() > 0) {
								activityIssueDetailBinding.notesInfoCard.setVisibility(
										View.VISIBLE);
								activityIssueDetailBinding.recyclerView.setAdapter(
										issueNotesAdapter);
							}

							activityIssueDetailBinding.progressBar.setVisibility(View.GONE);
						});
	}

	private void getLabels() {

		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 20, 0);

		if (!issue.getIssue().getLabels().isEmpty()) {

			infoCard = true;
			activityIssueDetailBinding.labelsScrollView.setVisibility(View.VISIBLE);
			activityIssueDetailBinding.labelsFrame.removeAllViews();

			for (int i = 0; i < issue.getIssue().getLabels().size(); i++) {

				ImageView labelsView = new ImageView(ctx);
				activityIssueDetailBinding.labelsFrame.setOrientation(LinearLayout.HORIZONTAL);
				activityIssueDetailBinding.labelsFrame.setGravity(Gravity.START | Gravity.TOP);
				labelsView.setLayoutParams(params);

				int height = Utils.getPixelsFromDensity(ctx, 22);
				int textSize = Utils.getPixelsFromScaledDensity(ctx, 14);

				Call<Labels> call =
						RetrofitClient.getApiInterface(ctx)
								.getProjectLabel(
										projectId, issue.getIssue().getLabels().get(i).toString());

				call.enqueue(
						new Callback<>() {

							@Override
							public void onResponse(
									@NonNull Call<Labels> call,
									@NonNull Response<Labels> response) {

								if (response.code() == 200) {

									assert response.body() != null;
									int labelColor =
											Color.parseColor(
													Utils.repeatString(
															response.body().getColor(), 4, 1, 2));
									int textColor =
											Color.parseColor(
													Utils.repeatString(
															response.body().getTextColor(),
															4,
															1,
															2));
									String labelName = response.body().getName();

									TextDrawable drawable =
											TextDrawable.builder()
													.beginConfig()
													.textColor(textColor)
													.fontSize(textSize)
													.width(
															Utils.calculateLabelWidth(
																	labelName,
																	textSize,
																	Utils.getPixelsFromDensity(
																			ctx, 8)))
													.height(height)
													.endConfig()
													.buildRoundRect(
															labelName,
															labelColor,
															Utils.getPixelsFromDensity(ctx, 18));

									labelsView.setImageDrawable(drawable);
									activityIssueDetailBinding.labelsFrame.addView(labelsView);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Labels> call, @NonNull Throwable t) {}
						});
			}
		}
	}
}
