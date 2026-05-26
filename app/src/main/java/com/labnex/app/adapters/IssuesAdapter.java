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
import com.labnex.app.databinding.ListIssuesBinding;
import com.labnex.app.helpers.AppSettingsInit;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.ColorInverter;
import com.labnex.app.helpers.LabelStylingHelper;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.models.issues.Issues;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.IssuesHolder> {

	private final Context context;
	private final List<Issues> list;
	private final OnIssueClickListener listener;

	public interface OnIssueClickListener {
		void onIssueClick(Issues issue);

		void onAuthorClick(Issues issue);
	}

	public IssuesAdapter(Context ctx, List<Issues> mainList, OnIssueClickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (mainList != null) this.list.addAll(mainList);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Issues> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public IssuesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListIssuesBinding binding =
				ListIssuesBinding.inflate(LayoutInflater.from(context), parent, false);
		return new IssuesHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull IssuesHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class IssuesHolder extends RecyclerView.ViewHolder {

		final ListIssuesBinding binding;

		IssuesHolder(ListIssuesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onIssueClick(list.get(pos));
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

		void bind(Issues issue) {
			String state = issue.getState();
			String stateLabel =
					state != null ? state.substring(0, 1).toUpperCase() + state.substring(1) : "";
			int stateColor;
			if ("closed".equalsIgnoreCase(state)) {
				stateColor =
						context.getResources()
								.getColor(R.color.label_default_color, context.getTheme());
			} else {
				stateColor = context.getResources().getColor(R.color.green, context.getTheme());
				stateLabel = context.getString(R.string.open);
			}
			binding.stateCard.setBackground(
					AvatarGenerator.getLabelDrawable(context, stateLabel, stateColor, 20));

			if (issue.isHasTasks() && issue.getTaskCompletionStatus() != null) {
				binding.tasksIcon.setVisibility(View.VISIBLE);
				binding.tasksCount.setVisibility(View.VISIBLE);
				binding.tasksCount.setText(
						issue.getTaskCompletionStatus().getCompletedCount()
								+ "/"
								+ issue.getTaskCompletionStatus().getCount());
			} else {
				binding.tasksIcon.setVisibility(View.GONE);
				binding.tasksCount.setVisibility(View.GONE);
			}

			if (issue.getCreatedAt() != null) {
				Date date = TimeHelper.parseIso8601(issue.getCreatedAt());
				binding.issueCreatedAt.setText(TimeHelper.formatTime(date));
				binding.issueCreatedAt.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(date, Locale.getDefault())));
			}

			String authorName = "";
			if (issue.getAuthor() != null) {
				authorName =
						issue.getAuthor().getName() != null
										&& !issue.getAuthor().getName().isEmpty()
								? issue.getAuthor().getName()
								: "@" + issue.getAuthor().getUsername();
				if (issue.getAuthor().getAvatarUrl() != null) {
					Glide.with(itemView.getContext())
							.load(issue.getAuthor().getAvatarUrl())
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

			binding.project.setText(
					issue.getReferences() != null ? issue.getReferences().getFull() : "");

			Markdown.render(
					context, EmojiParser.parseToUnicode(issue.getTitle().trim()), binding.title);

			binding.labelsContainer.removeAllViews();
			boolean showLabels =
					Boolean.parseBoolean(
							AppSettingsInit.getSettingsValue(
									context, AppSettingsInit.APP_SHOW_LABELS_IN_LISTS_KEY));
			boolean showColors =
					Boolean.parseBoolean(
							AppSettingsInit.getSettingsValue(
									context, AppSettingsInit.APP_SHOW_LABELS_COLORS_KEY));

			if (showLabels && issue.getLabels() != null && !issue.getLabels().isEmpty()) {
				binding.labelsScroll.setVisibility(View.VISIBLE);
				LabelStylingHelper styler = LabelStylingHelper.getInstance(context);

				for (String labelName : issue.getLabels()) {
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

			binding.issueNotesCount.setText(String.valueOf(issue.getUserNotesCount()));

			if (issue.getMilestone() != null && issue.getMilestone().getTitle() != null) {
				binding.milestoneIcon.setVisibility(View.VISIBLE);
				binding.milestoneText.setVisibility(View.VISIBLE);
				binding.milestoneText.setText(issue.getMilestone().getTitle());
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
