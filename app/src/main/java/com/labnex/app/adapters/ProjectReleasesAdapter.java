package com.labnex.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetProjectReleasesBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.TextDrawable.ColorGenerator;
import com.labnex.app.helpers.TextDrawable.TextDrawable;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.models.release.Releases;
import com.vdurmont.emoji.EmojiParser;
import java.time.Instant;
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
public class ProjectReleasesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Releases> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private int projectId;
	private BottomSheetProjectReleasesBinding binding;
	Bundle bundle = new Bundle();

	public ProjectReleasesAdapter(
			Context ctx,
			List<Releases> list,
			int projectId,
			BottomSheetProjectReleasesBinding binding) {
		this.context = ctx;
		this.list = list;
		this.projectId = projectId;
		this.binding = binding;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new ReleasesHolder(inflater.inflate(R.layout.list_releases, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		((ReleasesHolder) holder).bindData(list.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Releases> list_) {
		list = list_;
	}

	private void updateAdapter(int position) {
		list.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, list.size());
	}

	public void clearAdapter() {
		list.clear();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class ReleasesHolder extends RecyclerView.ViewHolder {

		private final TextView title;
		private final TextView description;
		private final TextView tag;
		private final ImageView avatar;
		private final TextView published;
		private Releases releases;

		ReleasesHolder(View itemView) {

			super(itemView);
			title = itemView.findViewById(R.id.title);
			description = itemView.findViewById(R.id.description);
			tag = itemView.findViewById(R.id.tag);
			avatar = itemView.findViewById(R.id.user_avatar);
			published = itemView.findViewById(R.id.published_info);
			ImageView delete = itemView.findViewById(R.id.delete);

			avatar.setOnClickListener(
					profile -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("source", "releases");
						intent.putExtra("userId", releases.getAuthor().getId());
						context.startActivity(intent);
					});

			delete.setOnClickListener(
					deleteRelease -> {
						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(
										context,
										com.google.android.material.R.style
												.ThemeOverlay_Material3_Dialog_Alert);

						materialAlertDialogBuilder
								.setTitle(
										context.getString(
												R.string.delete_dialog_title,
												releases.getTagName()))
								.setMessage(R.string.delete_release_message)
								.setPositiveButton(
										R.string.delete,
										(dialog, whichButton) -> {
											deleteRelease(
													releases.getTagName(),
													getBindingAdapterPosition());
										})
								.setNeutralButton(R.string.cancel, null)
								.show();
					});
		}

		void bindData(Releases releases) {

			this.releases = releases;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			ColorGenerator generator = ColorGenerator.MATERIAL;
			int color = generator.getColor(releases.getAuthor().getName());
			String firstCharacter = String.valueOf(releases.getAuthor().getName().charAt(0));

			tag.setText(releases.getTagName());
			title.setText(releases.getName());

			Markdown.render(
					context, EmojiParser.parseToUnicode(releases.getDescription()), description);

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

			if (releases.getAuthor().getAvatarUrl() != null) {

				Glide.with(context)
						.load(releases.getAuthor().getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(avatar);
			} else {
				avatar.setImageDrawable(drawable);
			}

			String modifiedTime =
					TimeUtils.formatTime(Date.from(OffsetDateTime.parse(releases.getCreatedAt()).toInstant()), locale);
			published.setText(
					context.getResources()
							.getString(
									R.string.published_by,
									releases.getAuthor().getUsername(),
									modifiedTime));
		}
	}

	private void deleteRelease(String tagName, int position) {

		RetrofitClient.getApiInterface(context)
				.deleteRelease(projectId, tagName)
				.enqueue(
						new Callback<>() {

							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {

								if (response.code() == 200) {

									updateAdapter(position);
									Snackbar.info(
											context,
											binding.getRoot(),
											context.getResources()
													.getString(R.string.release_deleted));

								} else if (response.code() == 401) {

									Snackbar.info(
											context,
											binding.getRoot(),
											context.getResources()
													.getString(R.string.not_authorized));
								} else if (response.code() == 403) {

									Snackbar.info(
											context,
											binding.getRoot(),
											context.getResources()
													.getString(R.string.access_forbidden_403));
								} else {

									Snackbar.info(
											context,
											binding.getRoot(),
											context.getResources()
													.getString(R.string.generic_error));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

								Snackbar.info(
										context,
										binding.getRoot(),
										context.getResources()
												.getString(R.string.generic_server_response_error));
							}
						});
	}
}
