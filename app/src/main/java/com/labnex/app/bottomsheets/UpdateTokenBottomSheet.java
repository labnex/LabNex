package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.core.CoreApplication;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.BottomsheetUpdateTokenBinding;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.personal_access_tokens.PersonalAccessTokens;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class UpdateTokenBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetUpdateTokenBinding binding;
	private int accountId;

	public static UpdateTokenBottomSheet newInstance(int accountId) {
		UpdateTokenBottomSheet sheet = new UpdateTokenBottomSheet();
		Bundle args = new Bundle();
		args.putInt("account_id", accountId);
		sheet.setArguments(args);
		return sheet;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetUpdateTokenBinding.inflate(inflater, container, false);

		accountId = requireArguments().getInt("account_id", -1);

		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.btnUpdate.setOnClickListener(
				v -> {
					String token =
							binding.tokenInput.getText() != null
									? binding.tokenInput.getText().toString().trim()
									: "";
					if (token.isEmpty()) {
						Toasty.show(requireContext(), getString(R.string.token_empty_error));
						return;
					}
					binding.tokenInputLayout.setError(null);
					updateToken(token);
				});

		return binding.getRoot();
	}

	private void updateToken(String token) {
		Context ctx = requireContext();
		UserAccountsApi api = BaseApi.getInstance(ctx, UserAccountsApi.class);
		if (api == null) return;
		UserAccount account = api.getAccountById(accountId);
		if (account == null) {
			Toasty.show(ctx, getString(R.string.account_not_found_error));
			dismiss();
			return;
		}

		binding.btnUpdate.setText(null);
		binding.btnUpdate.setEnabled(false);
		binding.loadingIndicator.setVisibility(View.VISIBLE);

		RetrofitClient.getApiInterface(ctx, account.getInstanceUrl(), "Bearer " + token)
				.getPersonalAccessTokenInfo()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<PersonalAccessTokens> call,
									@NonNull Response<PersonalAccessTokens> response) {
								if (response.isSuccessful() && response.body() != null) {
									String expiry = (String) response.body().getExpiresAt();
									api.updateToken(accountId, token);
									api.updateTokenExpiry(accountId, expiry != null ? expiry : "");

									SharedPrefDB.getInstance(ctx)
											.putBoolean(
													"token_expiry_warning_shown_" + accountId,
													false);

									CoreApplication app =
											(CoreApplication) ctx.getApplicationContext();
									UserAccount updated = api.getAccountById(accountId);
									if (updated != null) app.switchToAccount(updated, false);

									AppUIStateManager.invalidateUI();
									if (getActivity() != null) getActivity().recreate();
									dismiss();
								} else {
									resetButton();
									Toasty.show(ctx, getString(R.string.not_authorized));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<PersonalAccessTokens> call,
									@NonNull Throwable t) {
								resetButton();
								Toasty.show(ctx, getString(R.string.generic_server_response_error));
							}
						});
	}

	private void resetButton() {
		binding.loadingIndicator.setVisibility(View.GONE);
		binding.btnUpdate.setText(R.string.update);
		binding.btnUpdate.setEnabled(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applySheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
