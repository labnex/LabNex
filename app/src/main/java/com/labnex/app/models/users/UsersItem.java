package com.labnex.app.models.users;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class UsersItem implements Serializable {

	@SerializedName("can_create_project")
	private boolean canCreateProject;

	@SerializedName("private_profile")
	private boolean privateProfile;

	@SerializedName("theme_id")
	private int themeId;

	@SerializedName("last_sign_in_ip")
	private String lastSignInIp;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("bio")
	private String bio;

	@SerializedName("projects_limit")
	private int projectsLimit;

	@SerializedName("linkedin")
	private String linkedin;

	@SerializedName("last_activity_on")
	private String lastActivityOn;

	@SerializedName("current_sign_in_ip")
	private String currentSignInIp;

	@SerializedName("can_create_group")
	private boolean canCreateGroup;

	@SerializedName("is_admin")
	private boolean isAdmin;

	@SerializedName("skype")
	private String skype;

	@SerializedName("twitter")
	private String twitter;

	@SerializedName("identities")
	private List<IdentitiesItem> identities;

	@SerializedName("last_sign_in_at")
	private String lastSignInAt;

	@SerializedName("color_scheme_id")
	private int colorSchemeId;

	@SerializedName("id")
	private int id;

	@SerializedName("state")
	private String state;

	@SerializedName("locked")
	private boolean locked;

	@SerializedName("confirmed_at")
	private String confirmedAt;

	@SerializedName("job_title")
	private String jobTitle;

	@SerializedName("email")
	private String email;

	@SerializedName("current_sign_in_at")
	private String currentSignInAt;

	@SerializedName("two_factor_enabled")
	private boolean twoFactorEnabled;

	@SerializedName("created_by")
	private Object createdBy;

	@SerializedName("external")
	private boolean external;

	@SerializedName("discord")
	private String discord;

	@SerializedName("avatar_url")
	private String avatarUrl;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("website_url")
	private String websiteUrl;

	@SerializedName("namespace_id")
	private int namespaceId;

	@SerializedName("organization")
	private String organization;

	@SerializedName("name")
	private String name;

	@SerializedName("email_reset_offered_at")
	private Object emailResetOfferedAt;

	@SerializedName("location")
	private Object location;

	@SerializedName("username")
	private String username;

	@SerializedName("note")
	private String note;

	public boolean isCanCreateProject() {
		return canCreateProject;
	}

	public boolean isPrivateProfile() {
		return privateProfile;
	}

	public int getThemeId() {
		return themeId;
	}

	public String getLastSignInIp() {
		return lastSignInIp;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getBio() {
		return bio;
	}

	public int getProjectsLimit() {
		return projectsLimit;
	}

	public String getLinkedin() {
		return linkedin;
	}

	public String getLastActivityOn() {
		return lastActivityOn;
	}

	public String getCurrentSignInIp() {
		return currentSignInIp;
	}

	public boolean isCanCreateGroup() {
		return canCreateGroup;
	}

	public boolean isIsAdmin() {
		return isAdmin;
	}

	public String getSkype() {
		return skype;
	}

	public String getTwitter() {
		return twitter;
	}

	public List<IdentitiesItem> getIdentities() {
		return identities;
	}

	public String getLastSignInAt() {
		return lastSignInAt;
	}

	public int getColorSchemeId() {
		return colorSchemeId;
	}

	public int getId() {
		return id;
	}

	public String getState() {
		return state;
	}

	public boolean isLocked() {
		return locked;
	}

	public String getConfirmedAt() {
		return confirmedAt;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public String getEmail() {
		return email;
	}

	public String getCurrentSignInAt() {
		return currentSignInAt;
	}

	public boolean isTwoFactorEnabled() {
		return twoFactorEnabled;
	}

	public Object getCreatedBy() {
		return createdBy;
	}

	public boolean isExternal() {
		return external;
	}

	public String getDiscord() {
		return discord;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public String getWebsiteUrl() {
		return websiteUrl;
	}

	public int getNamespaceId() {
		return namespaceId;
	}

	public String getOrganization() {
		return organization;
	}

	public String getName() {
		return name;
	}

	public Object getEmailResetOfferedAt() {
		return emailResetOfferedAt;
	}

	public Object getLocation() {
		return location;
	}

	public String getUsername() {
		return username;
	}

	public String getNote() {
		return note;
	}
}
