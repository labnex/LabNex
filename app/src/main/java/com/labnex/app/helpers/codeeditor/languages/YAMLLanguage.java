package com.labnex.app.helpers.codeeditor.languages;

import com.amrdeveloper.codeview.Code;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mmarif
 */
public class YAMLLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("#[^\\n]*");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");

	public static String getCommentStart() {
		return "#";
	}

	public static String getCommentEnd() {
		return "";
	}

	@Override
	public Pattern getPattern(LanguageElement element) {
		return switch (element) {
			case NUMBER -> PATTERN_NUMBERS;
			case STRING -> PATTERN_STRING;
			case SINGLE_LINE_COMMENT -> PATTERN_SINGLE_LINE_COMMENT;
			case TODO_COMMENT -> PATTERN_SINGLE_LINE_COMMENT;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {};
	}

	@Override
	public List<Code> getCodeList() {
		return new ArrayList<>();
	}

	@Override
	public String getName() {
		return "YAML";
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
