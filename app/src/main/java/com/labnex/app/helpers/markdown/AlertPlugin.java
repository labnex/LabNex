package com.labnex.app.helpers.markdown;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.ReplacementSpan;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import com.labnex.app.R;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.MarkwonVisitor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.commonmark.node.Block;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.node.Visitor;
import org.commonmark.parser.Parser;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockParserFactory;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

/**
 * @author mmarif
 */
public class AlertPlugin extends AbstractMarkwonPlugin {

	private static final String NOTE = "Note";
	private static final String TIP = "Tip";
	private static final String IMPORTANT = "Important";
	private static final String WARNING = "Warning";
	private static final String CAUTION = "Caution";
	private static final Pattern ALERT_PATTERN =
			Pattern.compile(
					"^>\\s*\\[!(NOTE|TIP|IMPORTANT|WARNING|CAUTION)]\\s*(.*)$",
					Pattern.CASE_INSENSITIVE);

	private final Context context;

	public static AlertPlugin create(Context context) {
		return new AlertPlugin(context);
	}

	private AlertPlugin(Context context) {
		this.context = context;
	}

	@Override
	public void configureParser(@NonNull Parser.Builder builder) {
		builder.customBlockParserFactory(new AlertBlockParserFactory(this));
	}

	@Override
	public void configureSpansFactory(@NonNull MarkwonSpansFactory.Builder builder) {
		builder.setFactory(AlertBlock.class, (configuration, props) -> new Object());
	}

	@Override
	public void configureVisitor(@NonNull MarkwonVisitor.Builder builder) {
		builder.on(
				AlertBlock.class,
				(visitor, alertBlock) -> {
					int start = visitor.length();
					String title = toTitleCase(alertBlock.getType());
					visitor.builder().append(title);
					applyAlertSpans(
							visitor.builder().spannableStringBuilder(),
							start,
							start + title.length(),
							alertBlock.getType());
					visitor.builder().append("\n");

					String content = alertBlock.getContent();
					if (content != null && !content.isEmpty()) {
						int contentStart = visitor.length();
						visitor.builder().append(content);
						int end = visitor.length();
						visitor.builder()
								.setSpan(
										new android.text.style.LeadingMarginSpan.Standard(40),
										contentStart,
										end,
										SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
						visitor.builder()
								.setSpan(
										new ForegroundColorSpan(
												ContextCompat.getColor(
														context, R.color.alert_text)),
										contentStart,
										end,
										SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
						visitor.builder()
								.setSpan(
										new AlertVerticalLineSpan(
												getBorderColor(alertBlock.getType()), context),
										start,
										end,
										SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					visitor.ensureNewLine();
				});
	}

	@Override
	public void afterSetText(@NonNull TextView textView) {
		SpannableStringBuilder builder = new SpannableStringBuilder(textView.getText());
		String[] types = {NOTE, TIP, IMPORTANT, WARNING, CAUTION};
		for (String type : types) {
			String titleMarker = "[!" + type.toUpperCase() + "]";
			int start = builder.toString().toUpperCase().indexOf(titleMarker);
			while (start >= 0) {
				int end = start + titleMarker.length();
				StringBuilder cleanedContent = new StringBuilder();
				int contentEnd = end;
				String text = builder.toString();
				while (contentEnd < text.length()) {
					int nextNewline = text.indexOf("\n", contentEnd);
					if (nextNewline == -1) nextNewline = text.length();
					String line = text.substring(contentEnd, nextNewline).trim();
					if (line.toUpperCase().startsWith("[!")) break;
					if (line.startsWith(">")) {
						String cleanedLine = line.substring(1).trim();
						if (!cleanedLine.isEmpty()) cleanedContent.append(cleanedLine).append("\n");
					} else if (!line.isEmpty()) {
						cleanedContent.append(line).append("\n");
					}
					contentEnd = nextNewline + 1;
				}
				if (cleanedContent.length() > 0
						&& cleanedContent.charAt(cleanedContent.length() - 1) == '\n') {
					cleanedContent.setLength(cleanedContent.length() - 1);
				}
				builder.replace(start, end, toTitleCase(type));
				applyAlertSpans(
						builder, start, start + toTitleCase(type).length(), type.toUpperCase());
				builder.insert(start + toTitleCase(type).length(), "\n\n");
				int newContentStart = start + toTitleCase(type).length() + 2;
				if (cleanedContent.length() > 0) {
					if (contentEnd > builder.length()) contentEnd = builder.length();
					int replaceEnd = Math.min(contentEnd, builder.length());
					builder.replace(newContentStart, replaceEnd, cleanedContent.toString());
					contentEnd = newContentStart + cleanedContent.length();
					if (contentEnd > builder.length()) contentEnd = builder.length();
					builder.setSpan(
							new android.text.style.LeadingMarginSpan.Standard(10),
							newContentStart,
							contentEnd,
							SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
					builder.setSpan(
							new AlertVerticalLineSpan(getBorderColor(type.toUpperCase()), context),
							start,
							contentEnd,
							SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				start = builder.toString().toUpperCase().indexOf(titleMarker, contentEnd);
			}
		}
		textView.setText(builder);
	}

	private String toTitleCase(String text) {
		if (text == null || text.isEmpty()) return text;
		String[] words = text.split("\\s+");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (!word.isEmpty()) {
				result.append(Character.toUpperCase(word.charAt(0)))
						.append(word.substring(1).toLowerCase())
						.append(" ");
			}
		}
		return result.toString().trim();
	}

	int getBorderColor(String type) {
		return switch (type.toUpperCase()) {
			case "TIP" -> ContextCompat.getColor(context, R.color.alert_tip_border);
			case "IMPORTANT" -> ContextCompat.getColor(context, R.color.alert_important_border);
			case "WARNING" -> ContextCompat.getColor(context, R.color.alert_warning_border);
			case "CAUTION" -> ContextCompat.getColor(context, R.color.alert_caution_border);
			default -> ContextCompat.getColor(context, R.color.alert_note_border);
		};
	}

	private static class TitleIconSpan extends ReplacementSpan {

		private final Drawable icon;
		private final String title;
		private final int color;
		private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		public TitleIconSpan(Drawable icon, String title, int color) {
			this.icon = icon;
			this.title = title;
			this.color = color;
		}

		@Override
		public int getSize(
				@NonNull Paint paint,
				CharSequence text,
				int start,
				int end,
				@Nullable Paint.FontMetricsInt fm) {
			this.paint.set(paint);
			this.paint.setTypeface(Typeface.create(paint.getTypeface(), Typeface.BOLD));
			this.paint.setColor(color);
			int textWidth = (int) this.paint.measureText(title);
			return icon.getBounds().width() + 10 + textWidth;
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
			int iconHeight = icon.getBounds().height();
			int iconWidth = icon.getBounds().width();
			int iconTop = top + (bottom - top - iconHeight) / 2;

			icon.setBounds((int) x, iconTop, (int) x + iconWidth, iconTop + iconHeight);
			icon.draw(canvas);

			this.paint.set(paint);
			this.paint.setTypeface(Typeface.create(paint.getTypeface(), Typeface.BOLD));
			this.paint.setColor(color);

			float textX = x + iconWidth + 20;
			canvas.drawText(title, textX, y, this.paint);
		}
	}

	void applyAlertSpans(SpannableStringBuilder builder, int start, int end, String type) {
		int iconRes;
		switch (type.toUpperCase()) {
			case "NOTE":
				iconRes = R.drawable.ic_info;
				break;
			case "TIP":
				iconRes = R.drawable.ic_tip;
				break;
			case "IMPORTANT":
				iconRes = R.drawable.ic_important;
				break;
			case "WARNING":
				iconRes = R.drawable.ic_warning;
				break;
			case "CAUTION":
				iconRes = R.drawable.ic_caution;
				break;
			default:
				return;
		}

		if (start < end && start >= 0) {
			Drawable icon = ContextCompat.getDrawable(context, iconRes);
			if (icon != null) {
				int sizeInDp = 20;
				float scale = context.getResources().getDisplayMetrics().density;
				int size = (int) (sizeInDp * scale + 0.5f);
				icon.setBounds(0, 0, size, size);
				DrawableCompat.setTint(icon, getBorderColor(type));
				builder.setSpan(
						new TitleIconSpan(icon, toTitleCase(type), getBorderColor(type)),
						start,
						end,
						SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	public static class AlertBlock extends Block {
		private final String type;
		private String content;

		public AlertBlock(String type, String content, AlertPlugin plugin) {
			this.type = type;
			this.content = content;
		}

		public String getType() {
			return type;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		@Override
		public void accept(Visitor visitor) {
			if (getFirstChild() == null) {
				appendChild(new Text("[!" + type + "]\n" + (content != null ? content : "")));
			}
			Node node = getFirstChild();
			while (node != null) {
				Node next = node.getNext();
				node.accept(visitor);
				node = next;
			}
		}
	}

	private static class AlertBlockParser extends AbstractBlockParser {
		private final AlertBlock block;
		private final StringBuilder content = new StringBuilder();
		private boolean isFinished = false;

		public AlertBlockParser(String type, String firstLine, AlertPlugin plugin) {
			this.block = new AlertBlock(type, "", plugin);
			if (firstLine != null && !firstLine.trim().isEmpty()) {
				content.append(firstLine.trim());
			}
		}

		@Override
		public Block getBlock() {
			if (isFinished) {
				block.setContent(content.toString().trim());
			}
			return block;
		}

		@Override
		public BlockContinue tryContinue(ParserState state) {
			if (isFinished) {
				return BlockContinue.none();
			}
			String line = state.getLine().toString();
			if (line.startsWith("> ")) {
				String contentLine = line.substring(2).trim();
				if (!contentLine.isEmpty()) {
					if (content.length() > 0) {
						content.append("\n");
					}
					content.append(contentLine);
				}
				return BlockContinue.atIndex(state.getIndex());
			} else if (line.trim().isEmpty()) {
				isFinished = true;
				block.setContent(content.toString().trim());
				return BlockContinue.none();
			} else {
				isFinished = true;
				block.setContent(content.toString().trim());
				return BlockContinue.none();
			}
		}

		@Override
		public void closeBlock() {
			if (!isFinished) {
				isFinished = true;
				block.setContent(content.toString().trim());
			}
		}
	}

	private record AlertBlockParserFactory(AlertPlugin plugin) implements BlockParserFactory {
		@Override
		public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
			String line = state.getLine().toString();
			Matcher matcher = ALERT_PATTERN.matcher(line);
			if (matcher.matches()) {
				String type = matcher.group(1);
				String content = matcher.group(2);
				return BlockStart.of(new AlertBlockParser(type, content, plugin))
						.atIndex(state.getIndex());
			}
			return BlockStart.none();
		}
	}

	private record AlertVerticalLineSpan(int color, Context context) implements LeadingMarginSpan {
		@Override
		public int getLeadingMargin(boolean first) {
			return 40;
		}

		@Override
		public void drawLeadingMargin(
				Canvas c,
				Paint p,
				int x,
				int dir,
				int top,
				int baseline,
				int bottom,
				CharSequence text,
				int start,
				int end,
				boolean first,
				Layout layout) {
			int originalColor = p.getColor();
			p.setColor(color);
			p.setStyle(Paint.Style.FILL);
			int lineCount = layout.getLineCount();
			int dynamicBottom = layout.getLineBottom(lineCount - 1);
			c.drawRect(x + dir, top, x + 10 * dir, dynamicBottom, p);
			p.setColor(originalColor);
		}
	}
}
