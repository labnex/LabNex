package com.labnex.app.bottomsheets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.activities.SignInActivity;
import com.labnex.app.adapters.UserAccountsAdapter;
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
