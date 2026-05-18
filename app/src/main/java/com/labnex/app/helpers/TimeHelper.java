package com.labnex.app.helpers;

import android.content.res.Resources;
import com.labnex.app.R;
import com.labnex.app.core.CoreApplication;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author mmarif
 */
public class TimeHelper {

	public static String formatTime(Date date) {
		if (date == null) return "";

		Resources res = CoreApplication.getInstance().getResources();
		long time = date.getTime();
		long now = System.currentTimeMillis();
		long diff = now - time;
		boolean isFuture = diff < 0;
		diff = Math.abs(diff);

		long minute = 60000;
		long hour = 3600000;
		long day = 86400000;
		long month = 2592000000L;
		long year = 31536000000L;

		if (diff < minute) {
			return res.getString(isFuture ? R.string.time_in_moments : R.string.time_moments_ago);
		}

		if (diff < hour) {
			long count = diff / minute;
			int resId =
					isFuture
							? (count == 1 ? R.string.time_in_minute : R.string.time_in_minutes)
							: (count == 1 ? R.string.time_minute_ago : R.string.time_minutes_ago);
			return res.getString(resId, count);
		}

		if (diff < day) {
			long count = diff / hour;
			int resId =
					isFuture
							? (count == 1 ? R.string.time_in_hour : R.string.time_in_hours)
							: (count == 1 ? R.string.time_hour_ago : R.string.time_hours_ago);
			return res.getString(resId, count);
		}

		if (diff < month) {
			long count = diff / day;
			int resId =
					isFuture
							? (count == 1 ? R.string.time_in_day : R.string.time_in_days)
							: (count == 1 ? R.string.time_day_ago : R.string.time_days_ago);
			return res.getString(resId, count);
		}

		if (diff < year) {
			long count = diff / month;
			int resId;
			if (isFuture) {
				resId = (count == 1) ? R.string.time_in_month : R.string.time_in_months;
			} else {
				resId = (count == 1) ? R.string.time_month_ago : R.string.time_months_ago;
			}
			return res.getString(resId, count);
		}

		long count = diff / year;
		int resId;
		if (isFuture) {
			resId = (count == 1) ? R.string.time_in_year : R.string.time_in_years;
		} else {
			resId = (count == 1) ? R.string.time_year_ago : R.string.time_years_ago;
		}
		return res.getString(resId, count);
	}

	public static String formatDate(String isoDate) {
		if (isoDate == null) return "";
		Date date = parseIso8601(isoDate + "T00:00:00Z");
		return getAbsoluteDate(date, Locale.getDefault());
	}

	public static boolean timeBetweenHours(int fromHour, int toHour, int fromMinute, int toMinute) {

		Calendar cal = Calendar.getInstance();

		Calendar from = Calendar.getInstance();
		from.set(Calendar.HOUR_OF_DAY, fromHour);
		from.set(Calendar.MINUTE, fromMinute);

		Calendar to = Calendar.getInstance();
		to.set(Calendar.HOUR_OF_DAY, toHour);
		to.set(Calendar.MINUTE, toMinute);

		if (to.before(from)) {
			if (cal.after(to)) {
				to.add(Calendar.DATE, 1);
			} else {
				from.add(Calendar.DATE, -1);
			}
		}

		return cal.after(from) && cal.before(to);
	}

	public static String getAbsoluteDate(Date date, Locale locale) {
		if (date == null) return "";
		LocalDateTime dateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		DateTimeFormatter formatter =
				DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);

		return dateTime.format(formatter);
	}

	public static String getFullDateTime(Date date, Locale locale) {
		if (date == null) return "";
		DateTimeFormatter formatter =
				DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
						.withLocale(locale);

		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(formatter);
	}

	public static Date parseIso8601(String iso8601) {
		return Date.from(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(iso8601)));
	}
}
