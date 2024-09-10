package com.labnex.app.models.users;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Users implements Serializable {

	@SerializedName("Users")
	private List<UsersItem> users;

	public List<UsersItem> getUsers() {
		return users;
	}
}
