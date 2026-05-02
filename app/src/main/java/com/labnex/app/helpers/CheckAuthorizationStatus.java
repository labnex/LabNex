package com.labnex.app.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.AppSettingsActivity;
import com.labnex.app.activities.MainActivity;
import com.labnex.app.bottomsheets.UpdateTokenBottomSheet;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author mmarif
 */
public class CheckAuthorizationStatus {

	public static int getAccountCount(Context context) {
		UserAccountsApi api = BaseApi.getInstance(context, UserAccountsApi.class);
		return api != null ? api.getCount() : 0;
	}

	public static void authorizationErrorDialog(Context context) {
		if (!(context instanceof Activity)) return;

		int accountId = SharedPrefDB.getInstance(context).getInt("currentActiveAccountId");
		boolean hasMultipleAccounts = getAccountCount(context) > 1;

		MaterialAlertDialogBuilder builder =
				new MaterialAlertDialogBuilder(
						context,
						com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert);

		builder.setTitle(R.string.authorization_error)
				.setMessage(R.string.authorization_error_message)
				.setCancelable(false)
				.setPositiveButton(
						R.string.update,
						(dialog, which) -> showUpdateTokenBottomSheet(context, accountId));

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
						if (context instanceof MainActivity) ((MainActivity) context).finish();
						dialog.dismiss();
					});
		}

		builder.show();
	}

	public static void checkTokenExpiryWarning(Context context) {
		if (!(context instanceof Activity)) return;

		int accountId = SharedPrefDB.getInstance(context).getInt("currentActiveAccountId");
		UserAccountsApi api = BaseApi.getInstance(context, UserAccountsApi.class);
		if (api == null) return;
		UserAccount account = api.getAccountById(accountId);
		if (account == null) return;

		String warningKey = "token_expiry_warning_shown_" + accountId;
		if (SharedPrefDB.getInstance(context).getBoolean(warningKey, false)) return;

		String tokenExpiry = account.getTokenExpiry();
		if (tokenExpiry == null || tokenExpiry.isEmpty()) return;

		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			LocalDate expiryDate = LocalDate.parse(tokenExpiry, formatter);
			LocalDate today = LocalDate.now();
			LocalDate sevenDaysFromNow = today.plusDays(7);

			if (expiryDate.isAfter(today) && !expiryDate.isAfter(sevenDaysFromNow)) {
				boolean isSingleAccount = getAccountCount(context) == 1;

				MaterialAlertDialogBuilder builder =
						new MaterialAlertDialogBuilder(
								context,
								com.google.android.material.R.style
										.ThemeOverlay_Material3_Dialog_Alert);

				builder.setTitle(R.string.token_expiry_warning_title)
						.setMessage(R.string.token_expiry_warning_message)
						.setCancelable(false)
						.setPositiveButton(
								R.string.update,
								(dialog, which) -> showUpdateTokenBottomSheet(context, accountId));

				if (isSingleAccount) {
					builder.setNeutralButton(
							R.string.close,
							(dialog, which) -> {
								if (context instanceof MainActivity)
									((MainActivity) context).finish();
								dialog.dismiss();
							});
				} else {
					builder.setNeutralButton(
							R.string.switch_account,
							(dialog, which) -> {
								Intent intent = new Intent(context, AppSettingsActivity.class);
								intent.putExtra("openAccountsBottomSheet", true);
								context.startActivity(intent);
								dialog.dismiss();
							});
				}

				builder.show();
			}
		} catch (DateTimeParseException ignored) {
		}
	}

	public static void showUpdateTokenBottomSheet(Context context, int accountId) {
		if (!(context instanceof AppCompatActivity activity)) return;
		UpdateTokenBottomSheet.newInstance(accountId)
				.show(activity.getSupportFragmentManager(), "updateTokenBottomSheet");
	}
}
