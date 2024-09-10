package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.models.user.User;
import java.util.List;

/**
 * @author mmarif
 */
public class MembersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<User> list;
	// private final int groupId;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public MembersAdapter(Context ctx, List<User> listMain, int groupId) {
		this.context = ctx;
		this.list = listMain;
		// this.groupId = groupId;
	}

	public MembersAdapter(Context ctx, List<User> listMain) {
		this.context = ctx;
		this.list = listMain;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_members, parent, false);
		return new MembersHolder(view);
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

		((MembersHolder) holder).bindData(list.get(position));
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

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<User> list_) {
		list = list_;
		notifyDataChanged();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class MembersHolder extends RecyclerView.ViewHolder {

		private final ImageView avatar;
		private final TextView name;
		private final TextView userId;
		private User user;

		MembersHolder(View itemView) {

			super(itemView);

			avatar = itemView.findViewById(R.id.user_avatar);
			name = itemView.findViewById(R.id.user_name);
			userId = itemView.findViewById(R.id.user_id);

			avatar.setOnClickListener(
					profile -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("source", "members");
						intent.putExtra("userId", user.getId());
						context.startActivity(intent);
					});
		}

		void bindData(User user) {

			this.user = user;

			name.setText(user.getFullName());
			userId.setText(user.getUsername());

			Glide.with(itemView.getContext())
					.load(user.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.ic_spinner)
					.centerCrop()
					.into(avatar);
		}
	}

	private void updateAdapter(int position) {
		list.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, list.size());
	}

	public void clearAdapter() {
		list.clear();
		notifyDataChanged();
	}
}
