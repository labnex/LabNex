package com.labnex.app.models.approvals;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author lululujojo123
 */
public class ApprovedBy implements Serializable {

	@SerializedName("user")
	private User user;

	public User getUser() {
		return user;
	}
}
