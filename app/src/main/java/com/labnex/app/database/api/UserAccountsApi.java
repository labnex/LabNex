package com.labnex.app.database.api;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.labnex.app.database.dao.UserAccountsDao;
import com.labnex.app.database.models.UserAccount;
import java.util.List;

/**
 * @author mmarif
 */
public class UserAccountsApi extends BaseApi {

	private final UserAccountsDao userAccountsDao;

	UserAccountsApi(Context context) {
		super(context);
		userAccountsDao = labnexDatabase.userAccountsDao();
	}

	public long createNewAccount(
			String accountName,
			String instanceUrl,
			String userName,
			String token,
			String serverVersion,
			int maxResponseItems,
			int defaultPagingNumber,
			String tokenExpiry,
			int userId) {

		UserAccount userAccount = new UserAccount();
		userAccount.setAccountName(accountName);
		userAccount.setInstanceUrl(instanceUrl);
		userAccount.setUserName(userName);
		userAccount.setToken(token);
		userAccount.setServerVersion(serverVersion);
		userAccount.setMaxResponseItems(maxResponseItems);
		userAccount.setDefaultPagingNumber(defaultPagingNumber);
		userAccount.setTokenExpiry(tokenExpiry);
		userAccount.setUserId(userId);

		return userAccountsDao.createAccount(userAccount);
	}

	public void updateServerVersion(final String serverVersion, final int accountId) {
		executorService.execute(
				() -> userAccountsDao.updateServerVersion(serverVersion, accountId));
	}

	public void updateServerPagingLimit(
			final int maxResponseItems, final int defaultPagingNumber, final int accountId) {
		executorService.execute(
				() ->
						userAccountsDao.updateServerPagingLimit(
								maxResponseItems, defaultPagingNumber, accountId));
	}

	public void updateToken(final int accountId, final String token) {
		executorService.execute(() -> userAccountsDao.updateAccountToken(accountId, token));
	}

	public void updateTokenByAccountName(final String accountName, final String token) {
		executorService.execute(
				() -> userAccountsDao.updateAccountTokenByAccountName(accountName, token));
	}

	public void updateUsername(final int accountId, final String newName) {
		executorService.execute(() -> userAccountsDao.updateUserName(newName, accountId));
	}

	public UserAccount getAccountByName(String accountName) {
		return userAccountsDao.getAccountByName(accountName);
	}

	public UserAccount getAccountById(int accountId) {
		return userAccountsDao.getAccountById(accountId);
	}

	public Integer getCount() {
		return userAccountsDao.getCount();
	}

	public Boolean userAccountExists(String accountName) {
		return userAccountsDao.userAccountExists(accountName);
	}

	public LiveData<List<UserAccount>> getAllAccounts() {
		return userAccountsDao.getAllAccounts();
	}

	public List<UserAccount> usersAccounts() {
		return userAccountsDao.userAccounts();
	}

	public void deleteAccount(final int accountId) {
		executorService.execute(() -> userAccountsDao.deleteAccount(accountId));
	}

	public void deleteAllAccounts() {
		executorService.execute(userAccountsDao::deleteAllAccounts);
	}

	public void updateTokenExpiry(final int accountId, final String tokenExpiry) {
		executorService.execute(() -> userAccountsDao.updateTokenExpiry(accountId, tokenExpiry));
	}
}
