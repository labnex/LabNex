package com.labnex.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ActivitySignInBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.Utils;
import com.labnex.app.helpers.Version;
import com.labnex.app.models.metadata.Metadata;
import com.labnex.app.models.personal_access_tokens.PersonalAccessTokens;
import com.labnex.app.models.user.User;
import java.net.URI;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivitySignInBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		Context context = getApplicationContext();
		Intent intent = getIntent();

		if (intent.hasExtra("source")) {
			if (Objects.requireNonNull(intent.getStringExtra("source"))
					.equalsIgnoreCase("add_account")) {
				binding.signIn.setText(R.string.add_new_account);
				binding.signInText.setText(R.string.add_new_account);
				intent.removeExtra("source");
			}
		}

		binding.personalTokenHelper.setOnClickListener(
				personalTokenLink ->
						Utils.openUrlInBrowser(
								this,
								SignInActivity.this,
								getResources()
										.getString(R.string.personal_access_token_helper_link)));

		binding.signIn.setOnClickListener(
				checkUser -> {
					disableSignInButton();
					checkUserInput();
				});
	}

	private void checkUserInput() {

		try {

			String instanceUrl_ =
					Objects.requireNonNull(binding.instanceUrl.getText())
							.toString()
							.replaceAll("[\\uFEFF]", "")
							.trim();

			String loginToken =
					Objects.requireNonNull(binding.personalToken.getText())
							.toString()
							.replaceAll("[\\uFEFF|#]", "")
							.trim();

			instanceUrl = URI.create(("https://" + instanceUrl_ + "/api/v4/"));

			if (binding.instanceUrl.getText().toString().isEmpty()) {

				Snackbar.info(SignInActivity.this, getString(R.string.gitlab_url_empty_error));
				enableSignInButton();
				return;
			} else if (binding.instanceUrl.getText().toString().contains("http")) {
				Snackbar.info(SignInActivity.this, getString(R.string.gitlab_url_error));
				enableSignInButton();
				return;
			} else if (!binding.instanceUrl.getText().toString().contains(".")) {
				Snackbar.info(SignInActivity.this, getString(R.string.gitlab_url_error));
				enableSignInButton();
				return;
			}
			if (loginToken.isEmpty()) {
				Snackbar.info(SignInActivity.this, getString(R.string.gitlab_token_empty_error));
				enableSignInButton();
				return;
			}

			versionCheck(loginToken);
		} catch (Exception e) {
			Snackbar.info(SignInActivity.this, getString(R.string.generic_error));
			enableSignInButton();
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
	}

	private void enableSignInButton() {
		binding.signIn.setEnabled(true);
		binding.signIn.setAlpha(1F);
	}
}
