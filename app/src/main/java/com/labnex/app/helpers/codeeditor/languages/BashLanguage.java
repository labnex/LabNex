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
public class BashLanguage extends Language {

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
			case KEYWORD -> Pattern.compile("\\b(" + String.join("|", getKeywords()) + ")\\b");
			case NUMBER -> PATTERN_NUMBERS;
			case STRING -> PATTERN_STRING;
			case SINGLE_LINE_COMMENT -> PATTERN_SINGLE_LINE_COMMENT;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"if",
			"then",
			"else",
			"elif",
			"fi",
			"case",
			"esac",
			"for",
			"while",
			"until",
			"do",
			"done",
			"in",
			"function",
			"select",
			"time",
			"coproc",
			"declare",
			"typeset",
			"local",
			"readonly",
			"export",
			"unset",
			"alias",
			"unalias",
			"source",
			"exit",
			"return",
			"break",
			"continue",
			"trap",
			"eval",
			"exec",
			"echo",
			"printf",
			"read",
			"test",
			"shift",
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
		return "Bash";
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
