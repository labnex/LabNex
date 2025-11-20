package com.labnex.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.labnex.app.R;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ActivityDeeplinksBinding;
import com.labnex.app.helpers.Utils;
import java.util.List;

/**
 * @author @mmarif
 */
public class DeepLinksActivity extends BaseActivity {

	private ActivityDeeplinksBinding viewBinding;
	private boolean accountFound = false;
	private int accountsCount;
	private Intent mainIntent;
	private Intent issueIntent;
	private Intent projectIntent;
	private Intent groupIntent;
	private Intent userIntent;
	private Uri data;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityDeeplinksBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		mainIntent = new Intent(ctx, MainActivity.class);
		issueIntent = new Intent(ctx, IssueDetailActivity.class);
		projectIntent = new Intent(ctx, ProjectDetailActivity.class);
		groupIntent = new Intent(ctx, GroupDetailActivity.class);
		userIntent = new Intent(ctx, ProfileActivity.class);

		Intent intent = getIntent();
		data = intent.getData();
		if (data == null) {
			startActivity(mainIntent);
			finish();
		}

		if ("labnex".equals(data.getScheme())) {
			StringBuilder httpsUrl = getStringBuilder();
			data = Uri.parse(httpsUrl.toString());
		}

		// check for login
		if (sharedPrefDB.getInt("currentActiveAccountId", -1) <= -1) {
			Intent loginIntent = new Intent(ctx, SignInActivity.class);
			loginIntent.putExtra("instanceUrl", data.getHost());
			ctx.startActivity(loginIntent);
			finish();
			return;
		}

		// check for the links(URI) to be in the db
		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
		assert userAccountsApi != null;
		List<UserAccount> userAccounts = userAccountsApi.usersAccounts();
		accountsCount = userAccountsApi.getCount();

		for (UserAccount userAccount : userAccounts) {

			String hostUri = userAccount.getInstanceUrl();

			String hostExternal = data.getHost();
			int portExternal = data.getPort();

			String hostUrlExternal;
			if (portExternal > 0) {
				hostUrlExternal = hostExternal + ":" + portExternal;
			} else {
				hostUrlExternal = hostExternal;
			}

			if (hostUrlExternal == null) {
				hostUrlExternal = "";
			}

			if (hostUri.toLowerCase().contains(hostUrlExternal.toLowerCase())) {

				accountFound = true;

				Utils.switchToAccount(ctx, userAccount, false);
				break;
			}
		}

		if (accountFound) {

			viewBinding.progressBar.setVisibility(View.GONE);
			viewBinding.addNewAccountFrame.setVisibility(View.GONE);
			viewBinding.othersFrame.setVisibility(View.VISIBLE);

		} else {

			viewBinding.progressBar.setVisibility(View.GONE);
			viewBinding.addNewAccountFrame.setVisibility(View.VISIBLE);
			viewBinding.othersFrame.setVisibility(View.GONE);

			if (accountsCount > 0) {
				viewBinding.goToApp.setVisibility(View.VISIBLE);
				viewBinding.goToApp.setOnClickListener(
						goToApp -> {
							startActivity(mainIntent);
							finish();
						});
			}

			viewBinding.addAccountText.setText(
					String.format(
							getResources().getString(R.string.account_does_not_exist),
							data.getHost()));

			viewBinding.addNewAccount.setOnClickListener(
					addNewAccount -> {
						Intent accountIntent = new Intent(ctx, SignInActivity.class);
						accountIntent.putExtra("instanceUrl", data.getHost());
						accountIntent.putExtra("source", "add_account");
						startActivity(accountIntent);
						finish();
					});
		}
	}

	@NonNull private StringBuilder getStringBuilder() {
		String originalHost = data.getHost();
		String originalPath = data.getPath();
		String query = data.getQuery();
		String fragment = data.getFragment();

		StringBuilder httpsUrl = new StringBuilder("https://");
		httpsUrl.append(originalHost);

		if (originalPath != null) {
			httpsUrl.append(originalPath);
		}

		if (query != null) {
			httpsUrl.append("?").append(query);
		}

		if (fragment != null) {
			httpsUrl.append("#").append(fragment);
		}
		return httpsUrl;
	}
}
