package com.labnex.app.models.personal_access_tokens;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class PersonalAccessTokens implements Serializable {

	@SerializedName("last_used_at")
	private String lastUsedAt;

	@SerializedName("expires_at")
	private Object expiresAt;

	@SerializedName("user_id")
	private int userId;

	@SerializedName("name")
	private String name;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("active")
	private boolean active;

	@SerializedName("id")
	private int id;

	@SerializedName("scopes")
	private List<String> scopes;

	@SerializedName("revoked")
	private boolean revoked;

	public String getLastUsedAt() {
		return lastUsedAt;
	}

	public Object getExpiresAt() {
		return expiresAt;
	}

	public int getUserId() {
		return userId;
	}

	public String getName() {
		return name;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public boolean isActive() {
		return active;
	}

	public int getId() {
		return id;
	}

	public List<String> getScopes() {
		return scopes;
	}

	public boolean isRevoked() {
		return revoked;
	}
}
