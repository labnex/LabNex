package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.databinding.ListBranchesBinding;
import com.labnex.app.models.branches.Branches;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class BranchesAdapter extends RecyclerView.Adapter<BranchesAdapter.BranchesHolder> {

	private final Context context;
	private final List<Branches> list;
	private final OnBranchClickListener listener;

	public interface OnBranchClickListener {
		void onBranchClick(String branch);
	}

	public BranchesAdapter(Context ctx, List<Branches> listMain, OnBranchClickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (listMain != null) this.list.addAll(listMain);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Branches> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public BranchesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListBranchesBinding binding =
				ListBranchesBinding.inflate(LayoutInflater.from(context), parent, false);
		return new BranchesHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull BranchesHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class BranchesHolder extends RecyclerView.ViewHolder {

		final ListBranchesBinding binding;

		BranchesHolder(ListBranchesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onBranchClick(list.get(pos).getName());
						}
					});
		}

		void bind(Branches branch) {
			binding.branchName.setText(branch.getName());
		}
	}
}
