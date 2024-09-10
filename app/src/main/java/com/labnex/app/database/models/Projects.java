package com.labnex.app.database.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.io.Serializable;

/**
 * @author mmarif
 */
@Entity(
		tableName = "Projects",
		foreignKeys =
				@ForeignKey(
						entity = UserAccount.class,
						parentColumns = "accountId",
						childColumns = "projectAccountId",
						onDelete = CASCADE),
		indices = {@Index("projectAccountId")})
public class Projects implements Serializable {

	@PrimaryKey(autoGenerate = true)
	private int projAutoId;

	private int projectAccountId;
	private int projectId;
	private String projectName;
	private String projectPath;
	private int mostVisited;

	public int getProjAutoId() {
		return projAutoId;
	}

	public void setProjAutoId(int projAutoId) {
		this.projAutoId = projAutoId;
	}

	public int getProjectAccountId() {
		return projectAccountId;
	}

	public void setProjectAccountId(int projectAccountId) {
		this.projectAccountId = projectAccountId;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectPath() {
		return projectPath;
	}

	public void setProjectPath(String projectPath) {
		this.projectPath = projectPath;
	}

	public int getMostVisited() {
		return mostVisited;
	}

	public void setMostVisited(int mostVisited) {
		this.mostVisited = mostVisited;
	}
}
