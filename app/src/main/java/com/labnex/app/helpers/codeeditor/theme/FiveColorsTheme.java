package com.labnex.app.helpers.codeeditor.theme;

import static com.labnex.app.helpers.codeeditor.languages.LanguageElement.HEX;

import androidx.annotation.ColorRes;
import com.labnex.app.R;
import com.labnex.app.helpers.codeeditor.languages.LanguageElement;

/**
 * @author qwerty287
 * @author mmarif
 */
public class FiveColorsTheme implements Theme {

	@Override
	@ColorRes
	public int getColor(LanguageElement element) {
		return switch (element) {
			case HEX, NUMBER, KEYWORD, OPERATION, GENERIC -> R.color.five_dark_purple;
			case CHAR, STRING -> R.color.five_yellow;
			case SINGLE_LINE_COMMENT, MULTI_LINE_COMMENT -> R.color.five_dark_grey;
			case ATTRIBUTE, TODO_COMMENT, ANNOTATION -> R.color.five_dark_blue;
			default -> R.color.five_dark_black;
		};
	}

	@Override
	@ColorRes
	public int getDefaultColor() {
		return R.color.five_dark_black;
	}

	@Override
	@ColorRes
	public int getBackgroundColor() {
		return R.color.five_background_grey;
	}
}
