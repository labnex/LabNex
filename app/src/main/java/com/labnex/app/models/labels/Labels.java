package com.labnex.app.models.labels;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Labels implements Serializable {

	@SerializedName("subscribed")
	private boolean subscribed;

	@SerializedName("color")
	private String color;

	@SerializedName("open_issues_count")
	private int openIssuesCount;

	@SerializedName("name")
	private String name;

	@SerializedName("open_merge_requests_count")
	private int openMergeRequestsCount;

	@SerializedName("description")
	private Object description;

	@SerializedName("closed_issues_count")
	private int closedIssuesCount;

	@SerializedName("id")
	private int id;

	@SerializedName("text_color")
	private String textColor;

	@SerializedName("description_html")
	private Object descriptionHtml;

	@SerializedName("priority")
	private String priority;

	@SerializedName("is_project_label")
	private boolean is_project_label;

	public boolean isSubscribed() {
		return subscribed;
	}

	public String getColor() {
		return color;
	}

	public int getOpenIssuesCount() {
		return openIssuesCount;
	}

	public String getName() {
		return name;
	}

	public int getOpenMergeRequestsCount() {
		return openMergeRequestsCount;
	}

	public Object getDescription() {
		return description;
	}

	public int getClosedIssuesCount() {
		return closedIssuesCount;
	}

	public int getId() {
		return id;
	}

	public String getTextColor() {
		return textColor;
	}

	public Object getDescriptionHtml() {
		return descriptionHtml;
	}

	public String getPriority() {
		return priority;
	}

	public boolean isIs_project_label() {
		return is_project_label;
	}
}
