package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.LabelActionsBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetProjectLabelsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.labels.Labels;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ProjectLabelsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Labels> list;
	private final int projectId;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final BottomSheetProjectLabelsBinding bottomSheetProjectLabelsBinding;
	Bundle bundle = new Bundle();

	public ProjectLabelsAdapter(
			Context ctx,
			List<Labels> listMain,
			int projectId,
			BottomSheetProjectLabelsBinding bottomSheetProjectLabelsBinding) {
		this.context = ctx;
		this.list = listMain;
		this.projectId = projectId;
		this.bottomSheetProjectLabelsBinding = bottomSheetProjectLabelsBinding;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View view =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_labels, parent, false);
		return new LabelsHolder(view);
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

		((LabelsHolder) holder).bindData(list.get(position));
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

	public void updateList(List<Labels> list_) {
		list = list_;
		notifyDataChanged();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class LabelsHolder extends RecyclerView.ViewHolder {

		private final TextView labelName;
		private final MaterialCardView labelView;
		private final TextView description;
		private final TextView labelOpenIssues;
		private final TextView labelOpenMergeRequests;
		private Labels labels;

		LabelsHolder(View itemView) {

			super(itemView);

			labelName = itemView.findViewById(R.id.label_name);
			labelView = itemView.findViewById(R.id.labelView);
			description = itemView.findViewById(R.id.description);
			labelOpenIssues = itemView.findViewById(R.id.label_open_issues);
			labelOpenMergeRequests = itemView.findViewById(R.id.label_open_merge_requests);
			ImageView editLabel = itemView.findViewById(R.id.edit_label);
			ImageView deleteLabel = itemView.findViewById(R.id.delete_label);

			editLabel.setOnClickListener(
					edit -> {
						bundle.putString("type", "project");
						bundle.putString("source", "edit");
						bundle.putString("name", labels.getName());
						bundle.putString("description", labels.getDescription().toString());
						bundle.putString("color", labels.getColor());
						bundle.putInt("id", labels.getId());
						bundle.putInt("projectId", projectId);

						LabelActionsBottomSheet bottomSheet = new LabelActionsBottomSheet();
						bottomSheet.setArguments(bundle);
						bottomSheet.show(
								((FragmentActivity) context).getSupportFragmentManager(),
								"labelActionsBottomSheet");
					});

			deleteLabel.setOnClickListener(
					delete -> {
						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(
										context,
										com.google.android.material.R.style
												.ThemeOverlay_Material3_Dialog_Alert);

						materialAlertDialogBuilder
								.setTitle(
										context.getString(
												R.string.delete_dialog_title, labels.getName()))
								.setMessage(R.string.delete_label_dialog_message)
								.setPositiveButton(
										R.string.delete,
										(dialog, whichButton) -> {
											deleteLabel(
													labels.getId(),
													labels.getName(),
													getBindingAdapterPosition());
										})
								.setNeutralButton(R.string.cancel, null)
								.show();
					});
		}

		void bindData(Labels labels) {

			this.labels = labels;

			labelName.setText(labels.getName());
			labelName.setTextColor(
					Color.parseColor(Utils.repeatString(labels.getTextColor(), 4, 1, 2)));

			if (labels.getDescription() != null) {
				if (!labels.getDescription().toString().isEmpty()) {
					description.setVisibility(View.VISIBLE);
					description.setText(labels.getDescription().toString());
				}
			}
			labelOpenIssues.setText(String.valueOf(labels.getOpenIssuesCount()));
			labelOpenMergeRequests.setText(String.valueOf(labels.getOpenMergeRequestsCount()));

			labelView.setCardBackgroundColor(
					Color.parseColor(Utils.repeatString(labels.getColor(), 4, 1, 2)));
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

	private void deleteLabel(int id, String name, int position) {

		RetrofitClient.getApiInterface(context)
				.deleteProjectLabel(projectId, id)
				.enqueue(
						new Callback<>() {

							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {

								if (response.code() == 204) {

									updateAdapter(position);
									Snackbar.info(
											context,
											bottomSheetProjectLabelsBinding.getRoot(),
											context.getResources()
													.getString(R.string.label_deleted));

								} else if (response.code() == 401) {

									Snackbar.info(
											context,
											bottomSheetProjectLabelsBinding.getRoot(),
											context.getResources()
													.getString(R.string.not_authorized));
								} else if (response.code() == 403) {

									Snackbar.info(
											context,
											bottomSheetProjectLabelsBinding.getRoot(),
											context.getResources()
													.getString(R.string.access_forbidden_403));
								} else {

									Snackbar.info(
											context,
											bottomSheetProjectLabelsBinding.getRoot(),
											context.getResources()
													.getString(R.string.generic_error));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

								Snackbar.info(
										context,
										bottomSheetProjectLabelsBinding.getRoot(),
										context.getResources()
												.getString(R.string.generic_server_response_error));
							}
						});
	}
}
