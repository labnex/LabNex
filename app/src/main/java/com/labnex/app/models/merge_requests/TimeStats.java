package com.labnex.app.models.merge_requests;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class TimeStats implements Serializable {

	@SerializedName("time_estimate")
	private int timeEstimate;

	@SerializedName("total_time_spent")
	private int totalTimeSpent;

	@SerializedName("human_time_estimate")
	private Object humanTimeEstimate;

	@SerializedName("human_total_time_spent")
	private Object humanTotalTimeSpent;

	public int getTimeEstimate() {
		return timeEstimate;
	}

	public int getTotalTimeSpent() {
		return totalTimeSpent;
	}

	public Object getHumanTimeEstimate() {
		return humanTimeEstimate;
	}

	public Object getHumanTotalTimeSpent() {
		return humanTotalTimeSpent;
	}
}
