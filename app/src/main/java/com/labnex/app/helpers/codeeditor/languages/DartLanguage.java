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
public class DartLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_SINGLE_QUOTE_STRING = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_ANNOTATION = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
	private static final Pattern PATTERN_TODO_COMMENT =
			Pattern.compile("//\\s?(TODO|todo|FIXME|fixme)\\s[^\n]*");

	public static String getCommentStart() {
		return "//";
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
			case ANNOTATION -> PATTERN_ANNOTATION;
			case TODO_COMMENT -> PATTERN_TODO_COMMENT;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"abstract",
			"as",
			"assert",
			"async",
			"await",
			"base",
			"break",
			"case",
			"catch",
			"class",
			"const",
			"continue",
			"covariant",
			"default",
			"deferred",
			"do",
			"dynamic",
			"else",
			"enum",
			"export",
			"extends",
			"extension",
			"external",
			"factory",
			"false",
			"final",
			"finally",
			"for",
			"Function",
			"get",
			"hide",
			"if",
			"implements",
			"import",
			"in",
			"interface",
			"is",
			"late",
			"library",
			"mixin",
			"native",
			"new",
			"null",
			"of",
			"on",
			"operator",
			"part",
			"required",
			"rethrow",
			"return",
			"sealed",
			"set",
			"show",
			"static",
			"super",
			"switch",
			"sync",
			"this",
			"throw",
			"true",
			"try",
			"type",
			"typedef",
			"var",
			"void",
			"when",
			"while",
			"with",
			"yield",
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
		return "Dart";
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
