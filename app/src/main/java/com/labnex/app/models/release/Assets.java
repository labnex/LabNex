package com.labnex.app.models.release;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Assets implements Serializable {

	@SerializedName("sources")
	private List<SourcesItem> sources;

	@SerializedName("count")
	private int count;

	@SerializedName("links")
	private List<Object> links;

	public List<SourcesItem> getSources() {
		return sources;
	}

	public int getCount() {
		return count;
	}

	public List<Object> getLinks() {
		return links;
	}
}
