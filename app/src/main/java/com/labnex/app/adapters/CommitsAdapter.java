package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.CommitDiffsBottomSheet;
import com.labnex.app.databinding.ListCommitsBinding;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.commits.Commits;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class CommitsAdapter extends RecyclerView.Adapter<CommitsAdapter.CommitsViewHolder> {

	private final Context ctx;
	private List<Commits> list;
	private final long projectId;

	public CommitsAdapter(Context ctx, List<Commits> list, long projectId) {
		this.ctx = ctx;
		this.list = list != null ? list : new ArrayList<>();
		this.projectId = projectId;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Commits> newList) {
		this.list = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public CommitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListCommitsBinding binding =
				ListCommitsBinding.inflate(LayoutInflater.from(ctx), parent, false);
		return new CommitsViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull CommitsViewHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class CommitsViewHolder extends RecyclerView.ViewHolder {

		final ListCommitsBinding binding;

		CommitsViewHolder(ListCommitsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos == RecyclerView.NO_POSITION) return;

						Commits commit = list.get(pos);
						Bundle bundle = new Bundle();
						bundle.putString("source", "commits");
						bundle.putString("sha", commit.getId());
						bundle.putLong("projectId", projectId);

						CommitDiffsBottomSheet bottomSheet = new CommitDiffsBottomSheet();
						bottomSheet.setArguments(bundle);
						bottomSheet.show(
								((FragmentActivity) ctx).getSupportFragmentManager(),
								"CommitDiffsBottomSheet");
					});
		}

		void bind(Commits commit) {
			if (commit.getShortId() != null && !commit.getShortId().trim().isEmpty()) {
				binding.commitBadge.setVisibility(View.VISIBLE);

				int badgeColor = ContextCompat.getColor(ctx, R.color.alert_important_border);
				binding.commitBadge.setImageDrawable(
						AvatarGenerator.getLabelDrawable(ctx, commit.getShortId(), badgeColor, 22));

				binding.commitBadge.setOnClickListener(
						v -> {
							if (commit.getId() != null) {
								Utils.copyToClipboard(
										ctx,
										commit.getId(),
										ctx.getString(R.string.commit_sha_copied));
							}
						});
			} else {
				binding.commitBadge.setVisibility(View.GONE);
			}

			if (commit.getAuthorName() != null && !commit.getAuthorName().trim().isEmpty()) {
				binding.commitAuthor.setVisibility(View.VISIBLE);
				binding.commitAuthor.setText(commit.getAuthorName());
			} else {
				binding.commitAuthor.setVisibility(View.GONE);
			}

			if (commit.getCreatedAt() != null && !commit.getCreatedAt().trim().isEmpty()) {
				binding.commitTime.setVisibility(View.VISIBLE);
				Date date = TimeHelper.parseIso8601(commit.getCreatedAt());
				binding.commitTime.setText(TimeHelper.formatTime(date));

				binding.commitTime.setOnClickListener(
						v ->
								Toasty.show(
										ctx,
										TimeHelper.getFullDateTime(date, Locale.getDefault())));
			} else {
				binding.commitTime.setVisibility(View.GONE);
			}

			if (commit.getTitle() != null && !commit.getTitle().trim().isEmpty()) {
				binding.commitTitle.setVisibility(View.VISIBLE);
				binding.commitTitle.setText(commit.getTitle());
			} else {
				binding.commitTitle.setVisibility(View.GONE);
			}

			if (commit.getMessage() != null && !commit.getMessage().trim().isEmpty()) {
				String msgBody = commit.getMessage().trim();
				if (commit.getTitle() != null && msgBody.startsWith(commit.getTitle().trim())) {
					msgBody = msgBody.substring(commit.getTitle().trim().length()).trim();
				}

				if (!msgBody.isEmpty()) {
					binding.commitMessage.setVisibility(View.VISIBLE);
					binding.commitMessage.setText(msgBody);
				} else {
					binding.commitMessage.setVisibility(View.GONE);
				}
			} else {
				binding.commitMessage.setVisibility(View.GONE);
			}
		}
	}
}
