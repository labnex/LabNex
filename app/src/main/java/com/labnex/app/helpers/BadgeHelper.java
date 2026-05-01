package com.labnex.app.helpers;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.OptIn;
import com.google.android.material.badge.BadgeDrawable;
import com.labnex.app.R;

/**
 * @author mmarif
 */
public class BadgeHelper {

	@OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
	public static BadgeDrawable updateBadge(
			Context context, View anchor, BadgeDrawable badge, int count) {
		if (count > 0) {
			if (badge == null) {
				badge = BadgeDrawable.create(context);
				badge.setBackgroundColor(getThemeColor(context, R.attr.primaryTextColor));
				badge.setBadgeTextColor(getThemeColor(context, R.attr.materialCardBackgroundColor));

				badge.setMaxCharacterCount(4);

				int offset = context.getResources().getDimensionPixelSize(R.dimen.dimen20dp);
				badge.setHorizontalOffset(offset);
				badge.setVerticalOffset(offset);

				final BadgeDrawable finalBadge = badge;
				anchor.post(
						() ->
								com.google.android.material.badge.BadgeUtils.attachBadgeDrawable(
										finalBadge, anchor, null));
			}

			badge.setNumber(count);
			badge.setVisible(true);

			updateAnchorMargins(anchor, count);

		} else if (badge != null) {
			badge.setVisible(false);
			updateAnchorMargins(anchor, 0);
		}
		return badge;
	}

	private static void updateAnchorMargins(View anchor, int count) {
		if (!(anchor.getLayoutParams() instanceof LinearLayout.LayoutParams params)) return;

		Context context = anchor.getContext();
		int baseMargin = context.getResources().getDimensionPixelSize(R.dimen.dimen8dp);
		int extraSpacing = 0;

		if (count >= 1000) {
			extraSpacing = context.getResources().getDimensionPixelSize(R.dimen.dimen16dp);
		} else if (count >= 100) {
			extraSpacing = context.getResources().getDimensionPixelSize(R.dimen.dimen12dp);
		} else if (count >= 10) {
			extraSpacing = context.getResources().getDimensionPixelSize(R.dimen.dimen4dp);
		}

		params.setMarginEnd(baseMargin + extraSpacing);
		anchor.setLayoutParams(params);
	}

	public static int getThemeColor(Context context, int attr) {
		TypedValue typedValue = new TypedValue();
		if (context.getTheme().resolveAttribute(attr, typedValue, true)) {
			return typedValue.data;
		}
		return 0;
	}
}
