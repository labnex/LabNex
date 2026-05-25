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
public class ZigLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("//[^\\n]*");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
	private static final Pattern PATTERN_BUILTIN = Pattern.compile("@[a-zA-Z_][a-zA-Z0-9_]*");

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
			case ANNOTATION -> PATTERN_BUILTIN;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"align",
			"allowzero",
			"and",
			"anyframe",
			"anytype",
			"asm",
			"async",
			"await",
			"break",
			"catch",
			"comptime",
			"const",
			"continue",
			"defer",
			"else",
			"enum",
			"errdefer",
			"error",
			"export",
			"extern",
			"fn",
			"for",
			"if",
			"inline",
			"linksection",
			"noalias",
			"noinline",
			"nosuspend",
			"opaque",
			"or",
			"orelse",
			"packed",
			"pub",
			"resume",
			"return",
			"struct",
			"suspend",
			"switch",
			"test",
			"threadlocal",
			"try",
			"union",
			"unreachable",
			"usingnamespace",
			"var",
			"volatile",
			"while",
			"bool",
			"f16",
			"f32",
			"f64",
			"f80",
			"f128",
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
			"c_char",
			"c_int",
			"c_long",
			"c_uint",
			"c_ulong",
			"c_void",
			"void",
			"noreturn",
			"type",
			"true",
			"false",
			"null",
			"undefined",
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
		return "Zig";
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
