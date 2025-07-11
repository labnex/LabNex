package com.labnex.app.activities;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.biometric.BiometricManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.AppSettingsBottomSheet;
import com.labnex.app.bottomsheets.BackupBottomSheet;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.db.LabNexDatabase;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ActivityAppSettingsBinding;
import com.labnex.app.helpers.AppSettingsInit;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.Utils;
import com.labnex.app.interfaces.BottomSheetListener;
import io.mikael.urlbuilder.UrlBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * @author mmarif
 */
public class AppSettingsActivity extends BaseActivity
		implements BottomSheetListener, BackupBottomSheet.BackupCallback {

	private ActivityAppSettingsBinding binding;
	private static String[] themeList;
	private static int themeSelectedChoice;
	private static int langSelectedChoice;
	private static String[] homeScreenList;
	private static int homeScreenSelectedChoice;
	private ActivityResultLauncher<Intent> importFileLauncher;
	private ActivityResultLauncher<Intent> exportFileLauncher;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityAppSettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.bottomAppBar.setNavigationOnClickListener(topBar -> finish());

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

		exportFileLauncher =
				registerForActivityResult(
						new ActivityResultContracts.StartActivityForResult(),
						result -> {
							if (result.getResultCode() == Activity.RESULT_OK
									&& result.getData() != null) {
								Uri uri = result.getData().getData();
								if (uri != null) {
									exportDatabaseToUri(uri);
								} else {
									Snackbar.info(
											this,
											findViewById(R.id.bottom_app_bar),
											getString(R.string.backup_failed));
								}
							} else {
								Snackbar.info(
										this,
										findViewById(R.id.bottom_app_bar),
										getString(R.string.backup_failed));
							}
						});

		int accountId = SharedPrefDB.getInstance(ctx).getInt("currentActiveAccountId");
		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
		UserAccount account =
				userAccountsApi != null ? userAccountsApi.getAccountById(accountId) : null;

		if (account != null) {

			binding.accountsUserFullName.setText(account.getUserName());

			String accountName = account.getAccountName();
			if (accountName != null && accountName.contains("@")) {
				String username = accountName.split("@")[0];
				String instanceUrl = accountName.split("@")[1];

				UrlBuilder urlBuilder = UrlBuilder.fromString(instanceUrl);
				String hostName = urlBuilder.hostName;

				binding.accountsUsername.setText(
						getString(R.string.username_with_domain, username, hostName));
			} else {
				binding.accountsUsername.setText("");
			}

			if (getAccount().getUserInfo() != null) {
				Glide.with(ctx)
						.load(getAccount().getUserInfo().getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(binding.userAvatar);

				binding.userAvatar.setOnClickListener(
						profile -> {
							Intent intent =
									new Intent(AppSettingsActivity.this, ProfileActivity.class);
							intent.putExtra("source", "app_settings");
							intent.putExtra("userId", getAccount().getUserInfo().getId());
							AppSettingsActivity.this.startActivity(intent);
						});
			}
		}

		binding.sectionAbout.appVersion.setText(Utils.getAppVersion(ctx));
		binding.sectionAbout.gitlabVersion.setText(getAccount().getServerVersion().toString());

		binding.sectionLinks.supportPatreonFrame.setOnClickListener(
				v11 ->
						Utils.openUrlInBrowser(
								this,
								AppSettingsActivity.this,
								getResources().getString(R.string.support_link_patreon)));
		binding.sectionLinks.crowdinFrame.setOnClickListener(
				v13 ->
						Utils.openUrlInBrowser(
								this,
								AppSettingsActivity.this,
								getResources().getString(R.string.crowd_in_link)));
		binding.sectionLinks.websiteFrame.setOnClickListener(
				v14 ->
						Utils.openUrlInBrowser(
								this,
								AppSettingsActivity.this,
								getResources().getString(R.string.app_website_link)));
		binding.sectionLinks.sourceCodeFrame.setOnClickListener(
				v15 ->
						Utils.openUrlInBrowser(
								this,
								AppSettingsActivity.this,
								getResources().getString(R.string.source_code_link)));

		/*if (Utils.isPremium(ctx)) {
			binding.supportPatreonFrame.setVisibility(View.GONE);
			binding.dividerPatreon.setVisibility(View.GONE);
		}*/

		Bundle bsBundle = new Bundle();
		binding.accountsSheetLayout.setOnClickListener(
				accounts -> {
					bsBundle.putString("source", "accounts");
					AppSettingsBottomSheet bottomSheet = new AppSettingsBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "accountsBottomSheet");
				});

		if (getIntent().getBooleanExtra("openAccountsBottomSheet", false)) {
			bsBundle.putString("source", "accounts");
			AppSettingsBottomSheet bottomSheet = new AppSettingsBottomSheet();
			bottomSheet.setArguments(bsBundle);
			bottomSheet.show(getSupportFragmentManager(), "accountsBottomSheet");
		}

		// theme selection cards
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || "S".equals(Build.VERSION.CODENAME)) {
			themeList = getResources().getStringArray(R.array.themes);
		} else {
			binding.sectionAppearance.themeDynamic.setVisibility(View.GONE);
			themeList = getResources().getStringArray(R.array.themes_older_versions);
		}
		themeSelectedChoice =
				Integer.parseInt(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_THEME_KEY));

		updateThemeCardBorders(themeSelectedChoice);

		binding.sectionAppearance.themeDark.setOnClickListener(
				v -> selectTheme(getString(R.string.dark)));
		binding.sectionAppearance.themeLight.setOnClickListener(
				v -> selectTheme(getString(R.string.light)));
		binding.sectionAppearance.themeSystem.setOnClickListener(
				v -> selectTheme(getString(R.string.theme_system)));
		binding.sectionAppearance.themeDynamic.setOnClickListener(
				v -> selectTheme(getString(R.string.dynamic)));
		// theme selection cards

		// language selection dialog
		LinkedHashMap<String, String> lang = new LinkedHashMap<>();
		lang.put("sys", getString(R.string.system));
		for (String langCode : getResources().getStringArray(R.array.languages)) {
			lang.put(langCode, getLanguageDisplayName(langCode));
		}

		String[] locale =
				AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_LOCALE_KEY).split("\\|");
		langSelectedChoice = Integer.parseInt(locale[0]);
		binding.sectionAppearance.languageSelected.setText(
				lang.get(lang.keySet().toArray(new String[0])[langSelectedChoice]));

		binding.sectionAppearance.languageSelectionFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(AppSettingsActivity.this)
									.setTitle(R.string.settings_language_selector_dialog_title)
									.setCancelable(langSelectedChoice != -1)
									.setSingleChoiceItems(
											lang.values().toArray(new String[0]),
											langSelectedChoice,
											(dialogInterface, i) -> {
												String selectedLanguage =
														lang.keySet().toArray(new String[0])[i];
												AppSettingsInit.updateSettingsValue(
														ctx,
														i + "|" + selectedLanguage,
														AppSettingsInit.APP_LOCALE_KEY);

												String[] multiCodeLang =
														selectedLanguage.split("-");
												if (selectedLanguage.contains("-")) {
													selectedLanguage = multiCodeLang[0];
												}

												Utils.setLocale(this, selectedLanguage);

												MainActivity.refActivity = true;
												this.overridePendingTransition(0, 0);
												dialogInterface.dismiss();
												this.recreate();
											});

					materialAlertDialogBuilder.create().show();
				});
		// language selection dialog

		// home screen selection dialog
		homeScreenList = getResources().getStringArray(R.array.home_screen);
		homeScreenSelectedChoice =
				Integer.parseInt(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_HOME_SCREEN_KEY));
		binding.sectionAppearance.homeScreenSelected.setText(
				homeScreenList[homeScreenSelectedChoice]);

		binding.sectionAppearance.homeScreenSelectionFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(AppSettingsActivity.this)
									.setTitle(R.string.home_screen_dialog_title)
									.setSingleChoiceItems(
											homeScreenList,
											homeScreenSelectedChoice,
											(dialogInterfaceTheme, i) -> {
												homeScreenSelectedChoice = i;
												binding.sectionAppearance.homeScreenSelected
														.setText(homeScreenList[i]);
												AppSettingsInit.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppSettingsInit.APP_HOME_SCREEN_KEY);

												dialogInterfaceTheme.dismiss();
												Snackbar.info(
														AppSettingsActivity.this,
														findViewById(R.id.bottom_app_bar),
														getString(R.string.settings_saved));
											});

					materialAlertDialogBuilder.create().show();
				});
		// home screen selection dialog

		// biometric switcher
		binding.sectionSecurity.switchBiometric.setChecked(
				Boolean.parseBoolean(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_BIOMETRIC_KEY)));

		binding.sectionSecurity.switchBiometric.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {

						BiometricManager biometricManager = BiometricManager.from(ctx);
						KeyguardManager keyguardManager =
								(KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);

						if (!keyguardManager.isDeviceSecure()) {

							switch (biometricManager.canAuthenticate(
									BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
								case BiometricManager.BIOMETRIC_SUCCESS:
									AppSettingsInit.updateSettingsValue(
											ctx, "true", AppSettingsInit.APP_BIOMETRIC_KEY);
									Snackbar.info(
											AppSettingsActivity.this,
											findViewById(R.id.bottom_app_bar),
											getString(R.string.settings_saved));
									break;
								case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
								case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
								case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
								case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
									AppSettingsInit.updateSettingsValue(
											ctx, "false", AppSettingsInit.APP_BIOMETRIC_KEY);
									binding.sectionSecurity.switchBiometric.setChecked(false);
									Snackbar.info(
											AppSettingsActivity.this,
											findViewById(R.id.bottom_app_bar),
											getString(R.string.biometric_not_supported));
									break;
								case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
									AppSettingsInit.updateSettingsValue(
											ctx, "false", AppSettingsInit.APP_BIOMETRIC_KEY);
									binding.sectionSecurity.switchBiometric.setChecked(false);
									Snackbar.info(
											AppSettingsActivity.this,
											findViewById(R.id.bottom_app_bar),
											getString(R.string.biometric_not_available));
									break;
								case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
									AppSettingsInit.updateSettingsValue(
											ctx, "false", AppSettingsInit.APP_BIOMETRIC_KEY);
									binding.sectionSecurity.switchBiometric.setChecked(false);
									Snackbar.info(
											AppSettingsActivity.this,
											findViewById(R.id.bottom_app_bar),
											getString(R.string.enroll_biometric));
									break;
							}
						} else {

							AppSettingsInit.updateSettingsValue(
									ctx, "true", AppSettingsInit.APP_BIOMETRIC_KEY);
							Snackbar.info(
									AppSettingsActivity.this,
									findViewById(R.id.bottom_app_bar),
									getString(R.string.settings_saved));
						}
					} else {

						AppSettingsInit.updateSettingsValue(
								ctx, "false", AppSettingsInit.APP_BIOMETRIC_KEY);
						Snackbar.info(
								AppSettingsActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.settings_saved));
					}
				});

		binding.sectionSecurity.biometricFrameCard.setOnClickListener(
				v ->
						binding.sectionSecurity.switchBiometric.setChecked(
								!binding.sectionSecurity.switchBiometric.isChecked()));
		// biometric switcher

		// backup and restore
		binding.sectionBackup.backupFrameCard.setOnClickListener(
				v -> {
					BackupBottomSheet bottomSheet = BackupBottomSheet.newInstance(this);
					bottomSheet.show(getSupportFragmentManager(), "BackupBottomSheet");
				});
		// backup and restore
	}

	private void selectTheme(String themeName) {

		int themeIndex = Arrays.asList(themeList).indexOf(themeName);
		if (themeIndex == -1 || themeSelectedChoice == themeIndex) {
			return;
		}

		themeSelectedChoice = themeIndex;
		AppSettingsInit.updateSettingsValue(
				ctx, String.valueOf(themeIndex), AppSettingsInit.APP_THEME_KEY);

		updateThemeCardBorders(themeIndex);

		MainActivity.refActivity = true;
		this.recreate();
		this.overridePendingTransition(0, 0);
		Snackbar.info(
				AppSettingsActivity.this,
				findViewById(R.id.bottom_app_bar),
				getString(R.string.settings_saved));
	}

	private void updateThemeCardBorders(int selectedIndex) {

		MaterialCardView[] themeCards = {
			binding.sectionAppearance.themeDark,
			binding.sectionAppearance.themeLight,
			binding.sectionAppearance.themeSystem,
			binding.sectionAppearance.themeDynamic
		};
		String[] themeNames = {
			getString(R.string.dark),
			getString(R.string.light),
			getString(R.string.theme_system),
			getString(R.string.dynamic)
		};

		for (int i = 0; i < themeCards.length; i++) {
			if (i < themeList.length && themeNames[i].equals(themeList[selectedIndex])) {
				themeCards[i].setStrokeWidth(
						getResources().getDimensionPixelSize(R.dimen.dimen2dp));
			} else {
				themeCards[i].setStrokeWidth(0);
			}
		}
	}

	private static String getLanguageDisplayName(String langCode) {
		Locale english = new Locale("en");

		String[] multiCodeLang = langCode.split("-");
		String countryCode;
		if (langCode.contains("-")) {
			langCode = multiCodeLang[0];
			countryCode = multiCodeLang[1];
		} else {
			countryCode = "";
		}

		Locale translated = new Locale(langCode, countryCode);
		return String.format(
				"%s (%s)",
				translated.getDisplayName(translated), translated.getDisplayName(english));
	}

	@Override
	public void onButtonClicked(String text) {

		switch (text) {
			case "newAccount":
				// Utils.copyToClipboard(this, url, ctx.getString(R.string.copyIssueUrlToastMsg));
				break;
			case "share":
				// Utils.sharingIntent(this, url);
				break;
			case "open":
				// Utils.openUrlInBrowser(this, url);
				break;
		}
	}

	@Override
	public void onExport() {
		launchExportFilePicker();
	}

	@Override
	public void onImport() {
		launchImportFilePicker();
	}

	public void launchImportFilePicker() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("application/octet-stream");
		importFileLauncher.launch(intent);
	}

	public void launchExportFilePicker() {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("application/octet-stream");
		String timestamp = new SimpleDateFormat("yyyyMd-HHmmss", Locale.US).format(new Date());
		intent.putExtra(Intent.EXTRA_TITLE, "LabNex-" + timestamp + ".backup");
		exportFileLauncher.launch(intent);
	}

	public void exportDatabaseToUri(Uri uri) {
		Thread exportThread =
				new Thread(
						() -> {
							try {
								LabNexDatabase db = LabNexDatabase.getDatabaseInstance(this);
								db.close();

								boolean isWalEnabled = false;
								try (Cursor cursor =
										db.getOpenHelper()
												.getWritableDatabase()
												.query("PRAGMA journal_mode", new String[0])) {
									if (cursor.moveToFirst()) {
										isWalEnabled = "wal".equalsIgnoreCase(cursor.getString(0));
									}
								}
								if (isWalEnabled) {
									try (Cursor cursor =
											db.getOpenHelper()
													.getWritableDatabase()
													.query(
															"PRAGMA wal_checkpoint(FULL)",
															new String[0])) {
										cursor.moveToFirst();
									} catch (SQLiteException ignored) {
									}
								}

								File dbFile = getDatabasePath("labnex");
								if (!dbFile.exists()) {
									throw new IOException("Database file does not exist");
								}

								File tempDir = getCacheDir();
								File tempDbFile = new File(tempDir, "labnex_temp");
								try (FileInputStream fis = new FileInputStream(dbFile);
										FileOutputStream fos = new FileOutputStream(tempDbFile)) {
									FileChannel src = fis.getChannel();
									FileChannel dst = fos.getChannel();
									dst.transferFrom(src, 0, src.size());
								}

								try (InputStream inputStream = new FileInputStream(tempDbFile);
										OutputStream outputStream =
												getContentResolver().openOutputStream(uri)) {
									if (outputStream == null) {
										throw new IOException(
												"Failed to open output stream for URI: " + uri);
									}
									byte[] buffer = new byte[8192];
									int bytesRead;
									while ((bytesRead = inputStream.read(buffer)) != -1) {
										outputStream.write(buffer, 0, bytesRead);
									}
								} finally {
									if (tempDbFile.exists()) {
										boolean ignored = tempDbFile.delete();
									}
								}

								runOnUiThread(
										() -> {
											Snackbar.info(
													this,
													findViewById(R.id.bottom_app_bar),
													getString(R.string.backup_success));
											new Handler(Looper.getMainLooper())
													.postDelayed(this::restartApp, 1500);
										});
							} catch (IOException | SQLiteException e) {
								runOnUiThread(
										() ->
												Snackbar.info(
														this,
														findViewById(R.id.bottom_app_bar),
														getString(R.string.backup_failed)));
							}
						});
		exportThread.setDaemon(false);
		exportThread.start();
	}

	private void processImport(Uri uri) {
		try {
			File dbFile = getDatabasePath("labnex");

			LabNexDatabase db = LabNexDatabase.getDatabaseInstance(this);
			if (db.isOpen()) {
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
			db.getOpenHelper().getWritableDatabase();

			Snackbar.info(
					this, findViewById(R.id.bottom_app_bar), getString(R.string.import_success));
			new Handler(Looper.getMainLooper()).postDelayed(this::restartApp, 1500);
		} catch (IOException | SQLiteException e) {
			Snackbar.info(
					this, findViewById(R.id.bottom_app_bar), getString(R.string.import_failed));
		}
	}

	private void restartApp() {
		Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
			finish();
			startActivity(intent);
			Runtime.getRuntime().exit(0);
		}
	}
}
