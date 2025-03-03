package com.labnex.app.helpers.api;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.templates.Template;
import com.labnex.app.models.templates.Templates;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class TemplateFetcher {

	private static Call<Template> currentTemplateCall;

	public static void fetchAndPopulateTemplates(
			Context context,
			int projectId,
			String type,
			MaterialAutoCompleteTextView autoCompleteTextView,
			EditText descriptionEditText,
			View snackbarAnchorView,
			Runnable disableButton,
			Runnable enableButton) {

		Call<List<Templates>> call =
				RetrofitClient.getApiInterface(context).getTemplates(projectId, type);
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Templates>> call,
							@NonNull Response<List<Templates>> response) {

						if (response.isSuccessful() && response.body() != null) {

							List<Templates> templates = response.body();
							List<String> templateNames = new ArrayList<>();
							templateNames.add("No template");

							for (Templates item : templates) {
								templateNames.add(item.getName());
							}

							ArrayAdapter<String> adapter =
									new ArrayAdapter<>(
											context,
											android.R.layout.simple_dropdown_item_1line,
											templateNames);

							autoCompleteTextView.setAdapter(adapter);
							autoCompleteTextView.setDropDownBackgroundResource(
									R.drawable.dropdown_background);

							autoCompleteTextView.setOnClickListener(
									v -> autoCompleteTextView.showDropDown());
							autoCompleteTextView.setOnFocusChangeListener(
									(v, hasFocus) -> {
										if (hasFocus) {
											autoCompleteTextView.showDropDown();
										}
									});

							autoCompleteTextView.setOnItemClickListener(
									(parent, view, position, id) -> {
										String selectedTemplate = adapter.getItem(position);

										if (!"No template".equals(selectedTemplate)) {
											fetchTemplateContent(
													context,
													projectId,
													type,
													selectedTemplate,
													descriptionEditText,
													snackbarAnchorView,
													disableButton,
													enableButton);
										} else {
											descriptionEditText.setText("");
										}
									});
						} else {

							Snackbar.info(
									context,
									snackbarAnchorView,
									context.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Templates>> call, @NonNull Throwable t) {

						Snackbar.info(
								context,
								snackbarAnchorView,
								context.getString(R.string.generic_server_response_error));
					}
				});
	}

	private static void fetchTemplateContent(
			Context context,
			int projectId,
			String type,
			String templateName,
			EditText descriptionEditText,
			View snackbarAnchorView,
			Runnable disableButton,
			Runnable enableButton) {

		if (currentTemplateCall != null && !currentTemplateCall.isCanceled()) {
			currentTemplateCall.cancel();
		}
		if (disableButton != null) {
			disableButton.run();
		}

		currentTemplateCall =
				RetrofitClient.getApiInterface(context).getTemplate(projectId, type, templateName);

		currentTemplateCall.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Template> call, @NonNull Response<Template> response) {

						if (enableButton != null) {
							enableButton.run();
						}

						if (!call.isCanceled()
								&& response.isSuccessful()
								&& response.body() != null) {
							String content = response.body().getContent();
							descriptionEditText.setText(content);
						} else if (!call.isCanceled()) {
							Snackbar.info(
									context,
									snackbarAnchorView,
									context.getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Template> call, @NonNull Throwable t) {

						if (enableButton != null) {
							enableButton.run();
						}
						if (!call.isCanceled()) {
							Snackbar.info(
									context,
									snackbarAnchorView,
									context.getString(R.string.generic_server_response_error));
						}
					}
				});
	}
}
