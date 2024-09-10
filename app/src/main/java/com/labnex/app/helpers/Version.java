package com.labnex.app.helpers;

import android.util.Log;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 6543
 */
public class Version {

	// the raw String
	private String raw;
	// the version numbers in its order (dot separated)
	private List<Integer> values;
	private boolean dev;

	public Version(String value) {

		raw = value;
		this.init();
	}

	/**
	 * valid return true if string is a valid version
	 *
	 * @param value String
	 * @return true/false
	 */
	public static boolean valid(String value) {

		if (value == null) {
			return false;
		}
		final Pattern patternValid =
				Pattern.compile("^[vV]?(\\d+)+(\\.(\\d+))*([_\\-+][\\w\\-+.]*)?$");
		return value.equals("main") || patternValid.matcher(value).find();
	}

	/**
	 * init parse and store values for other functions of an Version instance it use the raw
	 * variable as base
	 */
	private void init() {

		final Pattern patternNumberDotNumber = Pattern.compile("^\\d+(\\.(\\d)+)*");

		if (raw.isEmpty()) {
			raw = "0";
		}

		if (!valid(raw)) {
			// throw new IllegalArgumentException("Invalid version format: " + raw);
			Log.e("Version", "Invalid version format: " + raw);
		}
		if (raw.equals("main")) {
			dev = true;
			values = new ArrayList<>();
			return;
		}

		if (raw.charAt(0) == 'v' || raw.charAt(0) == 'V') {
			raw = raw.substring(1);
		}

		values = new ArrayList<>();
		Matcher match = patternNumberDotNumber.matcher(raw);
		if (!match.find()) {
			dev = true;
			values = new ArrayList<>();
			return;
		}
		for (String i : match.group().split("\\.")) {
			values.add(Integer.parseInt(i));
		}
	}

	/**
	 * equal return true if version is the same
	 *
	 * @param value String
	 * @return true/false
	 */
	public boolean equal(String value) {

		return this.equal(new Version(value));
	}

	/**
	 * equal return true if version is the same
	 *
	 * @param v Version
	 * @return true/false
	 */
	public boolean equal(@NonNull Version v) {

		if (dev || v.dev) { // equal if raw is equal
			return Objects.equals(raw, v.raw);
		}

		int rounds = Math.min(this.values.size(), v.values.size());
		for (int i = 0; i < rounds; i++) {
			if (!Objects.equals(this.values.get(i), v.values.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * less return true if version is less
	 *
	 * @param value String
	 * @return true/false
	 */
	public boolean less(String value) {

		return this.less(new Version(value));
	}

	/**
	 * less return true if version is less
	 *
	 * @param v Version
	 * @return true/false
	 */
	public boolean less(@NonNull Version v) {

		return v.higher(this);
	}

	/**
	 * higher return true if version is higher
	 *
	 * @param value String
	 * @return true/false
	 */
	public boolean higher(String value) {

		return this.higher(new Version(value));
	}

	/**
	 * higher return true if version is higher
	 *
	 * @param v Version
	 * @return true/false
	 */
	public boolean higher(@NonNull Version v) {

		if (dev) {
			return !v.dev;
		} else if (v.dev) {
			return false;
		}

		int rounds = Math.min(this.values.size(), v.values.size());
		for (int i = 0; i < rounds; i++) {
			if (i + 1 == rounds) {
				if (this.values.get(i) <= v.values.get(i)) {
					return false;
				}
			} else {
				if (this.values.get(i) < v.values.get(i)) {
					return false;
				} else if (this.values.get(i) > v.values.get(i)) {
					return true;
				}
			}
		}
		return true;
	}

	/**
	 * lessOrEqual return true if version is less or equal
	 *
	 * @param value String
	 * @return true/false
	 */
	public boolean lessOrEqual(String value) {

		return this.lessOrEqual(new Version(value));
	}

	/**
	 * lessOrEqual return true if version is less or equal
	 *
	 * @param v Version
	 * @return true/false
	 */
	public boolean lessOrEqual(@NonNull Version v) {

		return v.higherOrEqual(this);
	}

	/**
	 * higherOrEqual return true if version is higher or equal
	 *
	 * @param value String
	 * @return true/false
	 */
	public boolean higherOrEqual(String value) {

		return this.higherOrEqual(new Version(value));
	}

	/**
	 * higherOrEqual return true if version is higher or equal
	 *
	 * @param v Version
	 * @return true/false
	 */
	public boolean higherOrEqual(@NonNull Version v) {

		if (dev || v.dev) { // if one is a dev version, only true if both are dev
			return v.dev && dev;
		}

		int rounds = Math.min(this.values.size(), v.values.size());
		for (int i = 0; i < rounds; i++) {
			if (this.values.get(i) > v.values.get(i)) {
				return true;
			} else if (this.values.get(i) < v.values.get(i)) {
				return false;
			}
		}
		return true;
	}

	@NonNull @Override
	public String toString() {

		return raw;
	}
}
