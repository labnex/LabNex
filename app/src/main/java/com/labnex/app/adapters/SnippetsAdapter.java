package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.databinding.ListSnippetsBinding;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.models.snippets.SnippetsItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class SnippetsAdapter extends RecyclerView.Adapter<SnippetsAdapter.SnippetsViewHolder> {

	private final Context context;
	private final List<SnippetsItem> snippetsList;
	private final OnSnippetClickListener listener;
	private final int currentUserId;

	public interface OnSnippetClickListener {
		void onSnippetClick(SnippetsItem snippet);

		void onSnippetDelete(SnippetsItem snippet, int position);
	}

	public SnippetsAdapter(
			Context ctx,
			List<SnippetsItem> mainList,
			OnSnippetClickListener listener,
			int currentUserId) {
		this.context = ctx;
		this.snippetsList = new ArrayList<>();
		if (mainList != null) this.snippetsList.addAll(mainList);
		this.listener = listener;
		this.currentUserId = currentUserId;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<SnippetsItem> newList) {
		snippetsList.clear();
		if (newList != null) snippetsList.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public SnippetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListSnippetsBinding binding =
				ListSnippetsBinding.inflate(LayoutInflater.from(context), parent, false);
		return new SnippetsViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull SnippetsViewHolder holder, int position) {
		holder.bind(snippetsList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return snippetsList.size();
	}

	public class SnippetsViewHolder extends RecyclerView.ViewHolder {

		final ListSnippetsBinding binding;

		SnippetsViewHolder(ListSnippetsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onSnippetClick(snippetsList.get(pos));
						}
					});

			binding.btnDelete.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onSnippetDelete(snippetsList.get(pos), pos);
						}
					});
		}

		void bind(SnippetsItem snippet) {
			if (snippet.getAuthor() != null && snippet.getAuthor().getAvatarUrl() != null) {
				Glide.with(itemView.getContext())
						.load(snippet.getAuthor().getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(binding.snippetAvatar);
			} else {
				String name = snippet.getAuthor() != null ? snippet.getAuthor().getName() : "?";
				binding.snippetAvatar.setImageDrawable(
						AvatarGenerator.getLetterAvatar(context, name, 40));
			}

			binding.snippetLockIcon.setVisibility(
					"private".equalsIgnoreCase(snippet.getVisibility()) ? View.VISIBLE : View.GONE);

			binding.snippetTitle.setText(snippet.getTitle());

			String authorName =
					snippet.getAuthor() != null
							? snippet.getAuthor().getName()
							: snippet.getAuthor().getUsername();
			String timeStr = "";
			if (snippet.getCreatedAt() != null) {
				Date date = TimeHelper.parseIso8601(snippet.getCreatedAt());
				timeStr = TimeHelper.formatTime(date);
				binding.snippetAuthor.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(date, Locale.getDefault())));
			}
			binding.snippetAuthor.setText(
					context.getString(R.string.created_by, authorName, timeStr));

			if (snippet.getDescription() != null && !snippet.getDescription().isEmpty()) {
				binding.snippetDescription.setVisibility(View.VISIBLE);
				binding.snippetDescription.setText(snippet.getDescription());
			} else {
				binding.snippetDescription.setVisibility(View.GONE);
			}

			int fileCount = 0;
			if (snippet.getFiles() != null && !snippet.getFiles().isEmpty()) {
				fileCount = snippet.getFiles().size();
			} else if (snippet.getFileName() != null && !snippet.getFileName().isEmpty()) {
				fileCount = 1;
			}
			if (fileCount > 0) {
				binding.fileCountIcon.setVisibility(View.VISIBLE);
				binding.fileCount.setVisibility(View.VISIBLE);
				binding.fileCount.setText(String.valueOf(fileCount));
			} else {
				binding.fileCountIcon.setVisibility(View.GONE);
				binding.fileCount.setVisibility(View.GONE);
			}

			boolean isOwner =
					snippet.getAuthor() != null && snippet.getAuthor().getId() == currentUserId;
			binding.btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);
		}
	}
}
