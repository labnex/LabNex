package com.labnex.app.models.groups;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class GroupsItem implements Serializable {

	@SerializedName("request_access_enabled")
	private boolean requestAccessEnabled;

	@SerializedName("repository_storage")
	private String repositoryStorage;

	@SerializedName("description")
	private String description;

	@SerializedName("share_with_group_lock")
	private boolean shareWithGroupLock;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("mentions_disabled")
	private Object mentionsDisabled;

	@SerializedName("lock_duo_features_enabled")
	private boolean lockDuoFeaturesEnabled;

	@SerializedName("path")
	private String path;

	@SerializedName("file_template_project_id")
	private int fileTemplateProjectId;

	@SerializedName("project_creation_level")
	private String projectCreationLevel;

	@SerializedName("wiki_access_level")
	private String wikiAccessLevel;

	@SerializedName("emails_enabled")
	private Object emailsEnabled;

	@SerializedName("id")
	private int id;

	@SerializedName("full_path")
	private String fullPath;

	@SerializedName("lfs_enabled")
	private boolean lfsEnabled;

	@SerializedName("emails_disabled")
	private Object emailsDisabled;

	@SerializedName("default_branch_protection_defaults")
	private DefaultBranchProtectionDefaults defaultBranchProtectionDefaults;

	@SerializedName("visibility")
	private String visibility;

	@SerializedName("two_factor_grace_period")
	private int twoFactorGracePeriod;

	@SerializedName("require_two_factor_authentication")
	private boolean requireTwoFactorAuthentication;

	@SerializedName("subgroup_creation_level")
	private String subgroupCreationLevel;

	@SerializedName("auto_devops_enabled")
	private Object autoDevopsEnabled;

	@SerializedName("full_name")
	private String fullName;

	@SerializedName("avatar_url")
	private String avatarUrl;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("parent_id")
	private Object parentId;

	@SerializedName("duo_features_enabled")
	private boolean duoFeaturesEnabled;

	@SerializedName("name")
	private String name;

	@SerializedName("default_branch")
	private Object defaultBranch;

	@SerializedName("default_branch_protection")
	private int defaultBranchProtection;

	@SerializedName("statistics")
	private Statistics statistics;

	public boolean isRequestAccessEnabled() {
		return requestAccessEnabled;
	}

	public String getRepositoryStorage() {
		return repositoryStorage;
	}

	public String getDescription() {
		return description;
	}

	public boolean isShareWithGroupLock() {
		return shareWithGroupLock;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public Object getMentionsDisabled() {
		return mentionsDisabled;
	}

	public boolean isLockDuoFeaturesEnabled() {
		return lockDuoFeaturesEnabled;
	}

	public String getPath() {
		return path;
	}

	public int getFileTemplateProjectId() {
		return fileTemplateProjectId;
	}

	public String getProjectCreationLevel() {
		return projectCreationLevel;
	}

	public String getWikiAccessLevel() {
		return wikiAccessLevel;
	}

	public Object getEmailsEnabled() {
		return emailsEnabled;
	}

	public int getId() {
		return id;
	}

	public String getFullPath() {
		return fullPath;
	}

	public boolean isLfsEnabled() {
		return lfsEnabled;
	}

	public Object getEmailsDisabled() {
		return emailsDisabled;
	}

	public DefaultBranchProtectionDefaults getDefaultBranchProtectionDefaults() {
		return defaultBranchProtectionDefaults;
	}

	public String getVisibility() {
		return visibility;
	}

	public int getTwoFactorGracePeriod() {
		return twoFactorGracePeriod;
	}

	public boolean isRequireTwoFactorAuthentication() {
		return requireTwoFactorAuthentication;
	}

	public String getSubgroupCreationLevel() {
		return subgroupCreationLevel;
	}

	public Object getAutoDevopsEnabled() {
		return autoDevopsEnabled;
	}

	public String getFullName() {
		return fullName;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public Object getParentId() {
		return parentId;
	}

	public boolean isDuoFeaturesEnabled() {
		return duoFeaturesEnabled;
	}

	public String getName() {
		return name;
	}

	public Object getDefaultBranch() {
		return defaultBranch;
	}

	public int getDefaultBranchProtection() {
		return defaultBranchProtection;
	}

	public Statistics getStatistics() {
		return statistics;
	}
}
