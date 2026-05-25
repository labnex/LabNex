package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.models.groups.GroupsItem;
import com.labnex.app.models.projects.CrudeProject;
import com.labnex.app.models.projects.Projects;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CreateProjectViewModel extends ViewModel {

	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<List<NamespaceItem>> namespaces =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoadingNamespaces = new MutableLiveData<>(false);

	public LiveData<List<NamespaceItem>> getNamespaces() {
		return namespaces;
	}

	public LiveData<Boolean> getIsLoadingNamespaces() {
		return isLoadingNamespaces;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getIsSuccess() {
		return isSuccess;
	}

	public LiveData<String> getError() {
		return error;
	}

	public void createProject(
			Context ctx,
			String name,
			String description,
			String visibility,
			boolean initWithReadme,
			String defaultBranch,
			boolean lfsEnabled,
			boolean emailsEnabled,
			long namespaceId) {
		isLoading.setValue(true);

		CrudeProject project = new CrudeProject();
		project.setName(name);
		project.setDescription(description);
		project.setVisibility(visibility);
		project.setInitializeWithReadme(initWithReadme);
		project.setDefaultBranch(defaultBranch);
		project.setLfsEnabled(lfsEnabled);
		project.setEmailsEnabled(emailsEnabled);
		project.setNamespaceId(namespaceId);

		RetrofitClient.getApiInterface(ctx)
				.createProject(project)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Projects> c, @NonNull Response<Projects> r) {
								ApiResponseHandler.handleAction(r, isLoading, isSuccess, error);
							}

							@Override
							public void onFailure(@NonNull Call<Projects> c, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void loadNamespaces(Context ctx, long userId, String username) {
		isLoadingNamespaces.setValue(true);

		List<NamespaceItem> list = new ArrayList<>();
		NamespaceItem personal = new NamespaceItem();
		personal.id = userId;
		personal.fullPath = username;
		personal.kind = "user";
		list.add(personal);

		RetrofitClient.getApiInterface(ctx)
				.getGroups(false, null, null, 100, 1)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<GroupsItem>> c,
									@NonNull Response<List<GroupsItem>> r) {
								isLoadingNamespaces.setValue(false);
								if (r.isSuccessful() && r.body() != null) {
									for (GroupsItem g : r.body()) {
										NamespaceItem item = new NamespaceItem();
										item.id = g.getId();
										item.fullPath = g.getFullPath();
										item.kind = "group";
										list.add(item);
									}
								}
								namespaces.setValue(list);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<GroupsItem>> c, @NonNull Throwable t) {
								isLoadingNamespaces.setValue(false);
								namespaces.setValue(list);
							}
						});
	}

	public void clearError() {
		error.setValue(null);
	}

	public static class NamespaceItem {
		public long id;
		public String fullPath;
		public String kind;

		@NonNull public String toString() {
			return fullPath;
		}
	}
}
