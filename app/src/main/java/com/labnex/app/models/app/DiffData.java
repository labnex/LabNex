package com.labnex.app.models.app;

import android.text.SpannableStringBuilder;

/**
 * @author mmarif
 */
public class DiffData {

	private final SpannableStringBuilder highlightedDiff;
	private final String statisticsHtml;

	public DiffData(SpannableStringBuilder highlightedDiff, String statisticsHtml) {
		this.highlightedDiff = highlightedDiff;
		this.statisticsHtml = statisticsHtml;
	}

	public SpannableStringBuilder getHighlightedDiff() {
		return highlightedDiff;
	}

	public String getStatisticsHtml() {
		return statisticsHtml;
	}
}
