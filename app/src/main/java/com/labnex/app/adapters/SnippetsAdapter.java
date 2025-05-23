package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.SnippetDetailActivity;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.AccountContext;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.models.snippets.SnippetsItem;
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
public class SnippetsAdapter extends RecyclerView.Adapter<SnippetsAdapter.SnippetsViewHolder> {

	private final Context context;
	private final Activity activity;
	private final List<SnippetsItem> snippetsList;
	private boolean moreDataAvailable = true;
	private final AccountContext accountContext;
	private final BottomAppBar bottomAppBar;

	public SnippetsAdapter(
			Context context,
			Activity activity,
			List<SnippetsItem> snippetsList,
			UserAccount userAccount,
			BottomAppBar bottomAppBar) {
		this.context = context;
		this.activity = activity;
		this.snippetsList = snippetsList;
		this.accountContext = new AccountContext(userAccount);
		this.bottomAppBar = bottomAppBar;
	}

	@NonNull @Override
	public SnippetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.list_snippets, parent, false);
		return new SnippetsViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull SnippetsViewHolder holder, int position) {
		SnippetsItem snippet = snippetsList.get(position);
		Locale locale = context.getResources().getConfiguration().getLocales().get(0);

		holder.itemView.setOnClickListener(
				v -> {
					Intent intent = new Intent(context, SnippetDetailActivity.class);
					intent.putExtra("MODE", "VIEW");
					intent.putExtra("SNIPPET_ID", snippet.getId());
					context.startActivity(intent);
				});

		if (snippet.getAuthor() != null && snippet.getAuthor().getAvatarUrl() != null) {
			Glide.with(context)
					.load(snippet.getAuthor().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.ic_spinner)
					.centerCrop()
					.into(holder.avatar);
		} else {
			holder.avatar.setImageResource(R.drawable.ic_spinner);
		}

		if ("private".equalsIgnoreCase(snippet.getVisibility())) {
			holder.lockIcon.setVisibility(View.VISIBLE);
		} else {
			holder.lockIcon.setVisibility(View.GONE);
		}

		holder.title.setText(snippet.getTitle());

		String authorName = snippet.getAuthor() != null ? snippet.getAuthor().getName() : "Unknown";
		String createdAt = snippet.getCreatedAt();
		String timeText =
				TimeUtils.formatTime(
						createdAt != null
								? Date.from(OffsetDateTime.parse(createdAt).toInstant())
								: new Date(),
						locale);
		holder.author.setText(context.getString(R.string.created_by, authorName, timeText));

		if (snippet.getDescription() != null && !snippet.getDescription().isEmpty()) {
			holder.description.setText(snippet.getDescription());
			holder.description.setVisibility(View.VISIBLE);
		} else {
			holder.description.setVisibility(View.GONE);
		}

		int fileCount = 0;
		if (snippet.getFiles() != null && !snippet.getFiles().isEmpty()) {
			fileCount = snippet.getFiles().size();
		} else if (snippet.getFileName() != null && !snippet.getFileName().isEmpty()) {
			fileCount = 1;
		}

		if (fileCount > 0) {
			holder.fileCountContainer.setVisibility(View.VISIBLE);
			holder.fileCount.setText(String.valueOf(fileCount));
		} else {
			holder.fileCountContainer.setVisibility(View.GONE);
		}

		if (snippet.getAuthor() != null
				&& snippet.getAuthor().getId() == accountContext.getAccount().getUserId()) {
			holder.deleteSnippet.setVisibility(View.VISIBLE);
			holder.deleteSnippet.setOnClickListener(
					v -> showDeleteConfirmationDialog(snippet, position, holder));
		} else {
			holder.deleteSnippet.setVisibility(View.GONE);
		}
	}

	private void showDeleteConfirmationDialog(
			SnippetsItem snippet, int position, SnippetsViewHolder holder) {
		new MaterialAlertDialogBuilder(context)
				.setTitle(R.string.delete_snippet)
				.setMessage(R.string.delete_snippet_confirmation)
				.setNeutralButton(R.string.cancel, null)
				.setPositiveButton(
						R.string.delete,
						(dialog, which) -> deleteSnippet(snippet, position, holder))
				.show();
	}

	private void deleteSnippet(SnippetsItem snippet, int position, SnippetsViewHolder holder) {
		RetrofitClient.getApiInterface(context)
				.deleteSnippet(snippet.getId())
				.enqueue(
						new Callback<Void>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.isSuccessful()) {
									snippetsList.remove(position);
									notifyItemRemoved(position);
									notifyItemRangeChanged(position, snippetsList.size());
									Snackbar.info(
											activity,
											bottomAppBar,
											context.getString(R.string.snippet_deleted));
								} else {
									Snackbar.info(
											activity,
											bottomAppBar,
											context.getString(R.string.delete_snippet_error));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								Snackbar.info(
										activity,
										bottomAppBar,
										context.getString(R.string.delete_snippet_error));
							}
						});
	}

	@Override
	public int getItemCount() {
		return snippetsList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<SnippetsItem> newSnippets) {
		snippetsList.clear();
		snippetsList.addAll(newSnippets);
		notifyDataSetChanged();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void clearAdapter() {
		snippetsList.clear();
		notifyDataSetChanged();
	}

	public void setMoreDataAvailable(boolean available) {
		this.moreDataAvailable = available;
	}

	public boolean isMoreDataAvailable() {
		return moreDataAvailable;
	}

	public static class SnippetsViewHolder extends RecyclerView.ViewHolder {
		ImageView avatar;
		ImageView lockIcon;
		TextView title;
		TextView author;
		TextView description;
		LinearLayout fileCountContainer;
		TextView fileCount;
		ImageView deleteSnippet;

		SnippetsViewHolder(View itemView) {
			super(itemView);
			avatar = itemView.findViewById(R.id.snippet_avatar);
			lockIcon = itemView.findViewById(R.id.snippet_lock_icon);
			title = itemView.findViewById(R.id.snippet_title);
			author = itemView.findViewById(R.id.snippet_author);
			description = itemView.findViewById(R.id.snippet_description);
			fileCountContainer = itemView.findViewById(R.id.file_count_container);
			fileCount = itemView.findViewById(R.id.file_count);
			deleteSnippet = itemView.findViewById(R.id.delete_snippet);
		}
	}
}
