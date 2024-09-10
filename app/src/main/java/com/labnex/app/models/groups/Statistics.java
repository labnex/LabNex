package com.labnex.app.models.groups;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author mmarif
 */
public class Statistics implements Serializable {

	@SerializedName("wiki_size")
	private int wikiSize;

	@SerializedName("packages_size")
	private int packagesSize;

	@SerializedName("lfs_objects_size")
	private int lfsObjectsSize;

	@SerializedName("job_artifacts_size")
	private int jobArtifactsSize;

	@SerializedName("repository_size")
	private int repositorySize;

	@SerializedName("storage_size")
	private int storageSize;

	@SerializedName("uploads_size")
	private int uploadsSize;

	@SerializedName("snippets_size")
	private int snippetsSize;

	@SerializedName("pipeline_artifacts_size")
	private int pipelineArtifactsSize;

	public int getWikiSize() {
		return wikiSize;
	}

	public int getPackagesSize() {
		return packagesSize;
	}

	public int getLfsObjectsSize() {
		return lfsObjectsSize;
	}

	public int getJobArtifactsSize() {
		return jobArtifactsSize;
	}

	public int getRepositorySize() {
		return repositorySize;
	}

	public int getStorageSize() {
		return storageSize;
	}

	public int getUploadsSize() {
		return uploadsSize;
	}

	public int getSnippetsSize() {
		return snippetsSize;
	}

	public int getPipelineArtifactsSize() {
		return pipelineArtifactsSize;
	}
}
