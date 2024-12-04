package com.labnex.app.database.api;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.labnex.app.database.dao.AppSettingsDao;
import com.labnex.app.database.models.AppSettings;
import java.util.List;

/**
 * @author mmarif
 */
public class AppSettingsApi extends BaseApi {

	private final AppSettingsDao appSettingsDao;

	AppSettingsApi(Context context) {
		super(context);
		appSettingsDao = labnexDatabase.appSettingsDao();
	}

	public long insertNewSetting(String settingKey, String settingValue, String settingDefault) {

		AppSettings appSettings = new AppSettings();
		appSettings.setSettingKey(settingKey);
		appSettings.setSettingValue(settingValue);
		appSettings.setSettingDefault(settingDefault);

		return insertSettingAsyncTask(appSettings);
	}

	public long insertSettingAsyncTask(AppSettings appSettings) {
		return appSettingsDao.insertNewSetting(appSettings);
	}

	public LiveData<List<AppSettings>> fetchAllSettings() {
		return appSettingsDao.fetchAllSettings();
	}

	public AppSettings fetchSettingById(int settingId) {
		return appSettingsDao.fetchSettingById(settingId);
	}

	public AppSettings fetchSettingByKey(String settingKey) {
		return appSettingsDao.fetchSettingByKey(settingKey);
	}

	public Integer fetchTotalSettingsCount() {
		return appSettingsDao.fetchTotalSettingsCount();
	}

	public Integer fetchSettingCountByKey(String settingKey) {
		return appSettingsDao.fetchSettingCountByKey(settingKey);
	}

	public void updateSettingValueByKey(String settingValue, String settingKey) {
		executorService.execute(
				() -> appSettingsDao.updateSettingValueByKey(settingValue, settingKey));
	}

	public void updateSettingDefaultByKey(String settingDefault, String settingKey) {
		executorService.execute(
				() -> appSettingsDao.updateSettingDefaultByKey(settingDefault, settingKey));
	}

	public void deleteBySettingKey(final String settingKey) {
		final AppSettings appSettings = appSettingsDao.fetchSettingByKey(settingKey);

		if (appSettings != null) {
			executorService.execute(() -> appSettingsDao.deleteBySettingKey(settingKey));
		}
	}

	public void deleteAllSettings() {
		executorService.execute(appSettingsDao::deleteAllAppSettings);
	}
}
