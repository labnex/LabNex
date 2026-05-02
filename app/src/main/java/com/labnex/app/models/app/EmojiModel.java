package com.labnex.app.models.app;

/**
 * @author mmarif
 */
public class EmojiModel {

	private final String name;
	private final String char_emoji;

	public EmojiModel(String name, String char_emoji) {
		this.name = name;
		this.char_emoji = char_emoji;
	}

	public String getName() {
		return name;
	}

	public String getCharEmoji() {
		return char_emoji;
	}
}
