package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.CommitDiffsBottomSheet;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.models.commits.Commits;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class CommitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	// private ProjectsContext projectsContext;
	private final Context context;
	private List<Commits> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	Bundle bundle = new Bundle();
	private final int projectId;

	public CommitsAdapter(Context ctx, List<Commits> mainList, int projectId) {
		this.context = ctx;
		this.list = mainList;
		this.projectId = projectId;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new CommitsHolder(inflater.inflate(R.layout.list_commits, parent, false));
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

		((CommitsHolder) holder).bindData(list.get(position));
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

	public void updateList(List<Commits> list_) {
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

	public class CommitsHolder extends RecyclerView.ViewHolder {

		private final TextView title;
		private final TextView info;
		private final TextView sha;
		private Commits commits;

		CommitsHolder(View itemView) {

			super(itemView);

			title = itemView.findViewById(R.id.title);
			info = itemView.findViewById(R.id.info_section);
			sha = itemView.findViewById(R.id.commit_sha);

			itemView.setOnClickListener(
					v -> {
						bundle.putString("source", "commits");
						bundle.putString("sha", commits.getId());
						bundle.putInt("projectId", projectId);

						CommitDiffsBottomSheet bottomSheet = new CommitDiffsBottomSheet();
						bottomSheet.setArguments(bundle);
						bottomSheet.show(
								((FragmentActivity) context).getSupportFragmentManager(),
								"CommitDiffsBottomSheet");
					});
		}

		void bindData(Commits commits) {

			this.commits = commits;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			title.setText(commits.getTitle());

			sha.setText(commits.getShortId());

			if (commits.getAuthorName() != null && commits.getCommitterName() != null) {
				String modifiedTime =
						TimeUtils.formatTime(
								Date.from(
										Instant.parse(
												commits.getCreatedAt()
																.substring(
																		0,
																		commits.getCommittedDate()
																				.indexOf("."))
														+ "Z")),
								locale);
				info.setText(
						String.format(
								context.getResources().getString(R.string.commit_author_info),
								commits.getAuthorName(),
								commits.getCommitterName(),
								modifiedTime));
			}
		}
	}
}
