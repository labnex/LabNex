package com.labnex.app.interfaces;

import com.labnex.app.models.approvals.Approvals;
import com.labnex.app.models.approvals.Rule;
import com.labnex.app.models.branches.Branches;
import com.labnex.app.models.broadcast_messages.Messages;
import com.labnex.app.models.commits.Commits;
import com.labnex.app.models.commits.CrudeCommit;
import com.labnex.app.models.commits.Diff;
import com.labnex.app.models.events.Events;
import com.labnex.app.models.groups.CreateGroup;
import com.labnex.app.models.groups.GroupsItem;
import com.labnex.app.models.issues.CrudeIssue;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.labels.CrudeLabel;
import com.labnex.app.models.labels.Labels;
import com.labnex.app.models.merge_requests.CrudeMergeRequest;
import com.labnex.app.models.merge_requests.MergeRequests;
import com.labnex.app.models.metadata.Metadata;
import com.labnex.app.models.milestone.CrudeMilestone;
import com.labnex.app.models.milestone.Milestones;
import com.labnex.app.models.notes.CreateNote;
import com.labnex.app.models.notes.Notes;
import com.labnex.app.models.personal_access_tokens.PersonalAccessTokens;
import com.labnex.app.models.projects.CrudeProject;
import com.labnex.app.models.projects.Projects;
import com.labnex.app.models.projects.Stars;
import com.labnex.app.models.release.CrudeRelease;
import com.labnex.app.models.release.Releases;
import com.labnex.app.models.repository.CrudeFile;
import com.labnex.app.models.repository.FileContents;
import com.labnex.app.models.repository.Tree;
import com.labnex.app.models.snippets.SnippetCreateModel;
import com.labnex.app.models.snippets.SnippetsItem;
import com.labnex.app.models.tags.TagsItem;
import com.labnex.app.models.templates.Template;
import com.labnex.app.models.templates.Templates;
import com.labnex.app.models.todo.ToDoItem;
import com.labnex.app.models.user.User;
import com.labnex.app.models.users.Users;
import com.labnex.app.models.wikis.CrudeWiki;
import com.labnex.app.models.wikis.Wiki;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author mmarif
 */
public interface ApiInterface {

	// User related endpoints
	@GET("user") // user API
	Call<User> getCurrentUser();

	@GET("users") // users API
	Call<Users> getUsers();

	@GET("users/{id}") // get single user (profile)
	Call<User> getSingleUser(@Path("id") int id);

	@GET("personal_access_tokens/self") // personal access token info
	Call<PersonalAccessTokens> getPersonalAccessTokenInfo();

	@GET("projects?owned=true") // get user projects
	Call<List<Projects>> getProjects(@Query("per_page") int per_page, @Query("page") int page);

	@GET("users/{user_id}/starred_projects") // get user starred projects
	Call<List<Projects>> getStarredProjects(
			@Path("user_id") int user_id, @Query("per_page") int per_page, @Query("page") int page);

	// Group endpoints
	@GET("groups") // get groups
	Call<List<GroupsItem>> getGroups(
			@Query("statistics") boolean statistics,
			@Query("order_by") String orderBy,
			@Query("sort") String sort,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@GET("groups/{id}") // get a group
	Call<GroupsItem> getGroup(@Path("id") long id);

	@GET("groups/{id}/subgroups") // get sub groups
	Call<List<GroupsItem>> getSubGroups(
			@Path("id") int id, @Query("per_page") int per_page, @Query("page") int page);

	@POST("groups") // create a group
	Call<GroupsItem> createGroup(@Body CreateGroup body);

	@PUT("groups/{id}") // update a group
	Call<GroupsItem> updateGroup(@Path("id") long id, @Body CreateGroup body);

	@GET("groups/{id}/projects") // get group projects
	Call<List<Projects>> getGroupProjects(
			@Path("id") int id, @Query("per_page") int per_page, @Query("page") int page);

	@GET("groups/{id}/labels") // get group labels
	Call<List<Labels>> getGroupLabels(
			@Path("id") long id,
			@Query("with_counts") boolean with_counts,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@POST("groups/{id}/labels") // create a group label
	Call<Labels> createGroupLabel(@Path("id") long id, @Body CrudeLabel body);

	@DELETE("groups/{id}/labels/{label_id}") // delete a group label
	Call<Void> deleteGroupLabel(@Path("id") long id, @Path("label_id") int label_id);

	@PUT("groups/{id}/labels/{label_id}") // update a group label
	Call<Labels> updateGroupLabel(
			@Path("id") long id, @Path("label_id") long label_id, @Body CrudeLabel body);

	@GET("groups/{id}/members") // get a group members
	Call<List<User>> getGroupMembers(
			@Path("id") long id, @Query("per_page") int per_page, @Query("page") int page);

	@GET("groups/{id}/members/{user_id}") // get a group member
	Call<User> getGroupMember(@Path("id") long id, @Path("user_id") long userId);

	@GET("groups/{id}/issues") // get group issues
	Call<List<Issues>> getGroupIssues(
			@Path("id") int groupId,
			@Query("state") String state,
			@Query("search") String search,
			@Query("per_page") int perPage,
			@Query("page") int page,
			@Query("scope") String scope);

	@GET("groups/{id}/merge_requests") // get a group merge requests
	Call<List<MergeRequests>> getGroupMergeRequests(
			@Path("id") int groupId,
			@Query("state") String state,
			@Query("search") String search,
			@Query("scope") String scope,
			@Query("sort") String sort,
			@Query("order_by") String orderBy,
			@Query("per_page") int perPage,
			@Query("page") int page);

	@POST("groups/{id}/issues")
	Call<Issues> createGroupIssue(@Path("id") long id, @Body CrudeIssue body);

	@PUT("groups/{id}/issues/{issue_iid}")
	Call<Issues> updateGroupIssue(
			@Path("id") long id, @Path("issue_iid") long issueIid, @Body CrudeIssue body);

	// Project endpoints
	@GET("projects/{id}") // get a single project details
	Call<Projects> getProjectInfo(@Path("id") long id);

	@GET("projects/{id}") // get a single project details by path
	Call<Projects> getProjectByPath(@Path("id") String id);

	@GET("projects/{id}/approval_rules") // get project approval rules
	Call<List<Rule>> getApprovalRules(@Path("id") int id);

	@GET("projects/{id}/repository/branches") // get project branches
	Call<List<Branches>> getProjectBranches(
			@Path("id") int id, @Query("per_page") int per_page, @Query("page") int page);

	@POST("projects/{id}/repository/branches") // create a branch
	Call<Branches> createBranch(
			@Path("id") int id, @Query("branch") String branch, @Query("ref") String ref);

	@GET("projects/{id}/repository/branches/{branch}") // get a branch
	Call<Branches> getBranch(@Path("id") int id, @Path("branch") String branch);

	@GET("projects/{id}/members") // get a project members
	Call<List<User>> getProjectMembers(
			@Path("id") long id, @Query("per_page") int per_page, @Query("page") int page);

	@GET("projects/{id}/members/{user_id}") // get a project member
	Call<User> getProjectMember(@Path("id") long projectId, @Path("user_id") long userId);

	@GET("projects/{id}/starrers") // get a project starrers
	Call<List<Stars>> getProjectStarrers(
			@Path("id") long id, @Query("per_page") int per_page, @Query("page") int page);

	@POST("projects/{id}/star") // star a project
	Call<Projects> starProject(@Path("id") int id);

	@POST("projects/{id}/unstar") // unstar a project
	Call<Projects> unstarProject(@Path("id") int id);

	@GET("projects/{id}/repository/files/{filename}") // get a project file
	Call<FileContents> getProjectFileContent(
			@Path("id") int id, @Path("filename") String filename, @Query("ref") String ref);

	@GET("projects/{id}/repository/tree") // get a project tree (files/folders)
	Call<Tree> getProjectTree(
			@Path("id") int id,
			@Query("ref") String ref,
			@Query("pagination") String pagination,
			@Query("per_page") int per_page,
			@Query("page_token") String page_token);

	@GET("projects/{id}/labels") // get project labels
	Call<List<Labels>> getProjectLabels(
			@Path("id") long id,
			@Query("with_counts") boolean with_counts,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@DELETE("projects/{id}/labels/{label_id}") // delete a project label
	Call<Void> deleteProjectLabel(@Path("id") long id, @Path("label_id") int label_id);

	@POST("projects/{id}/labels") // create a project label
	Call<Labels> createProjectLabel(@Path("id") long id, @Body CrudeLabel body);

	@PUT("projects/{id}/labels/{label_id}") // update a project label
	Call<Labels> updateProjectLabel(
			@Path("id") long id, @Path("label_id") long label_id, @Body CrudeLabel body);

	@GET("projects/{id}/labels/{label_id}") // get a project label
	Call<Labels> getProjectLabel(@Path("id") int id, @Path("label_id") String label_id);

	@GET("projects/{id}/wikis") // get project wikis
	Call<List<Wiki>> getProjectWikis(
			@Path("id") long id,
			@Query("with_content") int with_content,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@POST("projects/{id}/wikis") // create a wiki page
	Call<Wiki> createWikiPage(@Path("id") long id, @Body CrudeWiki body);

	@DELETE("projects/{id}/wikis/{slug}") // delete a wiki page
	Call<Void> deleteWikiPage(@Path("id") long id, @Path("slug") String slug);

	@PUT("projects/{id}/wikis/{slug}") // update a wiki page
	Call<Wiki> updateWikiPage(@Path("id") long id, @Path("slug") String slug, @Body CrudeWiki body);

	@GET("projects/{id}/releases") // get project releases
	Call<List<Releases>> getProjectReleases(
			@Path("id") long id, @Query("per_page") int per_page, @Query("page") int page);

	@DELETE("projects/{id}/releases/{tag_name}") // delete a release
	Call<Void> deleteRelease(@Path("id") long id, @Path("tag_name") String tag_name);

	@POST("projects/{id}/releases") // create a release
	Call<Releases> createRelease(@Path("id") long id, @Body CrudeRelease body);

	@PUT("projects/{id}/releases/{tag_name}") // update a release
	Call<Releases> updateRelease(
			@Path("id") long id, @Path("tag_name") String tagName, @Body CrudeRelease body);

	@GET("projects/{id}/milestones") // get project milestones
	Call<List<Milestones>> getProjectMilestones(
			@Path("id") long id,
			@Query("state") String state,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@POST("projects/{id}/milestones") // create a milestone
	Call<Milestones> createMilestone(@Path("id") long id, @Body CrudeMilestone body);

	@PUT("projects/{id}/milestones/{milestone_id}")
	Call<Milestones> updateMilestone(
			@Path("id") long id, @Path("milestone_id") long milestoneId, @Body CrudeMilestone body);

	@DELETE("projects/{id}/milestones/{milestone_id}") // delete a milestone
	Call<Void> deleteMilestone(@Path("id") long id, @Path("milestone_id") long milestone_id);

	@GET("projects/{id}/languages") // get project lang stats
	Call<Map<String, Float>> getProjectLanguages(@Path("id") int id);

	@GET("projects/{id}/repository/commits") // get a project commits
	Call<List<Commits>> getProjectCommits(
			@Path("id") int id,
			@Query("ref_name") String branch,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@GET("projects/{id}/repository/commits/{sha}/diff") // get a commit diffs
	Call<List<Diff>> getCommitDiffs(
			@Path("id") int id,
			@Path("sha") String sha,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@POST("projects") // create new project
	Call<Projects> createProject(@Body CrudeProject body);

	@GET("projects/{id}/forks") // get a project forks
	Call<List<Projects>> getProjectForks(
			@Path("id") int id, @Query("per_page") int per_page, @Query("page") int page);

	@POST("projects/{id}/fork") // fork a project
	Call<Projects> forkProject(@Path("id") long id, @Body CrudeProject body);

	// Merge request endpoints
	@GET("projects/{id}/merge_requests") // get project merge requests
	Call<List<MergeRequests>> getProjectMergeRequests(
			@Path("id") int id,
			@Query("state") String state,
			@Query("search") String search,
			@Query("sort") String sort,
			@Query("order_by") String orderBy,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@GET("projects/{id}/merge_requests/{merge_request_iid}") // get single merge request by id
	Call<MergeRequests> getMergeRequest(
			@Path("id") long projectId, @Path("merge_request_iid") long mergeRequestIid);

	@GET("projects/{id}/merge_requests/{merge_request_iid}/notes?sort=asc") // get merge request
	// notes/comments
	Call<List<Notes>> getMergeRequestNotes(
			@Path("id") int id,
			@Path("merge_request_iid") int merge_request_iid,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@POST("projects/{id}/merge_requests/{merge_request_iid}/notes") // create merge request
	// notes/comments
	Call<Notes> createMergeRequestNote(
			@Path("id") int id,
			@Path("merge_request_iid") int merge_request_iid,
			@Body CreateNote body);

	@GET("projects/{id}/merge_requests/{merge_request_iid}/approvals")
	// approvals
	Call<Approvals> getApprovals(
			@Path("id") int id, @Path("merge_request_iid") int merge_request_iid);

	@POST("projects/{id}/merge_requests/{merge_request_iid}/approve")
	// approve
	Call<Approvals> approve(@Path("id") int id, @Path("merge_request_iid") int merge_request_iid);

	@POST("projects/{id}/merge_requests/{merge_request_iid}/unapprove")
	// unapprove
	Call<Approvals> revokeApproval(
			@Path("id") int id, @Path("merge_request_iid") int merge_request_iid);

	@GET("merge_requests") // get user merge requests
	Call<List<MergeRequests>> getMergeRequests(
			@Query("scope") String scope,
			@Query("state") String state,
			@Query("search") String search,
			@Query("sort") String sort,
			@Query("order_by") String orderBy,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@GET("projects/{id}/merge_requests/{merge_request_iid}/commits") // get merge request commits
	Call<List<Commits>> getMergeRequestCommits(
			@Path("id") int id,
			@Path("merge_request_iid") int merge_request_iid,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@PUT("projects/{id}/merge_requests/{merge_request_iid}/merge") // merge a merge request
	Call<MergeRequests> mergeMergeRequest(
			@Path("id") int id,
			@Path("merge_request_iid") int merge_request_iid,
			@Body CrudeMergeRequest body);

	@POST("projects/{id}/merge_requests") // create merge request
	Call<MergeRequests> createMergeRequest(@Path("id") long id, @Body CrudeMergeRequest body);

	@PUT("projects/{id}/merge_requests/{merge_request_iid}") // update/edit/close/reopen merge
	// request
	Call<MergeRequests> updateMergeRequest(
			@Path("id") long id,
			@Path("merge_request_iid") int issue_iid,
			@Body CrudeMergeRequest body);

	// Issue endpoints
	@GET("projects/{id}/issues") // get project issues
	Call<List<Issues>> getProjectIssues(
			@Path("id") int id,
			@Query("state") String state,
			@Query("search") String search,
			@Query("scope") String scope,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@GET("projects/{id}/issues/{issue_iid}") // get issue by id
	Call<Issues> getIssue(@Path("id") long projectId, @Path("issue_iid") long issueIid);

	@GET("issues") // get user issues
	Call<List<Issues>> getIssues(
			@Query("scope") String scope,
			@Query("state") String state,
			@Query("search") String search,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@GET("projects/{id}/issues/{issue_iid}/notes?sort=asc") // get an issue notes/comments
	Call<List<Notes>> getIssueNotes(
			@Path("id") int id,
			@Path("issue_iid") int issue_iid,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@POST("projects/{id}/issues/{issue_iid}/notes") // create an issue note/comment
	Call<Notes> createIssueNote(
			@Path("id") int id, @Path("issue_iid") int issue_iid, @Body CreateNote body);

	@POST("projects/{id}/issues") // create a new issue
	Call<Issues> createIssue(@Path("id") long id, @Body CrudeIssue body);

	@PUT("projects/{id}/issues/{issue_iid}") // update/edit/close/reopen an issue
	Call<Issues> updateIssue(
			@Path("id") long id, @Path("issue_iid") long issue_iid, @Body CrudeIssue body);

	// Instance meta
	@GET("broadcast_messages?page=1&per_page=10") // get broadcast messages
	Call<List<Messages>> getBroadcastMessage();

	@GET("metadata") // metadata / version API
	Call<Metadata> getMetadata();

	// Events, search and activities
	@GET("events?sort=desc") // get all events
	Call<List<Events>> getEvents(
			@Query("per_page") int per_page,
			@Query("page") int page,
			@Query("target_type") String targetType);

	@GET("search?scope=projects&sort=desc&order_by=created_at") // search for projects
	Call<List<Projects>> searchProjects(
			@Query("search") String search,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@GET("search?scope=issues&sort=desc&order_by=created_at") // search for issues
	Call<List<Issues>> searchIssues(
			@Query("search") String search,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@GET("search?scope=merge_requests&sort=desc&order_by=created_at") // search for mr
	Call<List<MergeRequests>> searchMergeRequests(
			@Query("search") String search,
			@Query("per_page") int per_page,
			@Query("page") int page);

	@GET("search?scope=users&sort=desc&order_by=created_at") // search for users
	Call<List<User>> searchUsers(
			@Query("search") String search,
			@Query("per_page") int per_page,
			@Query("page") int page);

	// Files and repository
	@GET("projects/{id}/repository/tree?pagination=keyset") // get all files (tree)
	Call<List<Tree>> getFiles(
			@Path("id") int id,
			@Query("ref") String ref,
			@Query("page_token") String page_token,
			@Query("path") String path,
			@Query("per_page") int per_page);

	@GET("projects/{id}/repository/files/{filename}") // get file contents
	Call<FileContents> getFileContents(
			@Path("id") int id, @Path("filename") String filename, @Query("ref") String ref);

	@POST("projects/{id}/repository/files/{filename}") // create new file
	Call<FileContents> createFile(
			@Path("id") int id, @Path("filename") String filename, @Body CrudeFile body);

	@PUT("projects/{id}/repository/files/{filename}") // edit a file
	Call<FileContents> updateFile(
			@Path("id") int id, @Path("filename") String filename, @Body CrudeFile body);

	@HTTP(
			method = "DELETE",
			path = "projects/{id}/repository/files/{filename}",
			hasBody = true) // delete a file
	Call<Void> deleteFile(
			@Path("id") int id, @Path("filename") String filename, @Body CrudeFile body);

	// Templates
	@GET("projects/{id}/templates/{type}") // get all templates
	Call<List<Templates>> getTemplates(@Path("id") long id, @Path("type") String type);

	@GET("projects/{id}/templates/{type}/{name}") // get a template
	Call<Template> getTemplate(
			@Path("id") long id, @Path("type") String type, @Path("name") String name);

	// Snippets
	@GET("snippets") // get all snippets
	Call<List<SnippetsItem>> getSnippets(@Query("per_page") int per_page, @Query("page") int page);

	@GET("snippets/{id}") // get a snippet
	Call<SnippetsItem> getSnippet(@Path("id") long id);

	@GET("snippets/{id}/files/{ref}/{file_path}/raw")
	@Headers("Accept: text/plain")
	Call<ResponseBody> getSnippetFileContent(
			@Path("id") long id,
			@Path(value = "ref", encoded = true) String ref,
			@Path(value = "file_path", encoded = true) String filePath);

	@DELETE("snippets/{id}")
	Call<Void> deleteSnippet(@Path("id") long id);

	@POST("snippets")
	Call<SnippetsItem> createSnippet(@Body SnippetCreateModel model);

	@PUT("snippets/{id}")
	Call<SnippetsItem> updateSnippet(@Path("id") long id, @Body SnippetCreateModel model);

	// Tags
	@GET("projects/{id}/repository/tags") // get all project tags
	Call<List<TagsItem>> getProjectTags(
			@Path("id") long id, @Query("per_page") int perPage, @Query("page") int page);

	@POST("projects/{id}/repository/tags") // create a tag
	Call<TagsItem> createProjectTag(
			@Path("id") long projectId,
			@Query("tag_name") String tagName,
			@Query("ref") String ref,
			@Query("message") String message);

	@DELETE("projects/{id}/repository/tags/{tag_name}") // delete a tag
	Call<Void> deleteProjectTag(@Path("id") long projectId, @Path("tag_name") String tagName);

	// Todos
	@GET("todos")
	Call<List<ToDoItem>> getAllTodos(
			@Query("type") String type,
			@Query("state") String state,
			@Query("action") String action);

	@POST("todos/{id}/mark_as_done")
	Call<ToDoItem> markTodoAsDone(@Path("id") long todoId);

	@POST("todos/mark_as_done")
	Call<ToDoItem> markAllTodoAsDone();

	@POST("projects/{id}/repository/commits") // create files via commit api
	Call<Commits> createCommit(@Path("id") long id, @Body CrudeCommit commit);
}
