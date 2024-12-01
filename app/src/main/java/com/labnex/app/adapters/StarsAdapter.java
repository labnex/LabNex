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
import com.labnex.app.models.projects.Stars;
import java.util.List;

/**
 * @author mmarif
 */
public class StarsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Stars> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public StarsAdapter(Context ctx, List<Stars> listMain) {
		this.context = ctx;
		this.list = listMain;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_members, parent, false);
		return new StarsAdapter.StarsHolder(view);
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

		((StarsAdapter.StarsHolder) holder).bindData(list.get(position));
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

	public void updateList(List<Stars> list_) {
		list = list_;
		notifyDataChanged();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class StarsHolder extends RecyclerView.ViewHolder {

		private final ImageView avatar;
		private final TextView name;
		private final TextView userId;
		private Stars stars;

		StarsHolder(View itemView) {

			super(itemView);

			avatar = itemView.findViewById(R.id.user_avatar);
			name = itemView.findViewById(R.id.user_name);
			userId = itemView.findViewById(R.id.user_id);

			avatar.setOnClickListener(
					profile -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("source", "starrers");
						intent.putExtra("userId", stars.getUser().getId());
						context.startActivity(intent);
					});
		}

		void bindData(Stars stars) {

			this.stars = stars;

			name.setText(stars.getUser().getName());
			userId.setText(stars.getUser().getUsername());

			Glide.with(itemView.getContext())
					.load(stars.getUser().getAvatarUrl())
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
