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
import com.labnex.app.helpers.FileIcon;
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

			if (snippet.getFiles() != null && snippet.getFiles().size() == 1) {
				String singleFileName = snippet.getFiles().get(0).getPath();
				binding.fileNameIcon.setVisibility(View.VISIBLE);
				binding.fileName.setVisibility(View.VISIBLE);
				binding.fileNameIcon.setImageResource(
						FileIcon.getIconResource(singleFileName, "file"));
				binding.fileName.setText(singleFileName);
			} else if (snippet.getFiles() != null && snippet.getFiles().size() > 1) {
				binding.fileNameIcon.setVisibility(View.VISIBLE);
				binding.fileName.setVisibility(View.VISIBLE);
				binding.fileNameIcon.setImageResource(R.drawable.ic_file);
				binding.fileName.setText(String.valueOf(snippet.getFiles().size()));
			} else if (snippet.getFileName() != null && !snippet.getFileName().isEmpty()) {
				binding.fileNameIcon.setVisibility(View.VISIBLE);
				binding.fileName.setVisibility(View.VISIBLE);
				binding.fileNameIcon.setImageResource(
						FileIcon.getIconResource(snippet.getFileName(), "file"));
				binding.fileName.setText(snippet.getFileName());
			} else {
				binding.fileNameIcon.setVisibility(View.GONE);
				binding.fileName.setVisibility(View.GONE);
			}

			boolean isOwner =
					snippet.getAuthor() != null && snippet.getAuthor().getId() == currentUserId;
			binding.btnDelete.setVisibility(isOwner ? View.VISIBLE : View.GONE);
		}
	}
}
