package com.labnex.app.contexts;

import android.content.Context;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.helpers.Version;
import com.labnex.app.models.user.User;
import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import okhttp3.Credentials;

/**
 * @author qwerty287
 * @author mmarif
 */
public class AccountContext implements Serializable {

	private UserAccount account;
	private User userInfo;

	public AccountContext(UserAccount account) {
		this.account = account;
	}

	public static AccountContext fromId(int id, Context context) {
		return new AccountContext(
				Objects.requireNonNull(UserAccountsApi.getInstance(context, UserAccountsApi.class))
						.getAccountById(id));
	}

	public UserAccount getAccount() {

		return account;
	}

	public void setAccount(UserAccount account) {

		this.account = account;
	}

	public String getAuthorization() {
		return "Bearer " + account.getToken();
	}

	public String getWebAuthorization() {
		return Credentials.basic("", account.getToken());
	}

	public Version getServerVersion() {
		return new Version(account.getServerVersion());
	}

	public boolean requiresVersion(String version) {
		return getServerVersion().higherOrEqual(version);
	}

	public int getDefaultPageLimit() {
		return getAccount().getDefaultPagingNumber();
	}

	public int getMaxPageLimit() {
		return getAccount().getMaxResponseItems();
	}

	public User getUserInfo() {

		return userInfo;
	}

	public void setUserInfo(User userInfo) {

		this.userInfo = userInfo;
	}

	public String getName() {
		return userInfo != null
				? !userInfo.getFullName().isEmpty()
						? userInfo.getFullName()
						: userInfo.getUsername()
				: account.getUserName();
	}

	public File getCacheDir(Context context) {

		assert account.getAccountName() != null;
		return new File(context.getCacheDir() + "responses", account.getAccountName());
	}

	public int getUserId() {
		return getAccount().getUserId();
	}

	public String getTokenExpiry() {
		return getAccount().getTokenExpiry();
	}
}
