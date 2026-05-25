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
public class RustLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_CHAR = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("#\\[[^\\]]*\\]");
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
			case ATTRIBUTE, ANNOTATION -> PATTERN_ATTRIBUTE;
			case TODO_COMMENT -> PATTERN_TODO_COMMENT;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"as",
			"async",
			"await",
			"break",
			"const",
			"continue",
			"crate",
			"dyn",
			"else",
			"enum",
			"extern",
			"false",
			"fn",
			"for",
			"if",
			"impl",
			"in",
			"let",
			"loop",
			"match",
			"mod",
			"move",
			"mut",
			"pub",
			"ref",
			"return",
			"self",
			"Self",
			"static",
			"struct",
			"super",
			"trait",
			"true",
			"type",
			"union",
			"unsafe",
			"use",
			"where",
			"while",
			"abstract",
			"become",
			"box",
			"do",
			"final",
			"macro",
			"override",
			"priv",
			"try",
			"typeof",
			"unsized",
			"virtual",
			"yield",
			"i8",
			"i16",
			"i32",
			"i64",
			"i128",
			"isize",
			"u8",
			"u16",
			"u32",
			"u64",
			"u128",
			"usize",
			"f32",
			"f64",
			"bool",
			"char",
			"str",
			"String",
			"Vec",
			"Option",
			"Result",
			"Some",
			"None",
			"Ok",
			"Err",
			"Box",
			"Rc",
			"Arc",
			"Cell",
			"RefCell",
			"HashMap",
			"HashSet",
			"println",
			"print",
			"format",
			"panic",
			"assert",
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
		return "Rust";
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
