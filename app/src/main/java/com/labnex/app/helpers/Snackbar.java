package com.labnex.app.helpers;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import com.labnex.app.R;

/**
 * @author mmarif
 */
public class Snackbar {

	static TypedValue snackbarContainer = new TypedValue();
	static TypedValue snackbarOnContainer = new TypedValue();

	public static void info(Context context, View view, String message) {

		context.getTheme().resolveAttribute(R.attr.snackbarContainer, snackbarContainer, true);
		context.getTheme().resolveAttribute(R.attr.snackbarOnContainer, snackbarOnContainer, true);

		com.google.android.material.snackbar.Snackbar snackbar =
				com.google.android.material.snackbar.Snackbar.make(
						view, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
		snackbar.setBackgroundTint(snackbarOnContainer.data);
		snackbar.setActionTextColor(snackbarOnContainer.data);
		snackbar.show();
	}

	public static void info(Context context, View view, View bottomView, String message) {

		context.getTheme().resolveAttribute(R.attr.snackbarContainer, snackbarContainer, true);
		context.getTheme().resolveAttribute(R.attr.snackbarOnContainer, snackbarOnContainer, true);

		com.google.android.material.snackbar.Snackbar snackbar =
				com.google.android.material.snackbar.Snackbar.make(
						view, message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
		snackbar.setBackgroundTint(snackbarOnContainer.data);
		snackbar.setActionTextColor(snackbarOnContainer.data);
		snackbar.setAnchorView(bottomView);
		snackbar.show();
	}

	public static void info(Activity activity, String message) {

		activity.getTheme().resolveAttribute(R.attr.snackbarContainer, snackbarContainer, true);
		activity.getTheme().resolveAttribute(R.attr.snackbarOnContainer, snackbarOnContainer, true);

		com.google.android.material.snackbar.Snackbar snackbar =
				com.google.android.material.snackbar.Snackbar.make(
						activity.findViewById(android.R.id.content),
						message,
						com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
		snackbar.setBackgroundTint(snackbarOnContainer.data);
		snackbar.setActionTextColor(snackbarOnContainer.data);
		snackbar.show();
	}

	public static void info(Activity activity, View bottomView, String message) {

		activity.getTheme().resolveAttribute(R.attr.snackbarContainer, snackbarContainer, true);
		activity.getTheme().resolveAttribute(R.attr.snackbarOnContainer, snackbarOnContainer, true);

		com.google.android.material.snackbar.Snackbar snackbar =
				com.google.android.material.snackbar.Snackbar.make(
						activity.findViewById(android.R.id.content),
						message,
						com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
		snackbar.setBackgroundTint(snackbarOnContainer.data);
		snackbar.setActionTextColor(snackbarOnContainer.data);
		snackbar.setAnchorView(bottomView);
		snackbar.show();
	}
}
