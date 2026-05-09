package com.labnex.app.helpers;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author mmarif
 */
public class ApiResponseHandler {

	public static <T> void handleAction(
			retrofit2.Response<T> response,
			MutableLiveData<Boolean> isLoading,
			MutableLiveData<Boolean> success,
			MutableLiveData<String> error) {

		isLoading.setValue(false);

		if (response.isSuccessful()) {
			success.setValue(true);
			setErrorIfPresent(error, null);
		} else {
			setErrorIfPresent(error, getErrorMessage(response));
		}
	}

	public static <T> void handleFetch(
			retrofit2.Response<T> response,
			MutableLiveData<Boolean> isLoading,
			Runnable onSuccess,
			MutableLiveData<String> error) {

		isLoading.setValue(false);

		if (response.isSuccessful()) {
			onSuccess.run();
			setErrorIfPresent(error, null);
		} else {
			setErrorIfPresent(error, getErrorMessage(response));
		}
	}

	private static String getErrorMessage(retrofit2.Response<?> response) {
		int code = response.code();
		if (code == 401) return "auth_error";
		if (code == 403) return "access_forbidden_403";
		if (code == 404) return "not_found";

		return parseGitlabError(response);
	}

	private static String parseGitlabError(retrofit2.Response<?> response) {
		if (response.errorBody() == null) return "generic_error";
		try {
			String body = response.errorBody().string();
			JsonObject json = JsonParser.parseString(body).getAsJsonObject();

			if (json.has("message")) {
				JsonElement msg = json.get("message");
				if (msg.isJsonObject()) {
					StringBuilder sb = getStringBuilder(msg);
					return sb.length() > 0 ? sb.toString() : "generic_error";
				} else if (msg.isJsonPrimitive()) {
					return msg.getAsString();
				}
			}

			if (json.has("error")) {
				return json.get("error").getAsString();
			}

			return body;
		} catch (Exception e) {
			return "generic_error";
		}
	}

	@NonNull private static StringBuilder getStringBuilder(JsonElement msg) {
		StringBuilder sb = new StringBuilder();
		JsonObject fields = msg.getAsJsonObject();
		for (String key : fields.keySet()) {
			JsonElement val = fields.get(key);
			if (val.isJsonArray()) {
				for (JsonElement e : val.getAsJsonArray()) {
					if (sb.length() > 0) sb.append("\n");
					sb.append(e.getAsString());
				}
			}
		}
		return sb;
	}

	private static void setErrorIfPresent(MutableLiveData<String> error, String value) {
		if (error != null) {
			error.setValue(value);
		}
	}

	public static String getErrorMessageStatic(retrofit2.Response<?> response) {
		return getErrorMessage(response);
	}
}
