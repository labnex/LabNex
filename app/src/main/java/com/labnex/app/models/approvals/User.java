package com.labnex.app.models.approvals;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author lululujojo123
 */
public class User implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("username")
    private String username;

    @SerializedName("state")
    private String state;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("web_url")
    private String webUrl;

    public int getId() { return id; }

    public String getName() { return name; }

    public String getUsername() { return username; }

    public String getState() { return state; }

    public String getAvatarUrl() { return avatarUrl; }

    public String getWebUrl() { return webUrl; }
}
