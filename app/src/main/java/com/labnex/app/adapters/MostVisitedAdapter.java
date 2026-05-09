package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.activities.ProjectDetailActivity;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.database.models.Projects;
import com.labnex.app.databinding.ListMostVisitedBinding;
import com.labnex.app.helpers.AvatarGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class MostVisitedAdapter
		extends RecyclerView.Adapter<MostVisitedAdapter.MostVisitedViewHolder> {

	private final Context ctx;
	private final List<Projects> list;
	private final OnRemoveClickListener removeListener;

	public interface OnRemoveClickListener {
		void onRemove(Projects project, int position);
	}

	public MostVisitedAdapter(
			Context ctx, List<Projects> list, OnRemoveClickListener removeListener) {
		this.ctx = ctx;
		this.list = new ArrayList<>(list);
		this.removeListener = removeListener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Projects> newList) {
		list.clear();
		list.addAll(newList);
		notifyDataSetChanged();
	}

	public void removeItem(int position) {
		list.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, list.size());
	}

	@NonNull @Override
	public MostVisitedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListMostVisitedBinding binding =
				ListMostVisitedBinding.inflate(LayoutInflater.from(ctx), parent, false);
		return new MostVisitedViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull MostVisitedViewHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class MostVisitedViewHolder extends RecyclerView.ViewHolder {

		final ListMostVisitedBinding binding;
		private Projects current;

		MostVisitedViewHolder(ListMostVisitedBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						if (current == null) return;
						ProjectsContext pc =
								new ProjectsContext(
										current.getProjectName(),
										current.getProjectPath(),
										current.getProjectId(),
										ctx);
						Intent intent = pc.getIntent(ctx, ProjectDetailActivity.class);
						intent.putExtra("source", "most_visited");
						ctx.startActivity(intent);
					});

			binding.btnRemove.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && removeListener != null) {
							removeListener.onRemove(current, pos);
						}
					});
		}

		void bind(Projects project) {
			current = project;
			binding.projectName.setText(project.getProjectName());
			binding.projectPath.setText(project.getProjectPath());
			binding.avatar.setImageDrawable(
					AvatarGenerator.getLetterAvatar(ctx, project.getProjectName(), 40));
			binding.visitCount.setText(String.valueOf(project.getMostVisited()));
		}
	}
}
