package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.activities.ProjectDetailActivity;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ListProjectsBinding;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.projects.Projects;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectsHolder> {

	private final Context context;
	private final List<Projects> projectsList;
	private final String source;

	public ProjectsAdapter(Context ctx, List<Projects> projectsListMain, String source) {
		this.context = ctx;
		this.projectsList = new ArrayList<>();
		if (projectsListMain != null) {
			this.projectsList.addAll(projectsListMain);
		}
		this.source = source;
	}

	@NonNull @Override
	public ProjectsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListProjectsBinding binding =
				ListProjectsBinding.inflate(LayoutInflater.from(context), parent, false);
		return new ProjectsHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ProjectsHolder holder, int position) {
		holder.bindData(projectsList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return projectsList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Projects> list) {
		projectsList.clear();
		if (list != null) {
			projectsList.addAll(list);
		}
		notifyDataSetChanged();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void clearAdapter() {
		projectsList.clear();
		notifyDataSetChanged();
	}

	public class ProjectsHolder extends RecyclerView.ViewHolder {

		final ListProjectsBinding binding;

		ProjectsHolder(ListProjectsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos == RecyclerView.NO_POSITION) return;
						Projects projects = projectsList.get(pos);
						ProjectsContext projectCtx = new ProjectsContext(projects, context);
						projectCtx.saveToDB(context);
						Intent intent = projectCtx.getIntent(context, ProjectDetailActivity.class);
						context.startActivity(intent);
					});
		}

		void bindData(Projects projects) {
			binding.projectName.setText(projects.getName());
			binding.projectPath.setText(projects.getPathWithNamespace());

			if (projects.getVisibility() != null
					&& !"public".equalsIgnoreCase(projects.getVisibility())) {
				binding.visibilityIcon.setVisibility(View.VISIBLE);
			} else {
				binding.visibilityIcon.setVisibility(View.GONE);
			}

			if (projects.getAvatarUrl() != null
					&& "public".equalsIgnoreCase(projects.getVisibility())) {
				Glide.with(itemView.getContext())
						.load(projects.getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(binding.projectAvatar);
			} else {
				binding.projectAvatar.setImageDrawable(
						AvatarGenerator.getLetterAvatar(context, projects.getName(), 40));
			}

			if (projects.getDescription() != null && !projects.getDescription().isEmpty()) {
				binding.projectDescription.setVisibility(View.VISIBLE);
				binding.projectDescription.setText(projects.getDescription());
			} else {
				binding.projectDescription.setVisibility(View.GONE);
			}

			if ("starred".equalsIgnoreCase(source)) {
				binding.projectStarsIcon.setImageResource(R.drawable.ic_star_filled);
			}

			if (projects.isArchived()) {
				binding.archivedBadge.setVisibility(View.VISIBLE);
				int badgeColor =
						context.getResources()
								.getColor(R.color.alert_important_border, context.getTheme());
				binding.archivedBadge.setImageDrawable(
						AvatarGenerator.getLabelDrawable(context, "Archived", badgeColor, 18));
			} else {
				binding.archivedBadge.setVisibility(View.GONE);
			}

			binding.projectStars.setText(Utils.numberFormatter(projects.getStarCount()));
			binding.projectForks.setText(Utils.numberFormatter(projects.getForksCount()));
			binding.projectOpenIssues.setText(Utils.numberFormatter(projects.getOpenIssuesCount()));

			if (projects.getLastActivityAt() != null) {
				Date date = TimeHelper.parseIso8601(projects.getLastActivityAt());
				binding.projectUpdatedAt.setText(TimeHelper.formatTime(date));
				binding.projectUpdatedAt.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(date, Locale.getDefault())));
			}
		}
	}
}
