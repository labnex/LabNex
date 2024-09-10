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
		switch (element) {
			case HEX:
			case NUMBER:
			case KEYWORD:
			case OPERATION:
			case GENERIC:
				return R.color.five_dark_purple;
			case CHAR:
			case STRING:
				return R.color.five_yellow;
			case SINGLE_LINE_COMMENT:
			case MULTI_LINE_COMMENT:
				return R.color.five_dark_grey;
			case ATTRIBUTE:
			case TODO_COMMENT:
			case ANNOTATION:
				return R.color.five_dark_blue;
			default:
				return R.color.five_dark_black;
		}
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
