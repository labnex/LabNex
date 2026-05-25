package com.labnex.app.helpers.codeeditor.languages;

import com.amrdeveloper.codeview.Code;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author mmarif
 */
public class MarkdownLanguage extends Language {

	public static String getCommentStart() {
		return "";
	}

	public static String getCommentEnd() {
		return "";
	}

	@Override
	public Pattern getPattern(LanguageElement element) {
		return null;
	}

	@Override
	public String[] getKeywords() {
		return new String[] {};
	}

	@Override
	public List<Code> getCodeList() {
		return new ArrayList<>();
	}

	@Override
	public String getName() {
		return "Markdown";
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
