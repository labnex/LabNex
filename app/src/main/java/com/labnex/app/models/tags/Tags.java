package com.labnex.app.models.tags;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Tags implements Serializable {

	@SerializedName("Tags")
	private List<TagsItem> tags;

	public List<TagsItem> getTags() {
		return tags;
	}
}
