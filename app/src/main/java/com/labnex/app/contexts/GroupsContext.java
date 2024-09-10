package com.labnex.app.contexts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.models.groups.GroupsItem;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class GroupsContext implements Serializable {

	public static final String INTENT_EXTRA = "groups";
	private AccountContext account;
	private GroupsItem groupsItem;
	private final String name;
	private final String path;

	public GroupsContext(String name, String path, Context context) {
		this.account = ((BaseActivity) context).getAccount();
		this.name = name;
		this.path = path;
	}

	public static GroupsContext fromIntent(Intent intent) {
		return (GroupsContext) intent.getSerializableExtra(INTENT_EXTRA);
	}

	public static GroupsContext fromBundle(Bundle bundle) {
		return (GroupsContext) bundle.getSerializable(INTENT_EXTRA);
	}

	public GroupsItem getGroupsItem() {

		return groupsItem;
	}

	public void setGroupsItem(GroupsItem groupsItem) {
		this.groupsItem = groupsItem;
	}

	public void removeGroupItem() {

		groupsItem = null;
	}
}
