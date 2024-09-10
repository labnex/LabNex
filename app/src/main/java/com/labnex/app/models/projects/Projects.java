package com.labnex.app.models.projects;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * @author mmarif
 */
public class Projects implements Serializable {

	@SerializedName("ssh_url_to_repo")
	private String sshUrlToRepo;

	@SerializedName("only_allow_merge_if_all_discussions_are_resolved")
	private boolean onlyAllowMergeIfAllDiscussionsAreResolved;

	@SerializedName("enforce_auth_checks_on_uploads")
	private boolean enforceAuthChecksOnUploads;

	@SerializedName("security_and_compliance_access_level")
	private String securityAndComplianceAccessLevel;

	@SerializedName("path")
	private String path;

	@SerializedName("monitor_access_level")
	private String monitorAccessLevel;

	@SerializedName("repository_access_level")
	private String repositoryAccessLevel;

	@SerializedName("wiki_access_level")
	private String wikiAccessLevel;

	@SerializedName("shared_runners_enabled")
	private boolean sharedRunnersEnabled;

	@SerializedName("id")
	private int id;

	@SerializedName("import_type")
	private Object importType;

	@SerializedName("feature_flags_access_level")
	private String featureFlagsAccessLevel;

	@SerializedName("merge_requests_access_level")
	private String mergeRequestsAccessLevel;

	@SerializedName("group_runners_enabled")
	private boolean groupRunnersEnabled;

	@SerializedName("allow_merge_on_skipped_pipeline")
	private Object allowMergeOnSkippedPipeline;

	@SerializedName("lfs_enabled")
	private boolean lfsEnabled;

	@SerializedName("ci_separated_caches")
	private boolean ciSeparatedCaches;

	@SerializedName("resolve_outdated_diff_discussions")
	private boolean resolveOutdatedDiffDiscussions;

	@SerializedName("builds_access_level")
	private String buildsAccessLevel;

	@SerializedName("shared_with_groups")
	private List<Object> sharedWithGroups;

	@SerializedName("security_and_compliance_enabled")
	private boolean securityAndComplianceEnabled;

	@SerializedName("pages_access_level")
	private String pagesAccessLevel;

	@SerializedName("service_desk_enabled")
	private boolean serviceDeskEnabled;

	@SerializedName("creator_id")
	private int creatorId;

	@SerializedName("ci_forward_deployment_enabled")
	private boolean ciForwardDeploymentEnabled;

	@SerializedName("default_branch")
	private String defaultBranch;

	@SerializedName("auto_devops_deploy_strategy")
	private String autoDevopsDeployStrategy;

	@SerializedName("description_html")
	private String descriptionHtml;

	@SerializedName("can_create_merge_request_in")
	private boolean canCreateMergeRequestIn;

	@SerializedName("ci_allow_fork_pipelines_to_run_in_parent_project")
	private boolean ciAllowForkPipelinesToRunInParentProject;

	@SerializedName("runners_token")
	private Object runnersToken;

	@SerializedName("restrict_user_defined_variables")
	private boolean restrictUserDefinedVariables;

	@SerializedName("container_registry_image_prefix")
	private String containerRegistryImagePrefix;

	@SerializedName("auto_cancel_pending_pipelines")
	private String autoCancelPendingPipelines;

	@SerializedName("snippets_enabled")
	private boolean snippetsEnabled;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("ci_default_git_depth")
	private int ciDefaultGitDepth;

	@SerializedName("model_experiments_access_level")
	private String modelExperimentsAccessLevel;

	@SerializedName("ci_pipeline_variables_minimum_override_role")
	private String ciPipelineVariablesMinimumOverrideRole;

	@SerializedName("archived")
	private boolean archived;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("issues_access_level")
	private String issuesAccessLevel;

	@SerializedName("issue_branch_template")
	private Object issueBranchTemplate;

	@SerializedName("warn_about_potentially_unwanted_characters")
	private boolean warnAboutPotentiallyUnwantedCharacters;

	@SerializedName("printing_merge_request_link_enabled")
	private boolean printingMergeRequestLinkEnabled;

	@SerializedName("topics")
	private List<Object> topics;

	@SerializedName("import_url")
	private Object importUrl;

	@SerializedName("ci_forward_deployment_rollback_allowed")
	private boolean ciForwardDeploymentRollbackAllowed;

	@SerializedName("squash_option")
	private String squashOption;

	@SerializedName("jobs_enabled")
	private boolean jobsEnabled;

	@SerializedName("environments_access_level")
	private String environmentsAccessLevel;

	@SerializedName("squash_commit_template")
	private Object squashCommitTemplate;

	@SerializedName("only_allow_merge_if_pipeline_succeeds")
	private boolean onlyAllowMergeIfPipelineSucceeds;

	@SerializedName("analytics_access_level")
	private String analyticsAccessLevel;

	@SerializedName("avatar_url")
	private Object avatarUrl;

	@SerializedName("wiki_enabled")
	private boolean wikiEnabled;

	@SerializedName("remove_source_branch_after_merge")
	private boolean removeSourceBranchAfterMerge;

	@SerializedName("star_count")
	private int starCount;

	@SerializedName("_links")
	private Links links;

	@SerializedName("request_access_enabled")
	private boolean requestAccessEnabled;

	@SerializedName("runner_token_expiration_interval")
	private Object runnerTokenExpirationInterval;

	@SerializedName("build_timeout")
	private int buildTimeout;

	@SerializedName("infrastructure_access_level")
	private String infrastructureAccessLevel;

	@SerializedName("ci_job_token_scope_enabled")
	private boolean ciJobTokenScopeEnabled;

	@SerializedName("emails_enabled")
	private boolean emailsEnabled;

	@SerializedName("visibility")
	private String visibility;

	@SerializedName("requirements_access_level")
	private String requirementsAccessLevel;

	@SerializedName("merge_requests_enabled")
	private boolean mergeRequestsEnabled;

	@SerializedName("forking_access_level")
	private String forkingAccessLevel;

	@SerializedName("merge_commit_template")
	private Object mergeCommitTemplate;

	@SerializedName("suggestion_commit_message")
	private Object suggestionCommitMessage;

	@SerializedName("container_registry_access_level")
	private String containerRegistryAccessLevel;

	@SerializedName("auto_devops_enabled")
	private boolean autoDevopsEnabled;

	@SerializedName("requirements_enabled")
	private boolean requirementsEnabled;

	@SerializedName("compliance_frameworks")
	private List<Object> complianceFrameworks;

	@SerializedName("name")
	private String name;

	@SerializedName("external_authorization_classification_label")
	private String externalAuthorizationClassificationLabel;

	@SerializedName("name_with_namespace")
	private String nameWithNamespace;

	@SerializedName("model_registry_access_level")
	private String modelRegistryAccessLevel;

	@SerializedName("autoclose_referenced_issues")
	private boolean autocloseReferencedIssues;

	@SerializedName("issues_enabled")
	private boolean issuesEnabled;

	@SerializedName("service_desk_address")
	private String serviceDeskAddress;

	@SerializedName("open_issues_count")
	private int openIssuesCount;

	@SerializedName("packages_enabled")
	private boolean packagesEnabled;

	@SerializedName("keep_latest_artifact")
	private boolean keepLatestArtifact;

	@SerializedName("description")
	private String description;

	@SerializedName("import_status")
	private String importStatus;

	@SerializedName("ci_config_path")
	private String ciConfigPath;

	@SerializedName("build_git_strategy")
	private String buildGitStrategy;

	@SerializedName("last_activity_at")
	private String lastActivityAt;

	@SerializedName("container_expiration_policy")
	private ContainerExpirationPolicy containerExpirationPolicy;

	@SerializedName("emails_disabled")
	private boolean emailsDisabled;

	@SerializedName("path_with_namespace")
	private String pathWithNamespace;

	@SerializedName("releases_access_level")
	private String releasesAccessLevel;

	@SerializedName("snippets_access_level")
	private String snippetsAccessLevel;

	@SerializedName("http_url_to_repo")
	private String httpUrlToRepo;

	@SerializedName("readme_url")
	private String readmeUrl;

	@SerializedName("merge_method")
	private String mergeMethod;

	@SerializedName("repository_object_format")
	private String repositoryObjectFormat;

	@SerializedName("web_url")
	private String webUrl;

	@SerializedName("public_jobs")
	private boolean publicJobs;

	@SerializedName("namespace")
	private Namespace namespace;

	@SerializedName("empty_repo")
	private boolean emptyRepo;

	@SerializedName("forks_count")
	private int forksCount;

	public String getSshUrlToRepo() {
		return sshUrlToRepo;
	}

	public boolean isOnlyAllowMergeIfAllDiscussionsAreResolved() {
		return onlyAllowMergeIfAllDiscussionsAreResolved;
	}

	public boolean isEnforceAuthChecksOnUploads() {
		return enforceAuthChecksOnUploads;
	}

	public String getSecurityAndComplianceAccessLevel() {
		return securityAndComplianceAccessLevel;
	}

	public String getPath() {
		return path;
	}

	public String getMonitorAccessLevel() {
		return monitorAccessLevel;
	}

	public String getRepositoryAccessLevel() {
		return repositoryAccessLevel;
	}

	public String getWikiAccessLevel() {
		return wikiAccessLevel;
	}

	public boolean isSharedRunnersEnabled() {
		return sharedRunnersEnabled;
	}

	public int getId() {
		return id;
	}

	public Object getImportType() {
		return importType;
	}

	public String getFeatureFlagsAccessLevel() {
		return featureFlagsAccessLevel;
	}

	public String getMergeRequestsAccessLevel() {
		return mergeRequestsAccessLevel;
	}

	public boolean isGroupRunnersEnabled() {
		return groupRunnersEnabled;
	}

	public Object getAllowMergeOnSkippedPipeline() {
		return allowMergeOnSkippedPipeline;
	}

	public boolean isLfsEnabled() {
		return lfsEnabled;
	}

	public boolean isCiSeparatedCaches() {
		return ciSeparatedCaches;
	}

	public boolean isResolveOutdatedDiffDiscussions() {
		return resolveOutdatedDiffDiscussions;
	}

	public String getBuildsAccessLevel() {
		return buildsAccessLevel;
	}

	public List<Object> getSharedWithGroups() {
		return sharedWithGroups;
	}

	public boolean isSecurityAndComplianceEnabled() {
		return securityAndComplianceEnabled;
	}

	public String getPagesAccessLevel() {
		return pagesAccessLevel;
	}

	public boolean isServiceDeskEnabled() {
		return serviceDeskEnabled;
	}

	public int getCreatorId() {
		return creatorId;
	}

	public boolean isCiForwardDeploymentEnabled() {
		return ciForwardDeploymentEnabled;
	}

	public String getDefaultBranch() {
		return defaultBranch;
	}

	public String getAutoDevopsDeployStrategy() {
		return autoDevopsDeployStrategy;
	}

	public String getDescriptionHtml() {
		return descriptionHtml;
	}

	public boolean isCanCreateMergeRequestIn() {
		return canCreateMergeRequestIn;
	}

	public boolean isCiAllowForkPipelinesToRunInParentProject() {
		return ciAllowForkPipelinesToRunInParentProject;
	}

	public Object getRunnersToken() {
		return runnersToken;
	}

	public boolean isRestrictUserDefinedVariables() {
		return restrictUserDefinedVariables;
	}

	public String getContainerRegistryImagePrefix() {
		return containerRegistryImagePrefix;
	}

	public String getAutoCancelPendingPipelines() {
		return autoCancelPendingPipelines;
	}

	public boolean isSnippetsEnabled() {
		return snippetsEnabled;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public int getCiDefaultGitDepth() {
		return ciDefaultGitDepth;
	}

	public String getModelExperimentsAccessLevel() {
		return modelExperimentsAccessLevel;
	}

	public String getCiPipelineVariablesMinimumOverrideRole() {
		return ciPipelineVariablesMinimumOverrideRole;
	}

	public boolean isArchived() {
		return archived;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getIssuesAccessLevel() {
		return issuesAccessLevel;
	}

	public Object getIssueBranchTemplate() {
		return issueBranchTemplate;
	}

	public boolean isWarnAboutPotentiallyUnwantedCharacters() {
		return warnAboutPotentiallyUnwantedCharacters;
	}

	public boolean isPrintingMergeRequestLinkEnabled() {
		return printingMergeRequestLinkEnabled;
	}

	public List<Object> getTopics() {
		return topics;
	}

	public Object getImportUrl() {
		return importUrl;
	}

	public boolean isCiForwardDeploymentRollbackAllowed() {
		return ciForwardDeploymentRollbackAllowed;
	}

	public String getSquashOption() {
		return squashOption;
	}

	public boolean isJobsEnabled() {
		return jobsEnabled;
	}

	public String getEnvironmentsAccessLevel() {
		return environmentsAccessLevel;
	}

	public Object getSquashCommitTemplate() {
		return squashCommitTemplate;
	}

	public boolean isOnlyAllowMergeIfPipelineSucceeds() {
		return onlyAllowMergeIfPipelineSucceeds;
	}

	public String getAnalyticsAccessLevel() {
		return analyticsAccessLevel;
	}

	public Object getAvatarUrl() {
		return avatarUrl;
	}

	public boolean isWikiEnabled() {
		return wikiEnabled;
	}

	public boolean isRemoveSourceBranchAfterMerge() {
		return removeSourceBranchAfterMerge;
	}

	public int getStarCount() {
		return starCount;
	}

	public Links getLinks() {
		return links;
	}

	public boolean isRequestAccessEnabled() {
		return requestAccessEnabled;
	}

	public Object getRunnerTokenExpirationInterval() {
		return runnerTokenExpirationInterval;
	}

	public int getBuildTimeout() {
		return buildTimeout;
	}

	public String getInfrastructureAccessLevel() {
		return infrastructureAccessLevel;
	}

	public boolean isCiJobTokenScopeEnabled() {
		return ciJobTokenScopeEnabled;
	}

	public boolean isEmailsEnabled() {
		return emailsEnabled;
	}

	public String getVisibility() {
		return visibility;
	}

	public String getRequirementsAccessLevel() {
		return requirementsAccessLevel;
	}

	public boolean isMergeRequestsEnabled() {
		return mergeRequestsEnabled;
	}

	public String getForkingAccessLevel() {
		return forkingAccessLevel;
	}

	public Object getMergeCommitTemplate() {
		return mergeCommitTemplate;
	}

	public Object getSuggestionCommitMessage() {
		return suggestionCommitMessage;
	}

	public String getContainerRegistryAccessLevel() {
		return containerRegistryAccessLevel;
	}

	public boolean isAutoDevopsEnabled() {
		return autoDevopsEnabled;
	}

	public boolean isRequirementsEnabled() {
		return requirementsEnabled;
	}

	public List<Object> getComplianceFrameworks() {
		return complianceFrameworks;
	}

	public String getName() {
		return name;
	}

	public String getExternalAuthorizationClassificationLabel() {
		return externalAuthorizationClassificationLabel;
	}

	public String getNameWithNamespace() {
		return nameWithNamespace;
	}

	public String getModelRegistryAccessLevel() {
		return modelRegistryAccessLevel;
	}

	public boolean isAutocloseReferencedIssues() {
		return autocloseReferencedIssues;
	}

	public boolean isIssuesEnabled() {
		return issuesEnabled;
	}

	public String getServiceDeskAddress() {
		return serviceDeskAddress;
	}

	public int getOpenIssuesCount() {
		return openIssuesCount;
	}

	public boolean isPackagesEnabled() {
		return packagesEnabled;
	}

	public boolean isKeepLatestArtifact() {
		return keepLatestArtifact;
	}

	public String getDescription() {
		return description;
	}

	public String getImportStatus() {
		return importStatus;
	}

	public String getCiConfigPath() {
		return ciConfigPath;
	}

	public String getBuildGitStrategy() {
		return buildGitStrategy;
	}

	public String getLastActivityAt() {
		return lastActivityAt;
	}

	public ContainerExpirationPolicy getContainerExpirationPolicy() {
		return containerExpirationPolicy;
	}

	public boolean isEmailsDisabled() {
		return emailsDisabled;
	}

	public String getPathWithNamespace() {
		return pathWithNamespace;
	}

	public String getReleasesAccessLevel() {
		return releasesAccessLevel;
	}

	public String getSnippetsAccessLevel() {
		return snippetsAccessLevel;
	}

	public String getHttpUrlToRepo() {
		return httpUrlToRepo;
	}

	public String getReadmeUrl() {
		return readmeUrl;
	}

	public String getMergeMethod() {
		return mergeMethod;
	}

	public String getRepositoryObjectFormat() {
		return repositoryObjectFormat;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public boolean isPublicJobs() {
		return publicJobs;
	}

	public Namespace getNamespace() {
		return namespace;
	}

	public boolean isEmptyRepo() {
		return emptyRepo;
	}

	public int getForksCount() {
		return forksCount;
	}
}
