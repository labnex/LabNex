package com.labnex.app.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.labnex.app.database.models.UserAccount;
import java.util.List;

/**
 * @author mmarif
 */
@Dao
public interface UserAccountsDao {

	@Insert
	long createAccount(UserAccount userAccounts);

	@Query("SELECT * FROM UserAccounts ORDER BY accountId ASC")
	LiveData<List<UserAccount>> getAllAccounts();

	@Query("SELECT * FROM UserAccounts ORDER BY accountId ASC")
	List<UserAccount> userAccounts();

	@Query("SELECT COUNT(accountId) FROM UserAccounts")
	Integer getCount();

	@Query("SELECT COUNT(accountId) FROM UserAccounts WHERE accountName = :accountName LIMIT 1")
	Boolean userAccountExists(String accountName);

	@Query("SELECT * FROM UserAccounts WHERE accountName = :accountName LIMIT 1")
	UserAccount getAccountByName(String accountName);

	@Query("SELECT * FROM UserAccounts WHERE accountId = :accountId LIMIT 1")
	UserAccount getAccountById(int accountId);

	@Query("UPDATE UserAccounts SET serverVersion = :serverVersion WHERE accountId = :accountId")
	void updateServerVersion(String serverVersion, int accountId);

	@Query(
			"UPDATE UserAccounts SET maxResponseItems = :maxResponseItems, defaultPagingNumber = :defaultPagingNumber WHERE accountId = :accountId")
	void updateServerPagingLimit(int maxResponseItems, int defaultPagingNumber, int accountId);

	@Query("UPDATE UserAccounts SET accountName = :accountName WHERE accountId = :accountId")
	void updateAccountName(String accountName, int accountId);

	@Query("UPDATE UserAccounts SET token = :token WHERE accountId = :accountId")
	void updateAccountToken(int accountId, String token);

	@Query("UPDATE UserAccounts SET token = :token WHERE accountName = :accountName")
	void updateAccountTokenByAccountName(String accountName, String token);

	@Query(
			"UPDATE UserAccounts SET instanceUrl = :instanceUrl, token = :token WHERE accountId = :accountId")
	void updateHostInfo(String instanceUrl, String token, int accountId);

	@Query("UPDATE UserAccounts SET userName = :userName WHERE accountId = :accountId")
	void updateUserName(String userName, int accountId);

	@Query(
			"UPDATE UserAccounts SET instanceUrl = :instanceUrl, token = :token, userName = :userName, serverVersion = :serverVersion WHERE accountId = :accountId")
	void updateAll(
			String instanceUrl, String token, String userName, String serverVersion, int accountId);

	@Query("DELETE FROM UserAccounts WHERE accountId = :accountId")
	void deleteAccount(int accountId);

	@Query("DELETE FROM UserAccounts")
	void deleteAllAccounts();

	@Query("UPDATE UserAccounts SET tokenExpiry = :tokenExpiry WHERE accountId = :accountId")
	void updateTokenExpiry(int accountId, String tokenExpiry);
}
