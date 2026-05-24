package com.labnex.app.models.app;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mmarif
 */
public class Reactions {

	public static final Map<String, String> EMOJIS =
			new LinkedHashMap<>() {
				{
					put("thumbsup", "👍");
					put("thumbsdown", "👎");
					put("smile", "😄");
					put("tada", "🎉");
					put("heart", "❤️");
					put("rocket", "🚀");
					put("eyes", "👀");
					put("clap", "👏");
					put("laughing", "😆");
					put("cry", "😢");
					put("angry", "😠");
					put("thinking", "🤔");
					put("pray", "🙏");
					put("muscle", "💪");
					put("fire", "🔥");
					put("party", "🥳");
					put("heart_eyes", "😍");
					put("sunglasses", "😎");
					put("see_no_evil", "🙈");
					put("raised_hands", "🙌");
					put("wave", "👋");
					put("ok_hand", "👌");
					put("100", "💯");
					put("bulb", "💡");
					put("coffee", "☕");
					put("star", "⭐");
					put("question", "❓");
					put("exclamation", "❗");
					put("bug", "🐛");
					put("warning", "⚠️");
					put("construction", "🚧");
					put("white_check_mark", "✅");
					put("x", "❌");
					put("lock", "🔒");
					put("wrench", "🔧");
					put("gear", "⚙️");
					put("computer", "💻");
					put("checkered_flag", "🏁");
					put("sweat_smile", "😅");
					put("facepalm", "🤦");
				}
			};

	public static String getEmoji(String name) {
		return EMOJIS.getOrDefault(name, "❓");
	}
}
