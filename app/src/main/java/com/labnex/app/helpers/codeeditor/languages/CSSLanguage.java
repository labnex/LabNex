package com.labnex.app.helpers.codeeditor.languages;

import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.Keyword;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mmarif
 */
public class CSSLanguage extends Language {

	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("#[0-9a-fA-F]{3,8}");
	private static final Pattern PATTERN_CLASS = Pattern.compile("\\.[a-zA-Z-_][a-zA-Z0-9-_]*");

	public static String getCommentStart() {
		return "/*";
	}

	public static String getCommentEnd() {
		return "*/";
	}

	@Override
	public Pattern getPattern(LanguageElement element) {
		return switch (element) {
			case KEYWORD -> Pattern.compile("\\b(" + String.join("|", getKeywords()) + ")\\b");
			case NUMBER -> PATTERN_NUMBERS;
			case STRING -> PATTERN_STRING;
			case HEX -> PATTERN_HEX;
			case MULTI_LINE_COMMENT -> PATTERN_MULTI_LINE_COMMENT;
			case ATTRIBUTE -> PATTERN_CLASS;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"align-content",
			"align-items",
			"align-self",
			"animation",
			"background",
			"border",
			"bottom",
			"box-shadow",
			"color",
			"cursor",
			"display",
			"filter",
			"flex",
			"flex-direction",
			"font",
			"font-size",
			"font-weight",
			"gap",
			"grid",
			"height",
			"justify-content",
			"left",
			"margin",
			"max-height",
			"max-width",
			"min-height",
			"min-width",
			"opacity",
			"overflow",
			"padding",
			"position",
			"right",
			"text-align",
			"text-decoration",
			"top",
			"transform",
			"transition",
			"visibility",
			"width",
			"z-index",
		};
	}

	@Override
	public List<Code> getCodeList() {
		List<Code> codeList = new ArrayList<>();
		for (String keyword : getKeywords()) codeList.add(new Keyword(keyword));
		return codeList;
	}

	@Override
	public String getName() {
		return "CSS";
	}

	@Override
	public Set<Character> getIndentationStarts() {
		Set<Character> set = new HashSet<>();
		set.add('{');
		return set;
	}

	@Override
	public Set<Character> getIndentationEnds() {
		Set<Character> set = new HashSet<>();
		set.add('}');
		return set;
	}
}
