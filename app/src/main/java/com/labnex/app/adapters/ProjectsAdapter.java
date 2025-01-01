package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.activities.ProjectDetailActivity;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.helpers.TextDrawable.ColorGenerator;
import com.labnex.app.helpers.TextDrawable.TextDrawable;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.projects.Projects;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class ProjectsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Projects> projectsList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final String source;

	public ProjectsAdapter(Context ctx, List<Projects> projectsListMain, String source) {
		this.context = ctx;
		this.projectsList = projectsListMain;
		this.source = source;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new ProjectsAdapter.ProjectsHolder(
				inflater.inflate(R.layout.list_projects, parent, false));
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

		((ProjectsAdapter.ProjectsHolder) holder).bindData(projectsList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return projectsList.size();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void clearAdapter() {
		projectsList.clear();
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

	public void updateList(List<Projects> list) {
		projectsList = list;
		notifyDataChanged();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class ProjectsHolder extends RecyclerView.ViewHolder {

		private final TextView projectName;
		private final TextView projectPath;
		private final TextView projectDescription;
		private final ImageView projectAvatar;
		private final TextView projectStars;
		private final ImageView projectStarsIcon;
		private final TextView projectForks;
		private final TextView projectUpdatedAt;
		private Projects projects;

		ProjectsHolder(View itemView) {

			super(itemView);
			projectName = itemView.findViewById(R.id.project_name);
			projectPath = itemView.findViewById(R.id.project_path);
			projectDescription = itemView.findViewById(R.id.project_description);
			projectAvatar = itemView.findViewById(R.id.project_avatar);
			projectStars = itemView.findViewById(R.id.project_stars);
			projectStarsIcon = itemView.findViewById(R.id.project_stars_icon);
			projectForks = itemView.findViewById(R.id.project_forks);
			projectUpdatedAt = itemView.findViewById(R.id.project_updated_at);

			itemView.setOnClickListener(
					v -> {
						Context context = v.getContext();
						ProjectsContext project = new ProjectsContext(projects, context);
						project.saveToDB(context);
						Intent intent = project.getIntent(context, ProjectDetailActivity.class);
						context.startActivity(intent);
					});
		}

		void bindData(Projects projects) {

			this.projects = projects;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			ColorGenerator generator = ColorGenerator.MATERIAL;
			int color = generator.getColor(projects.getName());
			String firstCharacter = String.valueOf(projects.getName().charAt(0));

			TextDrawable drawable =
					TextDrawable.builder()
							.beginConfig()
							.useFont(Typeface.DEFAULT)
							.fontSize(16)
							.toUpperCase()
							.width(28)
							.height(28)
							.endConfig()
							.buildRoundRect(firstCharacter, color, 8);

			projectName.setText(projects.getName());
			projectPath.setText(projects.getPathWithNamespace());

			if (source.equalsIgnoreCase("starred")) {
				projectStarsIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_star_filled));
			}
			projectStars.setText(
					context.getResources()
							.getQuantityString(
									R.plurals.project_stars,
									projects.getStarCount(),
									Utils.numberFormatter(projects.getStarCount())));
			projectForks.setText(
					context.getResources()
							.getQuantityString(
									R.plurals.project_forks,
									projects.getForksCount(),
									Utils.numberFormatter(projects.getForksCount())));
			if (projects.getUpdatedAt() != null) {
				String modifiedTime =
						TimeUtils.formatTime(
								Date.from(
										OffsetDateTime.parse(projects.getUpdatedAt()).toInstant()),
								locale);
				projectUpdatedAt.setText(modifiedTime);
			} else {
				projectUpdatedAt.setVisibility(View.GONE);
			}

			if (projects.getAvatarUrl() != null
					&& projects.getVisibility() != null
					&& projects.getVisibility().equalsIgnoreCase("public")) {

				Glide.with(itemView.getContext())
						.load(projects.getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(projectAvatar);
			} else {
				projectAvatar.setImageDrawable(drawable);
			}

			if (projects.getDescription() != null && !projects.getDescription().isEmpty()) {

				projectDescription.setVisibility(View.VISIBLE);
				projectDescription.setText(projects.getDescription());
			} else {
				projectDescription.setVisibility(View.GONE);
			}
		}
	}
}
