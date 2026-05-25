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
public class KotlinLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_CHAR = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
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
			case CHAR -> PATTERN_CHAR;
			case STRING -> PATTERN_STRING;
			case HEX -> PATTERN_HEX;
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
			"actual",
			"annotation",
			"as",
			"break",
			"by",
			"catch",
			"class",
			"companion",
			"const",
			"constructor",
			"continue",
			"crossinline",
			"data",
			"delegate",
			"do",
			"dynamic",
			"else",
			"enum",
			"expect",
			"external",
			"false",
			"field",
			"final",
			"finally",
			"for",
			"fun",
			"get",
			"if",
			"import",
			"in",
			"infix",
			"init",
			"inline",
			"inner",
			"interface",
			"internal",
			"is",
			"it",
			"lateinit",
			"noinline",
			"null",
			"object",
			"open",
			"operator",
			"out",
			"override",
			"package",
			"param",
			"private",
			"property",
			"protected",
			"public",
			"receiver",
			"reified",
			"return",
			"sealed",
			"set",
			"super",
			"suspend",
			"tailrec",
			"this",
			"throw",
			"true",
			"try",
			"typealias",
			"typeof",
			"val",
			"var",
			"vararg",
			"when",
			"where",
			"while",
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
		return "Kotlin";
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
