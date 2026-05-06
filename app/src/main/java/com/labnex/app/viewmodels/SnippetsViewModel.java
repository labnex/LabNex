package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.models.snippets.SnippetsItem;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import org.apache.commons.io.FilenameUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class SnippetsViewModel extends ViewModel {

	private final MutableLiveData<List<SnippetsItem>> snippetList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<SnippetsItem> snippetDetail = new MutableLiveData<>();
	private final MutableLiveData<String> fileContent = new MutableLiveData<>();
	private final MutableLiveData<String> fileExtension = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isDetailLoading = new MutableLiveData<>(false);

	private int currentPage = 1;
	private int resultLimit;
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;
	private String snippetTitle;

	public LiveData<List<SnippetsItem>> getSnippetList() {
		return snippetList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<SnippetsItem> getSnippetDetail() {
		return snippetDetail;
	}

	public LiveData<String> getFileContent() {
		return fileContent;
	}

	public LiveData<String> getFileExtension() {
		return fileExtension;
	}

	public LiveData<Boolean> getIsDetailLoading() {
		return isDetailLoading;
	}

	public void setResultLimit(int limit) {
		this.resultLimit = limit;
	}

	public String getSnippetTitle() {
		return snippetTitle;
	}

	public void loadSnippets(Context ctx) {
		currentPage = 1;
		isLastPage = false;
		isLoadingMore = false;
		isLoading.setValue(true);
		fetch(ctx, 1);
	}

	public void loadNextPage(Context ctx) {
		if (isLoadingMore || isLastPage) return;
		isLoadingMore = true;
		currentPage++;
		fetch(ctx, currentPage);
	}

	private void fetch(Context ctx, int page) {
		Call<List<SnippetsItem>> call =
				RetrofitClient.getApiInterface(ctx).getSnippets(resultLimit, page);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<SnippetsItem>> call,
							@NonNull Response<List<SnippetsItem>> response) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (response.isSuccessful()) {
							String totalHeader = response.headers().get("x-total");
							List<SnippetsItem> body = response.body();
							List<SnippetsItem> current =
									(page == 1)
											? new ArrayList<>()
											: snippetList.getValue() != null
													? new ArrayList<>(snippetList.getValue())
													: new ArrayList<>();
							if (body != null) current.addAll(body);
							snippetList.setValue(current);
							checkLastPage(
									body != null ? body.size() : 0, totalHeader, current.size());
						} else {
							if (page == 1) snippetList.setValue(new ArrayList<>());
							if (response.code() == 401) error.setValue("auth_error");
							else if (response.code() == 403) error.setValue("access_forbidden_403");
							else error.setValue("generic_error");
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<SnippetsItem>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) snippetList.setValue(new ArrayList<>());
						error.setValue(t.getMessage());
					}
				});
	}

	private void checkLastPage(int bodySize, String totalHeader, int fullListSize) {
		if (bodySize < resultLimit) isLastPage = true;
		else if (totalHeader != null) {
			try {
				if (fullListSize >= Integer.parseInt(totalHeader)) isLastPage = true;
			} catch (NumberFormatException ignored) {
			}
		}
	}

	public void clearError() {
		error.setValue(null);
	}

	public void deleteSnippet(Context ctx, int snippetId, int position) {
		RetrofitClient.getApiInterface(ctx)
				.deleteSnippet(snippetId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> c, @NonNull Response<Void> r) {
								if (r.isSuccessful()) {
									List<SnippetsItem> current = snippetList.getValue();
									if (current != null) {
										current.remove(position);
										snippetList.setValue(new ArrayList<>(current));
									}
									error.setValue("deleted");
								} else {
									error.setValue("delete_error");
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> c, @NonNull Throwable t) {
								error.setValue("delete_error");
							}
						});
	}

	public void loadSnippetDetail(Context ctx, int snippetId) {
		isDetailLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getSnippet(snippetId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<SnippetsItem> c,
									@NonNull Response<SnippetsItem> r) {
								isDetailLoading.setValue(false);
								if (r.isSuccessful() && r.body() != null) {
									snippetDetail.setValue(r.body());
									snippetTitle = r.body().getTitle();
								} else {
									error.setValue("generic_error");
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<SnippetsItem> c, @NonNull Throwable t) {
								isDetailLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void loadSnippetFileContent(
			Context ctx, int snippetId, String ref, String filePath, String fileName) {
		isDetailLoading.setValue(true);
		fileExtension.setValue(FilenameUtils.getExtension(fileName));
		RetrofitClient.getApiInterface(ctx)
				.getSnippetFileContent(snippetId, ref, filePath)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<ResponseBody> c,
									@NonNull Response<ResponseBody> r) {
								isDetailLoading.setValue(false);
								String content = "";
								if (r.isSuccessful() && r.body() != null) {
									try {
										content = r.body().string();
									} catch (IOException e) {
										content = "";
									}
								}
								fileContent.setValue(content);
							}

							@Override
							public void onFailure(
									@NonNull Call<ResponseBody> c, @NonNull Throwable t) {
								isDetailLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void clearSnippetDetail() {
		snippetDetail.setValue(null);
	}

	public void clearFileContent() {
		fileContent.setValue(null);
	}
}
