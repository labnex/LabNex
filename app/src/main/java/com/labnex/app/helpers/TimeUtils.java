package com.labnex.app.helpers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * @author mmarif
 */
public class TimeUtils {

	public static String formatTime(Date date, Locale locale) {

		if (date != null) {
			PrettyTime prettyTime = new PrettyTime(locale);
			return prettyTime.format(date);
		}

		return "";
	}

	public static String formattedDate(String str) {

		DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate date = LocalDate.parse(str, inputFormatter);
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

		return date.format(outputFormatter);
	}
}
