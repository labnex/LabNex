package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.labnex.app.activities.MergeRequestDetailActivity;
import com.labnex.app.activities.ProfileActivity;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.MergeRequestContext;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.projects.Projects;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class MergeRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private ProjectsContext projectsContext;
	private final Context context;
	private final List<MergeRequests> list = new ArrayList<>();
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public MergeRequestsAdapter(
			Context ctx, List<MergeRequests> mainList, ProjectsContext projectsContext) {
		this.context = ctx;
		this.projectsContext = projectsContext;
		if (mainList != null) {
			this.list.addAll(mainList);
		}
	}

	public MergeRequestsAdapter(Context ctx, List<MergeRequests> mainList) {
		this.context = ctx;
		if (mainList != null) {
			this.list.addAll(mainList);
		}
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new MergeRequestsHolder(
				inflater.inflate(R.layout.list_merge_requests, parent, false));
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

		((MergeRequestsHolder) holder).bindData(list.get(position));
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
		if (!isMoreDataAvailable && loadMoreListener != null) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		if (loadMoreListener != null) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<MergeRequests> list_) {
		list.clear();
		if (list_ != null) {
			list.addAll(list_);
		}
		notifyDataChanged();
	}

	public void clearAdapter() {
		list.clear();
		notifyDataChanged();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class MergeRequestsHolder extends RecyclerView.ViewHolder {

		private final ImageView author;
		private final TextView title;
		private final TextView project;
		private final TextView issueNotesCount;
		private final TextView issueCreatedAt;
		private MergeRequests mergeRequests;

		MergeRequestsHolder(View itemView) {
			super(itemView);

			author = itemView.findViewById(R.id.avatar);
			title = itemView.findViewById(R.id.title);
			project = itemView.findViewById(R.id.project);
			issueNotesCount = itemView.findViewById(R.id.issue_notes_count);
			issueCreatedAt = itemView.findViewById(R.id.issue_created_at);

			author.setOnClickListener(
					profile -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("source", "mr");
						intent.putExtra("userId", mergeRequests.getAuthor().getId());
						context.startActivity(intent);
					});

			itemView.setOnClickListener(
					v -> {
						Call<Projects> call =
								RetrofitClient.getApiInterface(context)
										.getProjectInfo(mergeRequests.getProjectId());

						call.enqueue(
								new Callback<>() {
									@Override
									public void onResponse(
											@NonNull Call<Projects> call,
											@NonNull retrofit2.Response<Projects> response) {
										Projects projectDetails = response.body();

										if (response.isSuccessful() && response.code() == 200) {
											assert projectDetails != null;
											projectsContext =
													new ProjectsContext(projectDetails, context);

											Context context = v.getContext();
											MergeRequestContext mergeRequestContext =
													new MergeRequestContext(
															mergeRequests, projectsContext);
											Intent intent =
													mergeRequestContext.getIntent(
															context,
															MergeRequestDetailActivity.class);
											context.startActivity(intent);
										}
									}

									@Override
									public void onFailure(
											@NonNull Call<Projects> call, @NonNull Throwable t) {}
								});
					});
		}

		void bindData(MergeRequests mergeRequests) {
			this.mergeRequests = mergeRequests;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			title.setText(mergeRequests.getTitle());
			project.setText(mergeRequests.getReferences().getFull());

			if (mergeRequests.getAuthor() != null
					&& mergeRequests.getAuthor().getAvatarUrl() != null) {
				Glide.with(itemView.getContext())
						.load(mergeRequests.getAuthor().getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(author);
			}

			issueNotesCount.setText(String.valueOf(mergeRequests.getUserNotesCount()));
			String modifiedTime =
					TimeUtils.formatTime(
							Date.from(
									OffsetDateTime.parse(mergeRequests.getCreatedAt()).toInstant()),
							locale);
			issueCreatedAt.setText(modifiedTime);
		}
	}
}
