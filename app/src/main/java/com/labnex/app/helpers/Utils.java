package com.labnex.app.helpers;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Base64;
import android.util.TypedValue;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.core.content.pm.PackageInfoCompat;
import com.labnex.app.R;
import com.labnex.app.core.CoreApplication;
import com.labnex.app.database.models.UserAccount;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mmarif
 */
public class Utils {

	private static final HashMap<String[], FileType> extensions = new HashMap<>();

	static {
		extensions.put(
				new String[] {"jpg", "jpeg", "gif", "png", "ico", "tif", "tiff", "bmp"},
				FileType.IMAGE);
		extensions.put(
				new String[] {
					"mp3", "wav", "opus", "flac", "wma", "aac", "m4a", "oga", "mpc", "ogg"
				},
				FileType.AUDIO);
		extensions.put(
				new String[] {
					"mp4", "mkv", "avi", "mov", "wmv", "qt", "mts", "m2ts", "webm", "flv", "ogv",
					"amv", "mpg", "mpeg", "mpv", "m4v", "3gp", "wmv"
				},
				FileType.VIDEO);
		extensions.put(
				new String[] {
					"doc", "docx", "ppt", "pptx", "xls", "xlsx", "xlsm", "odt", "ott", "odf", "ods",
					"ots", "odg", "otg", "odp", "otp", "bin", "psd", "xcf", "pdf"
				},
				FileType.DOCUMENT);
		extensions.put(
				new String[] {"exe", "msi", "jar", "dmg", "deb", "apk"}, FileType.EXECUTABLE);
		extensions.put(
				new String[] {
					"txt",
					"md",
					"json",
					"java",
					"go",
					"php",
					"c",
					"cc",
					"cpp",
					"h",
					"cxx",
					"cyc",
					"m",
					"cs",
					"bash",
					"sh",
					"bsh",
					"cv",
					"python",
					"perl",
					"pm",
					"rb",
					"ruby",
					"javascript",
					"coffee",
					"rc",
					"rs",
					"rust",
					"basic",
					"clj",
					"css",
					"dart",
					"lisp",
					"erl",
					"hs",
					"lsp",
					"rkt",
					"ss",
					"llvm",
					"ll",
					"lua",
					"matlab",
					"pascal",
					"r",
					"scala",
					"sql",
					"latex",
					"tex",
					"vb",
					"vbs",
					"vhd",
					"tcl",
					"wiki.meta",
					"yaml",
					"yml",
					"markdown",
					"xml",
					"proto",
					"regex",
					"py",
					"pl",
					"js",
					"html",
					"htm",
					"volt",
					"ini",
					"htaccess",
					"conf",
					"gitignore",
					"gradle",
					"txt",
					"properties",
					"bat",
					"twig",
					"cvs",
					"cmake",
					"in",
					"info",
					"spec",
					"m4",
					"am",
					"dist",
					"pam",
					"hx",
					"ts",
					"kt",
					"kts"
				},
				FileType.TEXT);
		extensions.put(new String[] {"ttf", "otf", "woff", "woff2", "ttc", "eot"}, FileType.FONT);
	}

	public static boolean isNetworkAvailable(Context context) {

		ConnectivityManager cm =
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	public static boolean switchToAccount(Context context, UserAccount userAccount) {
		return ((CoreApplication) context.getApplicationContext())
				.switchToAccount(userAccount, false);
	}

	public static void switchToAccount(Context context, UserAccount userAccount, boolean tmp) {
		((CoreApplication) context.getApplicationContext()).switchToAccount(userAccount, tmp);
	}

	private static Intent wrapBrowserIntent(Context context, Intent intent) {

		final PackageManager pm = context.getPackageManager();
		final List<ResolveInfo> activities =
				pm.queryIntentActivities(
						new Intent(intent)
								.setData(
										Objects.requireNonNull(intent.getData())
												.buildUpon()
												.authority("example.com")
												.scheme("https")
												.build()),
						PackageManager.MATCH_ALL);
		final ArrayList<Intent> chooserIntents = new ArrayList<>();
		final String ourPackageName = context.getPackageName();

		activities.sort(new ResolveInfo.DisplayNameComparator(pm));

		for (ResolveInfo resInfo : activities) {
			ActivityInfo info = resInfo.activityInfo;
			if (!info.enabled || !info.exported) {
				continue;
			}
			if (info.packageName.equals(ourPackageName)) {
				continue;
			}

			Intent targetIntent = new Intent(intent);
			targetIntent.setPackage(info.packageName);
			targetIntent.setDataAndType(intent.getData(), intent.getType());
			chooserIntents.add(targetIntent);
		}

		if (chooserIntents.isEmpty()) {
			return null;
		}

		final Intent lastIntent = chooserIntents.remove(chooserIntents.size() - 1);
		if (chooserIntents.isEmpty()) {
			return lastIntent;
		}

		Intent chooserIntent = Intent.createChooser(lastIntent, null);
		String extraName = Intent.EXTRA_ALTERNATE_INTENTS;
		chooserIntent.putExtra(extraName, chooserIntents.toArray(new Intent[0]));
		return chooserIntent;
	}

	public static void openUrlInBrowser(Context ctx, Activity activity, String url) {

		Intent i;
		i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		i.addCategory(Intent.CATEGORY_BROWSABLE);

		try {
			Intent browserIntent = wrapBrowserIntent(ctx, i);
			if (browserIntent == null) {
				Snackbar.info(activity, ctx.getString(R.string.generic_error));
			}
			ctx.startActivity(browserIntent);
		} catch (ActivityNotFoundException e) {
			Snackbar.info(activity, ctx.getString(R.string.browserOpenFailed));
		} catch (Exception e) {
			Snackbar.info(activity, ctx.getString(R.string.generic_error));
		}
	}

	public static int getAppBuildNo(Context context) {

		try {
			PackageInfo packageInfo =
					context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return (int) PackageInfoCompat.getLongVersionCode(packageInfo);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static String getAppVersion(Context context) {

		try {
			PackageInfo packageInfo =
					context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	public static boolean isPremium(Context context) {
		return context.getPackageName().equals("com.labnex.app.premium");
	}

	public static Context setLocale(Context context, String languageCode) {

		Locale locale = new Locale(languageCode);
		Locale.setDefault(locale);

		Resources resources = context.getResources();
		Configuration config = resources.getConfiguration();

		config.setLocale(locale);
		return context.createConfigurationContext(config);
	}

	public static void copyToClipboard(
			Context ctx, Activity activity, CharSequence data, String message) {

		ClipboardManager clipboard =
				(ClipboardManager)
						Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
		assert clipboard != null;

		ClipData clip = ClipData.newPlainText(data, data);
		clipboard.setPrimaryClip(clip);

		Snackbar.info(activity, activity.findViewById(R.id.bottom_app_bar), message);
	}

	/** pretty number formatter: 1200 = 1.2k */
	public static String numberFormatter(Number number) {

		char[] suffix = {' ', 'k', 'M', 'B', 'T'};
		long numValue = number.longValue();
		int value = (int) Math.floor(Math.log10(numValue));
		int base = value / 3;
		if (value >= 3 && base < suffix.length) {
			return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3))
					+ suffix[base];
		}
		if (base >= suffix.length) {
			return new DecimalFormat("#0").format(numValue / Math.pow(10, base * 2)) + suffix[4];
		} else {
			return new DecimalFormat("#,##0").format(numValue);
		}
	}

	public static String encodeBase64(String str) {

		String base64Str = str;
		if (!str.isEmpty()) {
			byte[] data = str.getBytes(StandardCharsets.UTF_8);
			base64Str = Base64.encodeToString(data, Base64.DEFAULT);
		}

		return base64Str;
	}

	public static String decodeBase64(String str) {

		String base64Str = str;
		if (!str.isEmpty()) {
			byte[] data = Base64.decode(base64Str, Base64.DEFAULT);
			base64Str = new String(data, StandardCharsets.UTF_8);
		}

		return base64Str;
	}

	public static String imageEncodeToBase64(Bitmap image) {

		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		byte[] bytes = byteArrayOutputStream.toByteArray();

		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	public static String repeatString(String str, int length, int index, int count) {

		String colorString = str;
		if (colorString.length() == length) {
			String sub = colorString.substring(index);
			colorString = "#" + sub.repeat(count);
		}
		return colorString;
	}

	public static int getPixelsFromDensity(Context context, int dp) {
		return (int) (context.getResources().getDisplayMetrics().density * dp);
	}

	public static int getPixelsFromScaledDensity(Context context, int sp) {
		return (int) (context.getResources().getDisplayMetrics().scaledDensity * sp);
	}

	public static int calculateLabelWidth(String text, int textSize, int paddingLeftRight) {

		Paint paint = new Paint();
		Rect rect = new Rect();

		paint.setTextSize(textSize);
		paint.getTextBounds(text, 0, text.length(), rect);

		return rect.width() + (paddingLeftRight * 2);
	}

	@ColorInt
	public static int getColorFromAttribute(Context context, @AttrRes int resId) {

		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(resId, typedValue, true);

		return typedValue.data;
	}

	public static long getLineCount(String s) {

		if (s.isEmpty()) {
			return 0;
		}

		long lines = 1; // we start counting at 1 because there is always at least one line

		Pattern pattern = Pattern.compile("(\r\n|\n)");
		Matcher matcher = pattern.matcher(s);

		while (matcher.find()) lines++;

		return lines;
	}

	public enum FileType {
		IMAGE,
		AUDIO,
		VIDEO,
		DOCUMENT,
		TEXT,
		EXECUTABLE,
		FONT,
		UNKNOWN
	}

	public static FileType getFileType(String extension) {

		if (extension != null && !extension.isEmpty()) {
			for (String[] testExtensions : extensions.keySet()) {
				for (String testExtension : testExtensions) {

					if (testExtension.equalsIgnoreCase(extension)) {
						return extensions.get(testExtensions);
					}
				}
			}
		}

		return FileType.UNKNOWN;
	}

	public static void copyProgress(
			InputStream inputStream,
			OutputStream outputStream,
			long totalSize,
			ProgressListener progressListener)
			throws IOException {

		byte[] buffer = new byte[4096];
		int read;

		long totalSteps = (long) Math.ceil((double) totalSize / buffer.length);
		long stepsPerPercent = (long) Math.floor((double) totalSteps / 100);

		short percent = 0;
		long stepCount = 0;

		progressListener.onActionStarted();

		while ((read = inputStream.read(buffer)) != -1) {

			outputStream.write(buffer, 0, read);
			stepCount++;

			if (stepCount == stepsPerPercent) {
				percent++;
				if (percent <= 100) {
					progressListener.onProgressChanged(percent);
				}
				stepCount = 0;
			}
		}

		if (percent < 100) {
			progressListener.onProgressChanged((short) 100);
		}

		progressListener.onActionFinished();
	}

	public interface ProgressListener {

		default void onActionStarted() {}

		default void onActionFinished() {}

		void onProgressChanged(short progress);
	}
}
