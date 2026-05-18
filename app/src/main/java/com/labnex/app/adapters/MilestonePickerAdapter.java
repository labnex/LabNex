package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.databinding.ListMilestonePickerBinding;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.models.milestone.Milestones;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class MilestonePickerAdapter extends RecyclerView.Adapter<MilestonePickerAdapter.Holder> {

	private final Context context;
	private final List<Milestones> list;
	private Milestones selected;
	private final OnMilestonePickListener listener;

	public interface OnMilestonePickListener {
		void onPick(Milestones milestone);
	}

	public MilestonePickerAdapter(
			Context ctx,
			List<Milestones> listMain,
			Milestones preSelected,
			OnMilestonePickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (listMain != null) this.list.addAll(listMain);
		this.selected = preSelected;
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Milestones> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListMilestonePickerBinding binding =
				ListMilestonePickerBinding.inflate(LayoutInflater.from(context), parent, false);
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

		final ListMilestonePickerBinding binding;

		@SuppressLint("NotifyDataSetChanged")
		Holder(ListMilestonePickerBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							selected = list.get(pos);
							listener.onPick(selected);
							notifyDataSetChanged();
						}
					});
		}

		void bind(Milestones milestone) {
			binding.title.setText(milestone.getTitle());

			String statusText;
			int statusColor;
			if (milestone.isExpired()) {
				statusText = context.getString(R.string.expired);
				statusColor =
						context.getResources().getColor(R.color.five_yellow, context.getTheme());
			} else if (milestone.getDueDate() != null) {
				LocalDate due = null;
				try {
					due = OffsetDateTime.parse(milestone.getDueDate() + "T23:59:59Z").toLocalDate();
				} catch (Exception ignored) {
				}
				if (due != null && due.isBefore(LocalDate.now(ZoneId.systemDefault()))) {
					statusText = context.getString(R.string.expired);
					statusColor =
							context.getResources()
									.getColor(R.color.five_yellow, context.getTheme());
				} else {
					statusText = context.getString(R.string.open);
					statusColor =
							context.getResources().getColor(R.color.green, context.getTheme());
				}
			} else {
				statusText = context.getString(R.string.open);
				statusColor = context.getResources().getColor(R.color.green, context.getTheme());
			}

			binding.statusBadge.setImageDrawable(
					AvatarGenerator.getLabelDrawable(context, statusText, statusColor, 18));

			if (milestone.getStartDate() != null && milestone.getDueDate() != null) {
				binding.dueDate.setText(
						context.getString(
								R.string.data_concatenate,
								TimeHelper.formatDate(milestone.getStartDate()),
								TimeHelper.formatDate(milestone.getDueDate())));
				binding.dueDate.setVisibility(View.VISIBLE);
			} else if (milestone.getDueDate() != null) {
				binding.dueDate.setText(
						context.getString(
								R.string.due_on, TimeHelper.formatDate(milestone.getDueDate())));
				binding.dueDate.setVisibility(View.VISIBLE);
			} else {
				binding.dueDate.setVisibility(View.GONE);
			}

			boolean isSelected = selected != null && selected.getId() == milestone.getId();
			binding.checkIcon.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
		}
	}
}
