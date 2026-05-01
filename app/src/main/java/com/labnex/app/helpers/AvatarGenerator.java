package com.labnex.app.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * @author mmarif
 */
public class AvatarGenerator {

	private static final int[] MATERIAL_COLORS = {
		0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
		0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
		0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
		0xFFFFC107, 0xFFFF9800, 0xFFFF5722
	};

	public static Drawable getLetterAvatar(Context context, String label, int sizeDp) {
		if (label == null || label.trim().isEmpty()) {
			label = "?";
		}

		String trimmed = label.trim();
		String firstChar = extractFirstValidChar(trimmed);

		int color = getColorForString(label);

		float density = context.getResources().getDisplayMetrics().density;
		int sizePx = (int) (sizeDp * density);
		int cornerRadius = (int) (8 * density);

		Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgPaint.setColor(color);
		canvas.drawRoundRect(0, 0, sizePx, sizePx, cornerRadius, cornerRadius, bgPaint);

		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(ColorInverter.getContrastColor(color));
		textPaint.setTextSize(sizePx * 0.55f);
		textPaint.setTextAlign(Paint.Align.CENTER);

		Rect bounds = new Rect();
		textPaint.getTextBounds(firstChar, 0, 1, bounds);
		float x = sizePx / 2f;
		float y = (sizePx / 2f) - bounds.exactCenterY();

		canvas.drawText(firstChar, x, y, textPaint);

		return new BitmapDrawable(context.getResources(), bitmap);
	}

	public static Drawable getLabelDrawable(
			Context context, String labelName, int color, int heightDp) {
		if (labelName == null) labelName = "";

		float density = context.getResources().getDisplayMetrics().density;
		int heightPx = (int) (heightDp * density);
		int paddingPx = (int) (10 * density);
		int cornerRadius = (int) (16 * density);
		int textSizePx = (int) (heightPx * 0.6f);

		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		textPaint.setColor(ColorInverter.getContrastColor(color));
		textPaint.setTextSize(textSizePx);
		textPaint.setTextAlign(Paint.Align.CENTER);

		Rect textBounds = new Rect();
		textPaint.getTextBounds(labelName, 0, labelName.length(), textBounds);
		int widthPx = textBounds.width() + (paddingPx * 2);

		Bitmap bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgPaint.setColor(color);
		canvas.drawRoundRect(0, 0, widthPx, heightPx, cornerRadius, cornerRadius, bgPaint);

		float x = widthPx / 2f;
		float y = (heightPx / 2f) - textBounds.exactCenterY();
		canvas.drawText(labelName, x, y, textPaint);

		return new BitmapDrawable(context.getResources(), bitmap);
	}

	public static Drawable getCircleColorDrawable(Context context, int color, int sizeDp) {
		float density = context.getResources().getDisplayMetrics().density;
		int sizePx = (int) (sizeDp * density);

		Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color);

		float center = sizePx / 2f;
		canvas.drawCircle(center, center, center, paint);

		return new BitmapDrawable(context.getResources(), bitmap);
	}

	private static int getColorForString(String key) {
		int hash = Math.abs(key.hashCode());
		return MATERIAL_COLORS[hash % MATERIAL_COLORS.length];
	}

	private static String extractFirstValidChar(String text) {
		if (text == null || text.isEmpty()) {
			return "?";
		}

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
				return String.valueOf(c).toUpperCase();
			}
		}

		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c >= '0' && c <= '9') {
				return String.valueOf(c);
			}
		}

		return String.valueOf(text.charAt(0)).toUpperCase();
	}
}
