package com.labnex.app.models.issues;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class TaskCompletionStatus implements Serializable {

	@SerializedName("count")
	private int count;

	@SerializedName("completed_count")
	private int completedCount;

	public int getCount() {
		return count;
	}

	public int getCompletedCount() {
		return completedCount;
	}
}
