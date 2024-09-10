package com.labnex.app.helpers.codeeditor.languages;

import com.amrdeveloper.codeview.Code;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author qwerty287
 */
public class UnknownLanguage extends Language {

	@Override
	public Pattern getPattern(LanguageElement element) {
		return null;
	}

	@Override
	public Set<Character> getIndentationStarts() {
		return Set.of();
	}

	@Override
	public Set<Character> getIndentationEnds() {
		return Set.of();
	}

	@Override
	public String[] getKeywords() {
		return new String[0];
	}

	@Override
	public List<Code> getCodeList() {
		return new ArrayList<>();
	}

	@Override
	public String getName() {
		return "Unknown";
	}
}
