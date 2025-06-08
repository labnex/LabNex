package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.activities.ProjectDetailActivity;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.database.models.Projects;
import com.labnex.app.helpers.TextDrawable.TextDrawable;
import java.util.List;

/**
 * @author mmarif
 */
public class MostVisitedAdapter
		extends RecyclerView.Adapter<MostVisitedAdapter.MostVisitedViewHolder> {

	private final List<Projects> mostVisitedList;
	private final Context ctx;

	public MostVisitedAdapter(Context ctx, List<Projects> projectsList) {
		this.ctx = ctx;
		this.mostVisitedList = projectsList;
	}

	public static class MostVisitedViewHolder extends RecyclerView.ViewHolder {

		private Projects projects;

		private final ImageView avatar;
		private final TextView projectName;
		private final TextView projectPath;

		private MostVisitedViewHolder(View itemView) {

			super(itemView);

			avatar = itemView.findViewById(R.id.avatar);
			projectName = itemView.findViewById(R.id.project_name);
			projectPath = itemView.findViewById(R.id.project_path);

			itemView.setOnClickListener(
					v -> {
						Context context = v.getContext();
						ProjectsContext project =
								new ProjectsContext(
										projects.getProjectName(),
										projects.getProjectPath(),
										projects.getProjectId(),
										context);
						Intent intent = project.getIntent(context, ProjectDetailActivity.class);
						intent.putExtra("source", "most_visited");
						context.startActivity(intent);
					});
		}
	}

	@NonNull @Override
	public MostVisitedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_most_visited, parent, false);
		return new MostVisitedViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull MostVisitedViewHolder holder, int position) {

		Projects currentItem = mostVisitedList.get(position);
		holder.projects = currentItem;

		TypedValue typedValue = new TypedValue();
		ctx.getTheme().resolveAttribute(R.attr.homeIconsBackgroundColor, typedValue, true);
		@ColorInt int backgroundColor = typedValue.data;
		int textColor = getColorFromAttr(ctx, R.attr.iconsColor);

		String firstCharacter = "P";
		if (currentItem.getProjectName() != null && !currentItem.getProjectName().isEmpty()) {
			firstCharacter = String.valueOf(currentItem.getProjectName().charAt(0));
		}

		TextDrawable drawable =
				TextDrawable.builder()
						.beginConfig()
						.useFont(Typeface.DEFAULT)
						.textColor(textColor)
						.fontSize(16)
						.toUpperCase()
						.width(26)
						.height(26)
						.endConfig()
						.buildRoundRect(firstCharacter, backgroundColor, 9);

		holder.avatar.setImageDrawable(drawable);
		holder.projectName.setText(currentItem.getProjectName());
		holder.projectPath.setText(currentItem.getProjectPath());
	}

	@Override
	public int getItemCount() {
		return mostVisitedList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
	}

	private int getColorFromAttr(Context context, int attr) {
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(attr, typedValue, true);
		return typedValue.data;
	}
}
