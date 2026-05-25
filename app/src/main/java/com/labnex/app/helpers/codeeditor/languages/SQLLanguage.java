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
public class SQLLanguage extends Language {

	private static final Pattern PATTERN_SINGLE_LINE_COMMENT = Pattern.compile("--[^\\n]*");
	private static final Pattern PATTERN_MULTI_LINE_COMMENT =
			Pattern.compile("/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*/");
	private static final Pattern PATTERN_NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	private static final Pattern PATTERN_STRING = Pattern.compile("['](.*?)[']");

	public static String getCommentStart() {
		return "--";
	}

	public static String getCommentEnd() {
		return "";
	}

	@Override
	public Pattern getPattern(LanguageElement element) {
		return switch (element) {
			case KEYWORD ->
					Pattern.compile(
							"\\b(" + String.join("|", getKeywords()) + ")\\b",
							Pattern.CASE_INSENSITIVE);
			case NUMBER -> PATTERN_NUMBERS;
			case STRING -> PATTERN_STRING;
			case SINGLE_LINE_COMMENT -> PATTERN_SINGLE_LINE_COMMENT;
			case MULTI_LINE_COMMENT -> PATTERN_MULTI_LINE_COMMENT;
			default -> null;
		};
	}

	@Override
	public String[] getKeywords() {
		return new String[] {
			"SELECT",
			"FROM",
			"WHERE",
			"INSERT",
			"UPDATE",
			"DELETE",
			"CREATE",
			"ALTER",
			"DROP",
			"TABLE",
			"INDEX",
			"VIEW",
			"INTO",
			"VALUES",
			"SET",
			"JOIN",
			"LEFT",
			"RIGHT",
			"INNER",
			"OUTER",
			"ON",
			"AND",
			"OR",
			"NOT",
			"NULL",
			"IS",
			"IN",
			"LIKE",
			"BETWEEN",
			"ORDER",
			"BY",
			"ASC",
			"DESC",
			"GROUP",
			"HAVING",
			"LIMIT",
			"OFFSET",
			"UNION",
			"ALL",
			"DISTINCT",
			"AS",
			"CASE",
			"WHEN",
			"THEN",
			"ELSE",
			"END",
			"EXISTS",
			"PRIMARY",
			"KEY",
			"FOREIGN",
			"REFERENCES",
			"CONSTRAINT",
			"DEFAULT",
			"CHECK",
			"UNIQUE",
			"CASCADE",
			"TRUNCATE",
			"COUNT",
			"SUM",
			"AVG",
			"MIN",
			"MAX",
			"BEGIN",
			"COMMIT",
			"ROLLBACK",
			"TRANSACTION",
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
		return "SQL";
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
