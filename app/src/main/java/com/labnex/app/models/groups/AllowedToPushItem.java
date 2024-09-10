package com.labnex.app.models.groups;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class AllowedToPushItem implements Serializable {

	@SerializedName("access_level")
	private int accessLevel;

	public int getAccessLevel() {
		return accessLevel;
	}
}
