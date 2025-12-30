package com.labnex.app.helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author mmarif
 */
public class LabelStylingHelper {

	private static LabelStylingHelper instance;
	private final float density;
	private int callCount = 0;

	private LabelStylingHelper(Context context) {
		this.density = context.getResources().getDisplayMetrics().density;
	}

	public static synchronized LabelStylingHelper getInstance(Context context) {
		if (instance == null) {
			instance = new LabelStylingHelper(context.getApplicationContext());
		}
		return instance;
	}

	public static boolean isScopedLabel(String labelText) {
		return !TextUtils.isEmpty(labelText) && labelText.contains("::");
	}

	public void styleScopedLabel(
			String labelText,
			String colorHex,
			String textColorHex,
			TextView keyView,
			TextView valueView) {

		callCount++;
		String[] parts = labelText.split("::", 2);
		if (parts.length != 2) {
			return;
		}

		String key = parts[0].trim();
		String value = parts[1].trim();

		if (key.isEmpty() || value.isEmpty()) {
			return;
		}

		keyView.setText(key);
		valueView.setText(value);
		valueView.setVisibility(View.VISIBLE);

		try {
			int baseColor = Color.parseColor(Utils.repeatString(colorHex, 4, 1, 2));
			int textColor = Color.parseColor(Utils.repeatString(textColorHex, 4, 1, 2));

			float[] hsv = new float[3];
			Color.colorToHSV(baseColor, hsv);
			float hue = hsv[0];
			int valueBgColor = Color.HSVToColor(new float[] {hue, 0.15f, 0.95f});

			GradientDrawable keyBg = createLeftRoundedBackground(baseColor, dpToPx(18));
			GradientDrawable valueBg = createRightRoundedBackground(valueBgColor, dpToPx(18));

			keyView.setBackground(keyBg);
			valueView.setBackground(valueBg);

			keyView.setTextColor(textColor);
			valueView.setTextColor(Color.BLACK);

			int hPadding = dpToPx(12);
			int vPadding = dpToPx(6);
			keyView.setPadding(hPadding, vPadding, hPadding, vPadding);
			valueView.setPadding(hPadding, vPadding, hPadding, vPadding);

			removeMargins(keyView);
			removeMargins(valueView);

		} catch (Exception e) {
			keyView.setTextColor(Color.BLACK);
			valueView.setTextColor(Color.BLACK);
			keyView.setBackgroundColor(Color.LTGRAY);
			valueView.setBackgroundColor(Color.LTGRAY);
		}
	}

	public void styleRegularLabel(
			String labelText, String colorHex, String textColorHex, TextView labelView) {

		labelView.setText(labelText);

		try {
			int baseColor = Color.parseColor(Utils.repeatString(colorHex, 4, 1, 2));
			int textColor = Color.parseColor(Utils.repeatString(textColorHex, 4, 1, 2));

			GradientDrawable bg = new GradientDrawable();
			bg.setColor(baseColor);
			bg.setCornerRadius(dpToPx(18));

			labelView.setBackground(bg);
			labelView.setTextColor(textColor);

			int hPadding = dpToPx(12);
			int vPadding = dpToPx(6);
			labelView.setPadding(hPadding, vPadding, hPadding, vPadding);

		} catch (Exception e) {
			labelView.setTextColor(Color.BLACK);
			labelView.setBackgroundColor(Color.LTGRAY);
		}
	}

	private GradientDrawable createLeftRoundedBackground(int color, float radius) {
		GradientDrawable bg = new GradientDrawable();
		bg.setColor(color);
		bg.setCornerRadii(new float[] {radius, radius, 0, 0, 0, 0, radius, radius});
		return bg;
	}

	private GradientDrawable createRightRoundedBackground(int color, float radius) {
		GradientDrawable bg = new GradientDrawable();
		bg.setColor(color);
		bg.setCornerRadii(new float[] {0, 0, radius, radius, radius, radius, 0, 0});
		return bg;
	}

	private void removeMargins(TextView textView) {
		ViewGroup.LayoutParams params = textView.getLayoutParams();
		if (params instanceof ViewGroup.MarginLayoutParams marginParams) {
			marginParams.setMargins(0, 0, 0, 0);
			textView.setLayoutParams(marginParams);
		}
	}

	private int dpToPx(float dp) {
		return (int) (dp * density);
	}
}
