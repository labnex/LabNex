package com.labnex.app.models.snippets;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Snippets implements Serializable {

	@SerializedName("Snippets")
	private List<SnippetsItem> snippets;

	public List<SnippetsItem> getSnippets() {
		return snippets;
	}
}
