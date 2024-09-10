package com.labnex.app.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.labnex.app.R;

/**
 * @author mmarif
 */
public class DiffParser {

	private final Context context;
	private final TextView diffTextView;
	private final String diff;
	private TextView fileStatistics;

	public DiffParser(
			Context context, TextView diffTextView, String diff, TextView fileStatistics) {
		this.context = context;
		this.diffTextView = diffTextView;
		this.diff = diff;
		this.fileStatistics = fileStatistics;
	}

	public DiffParser(Context context, TextView diffTextView, String diff) {
		this.context = context;
		this.diffTextView = diffTextView;
		this.diff = diff;
	}

	public void highlightDiffWithStats() {

		SpannableStringBuilder highlightedDiff = new SpannableStringBuilder();
		Paint paint = new Paint();

		String[] lines = diff.split("\n");

		boolean firstLine = true;

		int removedLineColor = ContextCompat.getColor(context, R.color.diff_removed_color);
		int addedLineColor = ContextCompat.getColor(context, R.color.diff_added_color);
		int textColor = ContextCompat.getColor(context, R.color.five_dark_white);

		int paddingStart = 40;
		int paddingEnd = 40;
		int marginTopBottom = 10;
		float maxLineWidth = 0;
		int removedLineCount = 0;
		int addedLineCount = 0;

		for (String line : lines) {
			float lineWidth = paint.measureText(line) + paddingStart + paddingEnd;
			maxLineWidth = Math.max(maxLineWidth, lineWidth);
		}

		if (maxLineWidth < 420) {
			maxLineWidth = 420;
		}

		for (String line : lines) {

			if (firstLine && line.startsWith("@@")) {
				firstLine = false;
				continue;
			}

			int start = highlightedDiff.length();
			highlightedDiff.append(line).append("\n");
			int end = highlightedDiff.length();

			if (line.startsWith("-")) {

				highlightedDiff.setSpan(
						new PaddedBackgroundSpan(
								paddingStart,
								paddingEnd,
								removedLineColor,
								textColor,
								maxLineWidth * 3.2F),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				highlightedDiff.setSpan(
						new CustomLineHeightSpan(marginTopBottom),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				removedLineCount++;
			} else if (line.startsWith("+")) {

				highlightedDiff.setSpan(
						new PaddedBackgroundSpan(
								paddingStart,
								paddingEnd,
								addedLineColor,
								textColor,
								maxLineWidth * 3.2F),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				highlightedDiff.setSpan(
						new CustomLineHeightSpan(marginTopBottom),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				addedLineCount++;
			} else {

				highlightedDiff.setSpan(
						new PaddingSpan(paddingStart, paddingEnd, maxLineWidth * 3.2F),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		diffTextView.setText(highlightedDiff);
		fileStatistics.setText(
				context.getString(
						R.string.diff_statistics,
						String.valueOf(addedLineCount),
						String.valueOf(removedLineCount)));
	}

	public void highlightDiffWithoutStats() {

		SpannableStringBuilder highlightedDiff = new SpannableStringBuilder();
		Paint paint = new Paint();

		String[] lines = diff.split("\n");

		boolean firstLine = true;

		int removedLineColor = ContextCompat.getColor(context, R.color.diff_removed_color);
		int addedLineColor = ContextCompat.getColor(context, R.color.diff_added_color);
		int textColor = ContextCompat.getColor(context, R.color.five_dark_white);

		int paddingStart = 40;
		int paddingEnd = 40;
		int marginTopBottom = 10;
		float maxLineWidth = 0;

		for (String line : lines) {
			float lineWidth = paint.measureText(line) + paddingStart + paddingEnd;
			maxLineWidth = Math.max(maxLineWidth, lineWidth);
		}

		if (maxLineWidth < 420) {
			maxLineWidth = 420;
		}

		for (String line : lines) {

			if (firstLine && line.startsWith("@@")) {
				firstLine = false;
				continue;
			}

			int start = highlightedDiff.length();
			highlightedDiff.append(line).append("\n");
			int end = highlightedDiff.length();

			if (line.startsWith("-")) {

				highlightedDiff.setSpan(
						new PaddedBackgroundSpan(
								paddingStart,
								paddingEnd,
								removedLineColor,
								textColor,
								maxLineWidth * 3.2F),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				highlightedDiff.setSpan(
						new CustomLineHeightSpan(marginTopBottom),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else if (line.startsWith("+")) {

				highlightedDiff.setSpan(
						new PaddedBackgroundSpan(
								paddingStart,
								paddingEnd,
								addedLineColor,
								textColor,
								maxLineWidth * 3.2F),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				highlightedDiff.setSpan(
						new CustomLineHeightSpan(marginTopBottom),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			} else {

				highlightedDiff.setSpan(
						new PaddingSpan(paddingStart, paddingEnd, maxLineWidth * 3.2F),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		diffTextView.setText(highlightedDiff);
	}

	private static class CustomLineHeightSpan implements LineHeightSpan {

		private final int marginTopBottom;

		public CustomLineHeightSpan(int marginTopBottom) {
			this.marginTopBottom = marginTopBottom;
		}

		@Override
		public void chooseHeight(
				CharSequence text,
				int start,
				int end,
				int spanStart,
				int v,
				Paint.FontMetricsInt fm) {
			fm.ascent -= marginTopBottom;
			fm.descent += marginTopBottom;
		}
	}

	private static class PaddedBackgroundSpan extends ReplacementSpan {

		private final int paddingStart;
		private final int paddingEnd;
		private final int backgroundColor;
		private final int textColor;
		private final float maxLineWidth;

		public PaddedBackgroundSpan(
				int paddingStart,
				int paddingEnd,
				int backgroundColor,
				int textColor,
				float maxLineWidth) {
			this.paddingStart = paddingStart;
			this.paddingEnd = paddingEnd;
			this.backgroundColor = backgroundColor;
			this.textColor = textColor;
			this.maxLineWidth = maxLineWidth;
		}

		@Override
		public int getSize(
				@NonNull Paint paint,
				CharSequence text,
				int start,
				int end,
				Paint.FontMetricsInt fm) {
			return (int) maxLineWidth;
		}

		@Override
		public void draw(
				Canvas canvas,
				CharSequence text,
				int start,
				int end,
				float x,
				int top,
				int y,
				int bottom,
				Paint paint) {
			// Draw the background across the maxLineWidth
			paint.setColor(backgroundColor);
			canvas.drawRect(x, top, x + maxLineWidth, bottom, paint);

			// Draw the text with start padding
			paint.setColor(textColor);
			canvas.drawText(text, start, end, x + paddingStart, y, paint);
		}
	}

	private static class PaddingSpan extends ReplacementSpan {

		private final int paddingStart;
		private final int paddingEnd;
		// private final int textColor;
		private final float maxLineWidth;

		public PaddingSpan(int paddingStart, int paddingEnd, float maxLineWidth) {
			this.paddingStart = paddingStart;
			this.paddingEnd = paddingEnd;
			// this.textColor = textColor;
			this.maxLineWidth = maxLineWidth;
		}

		@Override
		public int getSize(
				@NonNull Paint paint,
				CharSequence text,
				int start,
				int end,
				Paint.FontMetricsInt fm) {
			return (int) maxLineWidth;
		}

		@Override
		public void draw(
				Canvas canvas,
				CharSequence text,
				int start,
				int end,
				float x,
				int top,
				int y,
				int bottom,
				@NonNull Paint paint) {
			// paint.setColor(textColor);
			canvas.drawText(text, start, end, x + paddingStart, y, paint);
		}
	}
}
