package com.labnex.app.adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.labnex.app.R;
import com.labnex.app.databinding.BottomSheetProjectMilestonesBinding;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.models.milestone.Milestones;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * @author mmarif
 */
public class ProjectMilestonesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Milestones> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private int projectId;
	private BottomSheetProjectMilestonesBinding binding;
	Bundle bundle = new Bundle();

	public ProjectMilestonesAdapter(
			Context ctx,
			List<Milestones> list,
			int projectId,
			BottomSheetProjectMilestonesBinding binding) {
		this.context = ctx;
		this.list = list;
		this.projectId = projectId;
		this.binding = binding;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new MilestonesHolder(inflater.inflate(R.layout.list_milestones, parent, false));
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

		((MilestonesHolder) holder).bindData(list.get(position));
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

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Milestones> list_) {
		list = list_;
	}

	private void updateAdapter(int position) {
		list.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, list.size());
	}

	public void clearAdapter() {
		list.clear();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class MilestonesHolder extends RecyclerView.ViewHolder {

		private final TextView title;
		private final TextView dueDate;
		private final TextView description;
		private final MaterialCardView statusCard;
		private final TextView status;
		private Milestones milestones;

		MilestonesHolder(View itemView) {

			super(itemView);
			title = itemView.findViewById(R.id.title);
			dueDate = itemView.findViewById(R.id.due_date);
			description = itemView.findViewById(R.id.description);
			statusCard = itemView.findViewById(R.id.status_view);
			status = itemView.findViewById(R.id.status);
			ImageView delete = itemView.findViewById(R.id.delete);

			/*delete.setOnClickListener(
			deleteRelease -> {
				MaterialAlertDialogBuilder materialAlertDialogBuilder =
						new MaterialAlertDialogBuilder(
								context,
								com.google.android.material.R.style
										.ThemeOverlay_Material3_Dialog_Alert);

				materialAlertDialogBuilder
						.setTitle(
								context.getString(
										R.string.delete_dialog_title,
										milestones.getTagName()))
						.setMessage(R.string.delete_release_message)
						.setPositiveButton(
								R.string.delete,
								(dialog, whichButton) -> {
									deleteRelease(
											milestones.getTagName(),
											getBindingAdapterPosition());
								})
						.setNeutralButton(R.string.cancel, null)
						.show();
			});*/
		}

		void bindData(Milestones milestones) {

			this.milestones = milestones;

			title.setText(milestones.getTitle());

			LocalDate currentDate = LocalDate.now(ZoneId.systemDefault());

			LocalDate startDate = null;
			if (milestones.getStartDate() != null) {
				try {
					OffsetDateTime startDateTime =
							OffsetDateTime.parse(milestones.getStartDate() + "T23:59:59Z");
					startDate = startDateTime.toLocalDate();
				} catch (Exception ignored) {
				}
			}

			if (startDate != null && startDate.isAfter(currentDate)) {
				status.setText(R.string.upcoming);
				statusCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.grey));
				status.setTextColor(
						ContextCompat.getColor(context, R.color.md_dark_theme_icons_color));
			} else {
				status.setText(R.string.open);
				statusCard.setCardBackgroundColor(
						ContextCompat.getColor(context, R.color.dark_green));
				status.setTextColor(
						ContextCompat.getColor(context, R.color.md_dark_theme_icons_color));
			}

			if (milestones.getStartDate() != null && milestones.getDueDate() != null) {
				dueDate.setText(
						context.getResources()
								.getString(
										R.string.data_concatenate,
										TimeUtils.formattedDate(milestones.getStartDate()),
										TimeUtils.formattedDate(milestones.getDueDate())));
			} else if (milestones.getStartDate() != null) {
				dueDate.setText(
						context.getResources()
								.getString(
										R.string.single_string_conversion,
										TimeUtils.formattedDate(milestones.getStartDate())));
			} else if (milestones.getDueDate() != null) {
				dueDate.setText(
						context.getResources()
								.getString(
										R.string.due_on,
										TimeUtils.formattedDate(milestones.getDueDate())));
			} else {
				dueDate.setVisibility(View.GONE);
			}

			if (milestones.getDescription() != null && !milestones.getDescription().isEmpty()) {
				description.setText(milestones.getDescription());
			} else {
				description.setAlpha(.5F);
			}
		}
	}
}
