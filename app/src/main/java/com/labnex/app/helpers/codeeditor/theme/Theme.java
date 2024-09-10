package com.labnex.app.helpers.codeeditor.theme;

import android.content.Context;
import androidx.annotation.ColorRes;
import com.labnex.app.helpers.codeeditor.languages.LanguageElement;

/**
 * @author qwerty287
 * @author mmarif
 */
public interface Theme {

	FiveColorsTheme FIVE_COLORS = new FiveColorsTheme();
	FiveColorsDarkTheme FIVE_COLORS_DARK = new FiveColorsDarkTheme();
	BlueMoonTheme BLUE_MOON_THEME = new BlueMoonTheme();
	BlueMoonDarkTheme BLUE_MOON_DARK_THEME = new BlueMoonDarkTheme();

	static Theme getDefaultTheme(Context context) {

		return FIVE_COLORS_DARK;

		/*if (Integer.parseInt(
				AppSettingsInit.getSettingsValue(
						context, AppSettingsInit.APP_THEME_KEY))
				== 0) {
			return Utils.getColorFromAttribute(context, R.attr.isDark) == 1
					? BLUE_MOON_DARK_THEME
					: BLUE_MOON_THEME;
		} else {
			return Utils.getColorFromAttribute(context, R.attr.isDark) == 1
					? FIVE_COLORS_DARK
					: FIVE_COLORS;
		}*/
	}

	@ColorRes
	int getColor(LanguageElement element);

	@ColorRes
	int getDefaultColor();

	@ColorRes
	int getBackgroundColor();
}
