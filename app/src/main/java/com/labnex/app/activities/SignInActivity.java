package com.labnex.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Patterns;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.TokenHelpBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.db.LabNexDatabase;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ActivitySignInBinding;
import com.labnex.app.helpers.ApiResponseHandler;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.labnex.app.helpers.Version;
import com.labnex.app.models.metadata.Metadata;
import com.labnex.app.models.personal_access_tokens.PersonalAccessTokens;
import com.labnex.app.models.user.User;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class SignInActivity extends BaseActivity {

	private ActivitySignInBinding binding;
	private URI instanceUrl;
	private Version gitlabVersion;
	private final int maxResponseItems = 50;
	private final int defaultPagingNumber = 25;
	private String tokenExpiry;
	private ActivityResultLauncher<Intent> importFileLauncher;
	private boolean isSigningIn = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivitySignInBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, null, binding.scrollView, null, null);

		Intent intent = getIntent();

		importFileLauncher =
				registerForActivityResult(
						new ActivityResultContracts.StartActivityForResult(),
						result -> {
							if (result.getResultCode() == Activity.RESULT_OK
									&& result.getData() != null) {
								Uri uri = result.getData().getData();
								if (uri != null) {
									processImport(uri);
								} else {
									Toasty.show(ctx, getString(R.string.import_failed));
								}
							} else {
								Toasty.show(ctx, getString(R.string.import_failed));
							}
						});

		if (intent.hasExtra("source")) {
			if ("add_account".equalsIgnoreCase(intent.getStringExtra("source"))) {
				binding.signIn.setText(R.string.add_new_account);
				binding.restore.setVisibility(View.GONE);
				intent.removeExtra("source");
			}
		}

		if (intent.hasExtra("instanceUrl")) {
			binding.instanceUrl.setText(intent.getStringExtra("instanceUrl"));
			intent.removeExtra("instanceUrl");
		}

		binding.personalTokenHelper.setOnClickListener(token -> showTokenHelpBottomSheet());
		binding.signIn.setOnClickListener(checkUser -> checkUserInput());
		binding.restore.setOnClickListener(checkUser -> launchImportFilePicker());
	}

	public void launchImportFilePicker() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("application/octet-stream");
		importFileLauncher.launch(intent);
	}

	private void processImport(Uri uri) {
		try {
			File dbFile = getDatabasePath("labnex");

			LabNexDatabase db = LabNexDatabase.getDatabaseInstance(this);
			if (db != null && db.isOpen()) {
				db.close();
			}
			BaseApi.clearInstance();

			try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
				if (pfd == null) {
					throw new IOException("Failed to open file descriptor for URI: " + uri);
				}
				try (FileInputStream fis = new FileInputStream(pfd.getFileDescriptor());
						FileChannel src = fis.getChannel();
						FileOutputStream fos = new FileOutputStream(dbFile);
						FileChannel dst = fos.getChannel()) {
					dst.transferFrom(src, 0, src.size());
				}
			}

			db = LabNexDatabase.getDatabaseInstance(this);
			SupportSQLiteDatabase writableDb = db.getOpenHelper().getWritableDatabase();
			if (!writableDb.isOpen()) {
				throw new SQLiteException("Database not opened after restore");
			}

			Toasty.show(ctx, getString(R.string.import_success));

			UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
			assert userAccountsApi != null;
			List<UserAccount> accounts = userAccountsApi.usersAccounts();

			if (!accounts.isEmpty()) {
				UserAccount account = accounts.get(0);
				Utils.switchToAccount(ctx, account);
				new Handler(Looper.getMainLooper()).postDelayed(this::restartApp, 1500);
			} else {
				LabNexDatabase.getDatabaseInstance(this);
				SharedPrefDB.getInstance(this).putInt("currentActiveAccountId", -1);
				Toasty.show(ctx, getString(R.string.import_failed));
			}
		} catch (IOException | SQLiteException e) {
			Toasty.show(ctx, getString(R.string.import_failed));
			LabNexDatabase.getDatabaseInstance(this);
		}
	}

	private void restartApp() {
		Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			finish();
			startActivity(intent);
		}
	}

	private void checkUserInput() {
		if (isSigningIn) return;
		isSigningIn = true;
		showLoading();

		String instanceUrlRaw =
				Objects.requireNonNull(binding.instanceUrl.getText())
						.toString()
						.replaceAll("\\uFEFF", "")
						.trim();
		String loginToken =
				Objects.requireNonNull(binding.personalToken.getText())
						.toString()
						.replaceAll("[\\uFEFF|#]", "")
						.trim();

		if (instanceUrlRaw.isEmpty()) {
			Toasty.show(ctx, getString(R.string.gitlab_url_empty_error));
			hideLoading();
			return;
		}
		if (loginToken.isEmpty()) {
			Toasty.show(ctx, getString(R.string.gitlab_token_empty_error));
			hideLoading();
			return;
		}
		if (instanceUrlRaw.contains(" ")) {
			Toasty.show(ctx, getString(R.string.gitlab_url_spaces_not_supported));
			hideLoading();
			return;
		}
		if (loginToken.contains(" ")) {
			Toasty.show(ctx, getString(R.string.gitlab_token_spaces_not_supported));
			hideLoading();
			return;
		}
		if (instanceUrlRaw.startsWith("http://") || instanceUrlRaw.startsWith("https://")) {
			Toasty.show(ctx, getString(R.string.gitlab_url_no_http_allowed));
			hideLoading();
			return;
		}
		if (!instanceUrlRaw.contains(".")) {
			Toasty.show(ctx, getString(R.string.gitlab_url_missing_dot));
			hideLoading();
			return;
		}
		if (!Patterns.WEB_URL.matcher(instanceUrlRaw).matches()) {
			Toasty.show(ctx, getString(R.string.gitlab_url_invalid_format));
			hideLoading();
			return;
		}

		try {
			instanceUrl = URI.create("https://" + instanceUrlRaw + "/api/v4/");
			versionCheck(loginToken);
		} catch (IllegalArgumentException e) {
			Toasty.show(ctx, getString(R.string.gitlab_url_invalid_format));
			hideLoading();
		}
	}

	private void showTokenHelpBottomSheet() {
		TokenHelpBottomSheet sheet = TokenHelpBottomSheet.newInstance();
		sheet.show(getSupportFragmentManager(), "tokenHelpSheet");
	}

	private void versionCheck(String token) {
		RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), "Bearer " + token)
				.getMetadata()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Metadata> call,
									@NonNull retrofit2.Response<Metadata> response) {
								if (response.isSuccessful() && response.body() != null) {
									Metadata metadata = response.body();

									if (!Version.valid(metadata.getVersion())) {
										Toasty.show(ctx, getString(R.string.version_unknown));
										hideLoading();
										return;
									}

									gitlabVersion = new Version(metadata.getVersion());

									if (gitlabVersion.less(getString(R.string.versionLow))) {
										new MaterialAlertDialogBuilder(ctx)
												.setTitle(
														getString(
																R.string
																		.version_alert_dialog_header))
												.setMessage(
														getString(
																R.string.version_unsupported_old,
																metadata.getVersion()))
												.setNeutralButton(
														R.string.cancel, (d, w) -> hideLoading())
												.setPositiveButton(
														R.string.proceed,
														(d, w) -> getTokenExpiry(token))
												.show();
									} else if (gitlabVersion.lessOrEqual(
											getString(R.string.versionHigh))) {
										getTokenExpiry(token);
									} else {
										Toasty.show(
												ctx, getString(R.string.version_unsupported_new));
										setupAccount(token);
									}
								} else {
									showApiError(response);
									hideLoading();
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Metadata> call, @NonNull Throwable t) {
								Toasty.show(
										ctx,
										t.getMessage() != null
												? t.getMessage()
												: getString(
														R.string.generic_server_response_error));
								hideLoading();
							}
						});
	}

	private void setupAccount(String token) {
		RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), "Bearer " + token)
				.getCurrentUser()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<User> call,
									@NonNull retrofit2.Response<User> response) {
								if (response.isSuccessful() && response.body() != null) {
									User userDetails = response.body();

									String accountName =
											userDetails.getUsername() + "@" + instanceUrl;
									UserAccountsApi userAccountsApi =
											BaseApi.getInstance(ctx, UserAccountsApi.class);
									assert userAccountsApi != null;
									boolean userAccountExists =
											userAccountsApi.userAccountExists(accountName);
									UserAccount account;
									if (!userAccountExists) {
										long accountId =
												userAccountsApi.createNewAccount(
														accountName,
														instanceUrl.toString(),
														userDetails.getFullName(),
														token,
														gitlabVersion.toString(),
														maxResponseItems,
														defaultPagingNumber,
														tokenExpiry,
														userDetails.getId());
										account = userAccountsApi.getAccountById((int) accountId);
									} else {
										userAccountsApi.updateTokenByAccountName(
												accountName, token);
										account = userAccountsApi.getAccountByName(accountName);
									}

									Utils.switchToAccount(SignInActivity.this, account);
									hideLoading();

									Intent intent =
											new Intent(SignInActivity.this, MainActivity.class);
									intent.setFlags(
											Intent.FLAG_ACTIVITY_NEW_TASK
													| Intent.FLAG_ACTIVITY_CLEAR_TASK);
									startActivity(intent);
									finish();
								} else {
									showApiError(response);
									hideLoading();
								}
							}

							@Override
							public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
								Toasty.show(
										ctx,
										t.getMessage() != null
												? t.getMessage()
												: getString(
														R.string.generic_server_response_error));
								hideLoading();
							}
						});
	}

	private void getTokenExpiry(String token) {
		RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), "Bearer " + token)
				.getPersonalAccessTokenInfo()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<PersonalAccessTokens> call,
									@NonNull retrofit2.Response<PersonalAccessTokens> response) {
								if (response.isSuccessful() && response.body() != null) {
									tokenExpiry = (String) response.body().getExpiresAt();
									setupAccount(token);
								} else {
									showApiError(response);
									hideLoading();
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<PersonalAccessTokens> call,
									@NonNull Throwable t) {
								hideLoading();
							}
						});
	}

	private void showApiError(retrofit2.Response<?> response) {
		switch (ApiResponseHandler.getErrorMessageStatic(response)) {
			case "auth_error":
				Toasty.show(ctx, getString(R.string.token_auth_error));
				break;
			case "access_forbidden_403":
				Toasty.show(ctx, getString(R.string.access_forbidden_403));
				break;
			case "not_found":
				Toasty.show(ctx, getString(R.string.not_found));
				break;
			case "generic_error":
				Toasty.show(ctx, getString(R.string.generic_error));
				break;
			default:
				Toasty.show(ctx, ApiResponseHandler.getErrorMessageStatic(response));
				break;
		}
	}

	private void showLoading() {
		binding.signIn.setText(null);
		binding.signIn.setEnabled(false);
		binding.restore.setEnabled(false);
		binding.restore.setAlpha(0.5f);
		binding.loadingIndicator.setVisibility(View.VISIBLE);
	}

	private void hideLoading() {
		isSigningIn = false;
		binding.loadingIndicator.setVisibility(View.GONE);
		binding.signIn.setText(
				getIntent().hasExtra("source")
								&& "add_account".equals(getIntent().getStringExtra("source"))
						? R.string.add_new_account
						: R.string.action_sign_in);
		binding.signIn.setEnabled(true);
		binding.restore.setEnabled(true);
		binding.restore.setAlpha(1f);
	}
}
