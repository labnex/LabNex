package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.models.metadata.Metadata;
import com.labnex.app.models.personal_access_tokens.PersonalAccessTokens;
import com.labnex.app.models.todo.ToDoItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MainActivityViewModel extends ViewModel {

	private final MutableLiveData<Integer> tokenCheckResult = new MutableLiveData<>();
	private final MutableLiveData<String> serverVersion = new MutableLiveData<>();
	private final MutableLiveData<Boolean> versionCheckDone = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Integer> pendingTodoCount = new MutableLiveData<>(0);

	public LiveData<Integer> getTokenCheckResult() {
		return tokenCheckResult;
	}

	public LiveData<String> getServerVersion() {
		return serverVersion;
	}

	public LiveData<Boolean> getVersionCheckDone() {
		return versionCheckDone;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Integer> getPendingTodoCount() {
		return pendingTodoCount;
	}

	public void checkPersonalAccessToken(Context ctx) {
		RetrofitClient.getApiInterface(ctx)
				.getPersonalAccessTokenInfo()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<PersonalAccessTokens> c,
									@NonNull Response<PersonalAccessTokens> r) {
								tokenCheckResult.setValue(r.code());
							}

							@Override
							public void onFailure(
									@NonNull Call<PersonalAccessTokens> c, @NonNull Throwable t) {
								error.setValue(t.getMessage());
							}
						});
	}

	public void fetchGitlabVersion(Context ctx) {
		RetrofitClient.getApiInterface(ctx)
				.getMetadata()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Metadata> c, @NonNull Response<Metadata> r) {
								if (r.isSuccessful() && r.body() != null) {
									String version = r.body().getVersion();
									SharedPrefDB sharedPrefDB = SharedPrefDB.getInstance(ctx);
									UserAccountsApi api =
											BaseApi.getInstance(ctx, UserAccountsApi.class);
									if (api != null) {
										api.updateServerVersion(
												version,
												sharedPrefDB.getInt("currentActiveAccountId"));
									}
									serverVersion.setValue(version);
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
								versionCheckDone.setValue(true);
							}

							@Override
							public void onFailure(@NonNull Call<Metadata> c, @NonNull Throwable t) {
								error.setValue(t.getMessage());
								versionCheckDone.setValue(true);
							}
						});
	}

	public void fetchPendingTodoCount(Context ctx) {
		RetrofitClient.getApiInterface(ctx)
				.getAllTodos(null, "pending", null)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<ToDoItem>> call,
									@NonNull Response<List<ToDoItem>> response) {
								if (response.isSuccessful()) {
									String xTotal = response.headers().get("x-total");
									if (xTotal != null) {
										try {
											int count = Integer.parseInt(xTotal);
											pendingTodoCount.setValue(count);
										} catch (NumberFormatException ignored) {
										}
									}
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<ToDoItem>> call, @NonNull Throwable t) {}
						});
	}
}
