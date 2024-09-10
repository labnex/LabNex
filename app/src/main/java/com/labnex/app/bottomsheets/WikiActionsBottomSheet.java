package com.labnex.app.bottomsheets;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetWikiActionsBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.wikis.CrudeWiki;
import com.labnex.app.models.wikis.Wiki;
import com.vdurmont.emoji.EmojiParser;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class WikiActionsBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetWikiActionsBinding bottomSheetWikiActionsBinding;
	private int projectId;
	private String source;
	private static UpdateInterface UpdateInterface;

	public interface UpdateInterface {
		void updateDataListener(String str);
	}

	public static void setUpdateListener(UpdateInterface updateInterface) {
		UpdateInterface = updateInterface;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetWikiActionsBinding =
				BottomSheetWikiActionsBinding.inflate(inflater, container, false);

		Bundle bundle = getArguments();
		assert bundle != null;

		projectId = bundle.getInt("projectId");

		if (bundle.getString("source") != null) {
			source = bundle.getString("source");
		} else {
			source = "";
		}

		bottomSheetWikiActionsBinding.closeBs.setOnClickListener(close -> dismiss());

		bottomSheetWikiActionsBinding.view.setOnClickListener(
				view -> {
					bottomSheetWikiActionsBinding.content.setVisibility(View.GONE);
					bottomSheetWikiActionsBinding.renderContent.setVisibility(View.VISIBLE);

					bottomSheetWikiActionsBinding.edit.setVisibility(View.VISIBLE);
					bottomSheetWikiActionsBinding.view.setVisibility(View.GONE);

					Markdown.render(
							requireContext(),
							EmojiParser.parseToUnicode(
									bottomSheetWikiActionsBinding.content.getText().toString()),
							bottomSheetWikiActionsBinding.renderContent);
				});

		bottomSheetWikiActionsBinding.edit.setOnClickListener(
				edit -> {
					bottomSheetWikiActionsBinding.content.setVisibility(View.VISIBLE);
					bottomSheetWikiActionsBinding.renderContent.setVisibility(View.GONE);

					bottomSheetWikiActionsBinding.edit.setVisibility(View.GONE);
					bottomSheetWikiActionsBinding.view.setVisibility(View.VISIBLE);
				});

		if (source.equalsIgnoreCase("view")) {

			bottomSheetWikiActionsBinding.view.setChecked(true);
			bottomSheetWikiActionsBinding.content.setVisibility(View.GONE);
			bottomSheetWikiActionsBinding.renderContent.setVisibility(View.VISIBLE);

			bottomSheetWikiActionsBinding.edit.setVisibility(View.VISIBLE);
			bottomSheetWikiActionsBinding.view.setVisibility(View.GONE);

			bottomSheetWikiActionsBinding.title.setText(bundle.getString("title"));
			bottomSheetWikiActionsBinding.content.setText(bundle.getString("content"));
			Markdown.render(
					requireContext(),
					EmojiParser.parseToUnicode(Objects.requireNonNull(bundle.getString("content"))),
					bottomSheetWikiActionsBinding.renderContent);

			bottomSheetWikiActionsBinding.save.setOnClickListener(
					save -> {
						disableButton();
						String title =
								Objects.requireNonNull(
												bottomSheetWikiActionsBinding.title.getText())
										.toString();
						String content =
								Objects.requireNonNull(
												bottomSheetWikiActionsBinding.content.getText())
										.toString();

						if (title.isEmpty()) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.title_required));
						} else if (content.isEmpty()) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.content_required));
						} else {
							updateWikiPage(title, content, bundle.getString("slug"));
						}
					});
		} else {

			bottomSheetWikiActionsBinding.save.setOnClickListener(
					save -> {
						disableButton();
						String title =
								Objects.requireNonNull(
												bottomSheetWikiActionsBinding.title.getText())
										.toString();
						String content =
								Objects.requireNonNull(
												bottomSheetWikiActionsBinding.content.getText())
										.toString();

						if (title.isEmpty()) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.title_required));
						} else if (content.isEmpty()) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.content_required));
						} else {
							createNewPage(title, content);
						}
					});
		}

		DisplayMetrics displayMetrics = new DisplayMetrics();
		((Activity) requireContext())
				.getWindowManager()
				.getDefaultDisplay()
				.getMetrics(displayMetrics);
		int height = displayMetrics.heightPixels;
		bottomSheetWikiActionsBinding.content.setMinHeight(height);
		bottomSheetWikiActionsBinding.renderContent.setMinHeight(height);

		return bottomSheetWikiActionsBinding.getRoot();
	}

	private void createNewPage(String title, String content) {

		CrudeWiki crudeWiki = new CrudeWiki();
		crudeWiki.setTitle(title);
		crudeWiki.setContent(content);

		Call<Wiki> call =
				RetrofitClient.getApiInterface(requireContext())
						.createWikiPage(projectId, crudeWiki);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Wiki> call, @NonNull retrofit2.Response<Wiki> response) {

						if (response.code() == 201) {

							dismiss();
							UpdateInterface.updateDataListener("created");
						} else if (response.code() == 401) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.access_forbidden_403));
						} else {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Wiki> call, @NonNull Throwable t) {

						enableButton();
						Snackbar.info(
								requireContext(),
								bottomSheetWikiActionsBinding.mainLayout,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void updateWikiPage(String title, String content, String slug) {

		CrudeWiki crudeWiki = new CrudeWiki();
		crudeWiki.setTitle(title);
		crudeWiki.setContent(content);

		Call<Wiki> call =
				RetrofitClient.getApiInterface(requireContext())
						.updateWikiPage(projectId, slug, crudeWiki);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Wiki> call, @NonNull retrofit2.Response<Wiki> response) {

						if (response.code() == 200) {

							dismiss();
							UpdateInterface.updateDataListener("updated");
						} else if (response.code() == 401) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.access_forbidden_403));
						} else {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetWikiActionsBinding.mainLayout,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Wiki> call, @NonNull Throwable t) {

						enableButton();
						Snackbar.info(
								requireContext(),
								bottomSheetWikiActionsBinding.mainLayout,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void disableButton() {
		bottomSheetWikiActionsBinding.save.setEnabled(false);
		bottomSheetWikiActionsBinding.save.setAlpha(.5F);
	}

	private void enableButton() {
		bottomSheetWikiActionsBinding.save.setEnabled(true);
		bottomSheetWikiActionsBinding.save.setAlpha(1F);
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_project_wikis);

		dialog.setOnShowListener(
				dialogInterface -> {
					BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
					View bottomSheet =
							bottomSheetDialog.findViewById(
									com.google.android.material.R.id.design_bottom_sheet);

					if (bottomSheet != null) {

						BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
						behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
						behavior.setPeekHeight(bottomSheet.getHeight());
						behavior.setHideable(false);
					}
				});

		if (dialog.getWindow() != null) {

			WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
			params.height = WindowManager.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setAttributes(params);
		}

		return dialog;
	}

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		try {
			BottomSheetListener bottomSheetListener = (BottomSheetListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}
}
