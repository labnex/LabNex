package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.models.user.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ProfileViewModel extends ViewModel {

	private final MutableLiveData<User> userInfo = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();

	public LiveData<User> getUserInfo() {
		return userInfo;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public void loadUser(Context ctx, int userId, int currentLoggedInId) {
		isLoading.setValue(true);

		if (userId == currentLoggedInId) {
			fetchPrivateProfile(ctx, userId);
		} else {
			fetchPublicProfile(ctx, userId);
		}
	}

	private void fetchPrivateProfile(Context ctx, int userId) {
		RetrofitClient.getApiInterface(ctx)
				.getCurrentUser()
				.enqueue(
						new Callback<User>() {
							@Override
							public void onResponse(
									@NonNull Call<User> call, @NonNull Response<User> response) {
								if (response.isSuccessful() && response.body() != null) {
									userInfo.setValue(response.body());
									fetchPublicProfile(ctx, userId);
								} else {
									isLoading.setValue(false);
									error.setValue("private_fetch_error");
								}
							}

							@Override
							public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	private void fetchPublicProfile(Context ctx, int userId) {
		RetrofitClient.getApiInterface(ctx)
				.getSingleUser(userId)
				.enqueue(
						new Callback<User>() {
							@Override
							public void onResponse(
									@NonNull Call<User> call, @NonNull Response<User> response) {
								isLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									userInfo.setValue(response.body());
								} else {
									error.setValue("public_fetch_error");
								}
							}

							@Override
							public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void clearError() {
		error.setValue(null);
	}
}
