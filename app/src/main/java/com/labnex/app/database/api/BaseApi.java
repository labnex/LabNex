package com.labnex.app.database.api;

import android.content.Context;
import androidx.annotation.NonNull;
import com.labnex.app.database.db.LabNexDatabase;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author mmarif
 */
public abstract class BaseApi {

	protected static final ExecutorService executorService = Executors.newCachedThreadPool();
	private static final Map<Class<? extends BaseApi>, Object> instances = new HashMap<>();
	protected final LabNexDatabase labnexDatabase;

	protected BaseApi(Context context) {
		labnexDatabase = LabNexDatabase.getDatabaseInstance(context);
	}

	public static <T extends BaseApi> T getInstance(
			@NonNull Context context, @NonNull Class<T> clazz) {

		try {

			if (!instances.containsKey(clazz)) {
				synchronized (BaseApi.class) {
					if (!instances.containsKey(clazz)) {

						T instance =
								clazz.getDeclaredConstructor(Context.class).newInstance(context);

						instances.put(clazz, instance);
						return instance;
					}
				}
			}

			return (T) instances.get(clazz);

		} catch (NoSuchMethodException
				| IllegalAccessException
				| InvocationTargetException
				| InstantiationException ignored) {
		}

		return null;
	}
}
