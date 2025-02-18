package com.labnex.app.clients;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.GsonBuilder;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.helpers.ApiKeyAuth;
import com.labnex.app.helpers.Utils;
import com.labnex.app.interfaces.WebInterface;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * @author mmarif
 */
public class RetrofitClient {

	private static final Map<String, ApiInterface> apiInterfaces = new ConcurrentHashMap<>();
	private static final Map<String, WebInterface> webInterfaces = new ConcurrentHashMap<>();

	private static Retrofit createRetrofit(Context ctx, String instanceUrl, String token) {

		final boolean connToInternet = Utils.isNetworkAvailable(ctx);
		File httpCacheDirectory = new File(ctx.getCacheDir(), "responses");
		Cache cache = new Cache(httpCacheDirectory, 500);

		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		// logging.setLevel(HttpLoggingInterceptor.Level.BODY);

		ApiKeyAuth auth = new ApiKeyAuth("header", "Authorization");
		auth.setApiKey(token);

		try {

			Interceptor onlineInterceptor =
					new Interceptor() {
						@NonNull @Override
						public okhttp3.Response intercept(Chain chain) throws IOException {

							okhttp3.Response response = chain.proceed(chain.request());
							int maxAge = 60;

							return response.newBuilder()
									.removeHeader("Pragma")
									.header("Cache-Control", "public, max-age=" + maxAge)
									.build();
						}
					};

			Interceptor offlineInterceptor =
					new Interceptor() {
						@NonNull @Override
						public okhttp3.Response intercept(Chain chain) throws IOException {

							Request request = chain.request();

							if (!connToInternet) {

								int stale = 60 * 60 * 24 * 30; // 30 days

								request =
										request.newBuilder()
												.removeHeader("Pragma")
												.header(
														"Cache-Control",
														"public, only-if-cached, max-stale="
																+ stale)
												.build();
							}

							return chain.proceed(request);
						}
					};

			OkHttpClient okHttpClient =
					new OkHttpClient.Builder()
							.addInterceptor(auth)
							.addInterceptor(logging)
							.cache(cache)
							.addInterceptor(offlineInterceptor)
							.addNetworkInterceptor(onlineInterceptor)
							.build();

			return new Retrofit.Builder()
					.baseUrl(instanceUrl)
					.client(okHttpClient)
					.addConverterFactory(ScalarsConverterFactory.create())
					.addConverterFactory(
							GsonConverterFactory.create(
									new GsonBuilder()
											.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
											.create()))
					.build();
		} catch (Exception e) {
			Log.e("onFailure", e.toString());
		}

		return null;
	}

	public static ApiInterface getApiInterface(Context context) {

		return getApiInterface(
				context,
				((BaseActivity) context).getAccount().getAccount().getInstanceUrl(),
				((BaseActivity) context).getAccount().getAuthorization());
	}

	public static ApiInterface getApiInterface(Context context, String url, String token) {

		String key = token.hashCode() + "@" + url;

		if (!apiInterfaces.containsKey(key)) {
			synchronized (RetrofitClient.class) {
				if (!apiInterfaces.containsKey(key)) {

					ApiInterface apiInterface =
							Objects.requireNonNull(createRetrofit(context, url, token))
									.create(ApiInterface.class);
					apiInterfaces.put(key, apiInterface);
					return apiInterface;
				}
			}
		}

		return apiInterfaces.get(key);
	}

	public static WebInterface getWebInterface(Context context, String url) {

		return getWebInterface(
				context, url, ((BaseActivity) context).getAccount().getAuthorization());
	}

	public static WebInterface getWebInterface(Context context, String url, String token) {

		String key = token.hashCode() + "@" + url;
		if (!webInterfaces.containsKey(key)) {
			synchronized (RetrofitClient.class) {
				if (!webInterfaces.containsKey(key)) {

					WebInterface webInterface =
							Objects.requireNonNull(createRetrofit(context, url, token))
									.create(WebInterface.class);
					webInterfaces.put(key, webInterface);

					return webInterface;
				}
			}
		}

		return webInterfaces.get(key);
	}

	public interface ApiInterface extends com.labnex.app.interfaces.ApiInterface {}
}
