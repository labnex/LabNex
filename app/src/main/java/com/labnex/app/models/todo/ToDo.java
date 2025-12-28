package com.labnex.app.models.todo;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author mmarif
 */
public class ToDo {

	@SerializedName("ToDo")
	private List<ToDoItem> toDo;

	public List<ToDoItem> getToDo() {
		return toDo;
	}
}
