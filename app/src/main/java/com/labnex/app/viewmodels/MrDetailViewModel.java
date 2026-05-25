package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.models.approvals.Approvals;
import com.labnex.app.models.labels.Labels;
import com.labnex.app.models.merge_requests.CrudeMergeRequest;
import com.labnex.app.models.merge_requests.MergeRequests;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MrDetailViewModel extends ViewModel {

	private final MutableLiveData<MergeRequests> mrData = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> closeSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> reopenSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> lockSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> unlockSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> mergeSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> approveSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Approvals> approvals = new MutableLiveData<>();
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Map<String, Labels>> labelCache =
			new MutableLiveData<>(new HashMap<>());

	public LiveData<MergeRequests> getMrData() {
		return mrData;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getCloseSuccess() {
		return closeSuccess;
	}

	public LiveData<Boolean> getReopenSuccess() {
		return reopenSuccess;
	}

	public LiveData<Boolean> getLockSuccess() {
		return lockSuccess;
	}

	public LiveData<Boolean> getUnlockSuccess() {
		return unlockSuccess;
	}

	public LiveData<Boolean> getMergeSuccess() {
		return mergeSuccess;
	}

	public LiveData<Boolean> getApproveSuccess() {
		return approveSuccess;
	}

	public LiveData<Approvals> getApprovals() {
		return approvals;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Map<String, Labels>> getLabelCache() {
		return labelCache;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void clearCloseSuccess() {
		closeSuccess.setValue(false);
	}

	public void clearReopenSuccess() {
		reopenSuccess.setValue(false);
	}

	public void clearLockSuccess() {
		lockSuccess.setValue(false);
	}

	public void clearUnlockSuccess() {
		unlockSuccess.setValue(false);
	}

	public void clearMergeSuccess() {
		mergeSuccess.setValue(false);
	}

	public void clearApproveSuccess() {
		approveSuccess.setValue(false);
	}

	public void loadMergeRequest(Context ctx, long projectId, long mrIid) {
		if (ctx == null) return;
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getMergeRequest(projectId, mrIid)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MergeRequests> c,
									@NonNull Response<MergeRequests> r) {
								ApiResponseHandler.handleFetch(
										r, isLoading, () -> mrData.setValue(r.body()), error);
							}

							@Override
							public void onFailure(
									@NonNull Call<MergeRequests> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void toggleMrState(Context ctx, long projectId, long mrIid, String stateEvent) {
		if (ctx == null) return;
		isLoading.setValue(true);
		CrudeMergeRequest body = new CrudeMergeRequest();
		body.setStateEvent(stateEvent);
		RetrofitClient.getApiInterface(ctx)
				.updateMergeRequest(projectId, mrIid, body)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MergeRequests> c,
									@NonNull Response<MergeRequests> r) {
								boolean isClose = "close".equals(stateEvent);
								ApiResponseHandler.handleAction(
										r,
										isLoading,
										isClose ? closeSuccess : reopenSuccess,
										error);
								if (r.isSuccessful() && r.body() != null) {
									mrData.setValue(r.body());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<MergeRequests> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void toggleDiscussionLock(Context ctx, long projectId, long mrIid, boolean lock) {
		if (ctx == null) return;
		isLoading.setValue(true);
		CrudeMergeRequest body = new CrudeMergeRequest();
		body.setDiscussionLocked(lock);
		RetrofitClient.getApiInterface(ctx)
				.updateMergeRequest(projectId, mrIid, body)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MergeRequests> c,
									@NonNull Response<MergeRequests> r) {
								ApiResponseHandler.handleAction(
										r, isLoading, lock ? lockSuccess : unlockSuccess, error);
								if (r.isSuccessful() && r.body() != null) {
									mrData.setValue(r.body());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<MergeRequests> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void toggleDraft(
			Context ctx, long projectId, long mrIid, String currentTitle, boolean markDraft) {
		if (ctx == null) return;
		isLoading.setValue(true);
		CrudeMergeRequest body = new CrudeMergeRequest();

		if (markDraft) {
			body.setTitle("Draft: " + currentTitle);
		} else {
			body.setTitle(currentTitle.replaceFirst("^(?i)Draft:\\s*", ""));
		}

		RetrofitClient.getApiInterface(ctx)
				.updateMergeRequest(projectId, mrIid, body)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MergeRequests> c,
									@NonNull Response<MergeRequests> r) {
								isLoading.setValue(false);
								if (r.isSuccessful() && r.body() != null) {
									mrData.setValue(r.body());
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<MergeRequests> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void mergeMr(
			Context ctx, long projectId, long mrIid, boolean removeSourceBranch, boolean squash) {
		if (ctx == null) return;
		isLoading.setValue(true);
		CrudeMergeRequest body = new CrudeMergeRequest();
		body.setShouldRemoveSourceBranch(removeSourceBranch);
		body.setSquash(squash);
		RetrofitClient.getApiInterface(ctx)
				.mergeMergeRequest(projectId, mrIid, body)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<MergeRequests> c,
									@NonNull Response<MergeRequests> r) {
								ApiResponseHandler.handleAction(r, isLoading, mergeSuccess, error);
								if (r.isSuccessful() && r.body() != null) {
									mrData.setValue(r.body());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<MergeRequests> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void fetchApprovals(Context ctx, long projectId, long mrIid) {
		RetrofitClient.getApiInterface(ctx)
				.getApprovals(projectId, mrIid)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Approvals> c, @NonNull Response<Approvals> r) {
								if (r.isSuccessful() && r.body() != null) {
									approvals.setValue(r.body());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Approvals> c, @NonNull Throwable t) {}
						});
	}

	public void approveMr(Context ctx, long projectId, long mrIid) {
		RetrofitClient.getApiInterface(ctx)
				.approve(projectId, mrIid)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Approvals> c, @NonNull Response<Approvals> r) {
								if (r.isSuccessful()) {
									approveSuccess.setValue(true);
									approvals.setValue(r.body());
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Approvals> c, @NonNull Throwable t) {
								error.setValue(t.getMessage());
							}
						});
	}

	public void revokeApproval(Context ctx, long projectId, long mrIid) {
		RetrofitClient.getApiInterface(ctx)
				.revokeApproval(projectId, mrIid)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Approvals> c, @NonNull Response<Approvals> r) {
								if (r.isSuccessful()) {
									approveSuccess.setValue(true);
									approvals.setValue(r.body());
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Approvals> c, @NonNull Throwable t) {
								error.setValue(t.getMessage());
							}
						});
	}

	public void fetchLabel(Context ctx, long projectId, String labelName) {
		if (ctx == null) return;
		Map<String, Labels> cache = labelCache.getValue();
		if (cache != null && cache.containsKey(labelName)) return;

		RetrofitClient.getApiInterface(ctx)
				.getProjectLabel(projectId, labelName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Labels> c, @NonNull Response<Labels> r) {
								if (r.isSuccessful() && r.body() != null) {
									Map<String, Labels> current = labelCache.getValue();
									if (current == null) current = new HashMap<>();
									current.put(labelName, r.body());
									labelCache.setValue(current);
								}
							}

							@Override
							public void onFailure(@NonNull Call<Labels> c, @NonNull Throwable t) {}
						});
	}
}
