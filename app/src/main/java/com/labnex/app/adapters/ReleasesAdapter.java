package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.databinding.ListReleasesBinding;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.models.release.Releases;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class ReleasesAdapter extends RecyclerView.Adapter<ReleasesAdapter.ReleasesHolder> {

	private final Context context;
	private final List<Releases> list;
	private final OnReleaseClickListener listener;
	private boolean canModify = true;

	public void setCanModify(boolean canModify) {
		this.canModify = canModify;
	}

	public interface OnReleaseClickListener {
		void onMenuClick(Releases release);
	}

	public ReleasesAdapter(Context ctx, List<Releases> listMain, OnReleaseClickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (listMain != null) this.list.addAll(listMain);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Releases> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public ReleasesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListReleasesBinding binding =
				ListReleasesBinding.inflate(LayoutInflater.from(context), parent, false);
		return new ReleasesHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ReleasesHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class ReleasesHolder extends RecyclerView.ViewHolder {

		final ListReleasesBinding binding;

		ReleasesHolder(ListReleasesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			if (!canModify) {
				binding.btnMenu.setVisibility(View.GONE);
			}

			binding.btnMenu.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onMenuClick(list.get(pos));
						}
					});

			binding.userAvatar.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && list.get(pos).getAuthor() != null) {
							context.startActivity(
									new Intent(context, ProfileActivity.class)
											.putExtra("source", "releases")
											.putExtra(
													"userId",
													String.valueOf(
															list.get(pos).getAuthor().getId())));
						}
					});
		}

		void bind(Releases release) {
			binding.title.setText(release.getName());

			binding.tagBadge.setImageDrawable(
					AvatarGenerator.getLabelDrawable(
							context,
							release.getTagName(),
							context.getResources()
									.getColor(R.color.alert_important_border, context.getTheme()),
							20));

			if (release.getAuthor() != null) {
				if (release.getAuthor().getAvatarUrl() != null) {
					Glide.with(context)
							.load(release.getAuthor().getAvatarUrl())
							.diskCacheStrategy(DiskCacheStrategy.ALL)
							.placeholder(R.drawable.ic_spinner)
							.centerCrop()
							.into(binding.userAvatar);
				} else {
					binding.userAvatar.setImageDrawable(
							AvatarGenerator.getLetterAvatar(
									context, release.getAuthor().getName(), 24));
				}

				String timeStr = "";
				if (release.getCreatedAt() != null) {
					Date date = TimeHelper.parseIso8601(release.getCreatedAt());
					timeStr = TimeHelper.formatTime(date);
					binding.publishedInfo.setOnClickListener(
							v ->
									Toasty.show(
											context,
											TimeHelper.getFullDateTime(date, Locale.getDefault())));
				}
				binding.publishedInfo.setText(
						context.getString(
								R.string.published_by, release.getAuthor().getUsername(), timeStr));
			}

			if (release.getDescription() != null && !release.getDescription().isEmpty()) {
				Markdown.render(
						context,
						EmojiParser.parseToUnicode(release.getDescription()),
						binding.description);
			}
		}
	}
}
