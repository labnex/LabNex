package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.databinding.ListTodoBinding;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.todo.ToDoItem;
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
	private final OnTodoClickListener listener;
	private String currentState = "pending";

	public interface OnTodoClickListener {
		void onTodoClick(ToDoItem todo);

		void onTodoMarkAsDone(ToDoItem todo);
	}

	public TodoAdapter(Context context, List<ToDoItem> todoList, OnTodoClickListener listener) {
		this.context = context;
		this.todoList = new ArrayList<>(todoList);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<ToDoItem> newList) {
		this.todoList = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setCurrentState(String state) {
		this.currentState = state;
		notifyDataSetChanged();
	}

	@NonNull @Override
	public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListTodoBinding binding =
				ListTodoBinding.inflate(LayoutInflater.from(context), parent, false);
		return new TodoViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
		holder.bind(todoList.get(position), currentState);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return todoList.size();
	}

	public class TodoViewHolder extends RecyclerView.ViewHolder {

		ListTodoBinding binding;
		private ToDoItem currentTodo;

		TodoViewHolder(ListTodoBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						if (listener != null && currentTodo != null) {
							listener.onTodoClick(currentTodo);
						}
					});

			binding.todoCheck.setOnClickListener(
					v -> {
						if (listener != null && currentTodo != null) {
							listener.onTodoMarkAsDone(currentTodo);
						}
					});
		}

		void bind(ToDoItem todo, String state) {
			currentTodo = todo;

			setupIcon(todo);

			String title = todo.getBody();
			if (todo.getTarget() != null && todo.getTarget().getTitle() != null) {
				title = todo.getTarget().getTitle();
			}
			binding.todoTitle.setText(title);

			binding.todoProject.setText(
					todo.getProject() != null ? todo.getProject().getPathWithNamespace() : "");

			StringBuilder sb = new StringBuilder();
			if (todo.getCreatedAt() != null) {
				Date date = TimeHelper.parseIso8601(todo.getCreatedAt());
				sb.append(TimeHelper.formatTime(date));
				binding.todoTimeAction.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(date, Locale.getDefault())));
			}
			if (todo.getActionName() != null) {
				if (sb.length() > 0) sb.append(" • ");
				sb.append(actionLabel(todo.getActionName()));
			}
			binding.todoTimeAction.setText(sb.toString());

			if ("done".equals(state)) {
				binding.todoCheck.setVisibility(View.GONE);
			} else {
				binding.todoCheck.setVisibility(View.VISIBLE);
			}
		}

		private void setupIcon(ToDoItem todo) {
			if (todo == null) return;

			int iconRes = iconForType(todo.getTargetType());
			binding.todoIcon.setImageResource(iconRes);

			int tintColor = Utils.getColorFromAttribute(context, R.attr.iconsColor);

			if (todo.getTarget() != null && todo.getTarget().getState() != null) {
				String type = todo.getTarget().getState();
				if (type.equalsIgnoreCase("closed")) {
					tintColor = context.getColor(R.color.alert_caution_border);
				} else if (type.equalsIgnoreCase("merged")) {
					tintColor = context.getColor(R.color.alert_important_border);
				}
			}

			binding.todoIcon.setImageTintList(ColorStateList.valueOf(tintColor));
		}

		private int iconForType(String type) {
			return switch (type.toLowerCase(Locale.getDefault())) {
				case "issue", "issuerequest" -> R.drawable.ic_issues;
				case "mergerequest" -> R.drawable.ic_merge_request;
				case "alert" -> R.drawable.ic_alert;
				case "design" -> R.drawable.ic_themes;
				case "epic" -> R.drawable.ic_epic;
				case "commit" -> R.drawable.ic_commits;
				default -> R.drawable.ic_list_details;
			};
		}

		private String actionLabel(String action) {
			return switch (action) {
				case "assigned" -> "Assigned";
				case "marked" -> "Marked";
				case "mentioned" -> "Mentioned";
				case "build_failed" -> "Build failed";
				case "approval_required" -> "Approval required";
				case "unmergeable" -> "Cannot be merged";
				case "directly_addressed" -> "Directly addressed";
				default -> action;
			};
		}
	}
}
