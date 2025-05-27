package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.models.repository.Tree;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class FilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private ProjectsContext projectsContext;
	private final Context context;
	private List<Tree> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final FilesAdapterListener filesListener;

	public interface FilesAdapterListener {
		void onClickFile(Tree tree);
	}

	public FilesAdapter(Context ctx, List<Tree> mainList, FilesAdapterListener filesListener) {
		this.context = ctx;
		this.list = mainList != null ? mainList : new ArrayList<>();
		this.filesListener = filesListener;
		this.loadMoreListener =
				new OnLoadMoreListener() {
					@Override
					protected void onLoadMore() {}
				};
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new FilesHolder(inflater.inflate(R.layout.list_files, parent, false));
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

		((FilesHolder) holder).bindData(list.get(position));
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
	}

	public void notifyLoadFinished() {
		if (loadMoreListener != null) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		if (loadMoreListener != null) {
			this.loadMoreListener = loadMoreListener;
		}
	}

	public void updateList(List<Tree> list_) {
		list = list_ != null ? list_ : new ArrayList<>();
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

	public class FilesHolder extends RecyclerView.ViewHolder {

		private final TextView title;
		private final ImageView folder;
		private final ImageView file;
		private Tree tree;

		FilesHolder(View itemView) {

			super(itemView);

			LinearLayout filesFrame = itemView.findViewById(R.id.files_frame);
			title = itemView.findViewById(R.id.title);
			folder = itemView.findViewById(R.id.folder);
			file = itemView.findViewById(R.id.file);

			filesFrame.setOnClickListener(v -> filesListener.onClickFile(tree));
		}

		void bindData(Tree tree) {

			this.tree = tree;

			if (tree.getType().equalsIgnoreCase("tree")) {
				folder.setVisibility(View.VISIBLE);
			}
			if (tree.getType().equalsIgnoreCase("blob")) {
				file.setVisibility(View.VISIBLE);
			}

			title.setText(tree.getName());
		}
	}
}
