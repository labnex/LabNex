package com.labnex.app.views.reactions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.FragmentManager;
import com.labnex.app.R;
import com.labnex.app.models.app.Reactions;
import com.labnex.app.models.award_emoji.AwardEmoji;
import com.labnex.app.viewmodels.ReactionsViewModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mmarif
 */
public class ReactionsView extends LinearLayout {

	private Context ctx;
	private ReactionsViewModel viewModel;
	private long projectId;
	private String type;
	private long iid;
	private Long noteId;
	private FragmentManager fragmentManager;
	private long currentUserId;
	private LinearLayout chipsContainer;
	private Long boundNoteId;

	public ReactionsView(Context context) {
		super(context);
		init(context);
	}

	public ReactionsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ReactionsView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {
		this.ctx = context;
		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER_VERTICAL);

		HorizontalScrollView scrollView = new HorizontalScrollView(ctx);
		scrollView.setHorizontalScrollBarEnabled(false);

		LinearLayout.LayoutParams scrollParams =
				new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
		scrollView.setLayoutParams(scrollParams);

		chipsContainer = new LinearLayout(ctx);
		chipsContainer.setOrientation(HORIZONTAL);
		chipsContainer.setGravity(Gravity.CENTER_VERTICAL);
		scrollView.addView(chipsContainer);
		addView(scrollView);
	}

	public void configure(
			long projectId,
			String type,
			long iid,
			Long noteId,
			FragmentManager fm,
			long currentUserId) {
		this.projectId = projectId;
		this.type = type;
		this.iid = iid;
		this.noteId = noteId;
		this.fragmentManager = fm;
		this.currentUserId = currentUserId;
		this.boundNoteId = noteId;

		chipsContainer.removeAllViews();
		while (getChildCount() > 1) {
			removeViewAt(getChildCount() - 1);
		}
	}

	public void loadReactions(ReactionsViewModel sharedViewModel) {
		this.viewModel = sharedViewModel;
		viewModel.loadReactions(
				ctx,
				projectId,
				type,
				iid,
				noteId,
				currentUserId,
				(callbackNoteId, reactions, userReactions) -> {
					boolean sameNote =
							(boundNoteId == null && callbackNoteId == null)
									|| (boundNoteId != null && boundNoteId.equals(callbackNoteId));
					if (sameNote) {
						post(() -> renderReactions(reactions, userReactions));
					}
				});
	}

	private void renderReactions(List<AwardEmoji> reactions, List<AwardEmoji> userReactions) {
		chipsContainer.removeAllViews();

		while (getChildCount() > 1) {
			removeViewAt(getChildCount() - 1);
		}

		if (reactions != null && !reactions.isEmpty()) {
			Map<String, Long> counts = new LinkedHashMap<>();
			Map<String, AwardEmoji> userEntries = new LinkedHashMap<>();

			for (AwardEmoji r : reactions) {
				counts.merge(r.getName(), 1L, Long::sum);
			}
			if (userReactions != null) {
				for (AwardEmoji r : userReactions) {
					userEntries.put(r.getName(), r);
				}
			}

			for (Map.Entry<String, Long> entry : counts.entrySet()) {
				String name = entry.getKey();
				long count = entry.getValue();
				String emoji = Reactions.getEmoji(name);
				boolean isUserReaction = userEntries.containsKey(name);
				AwardEmoji userEntry = userEntries.get(name);

				addReactionChip(name, emoji, count, isUserReaction, userEntry);
			}
		}

		addPlusButton();
	}

	@SuppressLint("SetTextI18n")
	private void addReactionChip(
			String name, String emoji, long count, boolean isUserReaction, AwardEmoji userEntry) {
		TextView chip = new TextView(ctx);
		chip.setText(emoji + " " + count);
		chip.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 15);
		chip.setGravity(Gravity.CENTER);

		int hPadding = (int) (10 * ctx.getResources().getDisplayMetrics().density);
		int vPadding = (int) (6 * ctx.getResources().getDisplayMetrics().density);
		chip.setPadding(hPadding, vPadding, hPadding, vPadding);

		if (isUserReaction) {
			chip.setBackgroundResource(R.drawable.bg_reaction_active);
		} else {
			chip.setBackgroundResource(R.drawable.bg_reaction_inactive);
		}

		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int margin = (int) (6 * ctx.getResources().getDisplayMetrics().density);
		params.setMargins(0, 0, margin, 0);
		chip.setLayoutParams(params);

		chip.setOnClickListener(
				v -> {
					if (isUserReaction && userEntry != null) {
						viewModel.deleteReaction(
								ctx,
								projectId,
								type,
								iid,
								noteId,
								userEntry.getId(),
								currentUserId,
								(callbackNoteId, reactions2, userReactions2) -> {
									boolean same =
											(boundNoteId == null && callbackNoteId == null)
													|| (boundNoteId != null
															&& boundNoteId.equals(callbackNoteId));
									if (same) {
										post(() -> renderReactions(reactions2, userReactions2));
									}
								});
					} else {
						viewModel.addReaction(
								ctx,
								projectId,
								type,
								iid,
								noteId,
								name,
								currentUserId,
								(callbackNoteId, reactions2, userReactions2) -> {
									boolean same =
											(boundNoteId == null && callbackNoteId == null)
													|| (boundNoteId != null
															&& boundNoteId.equals(callbackNoteId));
									if (same) {
										post(() -> renderReactions(reactions2, userReactions2));
									}
								});
					}
				});

		chipsContainer.addView(chip);
	}

	private void addPlusButton() {
		AppCompatImageButton btn = new AppCompatImageButton(ctx);
		btn.setImageResource(R.drawable.ic_emoji);
		btn.setBackgroundColor(android.graphics.Color.TRANSPARENT);

		int size = (int) (30 * ctx.getResources().getDisplayMetrics().density);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
		int margin = (int) (8 * ctx.getResources().getDisplayMetrics().density);
		params.setMargins(margin, 0, 0, 0);
		btn.setLayoutParams(params);

		int padding = (int) (4 * ctx.getResources().getDisplayMetrics().density);
		btn.setPadding(padding, padding, padding, padding);
		btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

		btn.setOnClickListener(
				v -> {
					EmojiPickerSheet sheet = EmojiPickerSheet.newInstance();
					sheet.setOnEmojiPicked(
							name ->
									viewModel.addReaction(
											ctx,
											projectId,
											type,
											iid,
											noteId,
											name,
											currentUserId,
											(callbackNoteId, reactions, userReactions) -> {
												boolean same =
														(boundNoteId == null
																		&& callbackNoteId == null)
																|| (boundNoteId != null
																		&& boundNoteId.equals(
																				callbackNoteId));
												if (same) {
													post(
															() ->
																	renderReactions(
																			reactions,
																			userReactions));
												}
											}));
					sheet.show(fragmentManager, "emojiPicker");
				});

		addView(btn);
	}
}
