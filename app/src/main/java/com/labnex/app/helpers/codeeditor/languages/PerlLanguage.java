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
public class PerlLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("#[^\\n]*");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_SINGLE_QUOTE_STRING = Pattern.compile("['](.*?)[']");

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
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"if",
			"else",
			"elsif",
			"unless",
			"while",
			"for",
			"foreach",
			"until",
			"do",
			"next",
			"last",
			"redo",
			"goto",
			"return",
			"sub",
			"my",
			"our",
			"local",
			"use",
			"require",
			"package",
			"bless",
			"new",
			"shift",
			"push",
			"pop",
			"undef",
			"defined",
			"delete",
			"exists",
			"die",
			"warn",
			"eval",
			"__FILE__",
			"__LINE__",
			"__PACKAGE__",
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
		return "Perl";
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
