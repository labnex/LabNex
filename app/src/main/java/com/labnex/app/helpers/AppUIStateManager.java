package com.labnex.app.helpers;

/**
 * @author mmarif
 */
public class AppUIStateManager {

	private static int uiVersion = 0;
	private static int dataVersion = 0;

	public static void invalidateUI() {
		uiVersion++;
	}

	public static int getUiVersion() {
		return uiVersion;
	}

	public static void refreshData() {
		dataVersion++;
	}

	public static int getDataVersion() {
		return dataVersion;
	}
}
