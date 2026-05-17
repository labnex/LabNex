package com.labnex.app.helpers;

import com.labnex.app.models.projects.Projects;

/**
 * @author mmarif
 */
public class AccessLevel {

	public static final int GUEST = 10;
	public static final int REPORTER = 20;
	public static final int DEVELOPER = 30;
	public static final int MAINTAINER = 40;
	public static final int OWNER = 50;

	public static int getUserAccessLevel(Projects project) {
		if (project == null || project.getPermissions() == null) return 0;
		int projectLevel =
				project.getPermissions().getProjectAccess() != null
						? project.getPermissions().getProjectAccess().getAccessLevel()
						: 0;
		int groupLevel =
				project.getPermissions().getGroupAccess() != null
						? project.getPermissions().getGroupAccess().getAccessLevel()
						: 0;
		return Math.max(projectLevel, groupLevel);
	}

	private static boolean isArchivedOrNull(Projects p) {
		return p == null || p.isArchived();
	}

	private static boolean hasAccess(String accessLevel, int userLevel, int requiredLevel) {
		if ("disabled".equals(accessLevel)) return false;
		if ("private".equals(accessLevel)) return userLevel >= requiredLevel;
		return true;
	}

	public static boolean canCreateIssue(Projects p, int userLevel) {
		if (isArchivedOrNull(p) || !p.isIssuesEnabled()) return false;
		return hasAccess(p.getIssuesAccessLevel(), userLevel, REPORTER);
	}

	public static boolean canCreateMergeRequest(Projects p, int userLevel) {
		if (isArchivedOrNull(p) || !p.isMergeRequestsEnabled()) return false;
		return hasAccess(p.getMergeRequestsAccessLevel(), userLevel, DEVELOPER);
	}

	public static boolean canPushToRepo(Projects p, int userLevel) {
		if (isArchivedOrNull(p)) return false;
		return hasAccess(p.getRepositoryAccessLevel(), userLevel, DEVELOPER);
	}

	public static boolean canCreateBranch(Projects p, int userLevel) {
		if (isArchivedOrNull(p)) return false;
		return userLevel >= DEVELOPER;
	}

	public static boolean canCreateTag(Projects p, int userLevel) {
		if (isArchivedOrNull(p)) return false;
		return userLevel >= DEVELOPER;
	}

	public static boolean canCreateRelease(Projects p, int userLevel) {
		if (isArchivedOrNull(p)) return false;
		return userLevel >= DEVELOPER;
	}

	public static boolean canCreateMilestone(Projects p, int userLevel) {
		if (isArchivedOrNull(p)) return false;
		return userLevel >= DEVELOPER;
	}

	public static boolean canCreateLabel(Projects p, int userLevel) {
		if (isArchivedOrNull(p)) return false;
		return userLevel >= DEVELOPER;
	}

	public static boolean canCreateWiki(Projects p, int userLevel) {
		if (isArchivedOrNull(p) || !p.isWikiEnabled()) return false;
		return userLevel >= DEVELOPER;
	}

	public static boolean canFork(Projects p, int userLevel) {
		if (isArchivedOrNull(p)) return false;
		return hasAccess(p.getForkingAccessLevel(), userLevel, REPORTER);
	}

	public static boolean canViewIssues(Projects p, int userLevel) {
		if (p == null || !p.isIssuesEnabled()) return false;
		return hasAccess(p.getIssuesAccessLevel(), userLevel, GUEST);
	}

	public static boolean canViewMergeRequests(Projects p, int userLevel) {
		if (p == null || !p.isMergeRequestsEnabled()) return false;
		return hasAccess(p.getMergeRequestsAccessLevel(), userLevel, GUEST);
	}
}
