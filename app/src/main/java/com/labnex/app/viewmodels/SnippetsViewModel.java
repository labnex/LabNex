package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.bottomsheets.CreateSnippetBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.snippets.FilesItem;
import com.labnex.app.models.snippets.SnippetCreateModel;
import com.labnex.app.models.snippets.SnippetsItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class SnippetsViewModel extends ViewModel {

	private final MutableLiveData<List<SnippetsItem>> snippetList = new MutableLiveData<>(null);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<ViewerPayload> singleViewerPayload = new MutableLiveData<>();
	private final MutableLiveData<SnippetsItem> multiFileList = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);

	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;
	private SnippetsItem currentSnippet;

	public LiveData<List<SnippetsItem>> getSnippetList() {
		return snippetList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<ViewerPayload> getSingleViewerPayload() {
		return singleViewerPayload;
	}

	public LiveData<SnippetsItem> getMultiFileList() {
		return multiFileList;
	}

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public LiveData<Boolean> getActionSuccess() {
		return actionSuccess;
	}

	public SnippetsItem getCurrentSnippet() {
		return currentSnippet;
	}

	public void clearActionSuccess() {
		actionSuccess.setValue(false);
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
							@NonNull Call<List<SnippetsItem>> c,
							@NonNull Response<List<SnippetsItem>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<SnippetsItem> body = r.body();
									List<SnippetsItem> current =
											(page == 1)
													? new ArrayList<>()
													: snippetList.getValue() != null
															? new ArrayList<>(
																	snippetList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									snippetList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(
							@NonNull Call<List<SnippetsItem>> c, @NonNull Throwable t) {
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

	public void deleteSnippet(Context ctx, long snippetId, int position) {
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

	public void loadSnippetAndOpen(Context ctx, long snippetId) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getSnippet(snippetId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<SnippetsItem> c,
									@NonNull Response<SnippetsItem> r) {
								if (r.isSuccessful() && r.body() != null) {
									SnippetsItem s = r.body();
									currentSnippet = s;
									List<FilesItem> files = s.getFiles();

									if (files != null && files.size() == 1) {
										fetchSingleFile(ctx, s.getId(), files.get(0));
									} else if (files != null && files.size() > 1) {
										isLoading.setValue(false);
										multiFileList.setValue(s);
									} else if (s.getFileName() != null) {
										FilesItem f = new FilesItem();
										f.setPath(s.getFileName());
										f.setRawUrl(null);
										fetchSingleFile(ctx, s.getId(), f);
									}
								} else {
									isLoading.setValue(false);
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<SnippetsItem> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	private void fetchSingleFile(Context ctx, long snippetId, FilesItem file) {
		String fileName = file.getPath();
		String rawUrl = file.getRawUrl();
		String ref = "main";
		String filePath = fileName;
		if (rawUrl != null && !rawUrl.isEmpty()) {
			java.util.regex.Pattern p = java.util.regex.Pattern.compile(".*/raw/([^/]+)/(.+)");
			java.util.regex.Matcher m = p.matcher(rawUrl);
			if (m.find()) {
				ref = m.group(1);
				filePath = m.group(2);
			}
		}

		RetrofitClient.getApiInterface(ctx)
				.getSnippetFileContent(snippetId, ref, filePath)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<ResponseBody> c,
									@NonNull Response<ResponseBody> r) {
								String content = "";
								if (r.isSuccessful() && r.body() != null) {
									try {
										content = r.body().string();
									} catch (Exception e) {
										content = "";
									}
								}
								isLoading.setValue(false);
								singleViewerPayload.setValue(new ViewerPayload(content, fileName));
							}

							@Override
							public void onFailure(
									@NonNull Call<ResponseBody> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								singleViewerPayload.setValue(new ViewerPayload("", fileName));
							}
						});
	}

	public void createSnippet(
			Context ctx,
			String title,
			String description,
			String visibility,
			List<SnippetCreateModel.File> files) {
		isActionLoading.setValue(true);
		SnippetCreateModel model = new SnippetCreateModel(title, description, visibility, files);
		RetrofitClient.getApiInterface(ctx)
				.createSnippet(model)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<SnippetsItem> c,
									@NonNull Response<SnippetsItem> r) {
								ApiResponseHandler.handleAction(
										r, isActionLoading, actionSuccess, error);
							}

							@Override
							public void onFailure(
									@NonNull Call<SnippetsItem> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void updateSnippet(
			Context ctx,
			long snippetId,
			String title,
			String description,
			String visibility,
			List<SnippetCreateModel.File> newFiles,
			List<String> originalFileNames) {
		isActionLoading.setValue(true);

		List<SnippetCreateModel.File> payload = new ArrayList<>();
		Set<String> newNames = new java.util.HashSet<>();
		for (SnippetCreateModel.File f : newFiles) newNames.add(f.getFilePath());

		for (SnippetCreateModel.File f : newFiles) {
			if (originalFileNames.contains(f.getFilePath())) {
				payload.add(
						new SnippetCreateModel.File(
								"update", f.getFilePath(), f.getContent(), null));
			} else {
				payload.add(
						new SnippetCreateModel.File(
								"create", f.getFilePath(), f.getContent(), null));
			}
		}
		for (String original : originalFileNames) {
			if (!newNames.contains(original)) {
				payload.add(new SnippetCreateModel.File("delete", original, null, null));
			}
		}

		SnippetCreateModel model = new SnippetCreateModel(title, description, visibility, payload);
		RetrofitClient.getApiInterface(ctx)
				.updateSnippet(snippetId, model)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<SnippetsItem> c,
									@NonNull Response<SnippetsItem> r) {
								ApiResponseHandler.handleAction(
										r, isActionLoading, actionSuccess, error);
							}

							@Override
							public void onFailure(
									@NonNull Call<SnippetsItem> c, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void fetchSingleFileForEdit(
			Context ctx,
			long snippetId,
			CreateSnippetBottomSheet.FileEntry entry,
			Runnable onComplete) {
		String ref = "main";
		String filePath = entry.fileName;
		RetrofitClient.getApiInterface(ctx)
				.getSnippetFileContent(snippetId, ref, filePath)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<ResponseBody> c,
									@NonNull Response<ResponseBody> r) {
								if (r.isSuccessful() && r.body() != null) {
									try {
										entry.content = r.body().string();
									} catch (Exception e) {
										entry.content = "";
									}
								}
								onComplete.run();
							}

							@Override
							public void onFailure(
									@NonNull Call<ResponseBody> c, @NonNull Throwable t) {
								onComplete.run();
							}
						});
	}

	public void fetchMultiFileContent(Context ctx, long snippetId, FilesItem file) {
		isLoading.setValue(true);
		fetchSingleFile(ctx, snippetId, file);
	}

	public void clearSingleViewerPayload() {
		singleViewerPayload.setValue(null);
	}

	public void clearMultiFileList() {
		multiFileList.setValue(null);
	}

	public void clearError() {
		error.setValue(null);
	}

	public static class ViewerPayload {
		public String content;
		public String fileName;

		public ViewerPayload(String content, String fileName) {
			this.content = content;
			this.fileName = fileName;
		}
	}
}
