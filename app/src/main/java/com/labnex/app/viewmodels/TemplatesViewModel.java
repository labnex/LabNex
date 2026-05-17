package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.models.templates.Template;
import com.labnex.app.models.templates.Templates;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class TemplatesViewModel extends ViewModel {

	private final MutableLiveData<List<Templates>> templateList = new MutableLiveData<>();
	private final MutableLiveData<String> templateContent = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoadingContent = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();

	public LiveData<List<Templates>> getTemplateList() {
		return templateList;
	}

	public LiveData<String> getTemplateContent() {
		return templateContent;
	}

	public LiveData<Boolean> getIsLoadingContent() {
		return isLoadingContent;
	}

	public LiveData<String> getError() {
		return error;
	}

	public void loadTemplates(Context ctx, long projectId, String type) {
		RetrofitClient.getApiInterface(ctx)
				.getTemplates(projectId, type)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Templates>> c,
									@NonNull Response<List<Templates>> r) {
								if (r.isSuccessful() && r.body() != null) {
									templateList.setValue(r.body());
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Templates>> c, @NonNull Throwable t) {
								error.setValue(t.getMessage());
							}
						});
	}

	public void loadTemplateContent(Context ctx, long projectId, String type, String name) {
		isLoadingContent.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.getTemplate(projectId, type, name)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Template> c, @NonNull Response<Template> r) {
								isLoadingContent.setValue(false);
								if (r.isSuccessful() && r.body() != null) {
									templateContent.setValue(r.body().getContent());
								} else {
									error.setValue(ApiResponseHandler.getErrorMessageStatic(r));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Template> c, @NonNull Throwable t) {
								isLoadingContent.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void clearError() {
		error.setValue(null);
	}
}
