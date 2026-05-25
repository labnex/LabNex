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
public class SwiftLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");
	private static final Pattern PATTERN_TODO_COMMENT =
			Pattern.compile("//\\s?(TODO|todo|FIXME|fixme|MARK|mark)\\s[^\n]*");

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
			case HEX -> PATTERN_HEX;
			case SINGLE_LINE_COMMENT -> PATTERN_SINGLE_LINE_COMMENT;
			case MULTI_LINE_COMMENT -> PATTERN_MULTI_LINE_COMMENT;
			case ATTRIBUTE, ANNOTATION -> PATTERN_ATTRIBUTE;
			case TODO_COMMENT -> PATTERN_TODO_COMMENT;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"actor",
			"associatedtype",
			"async",
			"await",
			"break",
			"case",
			"catch",
			"class",
			"continue",
			"convenience",
			"default",
			"defer",
			"deinit",
			"didSet",
			"distributed",
			"do",
			"dynamic",
			"else",
			"enum",
			"extension",
			"fallthrough",
			"false",
			"fileprivate",
			"final",
			"for",
			"func",
			"get",
			"guard",
			"if",
			"import",
			"in",
			"indirect",
			"infix",
			"init",
			"inout",
			"internal",
			"is",
			"isolated",
			"lazy",
			"let",
			"macro",
			"mutating",
			"nil",
			"nonisolated",
			"nonmutating",
			"open",
			"operator",
			"optional",
			"override",
			"package",
			"postfix",
			"precedencegroup",
			"prefix",
			"private",
			"protocol",
			"public",
			"repeat",
			"required",
			"rethrows",
			"return",
			"self",
			"Self",
			"set",
			"some",
			"static",
			"struct",
			"subscript",
			"super",
			"switch",
			"throw",
			"throws",
			"true",
			"try",
			"typealias",
			"unowned",
			"var",
			"weak",
			"where",
			"while",
			"willSet",
			"Int",
			"Float",
			"Double",
			"Bool",
			"String",
			"Character",
			"Array",
			"Dictionary",
			"Set",
			"Optional",
			"Data",
			"URL",
			"Error",
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
		return "Swift";
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
