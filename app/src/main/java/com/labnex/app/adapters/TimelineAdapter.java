package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ListTimelineCommentBinding;
import com.labnex.app.databinding.ListTimelineEventBinding;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.models.notes.Notes;
import com.labnex.app.viewmodels.ReactionsViewModel;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int VIEW_TYPE_COMMENT = 0;
	private static final int VIEW_TYPE_EVENT = 1;

	private final Context ctx;
	private List<Notes> list;
	private final ProjectsContext projectsContext;
	private final FragmentManager fragmentManager;
	private final long currentUserId;

	public TimelineAdapter(
			Context ctx,
			List<Notes> list,
			ProjectsContext projectsContext,
			FragmentManager fragmentManager,
			long currentUserId) {
		this.ctx = ctx;
		this.list = list != null ? list : new ArrayList<>();
		this.projectsContext = projectsContext;
		this.fragmentManager = fragmentManager;
		this.currentUserId = currentUserId;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Notes> newList) {
		this.list = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		Notes note = list.get(position);
		return note.isSystem() ? VIEW_TYPE_EVENT : VIEW_TYPE_COMMENT;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(ctx);
		if (viewType == VIEW_TYPE_EVENT) {
			return new EventViewHolder(ListTimelineEventBinding.inflate(inflater, parent, false));
		} else {
			return new CommentViewHolder(
					ListTimelineCommentBinding.inflate(inflater, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		Notes note = list.get(position);
		if (holder instanceof EventViewHolder) {
			((EventViewHolder) holder).bind(note);
			((EventViewHolder) holder).b.getRoot().updateAppearance(position, getItemCount());
		} else if (holder instanceof CommentViewHolder) {
			((CommentViewHolder) holder).bind(note, position);
			((CommentViewHolder) holder).b.getRoot().updateAppearance(position, getItemCount());
		}
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class EventViewHolder extends RecyclerView.ViewHolder {
		final ListTimelineEventBinding b;

		EventViewHolder(ListTimelineEventBinding binding) {
			super(binding.getRoot());
			this.b = binding;
		}

		void bind(Notes note) {
			loadAvatar(b.authorAvatar, note);

			String displayName = "";
			if (note.getAuthor() != null) {
				String name = note.getAuthor().getName();
				String username = note.getAuthor().getUsername();
				displayName = (name != null) ? name : ((username != null) ? username : "");
			}

			if (note.getCreatedAt() != null) {
				Date date = TimeHelper.parseIso8601(note.getCreatedAt());
				b.createdTime.setText(TimeHelper.formatTime(date));
				b.createdTime.setOnClickListener(
						v ->
								Toasty.show(
										ctx,
										TimeHelper.getFullDateTime(date, Locale.getDefault())));
			}

			String body = note.getBody() != null ? displayName + " " + note.getBody() : "";
			Markdown.render(
					ctx, EmojiParser.parseToUnicode(body.trim()), b.eventBody, projectsContext);

			b.authorAvatar.setOnClickListener(v -> openProfile(note.getAuthor().getId()));
		}
	}

	public class CommentViewHolder extends RecyclerView.ViewHolder {
		ListTimelineCommentBinding b;

		CommentViewHolder(ListTimelineCommentBinding binding) {
			super(binding.getRoot());
			this.b = binding;
		}

		void bind(Notes note, int position) {
			loadAvatar(b.authorAvatar, note);

			b.authorName.setText(note.getAuthor().getName());

			if (note.getCreatedAt() != null) {
				Date date = TimeHelper.parseIso8601(note.getCreatedAt());
				b.createdTime.setText(TimeHelper.formatTime(date));
				b.createdTime.setOnClickListener(
						v ->
								Toasty.show(
										ctx,
										TimeHelper.getFullDateTime(date, Locale.getDefault())));
			}

			String body = note.getBody() != null ? note.getBody() : "";
			Markdown.render(
					ctx, EmojiParser.parseToUnicode(body.trim()), b.commentBody, projectsContext);

			ReactionsViewModel reactionsViewModel =
					new ViewModelProvider((androidx.fragment.app.FragmentActivity) ctx)
							.get(ReactionsViewModel.class);

			b.reactionsView.configure(
					projectsContext.getProjectId(),
					"issue",
					note.getNoteableIid(),
					note.getId(),
					fragmentManager,
					currentUserId);
			b.reactionsView.loadReactions(reactionsViewModel);

			b.authorAvatar.setOnClickListener(v -> openProfile(note.getAuthor().getId()));
		}
	}

	private void loadAvatar(
			com.google.android.material.imageview.ShapeableImageView avatar, Notes note) {
		if (note.getAuthor() != null && note.getAuthor().getAvatarUrl() != null) {
			Glide.with(ctx)
					.load(note.getAuthor().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.ic_spinner)
					.centerCrop()
					.into(avatar);
		} else {
			String name = note.getAuthor() != null ? note.getAuthor().getName() : "?";
			avatar.setImageDrawable(AvatarGenerator.getLetterAvatar(ctx, name, 28));
		}
	}

	private void openProfile(long userId) {
		Intent intent = new Intent(ctx, ProfileActivity.class);
		intent.putExtra("userId", String.valueOf(userId));
		ctx.startActivity(intent);
	}
}
