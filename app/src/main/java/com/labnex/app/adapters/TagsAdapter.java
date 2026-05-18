package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.databinding.ListTagsBinding;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.tags.TagsItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsHolder> {

	private final Context context;
	private final List<TagsItem> list;
	private final OnTagClickListener listener;
	private boolean canModify = true;

	public void setCanModify(boolean canModify) {
		this.canModify = canModify;
	}

	public interface OnTagClickListener {
		void onMenuClick(TagsItem tag);
	}

	public TagsAdapter(Context ctx, List<TagsItem> listMain, OnTagClickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (listMain != null) this.list.addAll(listMain);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<TagsItem> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public TagsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListTagsBinding binding =
				ListTagsBinding.inflate(LayoutInflater.from(context), parent, false);
		return new TagsHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull TagsHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class TagsHolder extends RecyclerView.ViewHolder {

		final ListTagsBinding binding;

		TagsHolder(ListTagsBinding binding) {
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
		}

		void bind(TagsItem tag) {
			binding.tagName.setText(tag.getName());

			String info;
			if (tag.getCommit() != null) {
				String author =
						tag.getCommit().getAuthorName() != null
								? tag.getCommit().getAuthorName()
								: context.getString(R.string.unknown);
				String timeStr = "";
				String dateSource =
						tag.getCreatedAt() != null
								? tag.getCreatedAt()
								: tag.getCommit().getCreatedAt();
				if (dateSource != null) {
					Date date = TimeHelper.parseIso8601(dateSource);
					timeStr = TimeHelper.formatTime(date);
					binding.authorCommitterInfo.setOnClickListener(
							v ->
									Toasty.show(
											context,
											TimeHelper.getFullDateTime(date, Locale.getDefault())));
				}
				info = context.getString(R.string.tag_info, author, timeStr);
			} else {
				info = "";
			}
			binding.authorCommitterInfo.setText(info);

			if (tag.getCommit() != null && tag.getCommit().getId() != null) {
				binding.commitId.setText(tag.getCommit().getShortId());
				binding.copyCommitId.setVisibility(View.VISIBLE);
				binding.copyCommitId.setOnClickListener(
						v ->
								Utils.copyToClipboard(
										context,
										tag.getCommit().getId(),
										context.getString(R.string.commit_id_copied)));
			} else {
				binding.commitId.setText(R.string.no_commit);
				binding.copyCommitId.setVisibility(View.GONE);
			}
		}
	}
}
