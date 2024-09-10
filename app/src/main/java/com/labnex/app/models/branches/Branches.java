package com.labnex.app.models.branches;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Branches implements Serializable {

	@SerializedName("default")
	private boolean jsonMemberDefault;

	@SerializedName("protected")
	private boolean jsonMemberProtected;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("developers_can_push")
	private boolean developersCanPush;

	@SerializedName("developers_can_merge")
	private boolean developersCanMerge;

	@SerializedName("name")
	private String name;

	@SerializedName("commit")
	private Commit commit;

	@SerializedName("merged")
	private boolean merged;

	@SerializedName("can_push")
	private boolean canPush;

	public boolean isJsonMemberDefault() {
		return jsonMemberDefault;
	}

	public boolean isJsonMemberProtected() {
		return jsonMemberProtected;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public boolean isDevelopersCanPush() {
		return developersCanPush;
	}

	public boolean isDevelopersCanMerge() {
		return developersCanMerge;
	}

	public String getName() {
		return name;
	}

	public Commit getCommit() {
		return commit;
	}

	public boolean isMerged() {
		return merged;
	}

	public boolean isCanPush() {
		return canPush;
	}
}
