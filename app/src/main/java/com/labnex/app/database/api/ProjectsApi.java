package com.labnex.app.database.api;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.labnex.app.database.dao.ProjectsDao;
import com.labnex.app.database.models.Projects;
import java.util.List;

/**
 * @author mmarif
 */
public class ProjectsApi extends BaseApi {

	private final ProjectsDao projectsDao;

	ProjectsApi(Context context) {
		super(context);
		projectsDao = labnexDatabase.projectsDao();
	}

	public long insertProject(
			int projectAccountId,
			int projectId,
			String projectName,
			String projectPath,
			int mostVisited) {

		Projects projects = new Projects();
		projects.setProjectAccountId(projectAccountId);
		projects.setProjectId(projectId);
		projects.setProjectName(projectName);
		projects.setProjectPath(projectPath);
		projects.setMostVisited(mostVisited);

		return insertProjectAsyncTask(projects);
	}

	public long insertProjectAsyncTask(Projects projects) {
		return projectsDao.newProject(projects);
	}

	public Projects getProject(int projectAccountId, int projectId) {
		return projectsDao.getSingleProject(projectAccountId, projectId);
	}

	public LiveData<List<Projects>> getAllProjects() {
		return projectsDao.fetchAllProjects();
	}

	public LiveData<List<Projects>> getAllProjectsByAccount(int projectAccountId) {
		return projectsDao.getAllProjectsByAccount(projectAccountId);
	}

	public Integer checkProject(int projectAccountId, int projectId, String projectName) {
		return projectsDao.checkProject(projectAccountId, projectId, projectName);
	}

	public Projects fetchProjectById(int projAutoId) {
		return projectsDao.fetchProjectById(projAutoId);
	}

	public Projects fetchByProjectId(int projectId) {
		return projectsDao.fetchByProjectId(projectId);
	}

	public Projects fetchProjectByAccountIdByProjectIdDao(int projectId, int projectAccountId) {
		return projectsDao.fetchProjectByAccountIdByProjectIdDao(projectId, projectAccountId);
	}

	public void updateProjectNameAndPath(String projectName, String projectPath, int projectId) {
		executorService.execute(
				() -> projectsDao.updateProjectNameAndPath(projectName, projectPath, projectId));
	}

	public void deleteProjectsByAccount(final int projectAccountId) {
		executorService.execute(() -> projectsDao.deleteProjectsByAccount(projectAccountId));
	}

	public void deleteProject(final int projectId) {
		executorService.execute(() -> projectsDao.deleteProject(projectId));
	}

	public void deleteProjectByName(final int currentActiveAccountId, final String projectName) {
		executorService.execute(
				() -> projectsDao.deleteProjectByName(currentActiveAccountId, projectName));
	}

	public void updateProjectMostVisited(int mostVisited, int projectId) {
		executorService.execute(() -> projectsDao.updateProjectMostVisited(mostVisited, projectId));
	}

	public void resetAllProjectsMostVisited(int projectAccountId) {
		executorService.execute(() -> projectsDao.resetAllProjectsMostVisited(projectAccountId));
	}

	public LiveData<List<Projects>> fetchAllMostVisited(int projectAccountId) {
		return projectsDao.fetchAllMostVisited(projectAccountId);
	}

	public LiveData<List<Projects>> fetchMostVisitedWithLimit(int projectAccountId, int limit) {
		return projectsDao.fetchMostVisitedWithLimit(projectAccountId, limit);
	}

	public void deleteAllProjects() {
		executorService.execute(projectsDao::deleteAllProjects);
	}

	public Integer getCount() {
		return projectsDao.getCount();
	}
}
