package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.content.Context;
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
import com.labnex.app.databinding.BottomSheetGroupsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.groups.CreateGroup;
import com.labnex.app.models.groups.GroupsItem;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class GroupsBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetListener bottomSheetListener;
	private String source;
	private BottomSheetGroupsBinding bottomSheetGroupsBinding;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetGroupsBinding = BottomSheetGroupsBinding.inflate(inflater, container, false);

		Bundle bundle = getArguments();
		assert bundle != null;

		if (bundle.getString("source") != null) {
			source = bundle.getString("source");
		} else {
			source = "";
		}

		if (!Objects.requireNonNull(requireArguments().getString("source")).isEmpty()) {

			if (Objects.requireNonNull(requireArguments().getString("source"))
					.equalsIgnoreCase("new")) {

				bottomSheetGroupsBinding
						.bottomSheetGroupsCreate
						.getRoot()
						.setVisibility(View.VISIBLE);

				bottomSheetGroupsBinding.bottomSheetGroupsCreate.closeBs.setOnClickListener(
						close -> dismiss());

				bottomSheetGroupsBinding.bottomSheetGroupsCreate.create.setOnClickListener(
						create -> {
							String groupName =
									Objects.requireNonNull(
													bottomSheetGroupsBinding.bottomSheetGroupsCreate
															.groupName.getText())
											.toString();
							String groupDescription =
									Objects.requireNonNull(
													bottomSheetGroupsBinding.bottomSheetGroupsCreate
															.groupDescription.getText())
											.toString();

							if (groupName.isEmpty()) {

								Snackbar.info(
										requireContext(),
										bottomSheetGroupsBinding.mainBsFrame,
										getString(R.string.group_name_required));
							} else {
								createGroup(groupName, groupDescription);
							}
						});
			}
		} else {
			dismiss();
		}

		return bottomSheetGroupsBinding.getRoot();
	}

	private String getVisibility() {

		String visibility = "private";
		if (bottomSheetGroupsBinding.bottomSheetGroupsCreate.privateGroup.isChecked()) {
			visibility = "private";
		}
		if (bottomSheetGroupsBinding.bottomSheetGroupsCreate.internalGroup.isChecked()) {
			visibility = "internal";
		}
		if (bottomSheetGroupsBinding.bottomSheetGroupsCreate.publicGroup.isChecked()) {
			visibility = "public";
		}
		return visibility;
	}

	private void createGroup(String name, String description) {

		String visibility = getVisibility();
		CreateGroup createGroup = new CreateGroup();
		createGroup.setName(name);
		createGroup.setDescription(description);
		createGroup.setPath(name.replace(" ", "-").toLowerCase());
		createGroup.setVisibility(visibility);

		Call<GroupsItem> call =
				RetrofitClient.getApiInterface(requireContext()).createGroup(createGroup);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<GroupsItem> call,
							@NonNull Response<GroupsItem> response) {

						if (response.code() == 201) {

							Snackbar.info(
									requireContext(),
									bottomSheetGroupsBinding.mainBsFrame,
									getString(R.string.group_created));
							dismiss();

						} else if (response.code() == 401) {

							Snackbar.info(
									requireContext(),
									bottomSheetGroupsBinding.mainBsFrame,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							Snackbar.info(
									requireContext(),
									bottomSheetGroupsBinding.mainBsFrame,
									getString(R.string.access_forbidden_403));
						} else {

							Snackbar.info(
									requireContext(),
									bottomSheetGroupsBinding.mainBsFrame,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<GroupsItem> call, @NonNull Throwable t) {

						Snackbar.info(
								requireContext(),
								bottomSheetGroupsBinding.mainBsFrame,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_notes);

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
			bottomSheetListener = (BottomSheetListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}
}
