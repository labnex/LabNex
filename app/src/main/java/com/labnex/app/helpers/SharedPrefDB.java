package com.labnex.app.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author mmarif
 */
public class SharedPrefDB {

	private static volatile SharedPrefDB sharedPrefDB;

	private final SharedPreferences preferences;

	private SharedPrefDB(Context appContext) {
		preferences =
				appContext.getSharedPreferences(
						appContext.getPackageName() + "_preferences", Context.MODE_PRIVATE);
	}

	public static synchronized SharedPrefDB getInstance(Context context) {

		if (sharedPrefDB == null) {
			synchronized (SharedPrefDB.class) {
				if (sharedPrefDB == null) {
					sharedPrefDB = new SharedPrefDB(context);
				}
			}
		}

		return sharedPrefDB;
	}

	/**
	 * Check if external storage is writable or not
	 *
	 * @return true if writable, false otherwise
	 */
	public static boolean isExternalStorageWritable() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

	/**
	 * Check if external storage is readable or not
	 *
	 * @return true if readable, false otherwise
	 */
	public static boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();

		return Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
	}

	// Getters

	/**
	 * Saves 'theBitmap' into 'fullPath'
	 *
	 * @param fullPath full path of the image file e.g. "Images/MeAtLunch.png"
	 * @param theBitmap the image you want to save as a Bitmap
	 * @return true if image was saved, false otherwise
	 */
	public boolean putImageWithFullPath(String fullPath, Bitmap theBitmap) {
		return !(fullPath == null || theBitmap == null) && saveBitmap(fullPath, theBitmap);
	}

	/**
	 * Saves the Bitmap as a PNG file at path 'fullPath'
	 *
	 * @param fullPath path of the image file
	 * @param bitmap the image as a Bitmap
	 * @return true if it successfully saved, false otherwise
	 */
	private boolean saveBitmap(String fullPath, Bitmap bitmap) {
		if (fullPath == null || bitmap == null) {
			return false;
		}

		boolean fileCreated = false;
		boolean bitmapCompressed = false;
		boolean streamClosed = false;

		File imageFile = new File(fullPath);

		if (imageFile.exists()) {
			if (!imageFile.delete()) {
				return false;
			}
		}

		try {
			fileCreated = imageFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(imageFile);
			bitmapCompressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
					streamClosed = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return (fileCreated && bitmapCompressed && streamClosed);
	}

	/**
	 * Get int value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
	 *
	 * @param key SharedPreferences key
	 * @return int value at 'key' or 'defaultValue' if key not found
	 */
	public int getInt(String key) {
		return preferences.getInt(key, 0);
	}

	public int getInt(String key, int defaultValue) {
		return preferences.getInt(key, defaultValue);
	}

	/**
	 * Get long value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
	 *
	 * @param key SharedPreferences key
	 * @param defaultValue long value returned if key was not found
	 * @return long value at 'key' or 'defaultValue' if key not found
	 */
	public long getLong(String key, long defaultValue) {
		return preferences.getLong(key, defaultValue);
	}

	/**
	 * Get float value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
	 *
	 * @param key SharedPreferences key
	 * @return float value at 'key' or 'defaultValue' if key not found
	 */
	public float getFloat(String key) {
		return preferences.getFloat(key, 0);
	}

	public float getFloat(String key, float defaultValue) {
		return preferences.getFloat(key, defaultValue);
	}

	/**
	 * Get String value from SharedPreferences at 'key'. If key not found, return ""
	 *
	 * @param key SharedPreferences key
	 * @return String value at 'key' or "" (empty String) if key not found
	 */
	public String getString(String key) {
		return preferences.getString(key, "");
	}

	public String getString(String key, String defaultValue) {
		return preferences.getString(key, defaultValue);
	}

	// Put methods

	/**
	 * Get boolean value from SharedPreferences at 'key'. If key not found, return 'defaultValue'
	 *
	 * @param key SharedPreferences key
	 * @return boolean value at 'key' or 'defaultValue' if key not found
	 */
	public boolean getBoolean(String key) {
		return preferences.getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return preferences.getBoolean(key, defaultValue);
	}

	/**
	 * Put int value into SharedPreferences with 'key' and save
	 *
	 * @param key SharedPreferences key
	 * @param value int value to be added
	 */
	public void putInt(String key, int value) {
		checkForNullKey(key);
		preferences.edit().putInt(key, value).apply();
	}

	/**
	 * Put long value into SharedPreferences with 'key' and save
	 *
	 * @param key SharedPreferences key
	 * @param value long value to be added
	 */
	public void putLong(String key, long value) {
		checkForNullKey(key);
		preferences.edit().putLong(key, value).apply();
	}

	/**
	 * Put float value into SharedPreferences with 'key' and save
	 *
	 * @param key SharedPreferences key
	 * @param value float value to be added
	 */
	public void putFloat(String key, float value) {
		checkForNullKey(key);
		preferences.edit().putFloat(key, value).apply();
	}

	/**
	 * Put String value into SharedPreferences with 'key' and save
	 *
	 * @param key SharedPreferences key
	 * @param value String value to be added
	 */
	public void putString(String key, String value) {
		checkForNullKey(key);
		checkForNullValue(value);
		preferences.edit().putString(key, value).apply();
	}

	/**
	 * Put boolean value into SharedPreferences with 'key' and save
	 *
	 * @param key SharedPreferences key
	 * @param value boolean value to be added
	 */
	public void putBoolean(String key, boolean value) {
		checkForNullKey(key);
		preferences.edit().putBoolean(key, value).apply();
	}

	/**
	 * Remove SharedPreferences item with 'key'
	 *
	 * @param key SharedPreferences key
	 */
	public void remove(String key) {
		preferences.edit().remove(key).apply();
	}

	/**
	 * Delete image file at 'path'
	 *
	 * @param path path of image file
	 * @return true if it successfully deleted, false otherwise
	 */
	public boolean deleteImage(String path) {
		return new File(path).delete();
	}

	/** Clear SharedPreferences (remove everything) */
	public void clear() {
		preferences.edit().clear().apply();
	}

	/**
	 * Retrieve all values from SharedPreferences. Do not modify collection return by method
	 *
	 * @return a Map representing a list of key/value pairs from SharedPreferences
	 */
	public Map<String, ?> getAll() {
		return preferences.getAll();
	}

	/**
	 * Register SharedPreferences change listener
	 *
	 * @param listener listener object of OnSharedPreferenceChangeListener
	 */
	public void registerOnSharedPreferenceChangeListener(
			SharedPreferences.OnSharedPreferenceChangeListener listener) {

		preferences.registerOnSharedPreferenceChangeListener(listener);
	}

	/**
	 * Unregister SharedPreferences change listener
	 *
	 * @param listener listener object of OnSharedPreferenceChangeListener to be unregistered
	 */
	public void unregisterOnSharedPreferenceChangeListener(
			SharedPreferences.OnSharedPreferenceChangeListener listener) {

		preferences.unregisterOnSharedPreferenceChangeListener(listener);
	}

	/**
	 * null keys would corrupt the shared pref file and make them unreadable this is a preventive
	 * measure
	 *
	 * @param key the pref key
	 */
	public void checkForNullKey(String key) {
		if (key == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * null keys would corrupt the shared pref file and make them unreadable this is a preventive
	 * measure
	 *
	 * @param value the pref key
	 */
	public void checkForNullValue(String value) {
		if (value == null) {
			throw new NullPointerException();
		}
	}

	public boolean checkForExistingPref(String key) {
		return preferences.contains(key);
	}
}
