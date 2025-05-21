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
public class PhpLanguage extends Language {

	// Brackets and Colons
	private static final Pattern PATTERN_BUILTINS = Pattern.compile("[,:;[->]{}()]");

	// Data
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_CHAR = Pattern.compile("['](.*?)[']");
	private static final Pattern PATTERN_STRING = Pattern.compile("[\"](.*?)[\"]");
	private static final Pattern PATTERN_HEX = Pattern.compile("0x[0-9a-fA-F]+");
	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("(//|#)[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_TODO_COMMENT =
			Pattern.compile("(//|#)\\s?(TODO|todo)\\s[^\\n]*");
	private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("(?<=->)[a-zA-Z0-9_]+");
	private static final Pattern PATTERN_OPERATION =
			Pattern.compile(
					":|==|>|<|!=|>=|<=|->|=|>|<|%|-|-=|%=|\\+|\\-|\\-=|\\+=|\\^|\\&|\\|::|\\?|\\*");

	public static String getCommentStart() {
		return "//";
	}

	public static String getCommentEnd() {
		return "";
	}

	@Override
	public Pattern getPattern(LanguageElement element) {
		switch (element) {
			case KEYWORD:
				return Pattern.compile("\\b(" + String.join("|", getKeywords()) + ")\\b");
			case BUILTIN:
				return PATTERN_BUILTINS;
			case NUMBER:
				return PATTERN_NUMBERS;
			case CHAR:
				return PATTERN_CHAR;
			case STRING:
				return PATTERN_STRING;
			case HEX:
				return PATTERN_HEX;
			case SINGLE_LINE_COMMENT:
				return PATTERN_SINGLE_LINE_COMMENT;
			case MULTI_LINE_COMMENT:
				return PATTERN_MULTI_LINE_COMMENT;
			case ATTRIBUTE:
				return PATTERN_ATTRIBUTE;
			case OPERATION:
				return PATTERN_OPERATION;
			case TODO_COMMENT:
				return PATTERN_TODO_COMMENT;
			case ANNOTATION:
			// TODO supported by PHP
			case GENERIC:
			default:
				return null;
		}
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"<?php",
			"__construct",
			"var_dump",
			"define",
			"echo",
			"var",
			"float",
			"int",
			"bool",
			"false",
			"true",
			"function",
			"private",
			"public",
			"protected",
			"interface",
			"return",
			"copy",
			"struct",
			"abstract",
			"extends",
			"trait",
			"static",
			"namespace",
			"implements",
			"__set",
			"__get",
			"unlink",
			"this",
			"try",
			"catch",
			"Throwable",
			"Exception",
			"pdo",
			"throw",
			"new",
			"and",
			"or",
			"if",
			"else",
			"elseif",
			"switch",
			"case",
			"default",
			"match",
			"require",
			"include",
			"require_once",
			"include_once",
			"goto",
			"do",
			"while",
			"for",
			"foreach",
			"map",
			"hash",
			"array",
			"range",
			"break",
			"continue",
			"preg_match",
			"preg_match_all",
			"preg_replace",
			"str_replace",
			"form",
			"date",
			"abs",
			"min",
			"max",
			"strtotime",
			"mktime",
			"use",
			"enum",
			"class"
		};
	}

	@Override
	public List<Code> getCodeList() {
		List<Code> codeList = new ArrayList<>();
		String[] keywords = getKeywords();
		for (String keyword : keywords) {
			codeList.add(new Keyword(keyword));
		}
		return codeList;
	}

	@Override
	public String getName() {
		return "PHP";
	}

	@Override
	public Set<Character> getIndentationStarts() {
		Set<Character> characterSet = new HashSet<>();
		characterSet.add('{');
		return characterSet;
	}

	@Override
	public Set<Character> getIndentationEnds() {
		Set<Character> characterSet = new HashSet<>();
		characterSet.add('}');
		return characterSet;
	}
}
