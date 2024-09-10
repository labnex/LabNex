package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.databinding.BottomSheetCommitDiffsBinding;
import com.labnex.app.helpers.DiffParser;
import com.labnex.app.models.commits.Diff;
import java.util.List;

/**
 * @author mmarif
 */
public class CommitDiffsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Diff> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private int projectId;
	private BottomSheetCommitDiffsBinding binding;

	public CommitDiffsAdapter(
			Context ctx,
			List<Diff> mainList,
			int projectId,
			BottomSheetCommitDiffsBinding binding) {
		this.context = ctx;
		this.list = mainList;
		this.projectId = projectId;
		this.binding = binding;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new CommitDiffsHolder(inflater.inflate(R.layout.list_commit_diffs, parent, false));
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

		((CommitDiffsHolder) holder).bindData(list.get(position));
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

	public void updateList(List<Diff> list_) {
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

	public class CommitDiffsHolder extends RecyclerView.ViewHolder {

		private final TextView filename;
		private final TextView fileStatistics;
		private final TextView contents;
		private Diff diffs;

		CommitDiffsHolder(View itemView) {

			super(itemView);

			filename = itemView.findViewById(R.id.filename);
			fileStatistics = itemView.findViewById(R.id.file_statistics);
			contents = itemView.findViewById(R.id.contents);
		}

		void bindData(Diff diffs) {

			this.diffs = diffs;

			filename.setText(diffs.getNewPath());

			DiffParser diffParser =
					new DiffParser(context, contents, diffs.getDiff(), fileStatistics);
			diffParser.highlightDiffWithStats();
		}
	}
}
