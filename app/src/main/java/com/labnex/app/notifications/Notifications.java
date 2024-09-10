package com.labnex.app.notifications;

import android.content.Context;
import com.labnex.app.helpers.SharedPrefDB;

/**
 * @author opyale
 * @author mmarif
 */
public class Notifications {

	public static int uniqueNotificationId(Context context) {

		SharedPrefDB sharedPrefDB = SharedPrefDB.getInstance(context);

		int previousNotificationId = sharedPrefDB.getInt("previousNotificationId", 0);
		int nextPreviousNotificationId =
				previousNotificationId == Integer.MAX_VALUE ? 0 : previousNotificationId + 1;

		sharedPrefDB.putInt("previousNotificationId", nextPreviousNotificationId);
		return previousNotificationId;
	}
}
