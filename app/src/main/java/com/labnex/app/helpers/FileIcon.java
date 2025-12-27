package com.labnex.app.helpers;

import com.labnex.app.R;
import com.labnex.app.helpers.Utils.FileType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mmarif
 */
public class FileIcon {

	private static final Map<Utils.FileType, Integer> typeIcons = new HashMap<>();
	private static final Map<String, Integer> iconCache = new HashMap<>();
	private static final Map<String, Integer> extensionIcons = new HashMap<>();

	static {
		typeIcons.put(FileType.IMAGE, R.drawable.ic_file_image);
		typeIcons.put(FileType.AUDIO, R.drawable.ic_file_audio);
		typeIcons.put(FileType.VIDEO, R.drawable.ic_file_video);
		typeIcons.put(FileType.DOCUMENT, R.drawable.ic_file_document);
		typeIcons.put(FileType.EXECUTABLE, R.drawable.ic_file_executable);
		typeIcons.put(FileType.TEXT, R.drawable.ic_file_document);
		typeIcons.put(FileType.FONT, R.drawable.ic_file_font);
		typeIcons.put(FileType.UNKNOWN, R.drawable.ic_file_document);
		typeIcons.put(FileType.KEYSTORE, R.drawable.ic_file_lock);

		extensionIcons.put("txt", R.drawable.ic_file_txt);
		extensionIcons.put("md", R.drawable.ic_file_markdown);
		extensionIcons.put("json", R.drawable.ic_file_json);
		extensionIcons.put("java", R.drawable.ic_file_java);
		extensionIcons.put("go", R.drawable.ic_file_go);
		extensionIcons.put("php", R.drawable.ic_file_php);
		extensionIcons.put("c", R.drawable.ic_file_c);
		extensionIcons.put("cc", R.drawable.ic_file_cpp);
		extensionIcons.put("cpp", R.drawable.ic_file_cpp);
		extensionIcons.put("d", R.drawable.ic_file_d);
		extensionIcons.put("h", R.drawable.ic_file_c);
		extensionIcons.put("cxx", R.drawable.ic_file_cpp);
		extensionIcons.put("cyc", R.drawable.ic_file_c);
		extensionIcons.put("m", R.drawable.ic_file_matlab);
		extensionIcons.put("cs", R.drawable.ic_file_cs);
		extensionIcons.put("bash", R.drawable.ic_file_bash);
		extensionIcons.put("sh", R.drawable.ic_file_bash);
		extensionIcons.put("bsh", R.drawable.ic_file_bash);
		extensionIcons.put("cv", R.drawable.ic_file_document);
		extensionIcons.put("python", R.drawable.ic_file_python);
		extensionIcons.put("perl", R.drawable.ic_file_perl);
		extensionIcons.put("pm", R.drawable.ic_file_perl);
		extensionIcons.put("rb", R.drawable.ic_file_ruby);
		extensionIcons.put("ruby", R.drawable.ic_file_ruby);
		extensionIcons.put("coffee", R.drawable.ic_file_coffee);
		extensionIcons.put("rc", R.drawable.ic_file_rust);
		extensionIcons.put("rs", R.drawable.ic_file_rust);
		extensionIcons.put("rust", R.drawable.ic_file_rust);
		extensionIcons.put("basic", R.drawable.ic_file_basic);
		extensionIcons.put("clj", R.drawable.ic_file_clj);
		extensionIcons.put("css", R.drawable.ic_file_css);
		extensionIcons.put("dart", R.drawable.ic_file_dart);
		extensionIcons.put("lisp", R.drawable.ic_file_lisp);
		extensionIcons.put("erl", R.drawable.ic_file_erl);
		extensionIcons.put("hs", R.drawable.ic_file_hs);
		extensionIcons.put("lsp", R.drawable.ic_file_lisp);
		extensionIcons.put("rkt", R.drawable.ic_file_rkt);
		extensionIcons.put("ss", R.drawable.ic_file_ss);
		extensionIcons.put("lua", R.drawable.ic_file_lua);
		extensionIcons.put("matlab", R.drawable.ic_file_matlab);
		extensionIcons.put("pascal", R.drawable.ic_file_pascal);
		extensionIcons.put("r", R.drawable.ic_file_r);
		extensionIcons.put("scala", R.drawable.ic_file_scala);
		extensionIcons.put("sql", R.drawable.ic_file_sql);
		extensionIcons.put("latex", R.drawable.ic_file_latex);
		extensionIcons.put("tex", R.drawable.ic_file_latex);
		extensionIcons.put("vb", R.drawable.ic_file_vb);
		extensionIcons.put("vbs", R.drawable.ic_file_vb);
		extensionIcons.put("vhd", R.drawable.ic_file_vhd);
		extensionIcons.put("tcl", R.drawable.ic_file_tcl);
		extensionIcons.put("wiki.meta", R.drawable.ic_file_wiki);
		extensionIcons.put("yaml", R.drawable.ic_file_yml);
		extensionIcons.put("yml", R.drawable.ic_file_yml);
		extensionIcons.put("yamllint", R.drawable.ic_file_yml);
		extensionIcons.put("markdown", R.drawable.ic_file_markdown);
		extensionIcons.put("xml", R.drawable.ic_file_xml);
		extensionIcons.put("proto", R.drawable.ic_file_proto);
		extensionIcons.put("regex", R.drawable.ic_file_code);
		extensionIcons.put("py", R.drawable.ic_file_python);
		extensionIcons.put("pl", R.drawable.ic_file_perl);
		extensionIcons.put("javascript", R.drawable.ic_file_javascript);
		extensionIcons.put("js", R.drawable.ic_file_javascript);
		extensionIcons.put("mjs", R.drawable.ic_file_javascript);
		extensionIcons.put("cjs", R.drawable.ic_file_javascript);
		extensionIcons.put("html", R.drawable.ic_file_html);
		extensionIcons.put("htm", R.drawable.ic_file_html);
		extensionIcons.put("volt", R.drawable.ic_file_volt);
		extensionIcons.put("ini", R.drawable.ic_file_ini);
		extensionIcons.put("htaccess", R.drawable.ic_file_conf);
		extensionIcons.put("conf", R.drawable.ic_file_conf);
		extensionIcons.put("gradle", R.drawable.ic_file_gradle);
		extensionIcons.put("properties", R.drawable.ic_file_java);
		extensionIcons.put("bat", R.drawable.ic_file_bat);
		extensionIcons.put("twig", R.drawable.ic_file_twig);
		extensionIcons.put("cvs", R.drawable.ic_file_cvs);
		extensionIcons.put("cmake", R.drawable.ic_file_cmake);
		extensionIcons.put("in", R.drawable.ic_file_in);
		extensionIcons.put("info", R.drawable.ic_info);
		extensionIcons.put("spec", R.drawable.ic_file_ruby);
		extensionIcons.put("m4", R.drawable.ic_file_code);
		extensionIcons.put("am", R.drawable.ic_file_am);
		extensionIcons.put("dist", R.drawable.ic_file_python);
		extensionIcons.put("pam", R.drawable.ic_file_conf);
		extensionIcons.put("hx", R.drawable.ic_file_hx);
		extensionIcons.put("ts", R.drawable.ic_file_ts);
		extensionIcons.put("kt", R.drawable.ic_file_kt);
		extensionIcons.put("kts", R.drawable.ic_file_kt);
		extensionIcons.put("el", R.drawable.ic_file_el);
		extensionIcons.put("gitignore", R.drawable.ic_file_git);
		extensionIcons.put("gitattributes", R.drawable.ic_file_git);
		extensionIcons.put("gitmodules", R.drawable.ic_file_git);
		extensionIcons.put("gitleaksignore", R.drawable.ic_file_git);
		extensionIcons.put("editorconfig", R.drawable.ic_file_conf);
		extensionIcons.put("jenkinsfile", R.drawable.ic_file_jenkins);
		extensionIcons.put("toml", R.drawable.ic_file_toml);
		extensionIcons.put("lock", R.drawable.ic_file_lock);
		extensionIcons.put("pro", R.drawable.ic_file_prolog);
		extensionIcons.put("gradlew", R.drawable.ic_file_gradle);
		extensionIcons.put("zig", R.drawable.ic_file_zig);
	}

	public static int getIconResource(String fileName, String type) {

		if (fileName == null || type == null) {
			return R.drawable.ic_file_document;
		}

		switch (type) {
			case "dir":
				return R.drawable.ic_file_directory;
			case "submodule":
				return R.drawable.ic_file_submodule;
			case "symlink":
				return R.drawable.ic_file_symlink;
		}

		// Handle files without extension
		String extension = "";
		if (fileName.equalsIgnoreCase("Jenkinsfile")) {
			extension = "jenkinsfile";
		} else if (fileName.equalsIgnoreCase("gradlew")) {
			extension = "gradlew";
		} else if (fileName.contains(".")) {
			extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		}

		Integer cachedIcon = iconCache.get(extension);
		if (cachedIcon != null) {
			return cachedIcon;
		}

		FileType fileType = FileType.UNKNOWN;
		for (Map.Entry<String[], FileType> entry : Utils.getExtensions().entrySet()) {
			for (String ext : entry.getKey()) {
				if (ext.equalsIgnoreCase(extension)) {
					fileType = entry.getValue();
					break;
				}
			}
			if (fileType != FileType.UNKNOWN) {
				break;
			}
		}

		if (fileType == FileType.TEXT) {
			Integer iconId = extensionIcons.get(extension);
			if (iconId != null) {
				iconCache.put(extension, iconId);
				return iconId;
			}
		}

		Integer typeIcon = typeIcons.get(fileType);
		if (typeIcon != null) {
			iconCache.put(extension, typeIcon);
			return typeIcon;
		}

		iconCache.put(extension, R.drawable.ic_file_document);
		return R.drawable.ic_file_document;
	}
}
