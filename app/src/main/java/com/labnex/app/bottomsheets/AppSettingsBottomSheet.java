package com.labnex.app.bottomsheets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.SignInActivity;
import com.labnex.app.adapters.UserAccountsAdapter;
import com.labnex.app.database.api.AppSettingsApi;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.NotesApi;
import com.labnex.app.database.api.ProjectsApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.databinding.BottomSheetAppSettingsBinding;
import com.labnex.app.interfaces.BottomSheetListener;
import java.util.Objects;

/**
 * @author mmarif
 */
public class AppSettingsBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetListener bottomSheetListener;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		BottomSheetAppSettingsBinding bottomSheetAppSettingsBinding =
				BottomSheetAppSettingsBinding.inflate(inflater, container, false);

		if (!Objects.requireNonNull(requireArguments().getString("source")).isEmpty()) {

			if (Objects.requireNonNull(requireArguments().getString("source"))
					.equalsIgnoreCase("accounts")) {

				bottomSheetAppSettingsBinding
						.bottomSheetAppAccounts
						.getRoot()
						.setVisibility(View.VISIBLE);

				bottomSheetAppSettingsBinding.bottomSheetAppAccounts.addNewAccount
						.setOnClickListener(
								v1 -> {
									Intent intent = new Intent(getActivity(), SignInActivity.class);
									intent.putExtra("source", "add_account");
									startActivity(intent);
									dismiss();
								});

				bottomSheetAppSettingsBinding.bottomSheetAppAccounts.removeAllAccounts
						.setOnClickListener(
								v1 -> {
									MaterialAlertDialogBuilder materialAlertDialogBuilder =
											new MaterialAlertDialogBuilder(requireContext())
													.setTitle(
															requireContext()
																	.getResources()
																	.getString(
																			R.string
																					.remove_all_accounts))
													.setMessage(
															requireContext()
																	.getResources()
																	.getString(
																			R.string
																					.remove_all_accounts_from_app_message))
													.setNeutralButton(
															requireContext()
																	.getResources()
																	.getString(R.string.cancel),
															null)
													.setPositiveButton(
															requireContext()
																	.getResources()
																	.getString(R.string.remove),
															(dialog, which) -> {
																UserAccountsApi userAccountsApi =
																		BaseApi.getInstance(
																				requireContext(),
																				UserAccountsApi
																						.class);
																NotesApi notesApi =
																		BaseApi.getInstance(
																				requireContext(),
																				NotesApi.class);
																ProjectsApi projectsApi =
																		BaseApi.getInstance(
																				requireContext(),
																				ProjectsApi.class);

																assert userAccountsApi != null;
																assert notesApi != null;
																assert projectsApi != null;

																userAccountsApi.deleteAllAccounts();
																notesApi.deleteAllNotes();
																projectsApi.deleteAllProjects();

																new Handler()
																		.postDelayed(
																				() -> {
																					Intent intent =
																							new Intent(
																									requireContext(),
																									SignInActivity
																											.class);
																					intent.setFlags(
																							Intent
																											.FLAG_ACTIVITY_NEW_TASK
																									| Intent
																											.FLAG_ACTIVITY_CLEAR_TASK);
																					requireContext()
																							.startActivity(
																									intent);
																					dismiss();
																				},
																				500);
															});

									materialAlertDialogBuilder.create().show();
								});

				UserAccountsAdapter arrayAdapter = new UserAccountsAdapter(getContext());
				bottomSheetAppSettingsBinding.bottomSheetAppAccounts.accountsList.setLayoutManager(
						new LinearLayoutManager(getContext()));
				bottomSheetAppSettingsBinding.bottomSheetAppAccounts.accountsList.setAdapter(
						arrayAdapter);
			}
		} else {
			dismiss();
		}

		return bottomSheetAppSettingsBinding.getRoot();
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
