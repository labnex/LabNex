package com.labnex.app.models.commits;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * @author mmarif
 */
public class MrChanges {

	@SerializedName("changes")
	private List<Diff> changes;

	public List<Diff> getChanges() {
		return changes;
	}
}
