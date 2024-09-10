package com.labnex.app.helpers.codeeditor.markwon;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.labnex.app.helpers.codeeditor.theme.Theme;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.core.MarkwonTheme;

/**
 * @author qwerty287
 */
public class MarkwonHighlighter extends AbstractMarkwonPlugin {

	private final Theme theme;
	private final Context context;
	private final String fallbackLanguage;

	public MarkwonHighlighter(
			Context context, @NonNull Theme theme, @Nullable String fallbackLanguage) {
		this.theme = theme;
		this.context = context;
		this.fallbackLanguage = fallbackLanguage;
	}

	@NonNull public static MarkwonHighlighter create(Context context, @NonNull Theme theme) {
		return create(context, theme, null);
	}

	@NonNull public static MarkwonHighlighter create(
			Context context, @NonNull Theme theme, @Nullable String fallbackLanguage) {
		return new MarkwonHighlighter(context, theme, fallbackLanguage);
	}

	@Override
	public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
		builder.codeTextColor(context.getResources().getColor(theme.getDefaultColor(), null))
				.codeBackgroundColor(
						context.getResources().getColor(theme.getBackgroundColor(), null));
	}

	@Override
	public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
		builder.syntaxHighlight(SyntaxHighlighter.create(context, theme, fallbackLanguage));
	}
}
