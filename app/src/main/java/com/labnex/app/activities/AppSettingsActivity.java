package com.labnex.app.activities;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.biometric.BiometricManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.AppSettingsBottomSheet;
import com.labnex.app.databinding.ActivityAppSettingsBinding;
import com.labnex.app.helpers.AppSettingsInit;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.Utils;
import com.labnex.app.interfaces.BottomSheetListener;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * @author mmarif
 */
public class AppSettingsActivity extends BaseActivity implements BottomSheetListener {

	private static String[] themeList;
	private static int themeSelectedChoice;
	private static int langSelectedChoice;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		ActivityAppSettingsBinding binding =
				ActivityAppSettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.bottomAppBar.setNavigationOnClickListener(topBar -> finish());

		if (getAccount().getUserInfo() != null) {

			Glide.with(ctx)
					.load(getAccount().getUserInfo().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.ic_spinner)
					.centerCrop()
					.into(binding.userAvatar);

			binding.userAvatar.setOnClickListener(
					profile -> {
						Intent intent = new Intent(AppSettingsActivity.this, ProfileActivity.class);
						intent.putExtra("source", "app_settings");
						intent.putExtra("userId", getAccount().getUserInfo().getId());
						AppSettingsActivity.this.startActivity(intent);
					});

			binding.accountsUserFullName.setText(getAccount().getUserInfo().getFullName());

			binding.accountsUsername.setText(getAccount().getUserInfo().getUsername());
		}

		binding.appVersion.setText(Utils.getAppVersion(ctx));
		binding.gitlabVersion.setText(getAccount().getServerVersion().toString());

		binding.supportPatreonFrame.setOnClickListener(
				v11 ->
						Utils.openUrlInBrowser(
								this,
								AppSettingsActivity.this,
								getResources().getString(R.string.support_link_patreon)));
		binding.crowdinFrame.setOnClickListener(
				v13 ->
						Utils.openUrlInBrowser(
								this,
								AppSettingsActivity.this,
								getResources().getString(R.string.crowd_in_link)));
		binding.websiteFrame.setOnClickListener(
				v14 ->
						Utils.openUrlInBrowser(
								this,
								AppSettingsActivity.this,
								getResources().getString(R.string.app_website_link)));
		binding.sourceCodeFrame.setOnClickListener(
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

		// theme selection dialog
		themeList = getResources().getStringArray(R.array.themes);
		themeSelectedChoice =
				Integer.parseInt(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_THEME_KEY));
		binding.themeSelected.setText(themeList[themeSelectedChoice]);

		binding.themeSelectionFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(AppSettingsActivity.this)
									.setTitle(R.string.theme_selector_dialog_title)
									.setSingleChoiceItems(
											themeList,
											themeSelectedChoice,
											(dialogInterfaceTheme, i) -> {
												themeSelectedChoice = i;
												binding.themeSelected.setText(themeList[i]);
												AppSettingsInit.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppSettingsInit.APP_THEME_KEY);

												MainActivity.refActivity = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceTheme.dismiss();
												Snackbar.info(
														AppSettingsActivity.this,
														findViewById(R.id.bottom_app_bar),
														getString(R.string.settings_saved));
											});

					materialAlertDialogBuilder.create().show();
				});
		// theme selection dialog

		// language selection dialog
		LinkedHashMap<String, String> lang = new LinkedHashMap<>();
		lang.put("sys", getString(R.string.system));
		for (String langCode : getResources().getStringArray(R.array.languages)) {
			lang.put(langCode, getLanguageDisplayName(langCode));
		}

		String[] locale =
				AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_LOCALE_KEY).split("\\|");
		langSelectedChoice = Integer.parseInt(locale[0]);
		binding.languageSelected.setText(
				lang.get(lang.keySet().toArray(new String[0])[langSelectedChoice]));

		binding.languageSelectionFrame.setOnClickListener(
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

		// biometric switcher
		binding.switchBiometric.setChecked(
				Boolean.parseBoolean(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_BIOMETRIC_KEY)));

		binding.switchBiometric.setOnCheckedChangeListener(
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
									binding.switchBiometric.setChecked(false);
									Snackbar.info(
											AppSettingsActivity.this,
											findViewById(R.id.bottom_app_bar),
											getString(R.string.biometric_not_supported));
									break;
								case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
									AppSettingsInit.updateSettingsValue(
											ctx, "false", AppSettingsInit.APP_BIOMETRIC_KEY);
									binding.switchBiometric.setChecked(false);
									Snackbar.info(
											AppSettingsActivity.this,
											findViewById(R.id.bottom_app_bar),
											getString(R.string.biometric_not_available));
									break;
								case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
									AppSettingsInit.updateSettingsValue(
											ctx, "false", AppSettingsInit.APP_BIOMETRIC_KEY);
									binding.switchBiometric.setChecked(false);
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

		binding.biometricFrame.setOnClickListener(
				v -> binding.switchBiometric.setChecked(!binding.switchBiometric.isChecked()));
		// biometric switcher
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
}
