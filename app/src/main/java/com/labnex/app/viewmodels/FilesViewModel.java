package com.labnex.app.viewmodels;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.repository.FileContents;
import com.labnex.app.models.repository.Tree;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class FilesViewModel extends ViewModel {

	private final MutableLiveData<List<Tree>> fileList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<String> nextPageToken = new MutableLiveData<>(null);
	private final MutableLiveData<FileContents> fileContents = new MutableLiveData<>();

	private long projectId;
	private String branch;
	private String path;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public LiveData<List<Tree>> getFileList() {
		return fileList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<FileContents> getFileContents() {
		return fileContents;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void loadFiles(Context ctx, long projectId, String branch, String path) {
		if (ctx == null) return;

		this.projectId = projectId;
		this.branch = branch;
		this.path = path;
		isLastPage = false;
		isLoadingMore = false;
		isLoading.setValue(true);
		fetch(ctx, null);
	}

	public void loadNextPage(Context ctx) {
		if (ctx == null || isLoadingMore || isLastPage) return;

		String pageToken = nextPageToken.getValue();
		if (pageToken == null || pageToken.isEmpty()) {
			isLastPage = true;
			return;
		}

		isLoadingMore = true;
		fetch(ctx, pageToken);
	}

	private void fetch(Context ctx, String pageToken) {
		RetrofitClient.getApiInterface(ctx)
				.getFiles(projectId, branch, pageToken, path, resultLimit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Tree>> c, @NonNull Response<List<Tree>> r) {
								ApiResponseHandler.handleFetch(
										r,
										isLoading,
										() -> {
											String linkHeader = r.headers().get("Link");
											List<Tree> body = r.body();

											if (pageToken == null) {
												fileList.setValue(
														body != null ? body : new ArrayList<>());
											} else {
												List<Tree> current =
														fileList.getValue() != null
																? new ArrayList<>(
																		fileList.getValue())
																: new ArrayList<>();
												if (body != null) current.addAll(body);
												fileList.setValue(current);
											}

											String nextToken = parseNextPageToken(linkHeader);
											nextPageToken.setValue(nextToken);

											if (nextToken == null
													|| body == null
													|| body.size() < resultLimit) {
												isLastPage = true;
											}
										},
										error);
								isLoadingMore = false;
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Tree>> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								isLoadingMore = false;
								if (pageToken == null) fileList.setValue(new ArrayList<>());
								error.setValue(t.getMessage());
							}
						});
	}

	private String parseNextPageToken(String linkHeader) {
		if (linkHeader == null || linkHeader.isEmpty()) return null;

		String[] links = linkHeader.split(",");
		for (String link : links) {
			if (link.contains("rel=\"next\"")) {
				String url = link.split(";")[0].trim();
				url = url.replaceAll("[<>]", "");
				Uri uri = Uri.parse(url);
				return uri.getQueryParameter("page_token");
			}
		}
		return null;
	}

	public void fetchFileContents(Context ctx, long projectId, String filePath, String ref) {
		if (ctx == null) return;

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getFileContents(projectId, filePath, ref)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<FileContents> c,
									@NonNull Response<FileContents> r) {
								isLoading.setValue(false);
								if (r.isSuccessful() && r.body() != null) {
									fileContents.setValue(r.body());
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<FileContents> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}
}
