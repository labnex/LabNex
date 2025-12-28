package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.helpers.TimeUtils;
import com.labnex.app.models.todo.ToDoItem;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
	private final Context context;
	private List<ToDoItem> todoList;
	private final boolean isHomeScreen;
	private final OnTodoClickListener listener;

	public interface OnTodoClickListener {
		void onTodoClick(ToDoItem todo);

		void onTodoMarkAsDone(ToDoItem todo);
	}

	public TodoAdapter(
			Context context,
			List<ToDoItem> todoList,
			boolean isHomeScreen,
			OnTodoClickListener listener) {
		this.context = context;
		this.todoList = todoList;
		this.isHomeScreen = isHomeScreen;
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<ToDoItem> newList) {
		this.todoList = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

    @Override
	public int getItemViewType(int position) {
		return isHomeScreen ? 0 : 1;
	}

	@NonNull @Override
	public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());

		View view;
		if (viewType == 0) {
			// Home screen layout
			view = inflater.inflate(R.layout.list_todo, parent, false);
		} else {
			// Full list layout
			view = inflater.inflate(R.layout.list_todo_card, parent, false);
		}
		return new TodoViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
		ToDoItem todo = todoList.get(position);
		holder.bind(todo);
	}

	@Override
	public int getItemCount() {
		return todoList.size();
	}

	public class TodoViewHolder extends RecyclerView.ViewHolder {
		private final ImageView todoIcon;
		private final TextView todoTitle;
		private final TextView todoProject;
		private final TextView todoTimeAction;
		private ToDoItem currentTodo;

		TodoViewHolder(@NonNull View itemView) {
			super(itemView);

			todoIcon = itemView.findViewById(R.id.todo_icon);
			todoTitle = itemView.findViewById(R.id.todo_title);
			todoProject = itemView.findViewById(R.id.todo_project);
			todoTimeAction = itemView.findViewById(R.id.todo_time_action);
			ImageView todoCheck = itemView.findViewById(R.id.todo_check);

			itemView.setOnClickListener(
					v -> {
						if (listener != null && currentTodo != null) {
							listener.onTodoClick(currentTodo);
						}
					});

			if (todoCheck != null) {
				todoCheck.setOnClickListener(
						v -> {
							if (listener != null && currentTodo != null) {
								listener.onTodoMarkAsDone(currentTodo);
							}
						});
			}
		}

		void bind(ToDoItem todo) {
			currentTodo = todo;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			setIconForType(todo.getTargetType());

			String title = todo.getBody();
			if (todo.getTarget() != null && todo.getTarget().getTitle() != null) {
				title = todo.getTarget().getTitle();
			}
			todoTitle.setText(title);

			if (todo.getProject() != null) {
				String projectName = todo.getProject().getPathWithNamespace();
				todoProject.setText(projectName);

			} else {
				todoProject.setText("");
			}

			StringBuilder timeAction = new StringBuilder();

			if (todo.getCreatedAt() != null) {
				String modifiedTime =
						TimeUtils.formatTime(
								Date.from(OffsetDateTime.parse(todo.getCreatedAt()).toInstant()),
								locale);
				timeAction.append(modifiedTime);
			}

			if (todo.getActionName() != null) {
				if (timeAction.length() > 0) {
					timeAction.append(" • ");
				}
				timeAction.append(getActionNameText(todo.getActionName()));
			}

			todoTimeAction.setText(timeAction.toString());
		}

		private void setIconForType(String targetType) {
			int iconRes =
					switch (targetType != null ? targetType.toLowerCase() : "") {
						case "issuerequest", "issue" -> R.drawable.ic_issues;
						case "mergerequest" -> R.drawable.ic_merge_request;
						case "alert" -> R.drawable.ic_alert;
						case "design" -> R.drawable.ic_themes;
						case "epic" -> R.drawable.ic_epic;
						case "commit" -> R.drawable.ic_commits;
						default -> R.drawable.ic_list_details;
					};
			todoIcon.setImageResource(iconRes);
		}

		private String getActionNameText(String actionName) {
			return switch (actionName) {
				case "assigned" -> "Assigned";
				case "marked" -> "Marked";
				case "mentioned" -> "Mentioned";
				case "build_failed" -> "Build failed";
				case "approval_required" -> "Approval required";
				case "unmergeable" -> "Cannot be merged";
				case "directly_addressed" -> "Directly addressed";
				default -> actionName;
			};
		}
	}
}
