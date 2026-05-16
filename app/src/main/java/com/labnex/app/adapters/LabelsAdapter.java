package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.databinding.ListLabelsBinding;
import com.labnex.app.helpers.LabelStylingHelper;
import com.labnex.app.models.labels.Labels;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class LabelsAdapter extends RecyclerView.Adapter<LabelsAdapter.LabelsHolder> {

	private final Context context;
	private final List<Labels> list;
	private final OnLabelClickListener listener;

	public interface OnLabelClickListener {
		void onMenuClick(Labels label);
	}

	public LabelsAdapter(Context ctx, List<Labels> listMain, OnLabelClickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (listMain != null) this.list.addAll(listMain);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Labels> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public LabelsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListLabelsBinding binding =
				ListLabelsBinding.inflate(LayoutInflater.from(context), parent, false);
		return new LabelsHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull LabelsHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class LabelsHolder extends RecyclerView.ViewHolder {

		final ListLabelsBinding binding;
		private final LabelStylingHelper stylingHelper;

		LabelsHolder(ListLabelsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
			this.stylingHelper = LabelStylingHelper.getInstance(context);

			binding.btnMenu.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onMenuClick(list.get(pos));
						}
					});
		}

		void bind(Labels label) {
			String labelText = label.getName();

			if (LabelStylingHelper.isScopedLabel(labelText)) {
				stylingHelper.styleScopedLabel(
						labelText,
						label.getColor(),
						label.getTextColor(),
						binding.labelName,
						binding.labelValue);
			} else {
				binding.labelValue.setVisibility(View.GONE);
				stylingHelper.styleRegularLabel(
						labelText, label.getColor(), label.getTextColor(), binding.labelName);
			}

			if (label.getDescription() != null && !label.getDescription().toString().isEmpty()) {
				binding.description.setVisibility(View.VISIBLE);
				binding.description.setText(label.getDescription().toString());
			} else {
				binding.description.setVisibility(View.GONE);
			}

			binding.labelOpenIssues.setText(String.valueOf(label.getOpenIssuesCount()));
			binding.labelOpenMergeRequests.setText(
					String.valueOf(label.getOpenMergeRequestsCount()));
		}
	}
}
