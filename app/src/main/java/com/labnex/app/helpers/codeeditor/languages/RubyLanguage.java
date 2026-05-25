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
public class RubyLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("#[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("=begin[\\s\\S]*?=end");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_SINGLE_QUOTE_STRING = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_SYMBOL = Pattern.compile(":[a-zA-Z_][a-zA-Z0-9_]*");
	private static final Pattern PATTERN_TODO_COMMENT =
			Pattern.compile("#\\s?(TODO|todo|FIXME|fixme|HACK|hack)\\s[^\n]*");

	public static String getCommentStart() {
		return "#";
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
			case ATTRIBUTE -> PATTERN_SYMBOL;
			case TODO_COMMENT -> PATTERN_TODO_COMMENT;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"BEGIN",
			"END",
			"alias",
			"and",
			"begin",
			"break",
			"case",
			"class",
			"def",
			"defined?",
			"do",
			"else",
			"elsif",
			"end",
			"ensure",
			"false",
			"for",
			"if",
			"in",
			"module",
			"next",
			"nil",
			"not",
			"or",
			"redo",
			"rescue",
			"retry",
			"return",
			"self",
			"super",
			"then",
			"true",
			"undef",
			"unless",
			"until",
			"when",
			"while",
			"yield",
			"__FILE__",
			"__LINE__",
			"__ENCODING__",
			"attr_accessor",
			"attr_reader",
			"attr_writer",
			"include",
			"extend",
			"prepend",
			"private",
			"protected",
			"public",
			"require",
			"load",
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
		return "Ruby";
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
