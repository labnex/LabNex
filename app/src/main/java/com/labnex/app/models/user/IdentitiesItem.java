package com.labnex.app.models.user;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class IdentitiesItem implements Serializable {

	@SerializedName("provider")
	private String provider;

	@SerializedName("extern_uid")
	private String externUid;

	public String getProvider() {
		return provider;
	}

	public String getExternUid() {
		return externUid;
	}
}
