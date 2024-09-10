package com.labnex.app.adapters;

import android.annotation.SuppressLint;
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
import com.labnex.app.R;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.models.notes.Notes;
import com.vdurmont.emoji.EmojiParser;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class IssueNotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final ProjectsContext projectsContext;
	private final Context context;
	private List<Notes> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public IssueNotesAdapter(Context ctx, List<Notes> mainList, ProjectsContext projectsContext) {
		this.context = ctx;
		this.list = mainList;
		this.projectsContext = projectsContext;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new IssueNotesHolder(inflater.inflate(R.layout.list_issue_notes, parent, false));
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

		((IssueNotesHolder) holder).bindData(list.get(position), position);
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

	public void updateList(List<Notes> list_) {
		list = list_;
		notifyDataChanged();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public void clearAdapter() {
		list.clear();
		notifyDataChanged();
	}

	public class IssueNotesHolder extends RecyclerView.ViewHolder {

		private final ImageView userAvatar;
		private final TextView username;
		private final TextView date;
		private final RecyclerView noteContent;
		private final LinearLayout timelineDividerView;
		private Notes note;

		IssueNotesHolder(View itemView) {

			super(itemView);

			userAvatar = itemView.findViewById(R.id.user_avatar);
			username = itemView.findViewById(R.id.username);
			date = itemView.findViewById(R.id.date);
			noteContent = itemView.findViewById(R.id.note_content);
			timelineDividerView = itemView.findViewById(R.id.timeline_divider_view);

			userAvatar.setOnClickListener(
					profile -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("source", "issues");
						intent.putExtra("userId", note.getAuthor().getId());
						context.startActivity(intent);
					});
		}

		void bindData(Notes note, int position) {

			this.note = note;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			if (position == 0) {
				timelineDividerView.setVisibility(View.GONE);
			}

			username.setText(note.getAuthor().getName());

			if (note.getAuthor() != null && note.getAuthor().getAvatarUrl() != null) {

				Glide.with(itemView.getContext())
						.load(note.getAuthor().getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(userAvatar);
			}

			Markdown.render(
					context,
					EmojiParser.parseToUnicode(note.getBody().trim()),
					noteContent,
					projectsContext);

			String modifiedTime =
					TimeUtils.formatTime(Date.from(Instant.parse(note.getCreatedAt())), locale);
			date.setText(modifiedTime);
		}
	}
}
