package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.databinding.ListLabelPickerBinding;
import com.labnex.app.helpers.LabelStylingHelper;
import com.labnex.app.models.labels.Labels;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author mmarif
 */
public class LabelPickerAdapter extends RecyclerView.Adapter<LabelPickerAdapter.Holder> {

	private final Context context;
	private final List<Labels> list;
	private final Set<String> selectedLabels;
	private final OnLabelToggleListener listener;
	private final LabelStylingHelper stylingHelper;

	public interface OnLabelToggleListener {
		void onToggle(Labels label);
	}

	public LabelPickerAdapter(
			Context ctx,
			List<Labels> listMain,
			Set<String> selectedLabels,
			OnLabelToggleListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (listMain != null) this.list.addAll(listMain);
		this.selectedLabels = selectedLabels;
		this.listener = listener;
		this.stylingHelper = LabelStylingHelper.getInstance(context);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Labels> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListLabelPickerBinding binding =
				ListLabelPickerBinding.inflate(LayoutInflater.from(context), parent, false);
		return new Holder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull Holder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class Holder extends RecyclerView.ViewHolder {

		final ListLabelPickerBinding binding;

		Holder(ListLabelPickerBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onToggle(list.get(pos));
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
						binding.labelValue,
						13,
						6,
						12);
			} else {
				binding.labelValue.setVisibility(View.GONE);
				stylingHelper.styleRegularLabel(
						labelText,
						label.getColor(),
						label.getTextColor(),
						binding.labelName,
						13,
						6,
						12);
			}

			binding.checkbox.setChecked(selectedLabels.contains(label.getName()));
		}
	}
}
