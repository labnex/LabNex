package com.labnex.app.helpers.codeeditor.theme;

import androidx.annotation.ColorRes;
import com.labnex.app.R;
import com.labnex.app.helpers.codeeditor.languages.LanguageElement;

/**
 * @author mmarif
 */
public class BlueMoonTheme implements Theme {

	@Override
	@ColorRes
	public int getColor(LanguageElement element) {
		return switch (element) {
			case HEX, NUMBER, KEYWORD, OPERATION, GENERIC -> R.color.moon_dark_blue;
			case CHAR, STRING -> R.color.moon_dark_turquoise;
			case SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT -> R.color.moon_dark_grey;
			case ATTRIBUTE, TODO_COMMENT, ANNOTATION -> R.color.moon_deep_sky_blue;
			default -> R.color.moon_dark_black;
		};
	}

	@Override
	@ColorRes
	public int getDefaultColor() {
		return R.color.moon_dark_black;
	}

	@Override
	@ColorRes
	public int getBackgroundColor() {
		return R.color.moon_background_grey;
	}
}
