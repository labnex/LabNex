package com.labnex.app.helpers;

import android.content.Context;
import android.content.Intent;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.AppSettingsActivity;

/**
 * @author mmarif
 */
public class CheckAuthorizationStatus {

	public static void authorizationErrorDialog(final Context context) {

		MaterialAlertDialogBuilder materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context,
						com.google.android.material.R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder
				.setTitle(R.string.authorization_error)
				.setMessage(R.string.authorization_error_message)
				.setCancelable(true)
				.setNeutralButton(R.string.cancel, null)
				.setPositiveButton(
						R.string.update,
						(dialog, which) -> {
							Intent intent = new Intent(context, AppSettingsActivity.class);
							context.startActivity(intent);
						})
				.show();
	}
}
