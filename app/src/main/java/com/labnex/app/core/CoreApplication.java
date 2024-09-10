package com.labnex.app.core;

import android.app.Application;
import android.content.Context;
import com.labnex.app.contexts.AccountContext;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.helpers.AppSettingsInit;
import com.labnex.app.helpers.SharedPrefDB;

/**
 * @author opyale
 * @author mmarif
 */
public class CoreApplication extends Application {

	public AccountContext currentAccount;
	private SharedPrefDB sharedPrefDB;

	@Override
	public void onCreate() {

		super.onCreate();

		Context appCtx = getApplicationContext();
		sharedPrefDB = SharedPrefDB.getInstance(appCtx);

		currentAccount =
				AccountContext.fromId(sharedPrefDB.getInt("currentActiveAccountId", 0), appCtx);

		AppSettingsInit.initDefaultSettings(appCtx);

		AppSettingsInit.updateSettingsValue(
				getApplicationContext(), "false", AppSettingsInit.APP_BIOMETRIC_LIFE_CYCLE_KEY);
	}

	@Override
	protected void attachBaseContext(Context context) {

		super.attachBaseContext(context);

		sharedPrefDB = SharedPrefDB.getInstance(context);
	}

	public boolean switchToAccount(UserAccount userAccount, boolean tmp) {
		if (!tmp || sharedPrefDB.getInt("currentActiveAccountId") != userAccount.getAccountId()) {
			currentAccount = new AccountContext(userAccount);
			if (!tmp) {
				sharedPrefDB.putInt("currentActiveAccountId", userAccount.getAccountId());
			}
			return true;
		}
		return false;
	}
}
