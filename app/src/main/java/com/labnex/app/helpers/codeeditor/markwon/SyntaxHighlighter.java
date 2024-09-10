package com.labnex.app.helpers.codeeditor.markwon;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.labnex.app.core.MainGrammarLocator;
import com.labnex.app.helpers.codeeditor.languages.Language;
import com.labnex.app.helpers.codeeditor.languages.LanguageElement;
import com.labnex.app.helpers.codeeditor.theme.Theme;
import io.noties.markwon.syntax.SyntaxHighlight;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author qwerty287
 */
public class SyntaxHighlighter implements SyntaxHighlight {

	private final Theme theme;
	private final Context context;
	private final String fallback;

	protected SyntaxHighlighter(Context context, @NonNull Theme theme, @Nullable String fallback) {
		this.context = context;
		this.theme = theme;
		this.fallback = fallback;
	}

	@NonNull public static SyntaxHighlighter create(Context context, @NonNull Theme theme) {
		return new SyntaxHighlighter(context, theme, null);
	}

	@NonNull public static SyntaxHighlighter create(
			Context context, @NonNull Theme theme, @Nullable String fallback) {
		return new SyntaxHighlighter(context, theme, fallback);
	}

	@NonNull @Override
	public CharSequence highlight(@Nullable String info, @NonNull String code) {
		if (code.isEmpty()) {
			return code;
		}

		if (info == null) {
			info = fallback;
		}

		if (info != null) {
			info = MainGrammarLocator.fromExtension(info);
		}

		Editable highlightedCode = new SpannableStringBuilder(code);

		Language l = Language.fromName(info);

		for (LanguageElement e : Objects.requireNonNull(LanguageElement.class.getEnumConstants())) {
			Pattern p = l.getPattern(e);
			if (p != null) {
				Matcher matcher = p.matcher(highlightedCode);
				while (matcher.find()) {
					highlightedCode.setSpan(
							new ForegroundColorSpan(
									context.getResources().getColor(theme.getColor(e), null)),
							matcher.start(),
							matcher.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		return highlightedCode;
	}
}
