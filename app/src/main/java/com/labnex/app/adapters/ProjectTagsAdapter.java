package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.databinding.BottomSheetProjectTagsBinding;
import com.labnex.app.databinding.ListTagsBinding;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.tags.TagsItem;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class ProjectTagsAdapter extends RecyclerView.Adapter<ProjectTagsAdapter.TagsHolder> {

	private final Context context;
	private List<TagsItem> list;
	private final int projectId;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false;
	private boolean isMoreDataAvailable = true;
	private final BottomSheetProjectTagsBinding bottomSheetBinding;

	public ProjectTagsAdapter(
			Context ctx,
			List<TagsItem> listMain,
			int projectId,
			BottomSheetProjectTagsBinding binding) {
		this.context = ctx;
		this.list = listMain;
		this.projectId = projectId;
		this.bottomSheetBinding = binding;
		setHasStableIds(true);
	}

	@NonNull @Override
	public TagsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListTagsBinding binding =
				ListTagsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new TagsHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull TagsHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}
		holder.bindData(list.get(position));
	}

	@Override
	public int getItemCount() {
		return list != null ? list.size() : 0;
	}

	@Override
	public long getItemId(int position) {
		return list.get(position).getName().hashCode();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		if (loadMoreListener != null) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<TagsItem> newList) {
		list = newList;
		notifyDataChanged();
	}

	public void clearAdapter() {
		if (list != null) {
			list.clear();
			notifyDataChanged();
		}
	}

	public abstract static class OnLoadMoreListener {
		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class TagsHolder extends RecyclerView.ViewHolder {

		private final ListTagsBinding binding;
		private TagsItem tag;

		TagsHolder(ListTagsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bindData(TagsItem tag) {

			this.tag = tag;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			binding.tagName.setText(tag.getName());

			String authorCommitterInfo;
			if (tag.getCommit() != null) {
				String authorName =
						tag.getCommit().getAuthorName() != null
								? tag.getCommit().getAuthorName()
								: context.getString(R.string.unknown);
				String committerName =
						tag.getCommit().getCommitterName() != null
								? tag.getCommit().getCommitterName()
								: context.getString(R.string.unknown);
				String modifiedTime;
				if (tag.getCommit().getCreatedAt() != null) {
					modifiedTime =
							TimeUtils.formatTime(
									Date.from(
											OffsetDateTime.parse(tag.getCommit().getCreatedAt())
													.toInstant()),
									locale);
				} else {
					modifiedTime = "";
				}
				authorCommitterInfo =
						context.getString(
								R.string.author_committer_info,
								authorName,
								committerName,
								modifiedTime);

			} else {
				String modifiedTime =
						TimeUtils.formatTime(
								Date.from(
										OffsetDateTime.parse(tag.getCommit().getCreatedAt())
												.toInstant()),
								locale);
				authorCommitterInfo =
						context.getString(R.string.author_committer_info_no_commit, modifiedTime);
			}
			binding.authorCommitterInfo.setText(authorCommitterInfo);

			if (tag.getCommit() != null && tag.getCommit().getId() != null) {
				binding.commitId.setText(tag.getCommit().getShortId());
				binding.copyCommitId.setVisibility(View.VISIBLE);
				binding.copyCommitId.setOnClickListener(
						v ->
								Utils.copyToClipboard(
										context,
										(android.app.Activity) context,
										tag.getCommit().getId(),
										context.getString(R.string.commit_id_copied)));
			} else {
				binding.commitId.setText(context.getString(R.string.no_commit));
				binding.copyCommitId.setVisibility(View.GONE);
			}
		}
	}
}
