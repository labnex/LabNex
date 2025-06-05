package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
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
import com.labnex.app.databinding.BottomSheetTagActionsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.tags.TagsItem;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class TagActionsBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetTagActionsBinding binding;
	private int projectId;
	private static UpdateInterface updateInterface;

	public interface UpdateInterface {
		void updateDataListener(String str);
	}

	public static void setUpdateListener(UpdateInterface listener) {
		updateInterface = listener;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetTagActionsBinding.inflate(inflater, container, false);

		Bundle bundle = getArguments();
		assert bundle != null;
		projectId = bundle.getInt("projectId");

		binding.closeBs.setOnClickListener(close -> dismiss());

		binding.create.setOnClickListener(
				save -> {
					disableButton();
					String tagName =
							Objects.requireNonNull(binding.tagName.getText()).toString().trim();
					String ref = Objects.requireNonNull(binding.ref.getText()).toString().trim();
					String message =
							Objects.requireNonNull(binding.message.getText()).toString().trim();

					if (tagName.isEmpty()) {
						enableButton();
						Snackbar.info(
								requireContext(),
								binding.mainBsFrame,
								getString(R.string.tag_name_empty));
						return;
					}

					if (ref.isEmpty()) {
						enableButton();
						Snackbar.info(
								requireContext(),
								binding.mainBsFrame,
								getString(R.string.ref_empty));
						return;
					}

					createNewTag(tagName, ref, message.isEmpty() ? null : message);
				});

		return binding.getRoot();
	}

	private void createNewTag(String tagName, String ref, String message) {
		Call<TagsItem> call =
				RetrofitClient.getApiInterface(requireContext())
						.createProjectTag(projectId, tagName, ref, message);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<TagsItem> call,
							@NonNull retrofit2.Response<TagsItem> response) {
						if (response.code() == 201) {
							dismiss();
							updateInterface.updateDataListener("created");
						} else if (response.code() == 400) {
							enableButton();
							Snackbar.info(
									requireContext(),
									binding.mainBsFrame,
									getString(R.string.tag_ref_invalid));
						} else if (response.code() == 401) {
							enableButton();
							Snackbar.info(
									requireContext(),
									binding.mainBsFrame,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {
							enableButton();
							Snackbar.info(
									requireContext(),
									binding.mainBsFrame,
									getString(R.string.access_forbidden_403));
						} else if (response.code() == 409) {
							enableButton();
							Snackbar.info(
									requireContext(),
									binding.mainBsFrame,
									getString(R.string.tag_already_exists));
						} else {
							enableButton();
							Snackbar.info(
									requireContext(),
									binding.mainBsFrame,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<TagsItem> call, @NonNull Throwable t) {
						enableButton();
						Snackbar.info(
								requireContext(),
								binding.mainBsFrame,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void disableButton() {
		binding.create.setEnabled(false);
		binding.create.setAlpha(0.5F);
	}

	private void enableButton() {
		binding.create.setEnabled(true);
		binding.create.setAlpha(1F);
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_tag_actions);

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
}
