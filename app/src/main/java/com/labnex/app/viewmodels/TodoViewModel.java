package com.labnex.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.models.todo.ToDoItem;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class TodoViewModel extends ViewModel {

	private final MutableLiveData<List<ToDoItem>> todoList =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Long> removedTodoId = new MutableLiveData<>();

	public LiveData<List<ToDoItem>> getTodoList() {
		return todoList;
	}

	public LiveData<Long> getRemovedTodoId() {
		return removedTodoId;
	}

	public void setTodoList(List<ToDoItem> items) {
		todoList.setValue(items);
	}

	public void removeTodo(long todoId) {
		List<ToDoItem> current = todoList.getValue();
		if (current != null) {
			current.removeIf(todo -> todo.getId() == todoId);
			todoList.setValue(current);
			removedTodoId.setValue(todoId);
		}
	}

	public void clearRemovedTodo() {
		removedTodoId.setValue(null);
	}
}
