package com.labnex.app.activities;

import android.os.Bundle;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.ActivityCreateProjectBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.projects.CrudeProject;
import com.labnex.app.models.projects.Projects;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class CreateProjectActivity extends BaseActivity {

	ActivityCreateProjectBinding binding;

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
		binding = ActivityCreateProjectBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		binding.create.setOnClickListener(
				create -> {
					disableButton();
					String name = Objects.requireNonNull(binding.name.getText()).toString();
					String description =
							Objects.requireNonNull(binding.description.getText()).toString();

					int selectedId = binding.projectVisibility.getCheckedRadioButtonId();
					RadioButton radioButton = findViewById(selectedId);

					String visibility = radioButton.getText().toString().toLowerCase();

					boolean initWithReadme = binding.initializeWithReadme.isChecked();

					if (name.isEmpty()) {

						enableButton();
						Snackbar.info(
								CreateProjectActivity.this,
								binding.bottomAppBar,
								getString(R.string.project_name_required));
						return;
					}

					createProject(name, description, visibility, initWithReadme);
				});
	}

	private void createProject(
			String name, String description, String visibility, boolean initWithReadme) {

		CrudeProject crudeProject = new CrudeProject();
		crudeProject.setName(name);
		crudeProject.setDescription(description);
		crudeProject.setVisibility(visibility);
		crudeProject.setInitializeWithReadme(initWithReadme);

		Call<Projects> call = RetrofitClient.getApiInterface(ctx).createProject(crudeProject);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Projects> call,
							@NonNull retrofit2.Response<Projects> response) {

						if (response.code() == 201) {

							UpdateInterface.updateDataListener("created");
							finish();
						} else if (response.code() == 401) {

							enableButton();
							Snackbar.info(
									CreateProjectActivity.this,
									binding.bottomAppBar,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							enableButton();
							Snackbar.info(
									CreateProjectActivity.this,
									binding.bottomAppBar,
									getString(R.string.access_forbidden_403));
						} else {

							enableButton();
							Snackbar.info(
									CreateProjectActivity.this,
									binding.bottomAppBar,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Projects> call, @NonNull Throwable t) {

						enableButton();
						Snackbar.info(
								CreateProjectActivity.this,
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
