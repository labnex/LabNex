package com.labnex.app.models.user;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class User implements Serializable {

	@SerializedName("note")
	private String note;

	@SerializedName("can_create_project")
	private boolean canCreateProject;

	@SerializedName("private_profile")
	private boolean privateProfile;

	@SerializedName("work_information")
	private Object workInformation;

	@SerializedName("commit_email")
	private String commitEmail;

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

	@SerializedName("sign_in_count")
	private int signInCount;

	@SerializedName("linkedin")
	private String linkedin;

	@SerializedName("last_activity_on")
	private String lastActivityOn;

	@SerializedName("current_sign_in_ip")
	private String currentSignInIp;

	@SerializedName("can_create_group")
	private boolean canCreateGroup;

	@SerializedName("trial")
	private boolean trial;

	@SerializedName("is_admin")
	private boolean isAdmin;

	@SerializedName("skype")
	private String skype;

	@SerializedName("twitter")
	private String twitter;

	@SerializedName("identities")
	private List<IdentitiesItem> identities;

	@SerializedName("local_time")
	private String localTime;

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

	@SerializedName("plan")
	private String plan;

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

	@SerializedName("followers")
	private int followers;

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

	@SerializedName("following")
	private int following;

	@SerializedName("name")
	private String fullName;

	@SerializedName("email_reset_offered_at")
	private Object emailResetOfferedAt;

	@SerializedName("location")
	private String location;

	@SerializedName("pronouns")
	private String pronouns;

	@SerializedName("public_email")
	private String publicEmail;

	@SerializedName("username")
	private String username;

	public String getNote() {
		return note;
	}

	public boolean isCanCreateProject() {
		return canCreateProject;
	}

	public boolean isPrivateProfile() {
		return privateProfile;
	}

	public Object getWorkInformation() {
		return workInformation;
	}

	public String getCommitEmail() {
		return commitEmail;
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

	public int getSignInCount() {
		return signInCount;
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

	public boolean isTrial() {
		return trial;
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

	public String getLocalTime() {
		return localTime;
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

	public String getPlan() {
		return plan;
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

	public int getFollowers() {
		return followers;
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

	public int getFollowing() {
		return following;
	}

	public String getFullName() {
		return fullName;
	}

	public Object getEmailResetOfferedAt() {
		return emailResetOfferedAt;
	}

	public String getLocation() {
		return location;
	}

	public String getPronouns() {
		return pronouns;
	}

	public String getPublicEmail() {
		return publicEmail;
	}

	public String getUsername() {
		return username;
	}
}
