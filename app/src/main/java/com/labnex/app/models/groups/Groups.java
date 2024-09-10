package com.labnex.app.models.groups;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Groups implements Serializable {

	@SerializedName("Groups")
	private List<GroupsItem> groups;

	public List<GroupsItem> getGroups() {
		return groups;
	}
}
