package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.databinding.ListCommitDiffsBinding;
import com.labnex.app.helpers.DiffParser;
import com.labnex.app.models.commits.Diff;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class CommitDiffsAdapter extends RecyclerView.Adapter<CommitDiffsAdapter.DiffsViewHolder> {

	private final Context ctx;
	private List<Pair<Diff, Pair<SpannableStringBuilder, SpannableStringBuilder>>> list;

	public CommitDiffsAdapter(
			Context ctx,
			List<Pair<Diff, Pair<SpannableStringBuilder, SpannableStringBuilder>>> list) {
		this.ctx = ctx;
		this.list = list != null ? list : new ArrayList<>();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(
			List<Pair<Diff, Pair<SpannableStringBuilder, SpannableStringBuilder>>> newList) {
		this.list = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public DiffsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListCommitDiffsBinding binding =
				ListCommitDiffsBinding.inflate(LayoutInflater.from(ctx), parent, false);
		return new DiffsViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull DiffsViewHolder holder, int position) {
		holder.bind(list.get(position));
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class DiffsViewHolder extends RecyclerView.ViewHolder {
		final ListCommitDiffsBinding binding;

		DiffsViewHolder(ListCommitDiffsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(Pair<Diff, Pair<SpannableStringBuilder, SpannableStringBuilder>> itemPair) {
			Diff diff = itemPair.first;
			Pair<SpannableStringBuilder, SpannableStringBuilder> parsedSpans = itemPair.second;

			binding.filename.setText(diff.getNewPath());

			if (parsedSpans != null) {
				binding.contents.setText(parsedSpans.first);
				binding.fileStatistics.setText(parsedSpans.second);
			} else {
				DiffParser diffParser =
						new DiffParser(
								ctx, binding.contents, diff.getDiff(), binding.fileStatistics);
				diffParser.highlightDiffWithStats();
			}
		}
	}
}
