package com.labnex.app.models.approvals;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Rule implements Serializable {
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("rule_type")
    private String ruleType;
    @SerializedName("report_type")
    private Object reportType;
    @SerializedName("eligible_approvers")
    private List<User> eligibleApprovers;
    @SerializedName("approvals_required")
    private int approvalsRequired;
    @SerializedName("users")
    private List<User> users;
    @SerializedName("groups")
    private List<Group> groups;
    @SerializedName("applies_to_all_protected_branches")
    private boolean appliesToAllProtectedBranches;
    @SerializedName("protected_branches")
    private List<Branch> protectedBranches;
    @SerializedName("contains_hidden_groups")
    private boolean containsHiddenGroups;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRuleType() { return ruleType; }
    public Object getReportType() { return reportType; }
    public List<User> getEligibleApprovers() { return eligibleApprovers; }
    public int getApprovalsRequired() { return approvalsRequired; }
    public List<User> getUsers() { return users; }
    public List<Group> getGroups() { return groups; }
    public boolean appliesToAllProtectedBranches() { return appliesToAllProtectedBranches; }
    public boolean getContainsHiddenGroups() { return containsHiddenGroups; }
    public List<Branch> getProtectedBranches() { return protectedBranches; }
}
