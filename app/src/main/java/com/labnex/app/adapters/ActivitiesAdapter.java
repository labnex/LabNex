package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.activities.ProjectDetailActivity;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.models.events.Events;
import com.labnex.app.models.projects.Projects;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Events> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	Bundle bundle = new Bundle();

	public ActivitiesAdapter(Context ctx, List<Events> mainList) {
		this.context = ctx;
		this.list = mainList;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new ActivitiesHolder(inflater.inflate(R.layout.list_activities, parent, false));
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

		((ActivitiesHolder) holder).bindData(list.get(position));
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

	public void updateList(List<Events> list_) {
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

	public class ActivitiesHolder extends RecyclerView.ViewHolder {

		private final ImageView avatar;
		private final TextView authorName;
		private final TextView authorUsername;
		private final TextView time;
		private final TextView content;
		private final TextView body;
		private Events events;

		ActivitiesHolder(View itemView) {

			super(itemView);

			avatar = itemView.findViewById(R.id.avatar);
			authorName = itemView.findViewById(R.id.author_name);
			authorUsername = itemView.findViewById(R.id.author_username);
			time = itemView.findViewById(R.id.time);
			content = itemView.findViewById(R.id.content);
			body = itemView.findViewById(R.id.body);

			itemView.setOnClickListener(
					v -> {
						Call<Projects> call =
								RetrofitClient.getApiInterface(context)
										.getProjectInfo(events.getProjectId());

						call.enqueue(
								new Callback<>() {

									@Override
									public void onResponse(
											@NonNull Call<Projects> call,
											@NonNull retrofit2.Response<Projects> response) {

										Projects projectDetails = response.body();

										if (response.isSuccessful()) {

											if (response.code() == 200) {
												assert projectDetails != null;
												Context context = v.getContext();
												ProjectsContext project =
														new ProjectsContext(
																projectDetails, context);
												Intent intent =
														project.getIntent(
																context,
																ProjectDetailActivity.class);
												context.startActivity(intent);
											}
										}
									}

									@Override
									public void onFailure(
											@NonNull Call<Projects> call, @NonNull Throwable t) {}
								});
					});
		}

		void bindData(Events events) {

			this.events = events;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			if (events.getAuthor() != null && events.getAuthor().getAvatarUrl() != null) {

				Glide.with(itemView.getContext())
						.load(events.getAuthor().getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(avatar);
			}
			authorName.setText(events.getAuthor().getName());
			authorUsername.setText(
					context.getResources()
							.getString(
									R.string.username_with_at_sign,
									events.getAuthor().getUsername()));

			if (events.getActionName() != null) {

				switch (events.getActionName()) {
					case "commented on":
						if (events.getNote() != null
								&& events.getNote().getNoteableType().equalsIgnoreCase("Issue")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_commented_on_issue,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.data_concatenate,
													events.getTargetTitle(),
													String.valueOf(
															events.getNote().getNoteableIid())));
							body.setVisibility(View.VISIBLE);
							body.setText(events.getNote().getBody());
						}

						if (events.getNote() != null
								&& events.getNote()
										.getNoteableType()
										.equalsIgnoreCase("MergeRequest")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_commented_on_mr,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.data_concatenate,
													events.getTargetTitle(),
													String.valueOf(
															events.getNote().getNoteableIid())));
							body.setVisibility(View.VISIBLE);
							body.setText(events.getNote().getBody());
						}

						break;
					case "approved":
						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("MergeRequest")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_approved_mr,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.data_concatenate,
													events.getTargetTitle(),
													events.getTargetIid()));
						}
						break;
					case "opened":
						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("MergeRequest")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_opened_mr,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.data_concatenate,
													events.getTargetTitle(),
													events.getTargetIid()));
						}

						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("Issue")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_opened_issue,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.data_concatenate,
													events.getTargetTitle(),
													events.getTargetIid()));
						}

						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("Milestone")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_opened_milestone,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.single_string_conversion,
													events.getTargetTitle()));
						}
						break;
					case "closed":
						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("MergeRequest")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_closed_mr,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.data_concatenate,
													events.getTargetTitle(),
													events.getTargetIid()));
						}

						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("Issue")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_closed_issue,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.data_concatenate,
													events.getTargetTitle(),
													events.getTargetIid()));
						}

						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("Milestone")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_closed_milestone,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.single_string_conversion,
													events.getTargetTitle()));
						}
						break;
					case "pushed to":
						if (events.getPushData() != null
								&& events.getPushData().getAction().equalsIgnoreCase("pushed")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_pushed_to,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.data_concatenate,
													events.getPushData().getCommitTitle(),
													events.getPushData()
															.getCommitTo()
															.substring(0, 8)));
						}
						break;
					case "pushed new":
						if (events.getPushData() != null
								&& events.getPushData().getAction().equalsIgnoreCase("created")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_pushed_new_branch,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.single_string_conversion,
													events.getPushData().getRef()));
						}
						break;
					case "deleted":
						if (events.getPushData() != null
								&& events.getPushData().getAction().equalsIgnoreCase("removed")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_deleted_branch,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.single_string_conversion,
													events.getPushData().getRef()));
						}
						break;
					case "accepted":
						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("MergeRequest")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_accepted_mr,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.data_concatenate,
													events.getTargetTitle(),
													events.getTargetIid()));
						}
						break;
					case "created":
						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("WikiPage::Meta")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_created_wiki_page,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.single_string_conversion,
													events.getTargetTitle()));
						} else {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_created_project,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.single_string_conversion,
													String.valueOf(events.getProjectId())));
						}
						break;
					case "updated":
						if (events.getTargetType() != null
								&& events.getTargetType().equalsIgnoreCase("WikiPage::Meta")) {

							authorUsername.setText(
									context.getResources()
											.getString(
													R.string.activity_updated_wiki_page,
													events.getAuthor().getUsername()));

							content.setText(
									context.getResources()
											.getString(
													R.string.single_string_conversion,
													events.getTargetTitle()));
						}
						break;
				}
			}

			String modifiedTime =
					TimeUtils.formatTime(
							Date.from(OffsetDateTime.parse(events.getCreatedAt()).toInstant()),
							locale);
			time.setText(modifiedTime);
		}
	}
}
