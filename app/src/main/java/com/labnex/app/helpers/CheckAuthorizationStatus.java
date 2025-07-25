package com.labnex.app.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.labnex.app.R;
import com.labnex.app.activities.AppSettingsActivity;
import com.labnex.app.activities.MainActivity;
import com.labnex.app.activities.SignInActivity;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.core.CoreApplication;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.models.personal_access_tokens.PersonalAccessTokens;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class CheckAuthorizationStatus {

	public static int getAccountCount(Context context) {
		UserAccountsApi userAccountsApi = BaseApi.getInstance(context, UserAccountsApi.class);
		assert userAccountsApi != null;
		return userAccountsApi.getCount();
	}

	public static void authorizationErrorDialog(final Context context) {

		if (!(context instanceof Activity)) {
			return;
		}

		boolean hasMultipleAccounts = getAccountCount(context) > 1;

		MaterialAlertDialogBuilder materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context,
						com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder
				.setTitle(R.string.authorization_error)
				.setMessage(R.string.authorization_error_message)
				.setCancelable(false)
				.setPositiveButton(
						R.string.update,
						(dialog, which) -> showUpdateTokenDialog(context, null, false, true));

		if (hasMultipleAccounts) {
			materialAlertDialogBuilder.setNeutralButton(
					R.string.switch_account,
					(dialog, which) -> {
						Intent intent = new Intent(context, AppSettingsActivity.class);
						intent.putExtra("openAccountsBottomSheet", true);
						context.startActivity(intent);
						dialog.dismiss();
					});
		} else {
			materialAlertDialogBuilder.setNeutralButton(
					R.string.close,
					(dialog, which) -> {
						if (context instanceof MainActivity) {
							((MainActivity) context).finish();
						}
						dialog.dismiss();
					});
		}

		materialAlertDialogBuilder.show();
	}

	public static void checkTokenExpiryWarning(final Context context) {

		if (!(context instanceof Activity activity)) {
			return;
		}

		int accountId = SharedPrefDB.getInstance(context).getInt("currentActiveAccountId");
		UserAccountsApi userAccountsApi = BaseApi.getInstance(context, UserAccountsApi.class);
		assert userAccountsApi != null;
		UserAccount account = userAccountsApi.getAccountById(accountId);
		if (account == null) {
			return;
		}

		String warningKey = "token_expiry_warning_shown_" + accountId;
		if (SharedPrefDB.getInstance(context).getBoolean(warningKey, false)) {
			return;
		}

		String tokenExpiry = account.getTokenExpiry();
		if (tokenExpiry == null || tokenExpiry.isEmpty()) {
			return;
		}

		boolean hasMultipleAccounts = getAccountCount(context) == 1;

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate expiryDate = LocalDate.parse(tokenExpiry, formatter);
			LocalDate today = LocalDate.now();
			LocalDate sevenDaysFromNow = today.plusDays(7);

			if (expiryDate.isAfter(today) && !expiryDate.isAfter(sevenDaysFromNow)) {
				MaterialAlertDialogBuilder builder =
						new MaterialAlertDialogBuilder(
								activity,
								com.google.android.material.R.style
										.ThemeOverlay_Material3_Dialog_Alert);

				builder.setTitle(R.string.token_expiry_warning_title)
						.setMessage(R.string.token_expiry_warning_message)
						.setCancelable(false)
						.setPositiveButton(
								R.string.update,
								(dialog, which) ->
										showUpdateTokenDialog(context, accountId, false, false));

				if (hasMultipleAccounts) {
					builder.setNeutralButton(
							R.string.switch_account,
							(dialog, which) -> {
								Intent intent = new Intent(context, AppSettingsActivity.class);
								intent.putExtra("openAccountsBottomSheet", true);
								context.startActivity(intent);
								dialog.dismiss();
							});
				} else {
					builder.setNeutralButton(
							R.string.close,
							(dialog, which) -> {
								if (context instanceof MainActivity) {
									((MainActivity) context).finish();
								}
								dialog.dismiss();
							});
				}

				builder.show();
			}
		} catch (DateTimeParseException e) {
			// Invalid date format, skip warning
		}
	}

	public static void showUpdateTokenDialog(
			final Context context,
			Integer accountId,
			boolean fromAccountBs,
			boolean singleAccountClose) {
		if (!(context instanceof Activity activity)) {
			return;
		}

		boolean isSingleAccount = getAccountCount(context) == 1;
		boolean hasMultipleAccounts = getAccountCount(context) > 1;

		int targetAccountId =
				(accountId != null)
						? accountId
						: SharedPrefDB.getInstance(context).getInt("currentActiveAccountId");

		MaterialAlertDialogBuilder builder =
				new MaterialAlertDialogBuilder(
						activity,
						com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert);

		View view = LayoutInflater.from(context).inflate(R.layout.custom_update_token, null);
		TextInputLayout tokenInputLayout = view.findViewById(R.id.token_input_layout);
		EditText tokenInput = view.findViewById(R.id.token_input);

		builder.setTitle(R.string.update_token)
				.setCancelable(fromAccountBs)
				.setView(view)
				.setPositiveButton(R.string.update, null);

		if (fromAccountBs) {
			builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
		} else if (hasMultipleAccounts) {
			builder.setNeutralButton(
					R.string.switch_account,
					(dialog, which) -> {
						Intent intent = new Intent(context, AppSettingsActivity.class);
						intent.putExtra("openAccountsBottomSheet", true);
						context.startActivity(intent);
						dialog.dismiss();
					});
		}

		if (singleAccountClose && isSingleAccount) {
			builder.setNeutralButton(
					R.string.close,
					(dialog, which) -> {
						if (context instanceof MainActivity) {
							((MainActivity) context).finish();
						}
						dialog.dismiss();
					});
		}

		AlertDialog dialog = builder.create();
		dialog.show();

		dialog.getButton(AlertDialog.BUTTON_POSITIVE)
				.setOnClickListener(
						v -> {
							String newToken = tokenInput.getText().toString().trim();
							tokenInputLayout.setError(null);

							if (newToken.isEmpty()) {
								tokenInputLayout.setError(
										activity.getString(R.string.token_empty_error));
								return;
							}

							UserAccountsApi userAccountsApi =
									BaseApi.getInstance(context, UserAccountsApi.class);
							assert userAccountsApi != null;
							UserAccount account = userAccountsApi.getAccountById(targetAccountId);
							if (account == null) {
								Snackbar.info(
										activity,
										activity.getString(R.string.account_not_found_error));
								context.startActivity(new Intent(context, SignInActivity.class));
								if (context instanceof MainActivity) {
									((MainActivity) context).finish();
								}
								dialog.dismiss();
								return;
							}

							String instanceUrl = account.getInstanceUrl();
							getTokenExpiry(
									activity,
									instanceUrl,
									newToken,
									userAccountsApi,
									targetAccountId,
									dialog);
						});
	}

	private static void getTokenExpiry(
			Activity activity,
			String instanceUrl,
			String token,
			UserAccountsApi userAccountsApi,
			int accountId,
			AlertDialog dialog) {
		Call<PersonalAccessTokens> call =
				RetrofitClient.getApiInterface(activity, instanceUrl, "Bearer " + token)
						.getPersonalAccessTokenInfo();

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<PersonalAccessTokens> call,
							@NonNull retrofit2.Response<PersonalAccessTokens> response) {
						if (response.code() == 200 && response.body() != null) {
							String tokenExpiry = (String) response.body().getExpiresAt();
							userAccountsApi.updateToken(accountId, token);
							userAccountsApi.updateTokenExpiry(
									accountId, tokenExpiry != null ? tokenExpiry : "");

							String warningKey = "token_expiry_warning_shown_" + accountId;
							SharedPrefDB.getInstance(activity).putBoolean(warningKey, false);

							CoreApplication app =
									(CoreApplication) activity.getApplicationContext();
							UserAccount updatedAccount = userAccountsApi.getAccountById(accountId);
							if (updatedAccount != null) {
								if (accountId
										!= SharedPrefDB.getInstance(activity)
												.getInt("currentActiveAccountId")) {
									app.switchToAccount(updatedAccount, false);
								}
							}

							if (activity instanceof MainActivity) {
								MainActivity.refActivity = true;
								activity.recreate();
							}
							dialog.dismiss();
						} else {
							Snackbar.info(activity, activity.getString(R.string.not_authorized));
							dialog.dismiss();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<PersonalAccessTokens> call, @NonNull Throwable t) {
						Snackbar.info(
								activity,
								activity.getString(R.string.generic_server_response_error));
						dialog.dismiss();
					}
				});
	}
}
