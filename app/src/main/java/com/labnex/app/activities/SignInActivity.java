package com.labnex.app.activities;

import static android.view.View.GONE;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Patterns;
import android.util.TypedValue;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.db.LabNexDatabase;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ActivitySignInBinding;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Snackbar;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivitySignInBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

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
									Snackbar.info(
											this,
											findViewById(R.id.bottom_app_bar),
											getString(R.string.import_failed));
								}
							} else {
								Snackbar.info(
										this,
										findViewById(R.id.bottom_app_bar),
										getString(R.string.import_failed));
							}
						});

		if (intent.hasExtra("source")) {
			if (Objects.requireNonNull(intent.getStringExtra("source"))
					.equalsIgnoreCase("add_account")) {
				binding.signIn.setText(R.string.add_new_account);
				binding.signInText.setText(R.string.add_new_account);
				binding.restore.setVisibility(GONE);
				intent.removeExtra("source");
			}
		}

		if (intent.hasExtra("instanceUrl")) {
			binding.instanceUrl.setText(
					Objects.requireNonNull(intent.getStringExtra("instanceUrl")));
			intent.removeExtra("instanceUrl");
		}

		binding.personalTokenHelper.setOnClickListener(token -> showTokenHelpDialog());

		binding.signIn.setOnClickListener(
				checkUser -> {
					disableSignInButton();
					checkUserInput();
				});

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

			Snackbar.info(
					this, findViewById(R.id.bottom_app_bar), getString(R.string.import_success));

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
				Snackbar.info(
						this, findViewById(R.id.bottom_app_bar), getString(R.string.import_failed));
			}
		} catch (IOException | SQLiteException e) {
			Snackbar.info(
					this, findViewById(R.id.bottom_app_bar), getString(R.string.import_failed));
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
		try {
			String instanceUrlRaw =
					Objects.requireNonNull(binding.instanceUrl.getText())
							.toString()
							.replaceAll("[\\uFEFF]", "") // Remove BOM if present
							.trim();
			String loginToken =
					Objects.requireNonNull(binding.personalToken.getText())
							.toString()
							.replaceAll("[\\uFEFF|#]", "")
							.trim();

			if (instanceUrlRaw.isEmpty()) {
				Snackbar.info(SignInActivity.this, getString(R.string.gitlab_url_empty_error));
				enableSignInButton();
				return;
			}
			if (loginToken.isEmpty()) {
				Snackbar.info(SignInActivity.this, getString(R.string.gitlab_token_empty_error));
				enableSignInButton();
				return;
			}

			if (instanceUrlRaw.contains(" ")) {
				Snackbar.info(
						SignInActivity.this, getString(R.string.gitlab_url_spaces_not_supported));
				enableSignInButton();
				return;
			}

			if (loginToken.contains(" ")) {
				Snackbar.info(
						SignInActivity.this, getString(R.string.gitlab_token_spaces_not_supported));
				enableSignInButton();
				return;
			}

			if (instanceUrlRaw.startsWith("http://") || instanceUrlRaw.startsWith("https://")) {
				Snackbar.info(SignInActivity.this, getString(R.string.gitlab_url_no_http_allowed));
				enableSignInButton();
				return;
			}

			if (!instanceUrlRaw.contains(".")) {
				Snackbar.info(SignInActivity.this, getString(R.string.gitlab_url_missing_dot));
				enableSignInButton();
				return;
			}

			if (!Patterns.WEB_URL.matcher(instanceUrlRaw).matches()) {
				Snackbar.info(SignInActivity.this, getString(R.string.gitlab_url_invalid_format));
				enableSignInButton();
				return;
			}

			instanceUrl = URI.create("https://" + instanceUrlRaw + "/api/v4/");

			versionCheck(loginToken);

		} catch (IllegalArgumentException e) {
			Snackbar.info(SignInActivity.this, getString(R.string.gitlab_url_invalid_format));
			enableSignInButton();
		} catch (Exception e) {
			Snackbar.info(SignInActivity.this, getString(R.string.generic_error));
			enableSignInButton();
		}
	}

	private void showTokenHelpDialog() {

		MaterialAlertDialogBuilder dialogBuilder =
				new MaterialAlertDialogBuilder(this)
						.setTitle(R.string.how_to_get_access_token)
						.setMessage(
								HtmlCompat.fromHtml(
										getString(R.string.where_to_get_token_message),
										HtmlCompat.FROM_HTML_MODE_LEGACY))
						.setPositiveButton(R.string.close, null)
						.setCancelable(true);

		AlertDialog dialog = dialogBuilder.create();
		dialog.show();

		TextView messageView = dialog.findViewById(android.R.id.message);
		if (messageView != null) {
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			int paddingTop =
					(int)
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_DIP,
									16,
									getResources().getDisplayMetrics());
			messageView.setPadding(
					messageView.getPaddingLeft(),
					paddingTop,
					messageView.getPaddingRight(),
					messageView.getPaddingBottom());
		}
	}

	private void versionCheck(String token) {

		Call<Metadata> callVersion;

		if (!token.isEmpty()) {

			callVersion =
					RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), "Bearer " + token)
							.getMetadata();

			callVersion.enqueue(
					new Callback<>() {

						@Override
						public void onResponse(
								@NonNull final Call<Metadata> callVersion,
								@NonNull retrofit2.Response<Metadata> responseVersion) {

							if (responseVersion.code() == 401) {

								Snackbar.info(
										SignInActivity.this, getString(R.string.not_authorized));
								enableSignInButton();
							} else if (responseVersion.code() == 200) {

								Metadata metadata = responseVersion.body();
								assert metadata != null;

								if (!Version.valid(metadata.getVersion())) {

									Snackbar.info(
											SignInActivity.this,
											getString(R.string.version_unknown));
									enableSignInButton();
									return;
								}

								gitlabVersion = new Version(metadata.getVersion());

								if (gitlabVersion.less(getString(R.string.versionLow))) {

									MaterialAlertDialogBuilder materialAlertDialogBuilder =
											new MaterialAlertDialogBuilder(ctx)
													.setTitle(
															getString(
																	R.string
																			.version_alert_dialog_header))
													.setMessage(
															getResources()
																	.getString(
																			R.string
																					.version_unsupported_old,
																			metadata.getVersion()))
													.setNeutralButton(
															getString(R.string.cancel),
															(dialog, which) -> {
																dialog.dismiss();
																enableSignInButton();
															})
													.setPositiveButton(
															getString(R.string.proceed),
															(dialog, which) -> {
																dialog.dismiss();
																setupAccount(token);
															});

									materialAlertDialogBuilder.create().show();
								} else if (gitlabVersion.lessOrEqual(
										getString(R.string.versionHigh))) {

									getTokenExpiry(token);
								} else {

									Snackbar.info(
											SignInActivity.this,
											getString(R.string.version_unsupported_new));
									setupAccount(token);
								}
							}
						}

						@Override
						public void onFailure(
								@NonNull Call<Metadata> callVersion, @NonNull Throwable t) {

							Snackbar.info(
									SignInActivity.this,
									getString(R.string.generic_server_response_error));
							enableSignInButton();
						}
					});
		}
	}

	private void setupAccount(String token) {

		Call<User> call =
				RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), "Bearer " + token)
						.getCurrentUser();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

						User userDetails = response.body();

						switch (response.code()) {
							case 200:
								assert userDetails != null;

								// insert new account to db if does not exist
								String accountName = userDetails.getUsername() + "@" + instanceUrl;
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
									userAccountsApi.updateTokenByAccountName(accountName, token);
									account = userAccountsApi.getAccountByName(accountName);
								}

								Utils.switchToAccount(SignInActivity.this, account);

								enableSignInButton();
								Intent intent = new Intent(SignInActivity.this, MainActivity.class);
								intent.setFlags(
										Intent.FLAG_ACTIVITY_NEW_TASK
												| Intent.FLAG_ACTIVITY_CLEAR_TASK);
								startActivity(intent);
								finish();
								break;
							case 401:
								Snackbar.info(
										SignInActivity.this, getString(R.string.not_authorized));
								enableSignInButton();
								break;
							default:
								Snackbar.info(
										SignInActivity.this,
										getString(R.string.generic_api_error, response.code()));
								enableSignInButton();
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {

						Snackbar.info(
								SignInActivity.this,
								getString(R.string.generic_server_response_error));
						enableSignInButton();
					}
				});
	}

	private void getTokenExpiry(String token) {

		Call<PersonalAccessTokens> call =
				RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), "Bearer " + token)
						.getPersonalAccessTokenInfo();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<PersonalAccessTokens> call,
							@NonNull retrofit2.Response<PersonalAccessTokens> response) {

						if (response.code() == 200) {
							assert response.body() != null;
							tokenExpiry = (String) response.body().getExpiresAt();
							setupAccount(token);
						} else {
							Snackbar.info(
									SignInActivity.this,
									getString(R.string.generic_api_error, response.code()));
							enableSignInButton();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<PersonalAccessTokens> call, @NonNull Throwable t) {}
				});
	}

	private void disableSignInButton() {
		binding.signIn.setEnabled(false);
		binding.signIn.setAlpha(.5F);
		binding.restore.setEnabled(false);
		binding.restore.setAlpha(.5F);
	}

	private void enableSignInButton() {
		binding.signIn.setEnabled(true);
		binding.signIn.setAlpha(1F);
		binding.restore.setEnabled(true);
		binding.restore.setAlpha(1F);
	}
}
