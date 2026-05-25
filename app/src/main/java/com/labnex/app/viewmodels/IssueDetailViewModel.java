package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.models.issues.CrudeIssue;
import com.labnex.app.models.issues.Issues;
import com.labnex.app.models.labels.Labels;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssueDetailViewModel extends ViewModel {

	private final MutableLiveData<Issues> issueData = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> closeSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> reopenSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Map<String, Labels>> labelCache =
			new MutableLiveData<>(new HashMap<>());
	private final MutableLiveData<Boolean> lockSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> unlockSuccess = new MutableLiveData<>(false);

	public LiveData<Issues> getIssueData() {
		return issueData;
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

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Map<String, Labels>> getLabelCache() {
		return labelCache;
	}

	public LiveData<Boolean> getLockSuccess() {
		return lockSuccess;
	}

	public LiveData<Boolean> getUnlockSuccess() {
		return unlockSuccess;
	}

	public void clearLockSuccess() {
		lockSuccess.setValue(false);
	}

	public void clearUnlockSuccess() {
		unlockSuccess.setValue(false);
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

	public void loadIssue(Context ctx, long projectId, long issueIid) {
		if (ctx == null) return;
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getIssue(projectId, issueIid)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Issues> c, @NonNull Response<Issues> r) {
								ApiResponseHandler.handleFetch(
										r, isLoading, () -> issueData.setValue(r.body()), error);
							}

							@Override
							public void onFailure(@NonNull Call<Issues> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void toggleDiscussionLock(Context ctx, long projectId, long issueIid, boolean lock) {
		if (ctx == null) return;
		isLoading.setValue(true);
		CrudeIssue crudeIssue = new CrudeIssue();
		crudeIssue.setDiscussionLocked(lock);
		RetrofitClient.getApiInterface(ctx)
				.updateIssue(projectId, issueIid, crudeIssue)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Issues> c, @NonNull Response<Issues> r) {
								ApiResponseHandler.handleAction(
										r, isLoading, lock ? lockSuccess : unlockSuccess, error);
								if (r.isSuccessful() && r.body() != null) {
									issueData.setValue(r.body());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Issues> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void toggleIssueState(Context ctx, long projectId, long issueIid, String stateEvent) {
		if (ctx == null) return;
		isLoading.setValue(true);
		CrudeIssue crudeIssue = new CrudeIssue();
		crudeIssue.setStateEvent(stateEvent);
		RetrofitClient.getApiInterface(ctx)
				.updateIssue(projectId, issueIid, crudeIssue)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Issues> c, @NonNull Response<Issues> r) {
								boolean isClose = "close".equals(stateEvent);
								ApiResponseHandler.handleAction(
										r,
										isLoading,
										isClose ? closeSuccess : reopenSuccess,
										error);
								if (r.isSuccessful() && r.body() != null) {
									issueData.setValue(r.body());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Issues> c, @NonNull Throwable t) {
								isLoading.setValue(false);
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
