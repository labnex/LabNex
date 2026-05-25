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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.biometric.BiometricManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.AppAccountsBottomSheet;
import com.labnex.app.bottomsheets.BackupBottomSheet;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.db.LabNexDatabase;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ActivityAppSettingsBinding;
import com.labnex.app.helpers.AppSettingsInit;
import com.labnex.app.helpers.AppUIStateManager;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
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
	private String[] themeList;
	private int themeSelectedChoice;
	private int langSelectedChoice;
	private int homeScreenSelectedChoice;
	private ActivityResultLauncher<Intent> importFileLauncher;
	private ActivityResultLauncher<Intent> exportFileLauncher;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityAppSettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, binding.nestedScrollView, null, null);

		initCoreUI();
		setupAccountHero();
		setupAppearanceSection();
		setupSecuritySection();
		setupDynamicSections();
		initLaunchers();
	}

	private void initCoreUI() {
		binding.btnBack.setOnClickListener(v -> finish());

		String appVersion = Utils.getAppVersion(ctx);
		String serverVersion =
				getAccount().getServerVersion() != null
						? getAccount().getServerVersion().toString()
						: "0.0.0";
		binding.version.setText(getString(R.string.version_display, appVersion, serverVersion));
	}

	private void setupAccountHero() {
		int accountId = SharedPrefDB.getInstance(ctx).getInt("currentActiveAccountId");
		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
		UserAccount account =
				userAccountsApi != null ? userAccountsApi.getAccountById(accountId) : null;

		if (account != null) {
			binding.accountsUserFullName.setText(account.getUserName());
			handleAccountSubtext(account);

			if (getAccount().getUserInfo() != null) {
				Glide.with(ctx)
						.load(getAccount().getUserInfo().getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(binding.userAvatar);

				binding.userAvatar.setOnClickListener(
						v -> {
							Intent intent = new Intent(this, ProfileActivity.class);
							intent.putExtra("source", "app_settings");
							intent.putExtra(
									"userId", String.valueOf(getAccount().getUserInfo().getId()));
							startActivity(intent);
						});
			}
		}

		binding.accountsSheetLayout.setOnClickListener(v -> openAccountsBottomSheet());
		if (getIntent().getBooleanExtra("openAccountsBottomSheet", false)) {
			openAccountsBottomSheet();
		}
	}

	private void setupAppearanceSection() {
		boolean isSPlus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
		themeList =
				getResources()
						.getStringArray(isSPlus ? R.array.themes : R.array.themes_older_versions);
		themeSelectedChoice =
				Integer.parseInt(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_THEME_KEY));

		binding.sectionAll.themeDark.themeText.setText(R.string.dark);
		binding.sectionAll.themeDark.themeIcon.setImageResource(R.drawable.ic_theme_dark);

		binding.sectionAll.themeLight.themeText.setText(R.string.light);
		binding.sectionAll.themeLight.themeIcon.setImageResource(R.drawable.ic_theme_light);

		binding.sectionAll.themeSystem.themeText.setText(R.string.theme_system);
		binding.sectionAll.themeSystem.themeIcon.setImageResource(R.drawable.ic_phone);

		if (isSPlus) {
			binding.sectionAll.themeDynamic.themeText.setText(R.string.dynamic);
			binding.sectionAll.themeDynamic.themeIcon.setImageResource(R.drawable.ic_themes);
			binding.sectionAll
					.themeDynamic
					.getRoot()
					.setOnClickListener(v -> selectTheme(getString(R.string.dynamic)));
		} else {
			binding.sectionAll.themeDynamic.getRoot().setVisibility(View.GONE);
		}

		updateThemeCardBorders(themeSelectedChoice);

		binding.sectionAll
				.themeDark
				.getRoot()
				.setOnClickListener(v -> selectTheme(getString(R.string.dark)));
		binding.sectionAll
				.themeLight
				.getRoot()
				.setOnClickListener(v -> selectTheme(getString(R.string.light)));
		binding.sectionAll
				.themeSystem
				.getRoot()
				.setOnClickListener(v -> selectTheme(getString(R.string.theme_system)));

		initLanguageSelector();
		initHomeScreenSelector();
		initLabelsSettings();
	}

	private void initLabelsSettings() {
		binding.sectionAll.switchShowLabels.setChecked(
				Boolean.parseBoolean(
						AppSettingsInit.getSettingsValue(
								ctx, AppSettingsInit.APP_SHOW_LABELS_IN_LISTS_KEY)));

		binding.sectionAll.switchShowLabels.setOnCheckedChangeListener(
				(bv, isChecked) -> {
					AppSettingsInit.updateSettingsValue(
							ctx,
							String.valueOf(isChecked),
							AppSettingsInit.APP_SHOW_LABELS_IN_LISTS_KEY);
					Toasty.show(this, getString(R.string.settings_saved));
				});

		binding.sectionAll.switchLabelsColors.setChecked(
				Boolean.parseBoolean(
						AppSettingsInit.getSettingsValue(
								ctx, AppSettingsInit.APP_SHOW_LABELS_COLORS_KEY)));

		binding.sectionAll.switchLabelsColors.setOnCheckedChangeListener(
				(bv, isChecked) -> {
					AppSettingsInit.updateSettingsValue(
							ctx,
							String.valueOf(isChecked),
							AppSettingsInit.APP_SHOW_LABELS_COLORS_KEY);
					Toasty.show(this, getString(R.string.settings_saved));
				});
	}

	private void setupSecuritySection() {
		binding.sectionAll.switchBiometric.setChecked(
				Boolean.parseBoolean(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_BIOMETRIC_KEY)));

		binding.sectionAll.switchBiometric.setOnCheckedChangeListener(
				(bv, isChecked) -> handleBiometricToggle(isChecked));

		binding.sectionAll.backupFrameSelection.setOnClickListener(
				v -> {
					BackupBottomSheet bottomSheet = BackupBottomSheet.newInstance(this);
					bottomSheet.show(getSupportFragmentManager(), "BackupBottomSheet");
				});
	}

	private void setupDynamicSections() {
		LinearLayout linksContainer =
				binding.sectionLinks.getRoot().findViewById(R.id.links_container);
		LinearLayout appsContainer =
				binding.sectionApps.getRoot().findViewById(R.id.apps_container);
		linksContainer.removeAllViews();
		appsContainer.removeAllViews();

		addDynamicRow(
				linksContainer,
				getString(R.string.support_text_patreon),
				R.drawable.ic_patreon,
				getString(R.string.support_link_patreon));
		addDynamicRow(
				linksContainer,
				getString(R.string.source_code),
				R.drawable.ic_code,
				getString(R.string.source_code_link));
		addDynamicRow(
				linksContainer,
				getString(R.string.website),
				R.drawable.ic_browser,
				getString(R.string.app_website_link));
		addDynamicRow(
				linksContainer,
				getString(R.string.crowd_in_text),
				R.drawable.ic_language,
				getString(R.string.crowd_in_link));

		addDynamicRow(
				appsContainer,
				getString(R.string.gitnex),
				R.drawable.ic_app_gitnex,
				getString(R.string.gitnex_link));
		addDynamicRow(
				appsContainer,
				getString(R.string.oceannex),
				R.drawable.ic_app_oceannex,
				getString(R.string.oceannex_link));
		addDynamicRow(
				appsContainer,
				getString(R.string.nexnode),
				R.drawable.ic_app_nexnode,
				getString(R.string.nexnode_link));
	}

	private void addDynamicRow(LinearLayout container, String title, int iconRes, String url) {
		View row = getLayoutInflater().inflate(R.layout.item_settings_others, container, false);
		((TextView) row.findViewById(R.id.item_title)).setText(title);
		((ImageView) row.findViewById(R.id.item_icon)).setImageResource(iconRes);

		row.setOnClickListener(v -> Utils.openUrlInBrowser(this, url));
		container.addView(row);
	}

	private void handleAccountSubtext(UserAccount account) {
		String accountName = account.getAccountName();
		if (accountName != null && accountName.contains("@")) {
			String[] parts = accountName.split("@");
			String host = UrlBuilder.fromString(parts[1]).hostName;
			binding.accountsUsername.setText(
					getString(R.string.username_with_domain, parts[0], host));
		} else {
			binding.accountsUsername.setText("");
		}
	}

	private void selectTheme(String themeName) {
		int themeIndex = Arrays.asList(themeList).indexOf(themeName);
		if (themeIndex == -1 || themeSelectedChoice == themeIndex) return;

		themeSelectedChoice = themeIndex;
		AppSettingsInit.updateSettingsValue(
				ctx, String.valueOf(themeIndex), AppSettingsInit.APP_THEME_KEY);
		recreateWithAnimation();
	}

	private void recreateWithAnimation() {
		AppUIStateManager.invalidateUI();
		overridePendingTransition(0, 0);
		recreate();
	}

	private void updateThemeCardBorders(int selectedIndex) {
		MaterialCardView[] themeCards = {
			binding.sectionAll.themeDark.themeIconCard, binding.sectionAll.themeLight.themeIconCard,
			binding.sectionAll.themeSystem.themeIconCard,
					binding.sectionAll.themeDynamic.themeIconCard
		};
		String[] internalNames = {
			getString(R.string.dark),
			getString(R.string.light),
			getString(R.string.theme_system),
			getString(R.string.dynamic)
		};

		for (int i = 0; i < themeCards.length; i++) {
			boolean isSelected =
					i < themeList.length && internalNames[i].equals(themeList[selectedIndex]);
			themeCards[i].setStrokeWidth(
					isSelected ? getResources().getDimensionPixelSize(R.dimen.dimen2dp) : 0);
		}
	}

	private void initLanguageSelector() {
		LinkedHashMap<String, String> langMap = new LinkedHashMap<>();
		langMap.put("sys", getString(R.string.system));
		for (String code : getResources().getStringArray(R.array.languages)) {
			langMap.put(code, getLanguageDisplayName(code));
		}

		String[] localePref =
				AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_LOCALE_KEY).split("\\|");
		langSelectedChoice = Integer.parseInt(localePref[0]);
		binding.sectionAll.languageSelected.setText(
				langMap.get(langMap.keySet().toArray(new String[0])[langSelectedChoice]));

		binding.sectionAll.languageSelectionFrame.setOnClickListener(
				v -> {
					new MaterialAlertDialogBuilder(this)
							.setTitle(R.string.settings_language_selector_dialog_title)
							.setSingleChoiceItems(
									langMap.values().toArray(new String[0]),
									langSelectedChoice,
									(dialog, i) -> {
										String selectedLanguage =
												langMap.keySet().toArray(new String[0])[i];
										AppSettingsInit.updateSettingsValue(
												ctx,
												i + "|" + selectedLanguage,
												AppSettingsInit.APP_LOCALE_KEY);
										Utils.setLocale(this, selectedLanguage.split("-")[0]);
										dialog.dismiss();
										recreateWithAnimation();
									})
							.show();
				});
	}

	private void initHomeScreenSelector() {
		String[] homeScreenList = getResources().getStringArray(R.array.home_screen);
		homeScreenSelectedChoice =
				Integer.parseInt(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_HOME_SCREEN_KEY));
		binding.sectionAll.homeScreenSelected.setText(homeScreenList[homeScreenSelectedChoice]);

		binding.sectionAll.homeScreenSelectionFrame.setOnClickListener(
				v -> {
					new MaterialAlertDialogBuilder(this)
							.setTitle(R.string.home_screen_dialog_title)
							.setSingleChoiceItems(
									homeScreenList,
									homeScreenSelectedChoice,
									(dialog, i) -> {
										homeScreenSelectedChoice = i;
										binding.sectionAll.homeScreenSelected.setText(
												homeScreenList[i]);
										AppSettingsInit.updateSettingsValue(
												ctx,
												String.valueOf(i),
												AppSettingsInit.APP_HOME_SCREEN_KEY);
										dialog.dismiss();
										Toasty.show(this, getString(R.string.settings_saved));
									})
							.show();
				});
	}

	private void handleBiometricToggle(boolean isChecked) {
		if (isChecked) {
			BiometricManager bm = BiometricManager.from(ctx);
			KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

			if (!km.isDeviceSecure()) {
				int canAuth = bm.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
				if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
					saveBiometric(true);
				} else {
					binding.sectionAll.switchBiometric.setChecked(false);
					saveBiometric(false);
					Toasty.show(this, getString(R.string.biometric_not_supported));
				}
			} else {
				saveBiometric(true);
			}
		} else {
			saveBiometric(false);
		}
	}

	private void saveBiometric(boolean val) {
		AppSettingsInit.updateSettingsValue(
				ctx, String.valueOf(val), AppSettingsInit.APP_BIOMETRIC_KEY);
		Toasty.show(this, getString(R.string.settings_saved));
	}

	private void openAccountsBottomSheet() {
		Bundle b = new Bundle();
		b.putString("source", "accounts");
		AppAccountsBottomSheet bs = new AppAccountsBottomSheet();
		bs.setArguments(b);
		bs.show(getSupportFragmentManager(), "accountsBottomSheet");
	}

	private void initLaunchers() {
		importFileLauncher =
				registerForActivityResult(
						new ActivityResultContracts.StartActivityForResult(),
						result -> {
							if (result.getResultCode() == Activity.RESULT_OK
									&& result.getData() != null) {
								Uri uri = result.getData().getData();
								if (uri != null) processImport(uri);
							}
						});

		exportFileLauncher =
				registerForActivityResult(
						new ActivityResultContracts.StartActivityForResult(),
						result -> {
							if (result.getResultCode() == Activity.RESULT_OK
									&& result.getData() != null) {
								Uri uri = result.getData().getData();
								if (uri != null) exportDatabaseToUri(uri);
							}
						});
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
											Toasty.show(this, getString(R.string.backup_success));
											new Handler(Looper.getMainLooper())
													.postDelayed(this::restartApp, 1500);
										});
							} catch (IOException | SQLiteException e) {
								runOnUiThread(
										() -> Toasty.show(this, getString(R.string.backup_failed)));
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

			Toasty.show(this, getString(R.string.import_success));
			new Handler(Looper.getMainLooper()).postDelayed(this::restartApp, 1500);
		} catch (IOException | SQLiteException e) {
			Toasty.show(this, getString(R.string.import_failed));
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
