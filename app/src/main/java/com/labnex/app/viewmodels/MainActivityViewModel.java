package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.models.metadata.Metadata;
import com.labnex.app.models.personal_access_tokens.PersonalAccessTokens;
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

	public void checkPersonalAccessToken(Context ctx) {
		Call<PersonalAccessTokens> call =
				RetrofitClient.getApiInterface(ctx).getPersonalAccessTokenInfo();

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<PersonalAccessTokens> call,
							@NonNull Response<PersonalAccessTokens> response) {
						tokenCheckResult.setValue(response.code());
					}

					@Override
					public void onFailure(
							@NonNull Call<PersonalAccessTokens> call, @NonNull Throwable t) {
						error.setValue(t.getMessage());
					}
				});
	}

	public void fetchGitlabVersion(Context ctx) {
		Call<Metadata> callVersion = RetrofitClient.getApiInterface(ctx).getMetadata();
		callVersion.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Metadata> callVersion,
							@NonNull Response<Metadata> responseVersion) {
						if (responseVersion.code() == 200 && responseVersion.body() != null) {
							String version = responseVersion.body().getVersion();

							SharedPrefDB sharedPrefDB = SharedPrefDB.getInstance(ctx);
							UserAccountsApi userAccountsApi =
									BaseApi.getInstance(ctx, UserAccountsApi.class);

							if (userAccountsApi != null) {
								userAccountsApi.updateServerVersion(
										version, sharedPrefDB.getInt("currentActiveAccountId"));
							}

							serverVersion.setValue(version);
						}
						versionCheckDone.setValue(true);
					}

					@Override
					public void onFailure(
							@NonNull Call<Metadata> callVersion, @NonNull Throwable t) {
						error.setValue(t.getMessage());
						versionCheckDone.setValue(true);
					}
				});
	}
}
