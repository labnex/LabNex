package com.labnex.app.helpers;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import android.util.Pair;
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
	private final TextView fileStatistics;

	public DiffParser(
			Context context, TextView diffTextView, String diff, TextView fileStatistics) {
		this.context = context;
		this.diffTextView = diffTextView;
		this.diff = diff;
		this.fileStatistics = fileStatistics;
	}

	public void highlightDiffWithStats() {
		if (diff == null) return;
		Pair<SpannableStringBuilder, SpannableStringBuilder> parsedOutput =
				parseWorker(context, diff, diffTextView.getTextSize());

		diffTextView.setText(parsedOutput.first);
		if (fileStatistics != null) {
			fileStatistics.setText(parsedOutput.second);
		}
	}

	public static Pair<SpannableStringBuilder, SpannableStringBuilder> parseInBackground(
			Context context, String rawDiff, float textViewTextSizePx) {
		return parseWorker(context, rawDiff, textViewTextSizePx);
	}

	private static Pair<SpannableStringBuilder, SpannableStringBuilder> parseWorker(
			Context context, String diff, float textSizePx) {
		if (diff == null) {
			return new Pair<>(new SpannableStringBuilder(""), new SpannableStringBuilder(""));
		}

		SpannableStringBuilder highlightedDiff = new SpannableStringBuilder();

		Paint paint = new Paint();
		paint.setTypeface(Typeface.MONOSPACE);
		paint.setTextSize(textSizePx);

		String[] lines = diff.split("\n");
		boolean firstLine = true;

		int removedLineColor = ContextCompat.getColor(context, R.color.diff_removed_color);
		int addedLineColor = ContextCompat.getColor(context, R.color.diff_added_color);

		android.util.TypedValue typedValue = new android.util.TypedValue();
		context.getTheme()
				.resolveAttribute(
						com.google.android.material.R.attr.colorOnSurface, typedValue, true);
		int textColor = typedValue.data;

		int paddingStart = 40;
		int paddingEnd = 40;
		int marginTopBottom = 12;

		float maxLineWidth = 0;
		for (String line : lines) {
			float lineWidth = paint.measureText(line) + paddingStart + paddingEnd;
			maxLineWidth = Math.max(maxLineWidth, lineWidth);
		}

		if (maxLineWidth < 420) {
			maxLineWidth = 420;
		}

		int removedLineCount = 0;
		int addedLineCount = 0;

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
								paddingStart, removedLineColor, textColor, maxLineWidth),
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
								paddingStart, addedLineColor, textColor, maxLineWidth),
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
						new PaddingSpan(paddingStart, maxLineWidth),
						start,
						end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}

		SpannableStringBuilder statsBuilder = new SpannableStringBuilder();

		String addedText = "+" + addedLineCount;
		int addedStart = statsBuilder.length();
		statsBuilder.append(addedText);
		statsBuilder.setSpan(
				new ForegroundColorSpan(0xFF4CAF50),
				addedStart,
				statsBuilder.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		statsBuilder.append("  ");

		String removedText = "-" + removedLineCount;
		int removedStart = statsBuilder.length();
		statsBuilder.append(removedText);
		statsBuilder.setSpan(
				new ForegroundColorSpan(0xFFF44336),
				removedStart,
				statsBuilder.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		return new Pair<>(highlightedDiff, statsBuilder);
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
		private final int backgroundColor;
		private final int textColor;
		private final float maxLineWidth;

		public PaddedBackgroundSpan(
				int paddingStart, int backgroundColor, int textColor, float maxLineWidth) {
			this.paddingStart = paddingStart;
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
			return (int) (paint.measureText(text, start, end) + (paddingStart * 2));
		}

		@Override
		public void draw(
				@NonNull Canvas canvas,
				CharSequence text,
				int start,
				int end,
				float x,
				int top,
				int y,
				int bottom,
				@NonNull Paint paint) {
			float drawWidth = Math.max(canvas.getWidth(), maxLineWidth);

			paint.setColor(backgroundColor);
			canvas.drawRect(x, top, x + drawWidth, bottom, paint);

			paint.setColor(textColor);
			paint.setTypeface(Typeface.MONOSPACE);
			canvas.drawText(text, start, end, x + paddingStart, y, paint);
		}
	}

	private static class PaddingSpan extends ReplacementSpan {
		private final int paddingStart;

		public PaddingSpan(int paddingStart, float maxLineWidth) {
			this.paddingStart = paddingStart;
		}

		@Override
		public int getSize(
				@NonNull Paint paint,
				CharSequence text,
				int start,
				int end,
				Paint.FontMetricsInt fm) {
			return (int) (paint.measureText(text, start, end) + (paddingStart * 2));
		}

		@Override
		public void draw(
				@NonNull Canvas canvas,
				CharSequence text,
				int start,
				int end,
				float x,
				int top,
				int y,
				int bottom,
				@NonNull Paint paint) {
			paint.setTypeface(Typeface.MONOSPACE);
			canvas.drawText(text, start, end, x + paddingStart, y, paint);
		}
	}
}
