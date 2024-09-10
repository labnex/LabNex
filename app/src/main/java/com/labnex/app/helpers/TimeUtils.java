package com.labnex.app.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * @author mmarif
 */
public class TimeUtils {

	public static String customDateFormatForToast(String customDate) {

		String[] parts = customDate.split("\\+");
		String part1 = parts[0] + "Z";
		SimpleDateFormat formatter =
				new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
		Date createdTime = null;

		try {
			createdTime = formatter.parse(part1);
		} catch (ParseException ignored) {
		}

		assert createdTime != null;

		return customDateFormatForToastDateFormat(createdTime);
	}

	public static String formatTime(Date date, Locale locale) {

		if (date != null) {
			PrettyTime prettyTime = new PrettyTime(locale);
			return prettyTime.format(date);
		}

		return "";
	}

	public static String customDateFormatForToastDateFormat(Date customDate) {

		DateFormat format = DateFormat.getDateTimeInstance();
		return format.format(customDate);
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

	public static Date parseIso8601(String iso8601) {
		return Date.from(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(iso8601)));
	}

	public static String formattedDate(String str) {

		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate date = LocalDate.parse(str, inputFormatter);
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

		return date.format(outputFormatter);
	}
}
