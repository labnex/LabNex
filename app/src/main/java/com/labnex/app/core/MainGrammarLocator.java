package com.labnex.app.core;

/**
 * @author opyale
 */
public class MainGrammarLocator {

	public static final String DEFAULT_FALLBACK_LANGUAGE = null; // "clike";

	public static String fromExtension(String extension) {
		return switch (extension.toLowerCase()) {
			case "b", "bf" -> "brainfuck";
			case "c", "h", "hdl" -> "c";
			case "clj", "cljs", "cljc", "edn" -> "clojure";
			case "cc", "cpp", "cxx", "c++", "hh", "hpp", "hxx", "h++" -> "cpp";
			case "groovy", "gradle", "gvy", "gy", "gsh" -> "groovy";
			case "js", "cjs", "mjs" -> "javascript";
			case "kt", "kts", "ktm" -> "kotlin";
			case "md" -> "markdown";
			case "mathml", "svg" -> "markup";
			case "py", "pyi", "pyc", "pyd", "pyo", "pyw", "pyz" -> "python";
			case "scala", "sc" -> "scala";
			case "el", "lisp" -> "lisp";
			case "yaml", "yml", "properties" -> "yaml";
			case "rs" -> "rust";
			case "css" -> "css";
			case "sql", "psql" -> "sql";
			case "rb", "ruby" -> "ruby";
			case "swift" -> "swift";
			case "sh", "bash", "zsh", "fish" -> "bash";
			case "dockerfile" -> "dockerfile";
			case "toml" -> "toml";
			case "go" -> "go";
			case "html", "htm", "xhtml" -> "html";
			case "xml", "xsd", "xsl" -> "xml";
			case "json" -> "json";
			case "php", "phtml", "php3", "php4", "php5", "php7", "php8" -> "php";
			case "ts", "tsx" -> "typescript";
			case "dart" -> "dart";
			case "r" -> "r";
			case "lua" -> "lua";
			case "pl", "perl" -> "perl";
			case "scss", "sass" -> "scss";
			case "less" -> "less";
			case "ini", "cfg", "conf" -> "ini";
			case "makefile", "mk" -> "makefile";
			case "cmake" -> "cmake";
			case "bat", "cmd" -> "batch";
			case "ps1" -> "powershell";
			case "zig", "zir" -> "zig";
			case "cs", "csx", "cshtml" -> "csharp";
			default -> extension;
		};
	}
}
