package com.labnex.app.helpers;

import android.graphics.Color;
import androidx.annotation.ColorInt;

/**
 * @author mmarif
 */
public class ColorInverter {

	@ColorInt
	public static int getContrastColor(@ColorInt int color) {
		double luminance =
				(0.2126 * Color.red(color)
								+ 0.7152 * Color.green(color)
								+ 0.0722 * Color.blue(color))
						/ 255;

		double a = 1 - luminance;

		if (a < 0.30) {
			return Color.rgb(31, 31, 31);
		} else {
			return Color.WHITE;
		}
	}
}
