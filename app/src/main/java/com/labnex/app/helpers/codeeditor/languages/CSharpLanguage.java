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
public class CSharpLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_CHAR = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
	private static final Pattern PATTERN_ATTRIBUTE =
			Pattern.compile("\\[[a-zA-Z_][a-zA-Z0-9_]*\\]");
	private static final Pattern PATTERN_TODO_COMMENT =
			Pattern.compile("//\\s?(TODO|todo|FIXME|fixme|HACK|hack)\\s[^\n]*");

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
			case CHAR -> PATTERN_CHAR;
			case STRING -> PATTERN_STRING;
			case HEX -> PATTERN_HEX;
			case SINGLE_LINE_COMMENT -> PATTERN_SINGLE_LINE_COMMENT;
			case MULTI_LINE_COMMENT -> PATTERN_MULTI_LINE_COMMENT;
			case ATTRIBUTE -> PATTERN_ATTRIBUTE;
			case TODO_COMMENT -> PATTERN_TODO_COMMENT;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"abstract",
			"as",
			"base",
			"bool",
			"break",
			"byte",
			"case",
			"catch",
			"char",
			"checked",
			"class",
			"const",
			"continue",
			"decimal",
			"default",
			"delegate",
			"do",
			"double",
			"else",
			"enum",
			"event",
			"explicit",
			"extern",
			"false",
			"finally",
			"fixed",
			"float",
			"for",
			"foreach",
			"goto",
			"if",
			"implicit",
			"in",
			"int",
			"interface",
			"internal",
			"is",
			"lock",
			"long",
			"namespace",
			"new",
			"null",
			"object",
			"operator",
			"out",
			"override",
			"params",
			"private",
			"protected",
			"public",
			"readonly",
			"record",
			"ref",
			"return",
			"sbyte",
			"sealed",
			"short",
			"sizeof",
			"stackalloc",
			"static",
			"string",
			"struct",
			"switch",
			"this",
			"throw",
			"true",
			"try",
			"typeof",
			"uint",
			"ulong",
			"unchecked",
			"unsafe",
			"ushort",
			"using",
			"var",
			"virtual",
			"void",
			"volatile",
			"while",
			"async",
			"await",
			"from",
			"get",
			"init",
			"let",
			"orderby",
			"partial",
			"select",
			"set",
			"value",
			"where",
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
		return "CSharp";
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
