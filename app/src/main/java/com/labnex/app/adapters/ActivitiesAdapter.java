package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.databinding.ListActivitiesBinding;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.models.events.Events;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.ActivitiesHolder> {

	private final Context context;
	private List<Events> list;
	private final OnActivityClickListener listener;

	public interface OnActivityClickListener {
		void onActivityClick(Events event);
	}

	public ActivitiesAdapter(Context ctx, List<Events> mainList, OnActivityClickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>(mainList);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Events> newList) {
		this.list = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public ActivitiesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListActivitiesBinding binding =
				ListActivitiesBinding.inflate(LayoutInflater.from(context), parent, false);
		return new ActivitiesHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ActivitiesHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class ActivitiesHolder extends RecyclerView.ViewHolder {

		final ListActivitiesBinding binding;

		ActivitiesHolder(ListActivitiesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onActivityClick(list.get(pos));
						}
					});
		}

		void bind(Events events) {
			if (events.getAuthor() != null && events.getAuthor().getAvatarUrl() != null) {
				com.bumptech.glide.Glide.with(itemView.getContext())
						.load(events.getAuthor().getAvatarUrl())
						.diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
						.placeholder(com.labnex.app.R.drawable.ic_spinner)
						.centerCrop()
						.into(binding.avatar);
			}

			binding.authorName.setText(
					events.getAuthor() != null ? events.getAuthor().getName() : "");

			if (events.getCreatedAt() != null) {
				Date date = TimeHelper.parseIso8601(events.getCreatedAt());
				binding.time.setText(TimeHelper.formatTime(date));
				binding.time.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(date, Locale.getDefault())));
			}

			bindActionText(events);
		}

		private void bindActionText(Events events) {
			String action = events.getActionName();
			String author = events.getAuthor() != null ? events.getAuthor().getUsername() : "";

			binding.authorUsername.setText(author);
			binding.content.setText("");
			binding.body.setVisibility(View.GONE);
			binding.body.setText("");

			if (action == null) return;

			switch (action) {
				case "commented on":
					if (events.getNote() != null) {
						String type = events.getNote().getNoteableType();
						if ("Issue".equalsIgnoreCase(type)) {
							binding.authorUsername.setText(
									context.getString(
											com.labnex.app.R.string.activity_commented_on_issue,
											author));
						} else if ("MergeRequest".equalsIgnoreCase(type)) {
							binding.authorUsername.setText(
									context.getString(
											com.labnex.app.R.string.activity_commented_on_mr,
											author));
						}
						binding.content.setText(
								context.getString(
										com.labnex.app.R.string.data_concatenate,
										events.getTargetTitle(),
										String.valueOf(events.getNote().getNoteableIid())));
						binding.body.setVisibility(View.VISIBLE);
						binding.body.setText(events.getNote().getBody());
					}
					break;

				case "approved":
					if ("MergeRequest".equalsIgnoreCase(events.getTargetType())) {
						binding.authorUsername.setText(
								context.getString(
										com.labnex.app.R.string.activity_approved_mr, author));
						binding.content.setText(
								context.getString(
										com.labnex.app.R.string.data_concatenate,
										events.getTargetTitle(),
										events.getTargetIid()));
					}
					break;

				case "opened":
					binding.authorUsername.setText(getOpenedString(author, events.getTargetType()));
					binding.content.setText(getTargetContent(events));
					break;

				case "closed":
					binding.authorUsername.setText(getClosedString(author, events.getTargetType()));
					binding.content.setText(getTargetContent(events));
					break;

				case "accepted":
					if ("MergeRequest".equalsIgnoreCase(events.getTargetType())) {
						binding.authorUsername.setText(
								context.getString(
										com.labnex.app.R.string.activity_accepted_mr, author));
						binding.content.setText(
								context.getString(
										com.labnex.app.R.string.data_concatenate,
										events.getTargetTitle(),
										events.getTargetIid()));
					}
					break;

				case "pushed to":
					if (events.getPushData() != null
							&& "pushed".equalsIgnoreCase(events.getPushData().getAction())) {
						binding.authorUsername.setText(
								context.getString(
										com.labnex.app.R.string.activity_pushed_to, author));
						String commitTo = events.getPushData().getCommitTo();
						binding.content.setText(
								context.getString(
										com.labnex.app.R.string.data_concatenate,
										events.getPushData().getCommitTitle(),
										commitTo != null
												? commitTo.substring(
														0, Math.min(8, commitTo.length()))
												: ""));
					}
					break;

				case "pushed new":
					if (events.getPushData() != null
							&& "created".equalsIgnoreCase(events.getPushData().getAction())) {
						binding.authorUsername.setText(
								context.getString(
										com.labnex.app.R.string.activity_pushed_new_branch,
										author));
						binding.content.setText(
								context.getString(
										com.labnex.app.R.string.single_string_conversion,
										events.getPushData().getRef()));
					}
					break;

				case "deleted":
					if (events.getPushData() != null
							&& "removed".equalsIgnoreCase(events.getPushData().getAction())) {
						binding.authorUsername.setText(
								context.getString(
										com.labnex.app.R.string.activity_deleted_branch, author));
						binding.content.setText(
								context.getString(
										com.labnex.app.R.string.single_string_conversion,
										events.getPushData().getRef()));
					}
					break;

				case "created":
					if ("WikiPage::Meta".equalsIgnoreCase(events.getTargetType())) {
						binding.authorUsername.setText(
								context.getString(
										com.labnex.app.R.string.activity_created_wiki_page,
										author));
						binding.content.setText(
								context.getString(
										com.labnex.app.R.string.single_string_conversion,
										events.getTargetTitle()));
					} else {
						binding.authorUsername.setText(
								context.getString(
										com.labnex.app.R.string.activity_created_project, author));
						binding.content.setText(String.valueOf(events.getProjectId()));
					}
					break;

				case "updated":
					if ("WikiPage::Meta".equalsIgnoreCase(events.getTargetType())) {
						binding.authorUsername.setText(
								context.getString(
										com.labnex.app.R.string.activity_updated_wiki_page,
										author));
						binding.content.setText(
								context.getString(
										com.labnex.app.R.string.single_string_conversion,
										events.getTargetTitle()));
					}
					break;
			}
		}

		private String getOpenedString(String author, String targetType) {
			if ("MergeRequest".equalsIgnoreCase(targetType))
				return context.getString(com.labnex.app.R.string.activity_opened_mr, author);
			if ("Issue".equalsIgnoreCase(targetType))
				return context.getString(com.labnex.app.R.string.activity_opened_issue, author);
			if ("Milestone".equalsIgnoreCase(targetType))
				return context.getString(com.labnex.app.R.string.activity_opened_milestone, author);
			return author;
		}

		private String getClosedString(String author, String targetType) {
			if ("MergeRequest".equalsIgnoreCase(targetType))
				return context.getString(com.labnex.app.R.string.activity_closed_mr, author);
			if ("Issue".equalsIgnoreCase(targetType))
				return context.getString(com.labnex.app.R.string.activity_closed_issue, author);
			if ("Milestone".equalsIgnoreCase(targetType))
				return context.getString(com.labnex.app.R.string.activity_closed_milestone, author);
			return author;
		}

		private String getTargetContent(Events events) {
			if ("Milestone".equalsIgnoreCase(events.getTargetType())) {
				return context.getString(
						com.labnex.app.R.string.single_string_conversion, events.getTargetTitle());
			}
			return context.getString(
					com.labnex.app.R.string.data_concatenate,
					events.getTargetTitle(),
					events.getTargetIid());
		}
	}
}
