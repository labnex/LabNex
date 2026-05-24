package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.databinding.ListMergeRequestsBinding;
import com.labnex.app.helpers.AppSettingsInit;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.ColorInverter;
import com.labnex.app.helpers.LabelStylingHelper;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class MergeRequestsAdapter
		extends RecyclerView.Adapter<MergeRequestsAdapter.MergeRequestsHolder> {

	private final Context context;
	private final List<MergeRequests> list;
	private final OnMrClickListener listener;

	public interface OnMrClickListener {
		void onMrClick(MergeRequests mr);

		void onAuthorClick(MergeRequests mr);
	}

	public MergeRequestsAdapter(
			Context ctx, List<MergeRequests> mainList, OnMrClickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (mainList != null) this.list.addAll(mainList);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<MergeRequests> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void clearAdapter() {
		list.clear();
		notifyDataSetChanged();
	}

	@NonNull @Override
	public MergeRequestsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListMergeRequestsBinding binding =
				ListMergeRequestsBinding.inflate(LayoutInflater.from(context), parent, false);
		return new MergeRequestsHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull MergeRequestsHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class MergeRequestsHolder extends RecyclerView.ViewHolder {

		final ListMergeRequestsBinding binding;

		MergeRequestsHolder(ListMergeRequestsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onMrClick(list.get(pos));
						}
					});

			binding.avatar.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onAuthorClick(list.get(pos));
						}
					});
		}

		void bind(MergeRequests mr) {
			String state = mr.getState();
			String stateLabel =
					state != null ? state.substring(0, 1).toUpperCase() + state.substring(1) : "";
			int stateColor;
			if ("merged".equalsIgnoreCase(state)) {
				stateColor =
						context.getResources()
								.getColor(R.color.alert_important_border, context.getTheme());
			} else if ("closed".equalsIgnoreCase(state)) {
				stateColor =
						context.getResources()
								.getColor(R.color.label_default_color, context.getTheme());
			} else {
				stateColor = context.getResources().getColor(R.color.green, context.getTheme());
				stateLabel = context.getString(R.string.open);
			}
			binding.stateCard.setBackground(
					AvatarGenerator.getLabelDrawable(context, stateLabel, stateColor, 20));

			if (mr.getCreatedAt() != null) {
				Date date = TimeHelper.parseIso8601(mr.getCreatedAt());
				binding.mrCreatedAt.setText(TimeHelper.formatTime(date));
				binding.mrCreatedAt.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(date, Locale.getDefault())));
			}

			String authorName = "";
			if (mr.getAuthor() != null) {
				authorName =
						mr.getAuthor().getName() != null && !mr.getAuthor().getName().isEmpty()
								? mr.getAuthor().getName()
								: "@" + mr.getAuthor().getUsername();
				if (mr.getAuthor().getAvatarUrl() != null) {
					Glide.with(itemView.getContext())
							.load(mr.getAuthor().getAvatarUrl())
							.diskCacheStrategy(DiskCacheStrategy.ALL)
							.placeholder(R.drawable.ic_spinner)
							.centerCrop()
							.into(binding.avatar);
				} else {
					binding.avatar.setImageDrawable(
							AvatarGenerator.getLetterAvatar(context, authorName, 40));
				}
			}
			binding.authorName.setText(authorName);

			binding.project.setText(mr.getReferences() != null ? mr.getReferences().getFull() : "");

			String titleText = "!" + mr.getIid() + " " + mr.getTitle();
			Markdown.render(context, EmojiParser.parseToUnicode(titleText.trim()), binding.title);

			binding.labelsContainer.removeAllViews();
			boolean showLabels =
					Boolean.parseBoolean(
							AppSettingsInit.getSettingsValue(
									context, AppSettingsInit.APP_SHOW_LABELS_IN_LISTS_KEY));
			boolean showColors =
					Boolean.parseBoolean(
							AppSettingsInit.getSettingsValue(
									context, AppSettingsInit.APP_SHOW_LABELS_COLORS_KEY));

			if (showLabels && mr.getLabels() != null && !mr.getLabels().isEmpty()) {
				binding.labelsScroll.setVisibility(View.VISIBLE);
				LabelStylingHelper styler = LabelStylingHelper.getInstance(context);

				for (String labelName : mr.getLabels()) {
					int marginEnd = (int) (6 * context.getResources().getDisplayMetrics().density);

					if (showColors) {
						int color = getLabelColor(labelName);
						int textColor = ColorInverter.getContrastColor(color);
						String colorHex = String.format("#%06X", 0xFFFFFF & color);
						String textColorHex = String.format("#%06X", 0xFFFFFF & textColor);

						if (LabelStylingHelper.isScopedLabel(labelName)) {
							LinearLayout container = new LinearLayout(context);
							container.setOrientation(LinearLayout.HORIZONTAL);
							container.setGravity(Gravity.CENTER_VERTICAL);

							TextView labelNameView = new TextView(context);
							TextView labelValueView = new TextView(context);
							styler.styleScopedLabel(
									labelName,
									colorHex,
									textColorHex,
									labelNameView,
									labelValueView,
									11,
									2,
									8);

							container.addView(labelNameView);
							container.addView(labelValueView);

							LinearLayout.LayoutParams params =
									new LinearLayout.LayoutParams(
											ViewGroup.LayoutParams.WRAP_CONTENT,
											ViewGroup.LayoutParams.WRAP_CONTENT);
							params.setMarginEnd(marginEnd);
							container.setLayoutParams(params);
							binding.labelsContainer.addView(container);
						} else {
							TextView labelView = new TextView(context);
							styler.styleRegularLabel(
									labelName, colorHex, textColorHex, labelView, 11, 2, 8);
							LinearLayout.LayoutParams params =
									new LinearLayout.LayoutParams(
											ViewGroup.LayoutParams.WRAP_CONTENT,
											ViewGroup.LayoutParams.WRAP_CONTENT);
							params.setMarginEnd(marginEnd);
							labelView.setLayoutParams(params);
							binding.labelsContainer.addView(labelView);
						}
					} else {
						TextView labelView = getTextView(labelName, marginEnd);
						binding.labelsContainer.addView(labelView);
					}
				}
			} else {
				binding.labelsScroll.setVisibility(View.GONE);
			}

			binding.issueNotesCount.setText(String.valueOf(mr.getUserNotesCount()));

			if (mr.getMilestone() != null && mr.getMilestone().getTitle() != null) {
				binding.milestoneIcon.setVisibility(View.VISIBLE);
				binding.milestoneText.setVisibility(View.VISIBLE);
				binding.milestoneText.setText(mr.getMilestone().getTitle());
			} else {
				binding.milestoneIcon.setVisibility(View.GONE);
				binding.milestoneText.setVisibility(View.GONE);
			}
		}

		@NonNull private TextView getTextView(String labelName, int marginEnd) {
			TextView labelView = new TextView(context);
			labelView.setText(labelName);
			labelView.setTextAppearance(
					com.google.android.material.R.style.TextAppearance_Material3_LabelSmall);
			int paddingH = (int) (12 * context.getResources().getDisplayMetrics().density);
			int paddingV = (int) (4 * context.getResources().getDisplayMetrics().density);
			labelView.setPadding(paddingH, paddingV, paddingH, paddingV);
			labelView.setBackgroundResource(R.drawable.bg_label_outline);
			TypedValue typedValue = new TypedValue();
			context.getTheme()
					.resolveAttribute(
							com.google.android.material.R.attr.colorOnSurfaceVariant,
							typedValue,
							true);
			labelView.setTextColor(typedValue.data);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMarginEnd(marginEnd);
			labelView.setLayoutParams(params);
			return labelView;
		}

		private static final int[] LABEL_COLORS = {
			0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
			0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
			0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
			0xFFFFC107, 0xFFFF9800, 0xFFFF5722
		};

		private int getLabelColor(String label) {
			int hash = Math.abs(label.hashCode());
			return LABEL_COLORS[hash % LABEL_COLORS.length];
		}
	}
}
