package com.labnex.app.models.approvals;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Group implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("path")
    private String path;
    @SerializedName("description")
    private String description;
    @SerializedName("visibility")
    private String visibility;
    @SerializedName("lfs_enabled")
    private boolean lfsEnabled;
    @SerializedName("avatar_url")
    private String avatarUrl;
    @SerializedName("web_url")
    private String webUrl;
    @SerializedName("request_access_enabled")
    private boolean requestAccessEnabled;
    @SerializedName("full_name")
    private String fullName;
    @SerializedName("full_path")
    private String fullPath;
    @SerializedName("parent_id")
    private int parentId;
    @SerializedName("ldap_cn")
    private Object ldabCn;
    @SerializedName("ldap_access")
    private Object ldapAccess;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public String getVisibility() {
        return visibility;
    }

    public boolean getLfsEnabled() {
        return lfsEnabled;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public boolean getRequestAccessEnabled() {
        return requestAccessEnabled;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFullPath() {
        return fullPath;
    }

    public int getParentId() {
        return parentId;
    }

    public Object getLdabCn() {
        return ldabCn;
    }

    public Object getLdapAccess() {
        return ldapAccess;
    }
}
