package com.labnex.app.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.models.award_emoji.AwardEmoji;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ReactionsViewModel extends ViewModel {

	public void loadReactions(
			Context ctx,
			long projectId,
			String type,
			long iid,
			Long noteId,
			long currentUserId,
			ReactionsLoadedCallback callback) {
		if (ctx == null) return;

		Call<List<AwardEmoji>> call;
		if (noteId != null) {
			call = RetrofitClient.getApiInterface(ctx).getNoteAwardEmojis(projectId, iid, noteId);
		} else {
			call = RetrofitClient.getApiInterface(ctx).getIssueAwardEmojis(projectId, iid);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<AwardEmoji>> c,
							@NonNull Response<List<AwardEmoji>> r) {
						if (r.isSuccessful() && r.body() != null && callback != null) {
							List<AwardEmoji> all = r.body();
							List<AwardEmoji> mine = new ArrayList<>();
							for (AwardEmoji a : all) {
								if (a.getUser().getId() == currentUserId) mine.add(a);
							}
							callback.onLoaded(noteId, all, mine);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<AwardEmoji>> c, @NonNull Throwable t) {}
				});
	}

	public void addReaction(
			Context ctx,
			long projectId,
			String type,
			long iid,
			Long noteId,
			String name,
			long currentUserId,
			ReactionsLoadedCallback callback) {
		if (ctx == null) return;

		Call<AwardEmoji> call;
		if (noteId != null) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.addNoteAwardEmoji(projectId, iid, noteId, name);
		} else {
			call = RetrofitClient.getApiInterface(ctx).addIssueAwardEmoji(projectId, iid, name);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<AwardEmoji> c, @NonNull Response<AwardEmoji> r) {
						if (r.isSuccessful()) {
							loadReactions(
									ctx, projectId, type, iid, noteId, currentUserId, callback);
						}
					}

					@Override
					public void onFailure(@NonNull Call<AwardEmoji> c, @NonNull Throwable t) {}
				});
	}

	public void deleteReaction(
			Context ctx,
			long projectId,
			String type,
			long iid,
			Long noteId,
			long awardId,
			long currentUserId,
			ReactionsLoadedCallback callback) {
		if (ctx == null) return;

		Call<Void> call;
		if (noteId != null) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.deleteNoteAwardEmoji(projectId, iid, noteId, awardId);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.deleteIssueAwardEmoji(projectId, iid, awardId);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(@NonNull Call<Void> c, @NonNull Response<Void> r) {
						if (r.isSuccessful()) {
							loadReactions(
									ctx, projectId, type, iid, noteId, currentUserId, callback);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> c, @NonNull Throwable t) {}
				});
	}

	public interface ReactionsLoadedCallback {
		void onLoaded(Long noteId, List<AwardEmoji> reactions, List<AwardEmoji> userReactions);
	}
}
