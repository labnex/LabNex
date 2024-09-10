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
import com.labnex.app.databinding.BottomSheetBranchesBinding;
import com.labnex.app.models.branches.Branches;
import java.util.List;

/**
 * @author mmarif
 */
public class BranchesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Branches> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final BranchesAdapterListener branchesAdapterListener;
	private final BottomSheetBranchesBinding bottomSheetBranchesBinding;

	public interface BranchesAdapterListener {
		void onClickItem(String branch);
	}

	public BranchesAdapter(
			Context ctx,
			List<Branches> listMain,
			int id,
			BottomSheetBranchesBinding bottomSheetBranchesBinding,
			BranchesAdapterListener branchesAdapterListener) {
		this.context = ctx;
		this.list = listMain;
		this.branchesAdapterListener = branchesAdapterListener;
		this.bottomSheetBranchesBinding = bottomSheetBranchesBinding;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_branches, parent, false);
		return new BranchesHolder(view);
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

		((BranchesHolder) holder).bindData(list.get(position));
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

	public void updateList(List<Branches> list_) {
		list = list_;
		notifyDataChanged();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class BranchesHolder extends RecyclerView.ViewHolder {

		private final TextView branchName;
		private Branches branches;

		BranchesHolder(View itemView) {

			super(itemView);

			branchName = itemView.findViewById(R.id.branch_name);

			itemView.setOnClickListener(
					view -> {
						branchesAdapterListener.onClickItem(branches.getName());
					});
		}

		void bindData(Branches branches) {

			this.branches = branches;

			branchName.setText(branches.getName());
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
