package com.labnex.app.helpers;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.StringRes;

/**
 * @author mmarif
 */
public class Toasty {

	public static void show(Context context, String message) {
		if (context == null || message == null || message.isEmpty()) return;
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void show(Context context, @StringRes int resId) {
		if (context == null) return;
		Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show();
	}
}
