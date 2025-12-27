package com.labnex.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
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
import com.labnex.app.contexts.MergeRequestContext;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityMergeRequestDetailBinding;
import com.labnex.app.databinding.BottomSheetMergeRequestActionsBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.TextDrawable.ColorGenerator;
import com.labnex.app.helpers.TextDrawable.TextDrawable;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.helpers.Utils;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.approvals.Approvals;
import com.labnex.app.models.approvals.ApprovedBy;
import com.labnex.app.models.labels.Labels;
import com.labnex.app.models.merge_requests.CrudeMergeRequest;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.user.User;
import com.labnex.app.viewmodels.IssueMrNotesViewModel;
import com.vdurmont.emoji.EmojiParser;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MergeRequestDetailActivity extends BaseActivity
		implements BottomSheetListener, CommentOnIssueBottomSheet.UpdateInterface {

	public MergeRequestContext mergeRequestContext;
	private int mergeRequestIndex;
	private int projectId;
	private ActivityMergeRequestDetailBinding activityMergeRequestDetailBinding;
	private IssueMrNotesViewModel issueMrNotesViewModel;
	private IssueNotesAdapter issueNotesAdapter;
	private boolean infoCard = false;
	private int page = 1;
	private int resultLimit;
	private final String type = "mr";
	private int approvals = 0;
	private int requiredApprovals = 0;
	private BottomSheetMergeRequestActionsBinding sheetBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityMergeRequestDetailBinding =
				ActivityMergeRequestDetailBinding.inflate(getLayoutInflater());
		setContentView(activityMergeRequestDetailBinding.getRoot());

		issueMrNotesViewModel = new ViewModelProvider(this).get(IssueMrNotesViewModel.class);

		CommentOnIssueBottomSheet.setUpdateListener(this);

		Locale locale = ctx.getResources().getConfiguration().getLocales().get(0);
		mergeRequestContext = MergeRequestContext.fromIntent(getIntent());

		resultLimit = getAccount().getMaxPageLimit();

		mergeRequestIndex = mergeRequestContext.getMergeRequestIndex();
		if (mergeRequestContext.getProjects() != null) {
			projectId = mergeRequestContext.getProjects().getProjectId();
		}

		activityMergeRequestDetailBinding.bottomAppBar.setNavigationOnClickListener(
				bottomAppBar -> finish());

		activityMergeRequestDetailBinding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					if (menuItem.getItemId() == R.id.menu) {
						showMergeRequestActionsBottomSheet();
						return true;
					}
					return false;
				});

		if (!mergeRequestContext.getProjects().getProject().isArchived()) {
			activityMergeRequestDetailBinding.newNote.setOnClickListener(
					accounts -> {
						if (mergeRequestContext.getMergeRequest().getDiscussionLocked()) {
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
											(dialog, whichButton) -> initComment())
									.setNeutralButton(R.string.cancel, null)
									.show();
						} else {
							initComment();
						}
					});
		} else {
			activityMergeRequestDetailBinding.newNote.setVisibility(View.GONE);
		}

		activityMergeRequestDetailBinding.recyclerView.setHasFixedSize(true);
		activityMergeRequestDetailBinding.recyclerView.setLayoutManager(
				new LinearLayoutManager(this));
		activityMergeRequestDetailBinding.recyclerView.setNestedScrollingEnabled(false);

		ColorGenerator generator = ColorGenerator.MATERIAL;
		int color = generator.getColor(mergeRequestContext.getMergeRequest().getAuthor().getName());
		String firstCharacter =
				String.valueOf(
						mergeRequestContext.getMergeRequest().getAuthor().getName().charAt(0));

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

		String mrId =
				"<font color='"
						+ ResourcesCompat.getColor(
								getResources(), R.color.md_theme_onBackground, null)
						+ "'>"
						+ appCtx.getResources().getString(R.string.hash)
						+ mergeRequestIndex
						+ "</font>";
		String modifiedTime =
				TimeUtils.formatTime(
						Date.from(
								OffsetDateTime.parse(
												mergeRequestContext
														.getMergeRequest()
														.getCreatedAt())
										.toInstant()),
						locale);

		if (mergeRequestContext.getMergeRequest().getAuthor().getAvatarUrl() != null) {

			Glide.with(ctx)
					.load(mergeRequestContext.getMergeRequest().getAuthor().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.ic_spinner)
					.centerCrop()
					.into(activityMergeRequestDetailBinding.userAvatar);
		} else {
			activityMergeRequestDetailBinding.userAvatar.setImageDrawable(drawable);
		}

		Markdown.render(
				ctx,
				String.valueOf(
						HtmlCompat.fromHtml(
								EmojiParser.parseToUnicode(
												mergeRequestContext.getMergeRequest().getTitle())
										+ " "
										+ mrId,
								HtmlCompat.FROM_HTML_MODE_LEGACY)),
				activityMergeRequestDetailBinding.mrTitle);

		if (mergeRequestContext.getMergeRequest().getMilestone() != null) {

			activityMergeRequestDetailBinding.milestoneFrame.setVisibility(View.VISIBLE);
			activityMergeRequestDetailBinding.mrMilestone.setText(
					mergeRequestContext.getMergeRequest().getMilestone().getTitle());
			infoCard = true;
		}

		getLabels();

		if (!mergeRequestContext.getMergeRequest().getDescription().isEmpty()) {
			Markdown.render(
					ctx,
					EmojiParser.parseToUnicode(
							mergeRequestContext.getMergeRequest().getDescription().trim()),
					activityMergeRequestDetailBinding.mrDescription,
					mergeRequestContext.getProjects());
		} else {
			activityMergeRequestDetailBinding.mrDescription.setVisibility(View.GONE);
		}

		activityMergeRequestDetailBinding.username.setText(
				mergeRequestContext.getMergeRequest().getAuthor().getName());
		activityMergeRequestDetailBinding.createdTime.setText(modifiedTime);
		activityMergeRequestDetailBinding.mrThumbsUpCount.setText(
				String.valueOf(mergeRequestContext.getMergeRequest().getUpvotes()));
		activityMergeRequestDetailBinding.mrThumbsDownCount.setText(
				String.valueOf(mergeRequestContext.getMergeRequest().getDownvotes()));

		activityMergeRequestDetailBinding.userAvatar.setOnClickListener(
				profile -> {
					Intent intent = new Intent(ctx, ProfileActivity.class);
					intent.putExtra("source", "mr_detail");
					intent.putExtra(
							"userId", mergeRequestContext.getMergeRequest().getAuthor().getId());
					ctx.startActivity(intent);
				});

		if (!mergeRequestContext.getMergeRequest().getAssignees().isEmpty()) {

			infoCard = true;
			LinearLayout.LayoutParams paramsAssignees = new LinearLayout.LayoutParams(64, 64);
			paramsAssignees.setMargins(0, 0, 16, 0);

			activityMergeRequestDetailBinding.assigneesScrollView.setVisibility(View.VISIBLE);

			for (int i = 0; i < mergeRequestContext.getMergeRequest().getAssignees().size(); i++) {

				ImageView assigneesView = new ImageView(ctx);

				Glide.with(ctx)
						.load(
								mergeRequestContext
										.getMergeRequest()
										.getAssignees()
										.get(i)
										.getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.apply(RequestOptions.bitmapTransform(new RoundedCorners(20)))
						.into(assigneesView);

				activityMergeRequestDetailBinding.assigneesFrame.addView(assigneesView);
				assigneesView.setLayoutParams(paramsAssignees);

				int finalI = i;
				assigneesView.setOnClickListener(
						profile -> {
							Intent intent = new Intent(ctx, ProfileActivity.class);
							intent.putExtra("source", "mr_detail");
							intent.putExtra(
									"userId",
									mergeRequestContext
											.getMergeRequest()
											.getAssignees()
											.get(finalI)
											.getId());
							ctx.startActivity(intent);
						});
			}
		}

		if (!infoCard) {
			activityMergeRequestDetailBinding.mrInfoCard.setVisibility(View.GONE);
		}

		activityMergeRequestDetailBinding.mergeInfo.setText(
				getString(
						R.string.merge_info,
						mergeRequestContext.getMergeRequest().getAuthor().getName(),
						mergeRequestContext.getMergeRequest().getSourceBranch(),
						mergeRequestContext.getMergeRequest().getTargetBranch()));

		getMergeRequestNotesData();

		fetchApprovals();
	}

	private void showMergeRequestActionsBottomSheet() {

		sheetBinding =
				BottomSheetMergeRequestActionsBinding.inflate(
						LayoutInflater.from(this), null, false);
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		if (mergeRequestContext.getMergeRequest().isWorkInProgress()
				|| mergeRequestContext.getMergeRequest().isDraft()
				|| !mergeRequestContext.getMergeRequest().isBlockingDiscussionsResolved()
				|| mergeRequestContext.getMergeRequest().isHasConflicts()) {
			sheetBinding.mergeItemCard.setVisibility(View.GONE);
		}
		if (mergeRequestContext.getMergeRequest().getState().equalsIgnoreCase("closed")) {
			sheetBinding.closeItemCard.setVisibility(View.GONE);
		}

		sheetBinding.commitsItem.setOnClickListener(
				v -> {
					ProjectsContext project =
							new ProjectsContext(
									mergeRequestContext.getProjects().getProject(), ctx);
					Intent intent = project.getIntent(ctx, CommitsActivity.class);
					intent.putExtra("source", "mr");
					intent.putExtra(
							"mergeRequestIid", mergeRequestContext.getMergeRequest().getIid());
					intent.putExtra("projectId", projectId);
					ctx.startActivity(intent);
					bottomSheetDialog.dismiss();
				});

		sheetBinding.mergeItem.setOnClickListener(
				v -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx);

					View customDialogView =
							LayoutInflater.from(this)
									.inflate(
											R.layout.custom_merge_mr_dialog,
											findViewById(android.R.id.content),
											false);

					if (mergeRequestContext
							.getMergeRequest()
							.getMergeStatus()
							.equalsIgnoreCase("can_be_merged")) {
						materialAlertDialogBuilder
								.setView(customDialogView)
								.setTitle(R.string.merge)
								.setMessage(R.string.merge_fail_text)
								.setPositiveButton(
										R.string.merge,
										(dialog, whichButton) -> {
											CheckBox removeSourceBranchCheckbox =
													customDialogView.findViewById(
															R.id.remove_source_branch);
											boolean removeSourceBranch =
													removeSourceBranchCheckbox.isChecked();
											CheckBox squashCheckbox =
													customDialogView.findViewById(
															R.id.squash_commits);
											boolean squash = squashCheckbox.isChecked();

											mergeMergeRequest(removeSourceBranch, squash);
										})
								.setNeutralButton(R.string.cancel, null)
								.show();
					} else {
						materialAlertDialogBuilder
								.setTitle(R.string.merge)
								.setMessage(R.string.mr_cannot_be_merged)
								.setNeutralButton(R.string.cancel, null)
								.show();
					}
					bottomSheetDialog.dismiss();
				});

		sheetBinding.closeItem.setOnClickListener(
				v -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx);

					materialAlertDialogBuilder
							.setTitle(R.string.close)
							.setMessage(R.string.close_mr)
							.setPositiveButton(
									R.string.close, (dialog, whichButton) -> closeMergeRequest())
							.setNeutralButton(R.string.cancel, null)
							.show();
					bottomSheetDialog.dismiss();
				});

		sheetBinding.urlCopyItem.setOnClickListener(
				v -> {
					Utils.copyToClipboard(
							ctx,
							MergeRequestDetailActivity.this,
							mergeRequestContext.getMergeRequest().getWebUrl(),
							getString(R.string.copy_url_message));
					bottomSheetDialog.dismiss();
				});

		sheetBinding.openInBrowserItem.setOnClickListener(
				v -> {
					Utils.openUrlInBrowser(
							this,
							MergeRequestDetailActivity.this,
							mergeRequestContext.getMergeRequest().getWebUrl());
					bottomSheetDialog.dismiss();
				});

		bottomSheetDialog.show();
	}

	@Override
	public void updateDataListener(String str) {

		if (str.equalsIgnoreCase("created")) {
			Snackbar.info(
					MergeRequestDetailActivity.this,
					activityMergeRequestDetailBinding.bottomAppBar,
					getString(R.string.comment_posted));
		}

		issueNotesAdapter.clearAdapter();
		page = 1;
		getMergeRequestNotesData();
	}

	private void initComment() {

		Bundle bsBundle = new Bundle();
		bsBundle.putString("source", "mr_comment");
		bsBundle.putInt("projectId", mergeRequestContext.getProjects().getProjectId());
		bsBundle.putInt("mergeRequestIid", mergeRequestContext.getMergeRequestIndex());
		CommentOnIssueBottomSheet bottomSheet = new CommentOnIssueBottomSheet();
		bottomSheet.setArguments(bsBundle);
		bottomSheet.show(getSupportFragmentManager(), "commentOnMergeRequestBottomSheet");
	}

	@Override
	public void onButtonClicked(String text) {}

	private void closeMergeRequest() {

		CrudeMergeRequest crudeMergeRequest = new CrudeMergeRequest();
		crudeMergeRequest.setStateEvent("close");

		Call<MergeRequests> call =
				RetrofitClient.getApiInterface(ctx)
						.updateMergeRequest(projectId, mergeRequestIndex, crudeMergeRequest);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<MergeRequests> call,
							@NonNull Response<MergeRequests> response) {

						if (response.code() == 200) {

							sheetBinding.closeItemCard.setVisibility(View.GONE);
							MergeRequestsActivity.updateMergeRequestList = true;
							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									ctx.getString(R.string.mr_closed));
						} else if (response.code() == 401) {

							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<MergeRequests> call, @NonNull Throwable t) {

						Snackbar.info(
								MergeRequestDetailActivity.this,
								activityMergeRequestDetailBinding.bottomAppBar,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void mergeMergeRequest(boolean removeSourceBranch, boolean squash) {

		CrudeMergeRequest crudeMergeRequest = new CrudeMergeRequest();
		crudeMergeRequest.setShouldRemoveSourceBranch(removeSourceBranch);
		crudeMergeRequest.setSquash(squash);

		Call<MergeRequests> call =
				RetrofitClient.getApiInterface(ctx)
						.mergeMergeRequest(projectId, mergeRequestIndex, crudeMergeRequest);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<MergeRequests> call,
							@NonNull Response<MergeRequests> response) {

						if (response.code() == 200) {

							MergeRequestsActivity.updateMergeRequestList = true;
							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									ctx.getString(R.string.merge_request_merged_text));
						} else if (response.code() == 401) {

							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									ctx.getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									ctx.getString(R.string.access_forbidden_403));
						} else if (response.code() == 405) {

							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									ctx.getString(R.string.merge_error_405));
						} else if (response.code() == 409) {

							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									ctx.getString(R.string.merge_error_409));
						} else if (response.code() == 422) {

							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									ctx.getString(R.string.merge_error_422));
						} else {

							Snackbar.info(
									MergeRequestDetailActivity.this,
									activityMergeRequestDetailBinding.bottomAppBar,
									ctx.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<MergeRequests> call, @NonNull Throwable t) {
						Snackbar.info(
								MergeRequestDetailActivity.this,
								activityMergeRequestDetailBinding.bottomAppBar,
								ctx.getString(R.string.generic_server_response_error));
					}
				});
	}

	private void getMergeRequestNotesData() {

		activityMergeRequestDetailBinding.progressBar.setVisibility(View.VISIBLE);

		issueMrNotesViewModel
				.getNotes(
						ctx,
						projectId,
						mergeRequestIndex,
						type,
						resultLimit,
						page,
						MergeRequestDetailActivity.this,
						activityMergeRequestDetailBinding.bottomAppBar)
				.observe(
						MergeRequestDetailActivity.this,
						mainList -> {
							issueNotesAdapter =
									new IssueNotesAdapter(
											MergeRequestDetailActivity.this,
											mainList,
											mergeRequestContext.getProjects());
							issueNotesAdapter.setLoadMoreListener(
									new IssueNotesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											issueMrNotesViewModel.loadMore(
													ctx,
													projectId,
													mergeRequestIndex,
													type,
													resultLimit,
													page,
													issueNotesAdapter,
													MergeRequestDetailActivity.this,
													activityMergeRequestDetailBinding.bottomAppBar);
											activityMergeRequestDetailBinding.progressBar
													.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											activityMergeRequestDetailBinding.progressBar
													.setVisibility(View.GONE);
										}
									});

							if (issueNotesAdapter.getItemCount() > 0) {
								activityMergeRequestDetailBinding.notesInfoCard.setVisibility(
										View.VISIBLE);
								activityMergeRequestDetailBinding.recyclerView.setAdapter(
										issueNotesAdapter);
							}

							activityMergeRequestDetailBinding.progressBar.setVisibility(View.GONE);
						});
	}

	private void getLabels() {

		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 20, 0);

		if (!mergeRequestContext.getMergeRequest().getLabels().isEmpty()) {

			infoCard = true;
			activityMergeRequestDetailBinding.labelsScrollView.setVisibility(View.VISIBLE);
			activityMergeRequestDetailBinding.labelsFrame.removeAllViews();

			for (int i = 0; i < mergeRequestContext.getMergeRequest().getLabels().size(); i++) {

				ImageView labelsView = new ImageView(ctx);
				activityMergeRequestDetailBinding.labelsFrame.setOrientation(
						LinearLayout.HORIZONTAL);
				activityMergeRequestDetailBinding.labelsFrame.setGravity(
						Gravity.START | Gravity.TOP);
				labelsView.setLayoutParams(params);

				int height = Utils.getPixelsFromDensity(ctx, 22);
				int textSize = Utils.getPixelsFromScaledDensity(ctx, 14);

				Call<Labels> call =
						RetrofitClient.getApiInterface(ctx)
								.getProjectLabel(
										projectId,
										mergeRequestContext.getMergeRequest().getLabels().get(i));

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
									activityMergeRequestDetailBinding.labelsFrame.addView(
											labelsView);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Labels> call, @NonNull Throwable t) {}
						});
			}
		}
	}

	private void fetchApprovals() {
		Call<Approvals> call =
				RetrofitClient.getApiInterface(ctx)
						.getApprovals(projectId, mergeRequestContext.getMergeRequest().getIid());

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Approvals> call, @NonNull Response<Approvals> response) {

						if (response.code() == 200) {
							Call<User> userCall =
									RetrofitClient.getApiInterface(ctx).getCurrentUser();

							userCall.enqueue(
									new Callback<>() {
										@Override
										public void onResponse(
												@NonNull Call<User> userCall,
												@NonNull Response<User> userResponse) {
											assert response.body() != null;
											assert userResponse.body() != null;

											List<ApprovedBy> approvedByList =
													response.body().getApprovedBy();

											for (ApprovedBy approvedBy : approvedByList) {
												if (approvedBy.getUser().getId()
														== userResponse.body().getId()) {
													activityMergeRequestDetailBinding.approveMr
															.setText(
																	getString(
																			R.string
																					.revoke_approval));
												}
											}

											approvals = approvedByList.size();
											requiredApprovals =
													response.body().getApprovalsRequired();

											activityMergeRequestDetailBinding.mrRequiredApprovals
													.setText(
															getString(
																	R.string.activity_mr_approvals,
																	approvals,
																	requiredApprovals));

											activityMergeRequestDetailBinding.approveMr
													.setOnClickListener(
															view -> {
																activityMergeRequestDetailBinding
																		.progressBar.setVisibility(
																		View.VISIBLE);

																if (activityMergeRequestDetailBinding
																		.approveMr
																		.getText()
																		.toString()
																		.equals(
																				getString(
																						R.string
																								.approve))) {
																	Call<Approvals> approveCall =
																			RetrofitClient
																					.getApiInterface(
																							ctx)
																					.approve(
																							projectId,
																							mergeRequestContext
																									.getMergeRequest()
																									.getIid());

																	approveCall.enqueue(
																			new Callback<>() {
																				@Override
																				public void
																						onResponse(
																								@NonNull Call<
																														Approvals>
																												approveCall,
																								@NonNull Response<
																														Approvals>
																												approveResponse) {
																					activityMergeRequestDetailBinding
																							.progressBar
																							.setVisibility(
																									View
																											.GONE);

																					if (approveResponse
																									.code()
																							== 201) {
																						activityMergeRequestDetailBinding
																								.approveMr
																								.setText(
																										getString(
																												R
																														.string
																														.revoke_approval));

																						assert approveResponse
																										.body()
																								!= null;
																						approvals =
																								approveResponse
																										.body()
																										.getApprovedBy()
																										.size();
																						requiredApprovals =
																								approveResponse
																										.body()
																										.getApprovalsRequired();

																						activityMergeRequestDetailBinding
																								.mrRequiredApprovals
																								.setText(
																										getString(
																												R
																														.string
																														.activity_mr_approvals,
																												approvals,
																												requiredApprovals));

																						refreshNotes();
																					} else {
																						Snackbar
																								.info(
																										ctx,
																										findViewById(
																												R
																														.id
																														.bottom_app_bar),
																										getString(
																												R
																														.string
																														.mr_approve_failed));
																					}
																				}

																				@Override
																				public void
																						onFailure(
																								@NonNull Call<
																														Approvals>
																												approveCall,
																								@NonNull Throwable
																												approveThrowable) {
																					activityMergeRequestDetailBinding
																							.progressBar
																							.setVisibility(
																									View
																											.GONE);
																				}
																			});
																} else if (activityMergeRequestDetailBinding
																		.approveMr
																		.getText()
																		.toString()
																		.equals(
																				getString(
																						R.string
																								.revoke_approval))) {
																	Call<Approvals> revokeCall =
																			RetrofitClient
																					.getApiInterface(
																							ctx)
																					.revokeApproval(
																							projectId,
																							mergeRequestContext
																									.getMergeRequest()
																									.getIid());

																	revokeCall.enqueue(
																			new Callback<>() {
																				@Override
																				public void
																						onResponse(
																								@NonNull Call<
																														Approvals>
																												revokeCall,
																								@NonNull Response<
																														Approvals>
																												revokeResponse) {
																					activityMergeRequestDetailBinding
																							.progressBar
																							.setVisibility(
																									View
																											.GONE);

																					if (revokeResponse
																									.code()
																							== 201) {
																						activityMergeRequestDetailBinding
																								.approveMr
																								.setText(
																										getText(
																												R
																														.string
																														.approve));

																						approvals--;

																						activityMergeRequestDetailBinding
																								.mrRequiredApprovals
																								.setText(
																										getString(
																												R
																														.string
																														.activity_mr_approvals,
																												approvals,
																												requiredApprovals));

																						refreshNotes();
																					}
																				}

																				@Override
																				public void
																						onFailure(
																								@NonNull Call<
																														Approvals>
																												revokeCall,
																								@NonNull Throwable
																												revokeThrowable) {
																					activityMergeRequestDetailBinding
																							.progressBar
																							.setVisibility(
																									View
																											.GONE);
																				}
																			});
																}
															});
										}

										@Override
										public void onFailure(
												@NonNull Call<User> userCall,
												@NonNull Throwable userThrowable) {}
									});
						}
					}

					@Override
					public void onFailure(@NonNull Call<Approvals> call, @NonNull Throwable t) {}
				});
	}

	private void refreshNotes() {
		page = 1;
		getMergeRequestNotesData();
	}
}
