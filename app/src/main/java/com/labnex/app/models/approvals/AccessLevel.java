package com.labnex.app.models.approvals;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AccessLevel implements Serializable {
    @SerializedName("access_level")
    private int accessLevel;
    @SerializedName("access_level_description")
    private String accessLevelDescription;

    public int getAccessLevel() { return accessLevel; }
    public String getAccessLevelDescription() { return accessLevelDescription; }
}
