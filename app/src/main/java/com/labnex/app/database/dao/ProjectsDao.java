package com.labnex.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.labnex.app.database.models.Projects;
import java.util.List;

/**
 * @author mmarif
 */
@Dao
public interface ProjectsDao {

	@Insert
	long newProject(Projects projects);

	@Query("SELECT * FROM Projects ORDER BY projAutoId ASC")
	LiveData<List<Projects>> fetchAllProjects();

	@Query("SELECT * FROM Projects WHERE projectAccountId = :projectAccountId")
	LiveData<List<Projects>> getAllProjectsByAccount(int projectAccountId);

	@Query(
			"SELECT count(projAutoId) FROM Projects WHERE projectAccountId = :projectAccountId AND projectId = :projectId AND projectName = :projectName")
	Integer checkProject(int projectAccountId, int projectId, String projectName);

	@Query(
			"SELECT * FROM Projects WHERE projectAccountId = :projectAccountId AND projectId = :projectId")
	Projects getSingleProject(int projectAccountId, int projectId);

	@Query("SELECT * FROM Projects WHERE projAutoId = :projAutoId")
	Projects fetchProjectById(int projAutoId);

	@Query("SELECT * FROM Projects WHERE projectId = :projectId")
	Projects fetchByProjectId(int projectId);

	@Query(
			"SELECT * FROM Projects WHERE projectId = :projectId AND projectAccountId = :projectAccountId")
	Projects fetchProjectByAccountIdByProjectIdDao(int projectId, int projectAccountId);

	@Query(
			"UPDATE Projects SET projectName = :projectName, projectPath = :projectPath  WHERE projectId = :projectId")
	void updateProjectNameAndPath(String projectName, String projectPath, int projectId);

	@Query("DELETE FROM Projects WHERE projectId = :projectId")
	void deleteProject(int projectId);

	@Query(
			"DELETE FROM Projects WHERE projectName = :projectName AND projectAccountId = :currentActiveAccountId")
	void deleteProjectByName(int currentActiveAccountId, String projectName);

	@Query("DELETE FROM Projects WHERE projectAccountId = :projectAccountId")
	void deleteProjectsByAccount(int projectAccountId);

	@Query("UPDATE Projects SET mostVisited = :mostVisited WHERE projectId = :projectId")
	void updateProjectMostVisited(int mostVisited, int projectId);

	@Query(
			"SELECT * FROM Projects WHERE mostVisited > 0  AND projectAccountId = :projectAccountId ORDER BY mostVisited DESC LIMIT 50")
	LiveData<List<Projects>> fetchAllMostVisited(int projectAccountId);

	@Query(
			"SELECT * FROM Projects WHERE mostVisited > 0 AND projectId != 0 AND projectAccountId = :projectAccountId ORDER BY mostVisited DESC LIMIT :limit")
	LiveData<List<Projects>> fetchMostVisitedWithLimit(int projectAccountId, int limit);

	@Query("UPDATE Projects SET mostVisited = 0 WHERE projectAccountId = :projectAccountId")
	void resetAllProjectsMostVisited(int projectAccountId);

	@Query("DELETE FROM Projects")
	void deleteAllProjects();

	@Query("SELECT count(projectId) FROM Projects")
	Integer getCount();
}
