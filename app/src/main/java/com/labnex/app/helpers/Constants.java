package com.labnex.app.helpers;

import com.labnex.app.core.CoreApplication;

/**
 * @author mmarif
 */
public class Constants {

	public static final int maximumFileViewerSize = 3 * 1024 * 1024;
	public static final String downloadNotificationChannelId = "dl_channel";
	public static final int DEFAULT_RESULT_LIMIT = 100;

	public static int getResultLimit() {
		int limit = CoreApplication.getInstance().currentAccount.getMaxPageLimit();
		return limit > 0 ? limit : DEFAULT_RESULT_LIMIT;
	}
}
