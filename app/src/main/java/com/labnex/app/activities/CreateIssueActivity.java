package com.labnex.app.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.contexts.ProjectsContext;
import com.labnex.app.databinding.ActivityCreateIssueBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.issues.CrudeIssue;
import com.labnex.app.models.issues.Issues;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class CreateIssueActivity extends BaseActivity {

	ActivityCreateIssueBinding binding;
	private int projectId;

	private static UpdateInterface UpdateInterface;

	public interface UpdateInterface {
		void updateDataListener(String str);
	}

	public static void setUpdateListener(UpdateInterface updateInterface) {
		UpdateInterface = updateInterface;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityCreateIssueBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		ProjectsContext projectsContext = ProjectsContext.fromIntent(getIntent());
		projectId = projectsContext.getProjectId();

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.create.setOnClickListener(
				create -> {
					disableButton();
					String title = Objects.requireNonNull(binding.title.getText()).toString();
					String description =
							Objects.requireNonNull(binding.description.getText()).toString();

					if (title.isEmpty()) {

						enableButton();
						Snackbar.info(
								CreateIssueActivity.this,
								binding.bottomAppBar,
								getString(R.string.title_required));
						return;
					}

					createNewIssue(title, description);
				});
	}

	private void createNewIssue(String title, String description) {

		CrudeIssue createIssue = new CrudeIssue();
		createIssue.setTitle(title);
		createIssue.setDescription(description);

		Call<Issues> call = RetrofitClient.getApiInterface(ctx).createIssue(projectId, createIssue);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Issues> call,
							@NonNull retrofit2.Response<Issues> response) {

						if (response.code() == 201) {

							UpdateInterface.updateDataListener("created");
							finish();
						} else if (response.code() == 401) {

							enableButton();
							Snackbar.info(
									CreateIssueActivity.this,
									binding.bottomAppBar,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							enableButton();
							Snackbar.info(
									CreateIssueActivity.this,
									binding.bottomAppBar,
									getString(R.string.access_forbidden_403));
						} else {

							enableButton();
							Snackbar.info(
									CreateIssueActivity.this,
									binding.bottomAppBar,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {

						enableButton();
						Snackbar.info(
								CreateIssueActivity.this,
								binding.bottomAppBar,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void disableButton() {
		binding.create.setEnabled(false);
		binding.create.setAlpha(.5F);
	}

	private void enableButton() {
		binding.create.setEnabled(true);
		binding.create.setAlpha(1F);
	}
}
