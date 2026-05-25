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
public class LuaLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("--[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("--\\[\\[[\\s\\S]*?\\]\\]");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_SINGLE_QUOTE_STRING = Pattern.compile("['](.*?)[']");

	public static String getCommentStart() {
		return "--";
	}

	public static String getCommentEnd() {
		return "";
	}

	@Override
	public Pattern getPattern(LanguageElement element) {
		return switch (element) {
			case KEYWORD -> Pattern.compile("\\b(" + String.join("|", getKeywords()) + ")\\b");
			case NUMBER -> PATTERN_NUMBERS;
			case STRING -> PATTERN_STRING;
			case CHAR -> PATTERN_SINGLE_QUOTE_STRING;
			case SINGLE_LINE_COMMENT -> PATTERN_SINGLE_LINE_COMMENT;
			case MULTI_LINE_COMMENT -> PATTERN_MULTI_LINE_COMMENT;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"and", "break", "do", "else", "elseif", "end", "false", "for", "function",
			"goto", "if", "in", "local", "nil", "not", "or", "repeat", "return",
			"then", "true", "until", "while",
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
		return "Lua";
	}

	@Override
	public Set<Character> getIndentationStarts() {
		return new HashSet<>();
	}

	@Override
	public Set<Character> getIndentationEnds() {
		return new HashSet<>();
	}
}
