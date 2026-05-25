package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.Constants;
import com.labnex.app.models.notes.CrudeNote;
import com.labnex.app.models.notes.Notes;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class TimelineViewModel extends ViewModel {

	private final MutableLiveData<List<Notes>> timelineList =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isSubmitting = new MutableLiveData<>(false);
	private final MutableLiveData<Notes> submittedComment = new MutableLiveData<>();

	private long projectId;
	private long iid;
	private String type;
	private int currentPage = 1;
	private final int resultLimit = Constants.getResultLimit();
	private boolean isLastPage = false;
	private boolean isLoadingMore = false;

	public LiveData<List<Notes>> getTimelineList() {
		return timelineList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Boolean> getIsSubmitting() {
		return isSubmitting;
	}

	public LiveData<Notes> getSubmittedComment() {
		return submittedComment;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void clearSubmittedComment() {
		submittedComment.setValue(null);
	}

	public void loadTimeline(Context ctx, long projectId, long iid, String type) {
		if (ctx == null) return;
		this.projectId = projectId;
		this.iid = iid;
		this.type = type;
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
		Call<List<Notes>> call;
		if ("issue".equals(type)) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getIssueNotes(projectId, iid, resultLimit, page);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.getMergeRequestNotes(projectId, iid, resultLimit, page);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Notes>> c, @NonNull Response<List<Notes>> r) {
						ApiResponseHandler.handleFetch(
								r,
								isLoading,
								() -> {
									String totalHeader = r.headers().get("x-total");
									List<Notes> body = r.body();
									List<Notes> current =
											(page == 1)
													? new ArrayList<>()
													: timelineList.getValue() != null
															? new ArrayList<>(
																	timelineList.getValue())
															: new ArrayList<>();
									if (body != null) current.addAll(body);
									timelineList.setValue(current);
									checkLastPage(
											body != null ? body.size() : 0,
											totalHeader,
											current.size());
								},
								error);
						isLoadingMore = false;
					}

					@Override
					public void onFailure(@NonNull Call<List<Notes>> c, @NonNull Throwable t) {
						isLoading.setValue(false);
						isLoadingMore = false;
						if (page == 1) timelineList.setValue(new ArrayList<>());
						error.setValue(t.getMessage());
					}
				});
	}

	private void checkLastPage(int bodySize, String totalHeader, int fullListSize) {
		if (bodySize < resultLimit) {
			isLastPage = true;
		} else if (totalHeader != null) {
			try {
				if (fullListSize >= Integer.parseInt(totalHeader)) isLastPage = true;
			} catch (NumberFormatException ignored) {
			}
		}
	}

	public void addComment(Context ctx, long projectId, long iid, String body) {
		if (ctx == null) return;
		isSubmitting.setValue(true);
		error.setValue(null);

		CrudeNote note = new CrudeNote();
		note.setBody(body);

		Call<Notes> call;
		if ("mr".equals(type)) {
			call = RetrofitClient.getApiInterface(ctx).createMergeRequestNote(projectId, iid, note);
		} else {
			call = RetrofitClient.getApiInterface(ctx).createIssueNote(projectId, iid, note);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(@NonNull Call<Notes> c, @NonNull Response<Notes> r) {
						isSubmitting.setValue(false);
						if (r.isSuccessful() && r.body() != null) {
							submittedComment.setValue(r.body());
						} else {
							error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Notes> c, @NonNull Throwable t) {
						isSubmitting.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}
}
