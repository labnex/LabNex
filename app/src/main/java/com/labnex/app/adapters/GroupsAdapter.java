package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.labnex.app.R;
import com.labnex.app.activities.GroupDetailActivity;
import com.labnex.app.helpers.TextDrawable.ColorGenerator;
import com.labnex.app.helpers.TextDrawable.TextDrawable;
import com.labnex.app.models.groups.GroupsItem;
import java.util.List;

/**
 * @author mmarif
 */
public class GroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<GroupsItem> groupsList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public GroupsAdapter(Context ctx, List<GroupsItem> groupsListMain) {
		this.context = ctx;
		this.groupsList = groupsListMain;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new GroupsHolder(inflater.inflate(R.layout.list_groups, parent, false));
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

		((GroupsHolder) holder).bindData(groupsList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return groupsList.size();
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
		loadMoreListener.onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<GroupsItem> list) {
		groupsList = list;
		notifyDataChanged();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public static class GroupsHolder extends RecyclerView.ViewHolder {

		private final TextView groupName;
		private final TextView groupId;
		private final TextView groupDescription;
		private final ImageView groupAvatar;
		private GroupsItem groupsItem;
		private final ImageView subgroupArrow;
		private final MaterialCardView groupCard;

		GroupsHolder(View itemView) {

			super(itemView);
			groupName = itemView.findViewById(R.id.group_name);
			groupId = itemView.findViewById(R.id.group_id);
			groupDescription = itemView.findViewById(R.id.group_description);
			groupAvatar = itemView.findViewById(R.id.group_avatar);
			subgroupArrow = itemView.findViewById(R.id.subgroup_arrow);
			groupCard = itemView.findViewById(R.id.group_card);

			itemView.setOnClickListener(
					v -> {
						Context context = v.getContext();
						Intent intent = new Intent(context, GroupDetailActivity.class);
						intent.putExtra("groupId", groupsItem.getId());
						context.startActivity(intent);
					});
		}

		void bindData(GroupsItem groupsItem) {

			this.groupsItem = groupsItem;

			int level = groupsItem.getLevel();
			positionArrowAndCard(level);

			ColorGenerator generator = ColorGenerator.MATERIAL;
			int color = generator.getColor(groupsItem.getName());
			String firstCharacter = String.valueOf(groupsItem.getFullName().charAt(0));

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

			groupName.setText(groupsItem.getName());
			groupId.setText(groupsItem.getFullPath());

			if (groupsItem.getAvatarUrl() != null) {

				Glide.with(itemView.getContext())
						.load(groupsItem.getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(groupAvatar);
			} else {
				groupAvatar.setImageDrawable(drawable);
			}

			if (!groupsItem.getDescription().isEmpty()) {

				groupDescription.setVisibility(View.VISIBLE);
				groupDescription.setText(groupsItem.getDescription());
			} else {
				groupDescription.setVisibility(View.GONE);
			}
		}

		private void positionArrowAndCard(int level) {
			if (level > 0) {
				subgroupArrow.setVisibility(View.VISIBLE);

				int totalIndent = level * dpToPx(itemView.getContext(), 32);
				int arrowOffset = totalIndent - dpToPx(itemView.getContext(), 24);

				FrameLayout.LayoutParams arrowParams =
						(FrameLayout.LayoutParams) subgroupArrow.getLayoutParams();
				arrowParams.leftMargin = Math.max(0, arrowOffset);
				subgroupArrow.setLayoutParams(arrowParams);

				FrameLayout.LayoutParams cardParams =
						(FrameLayout.LayoutParams) groupCard.getLayoutParams();
				cardParams.leftMargin = totalIndent;
				groupCard.setLayoutParams(cardParams);

				adjustArrowAppearance(level);

			} else {
				subgroupArrow.setVisibility(View.GONE);
				FrameLayout.LayoutParams cardParams =
						(FrameLayout.LayoutParams) groupCard.getLayoutParams();
				cardParams.leftMargin = 0;
				groupCard.setLayoutParams(cardParams);
			}
		}

		private void adjustArrowAppearance(int level) {
			switch (level) {
				case 1:
					// First-level subgroup
					subgroupArrow.setAlpha(.8f);
					subgroupArrow.setRotation(0);
					break;
				case 2:
					// Second-level
					subgroupArrow.setAlpha(0.7f);
					subgroupArrow.setRotation(0);
					break;
				default:
					// Deeper levels
					subgroupArrow.setAlpha(0.6f);
					subgroupArrow.setRotation(0);
					break;
			}
		}

		private int dpToPx(Context context, int dp) {
			return (int) (dp * context.getResources().getDisplayMetrics().density);
		}
	}
}
