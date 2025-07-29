package com.labnex.app.activities;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.ActivityProfileBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.user.User;
import com.vdurmont.emoji.EmojiParser;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class ProfileActivity extends BaseActivity {

	ActivityProfileBinding binding;
	private int userId;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityProfileBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		userId = getIntent().getIntExtra("userId", 0);

		getUserMeta();
	}

	private void getUserMeta() {

		Call<User> call = RetrofitClient.getApiInterface(ctx).getSingleUser(userId);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

						User user = response.body();

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								assert user != null;

								Glide.with(ctx)
										.load(user.getAvatarUrl())
										.diskCacheStrategy(DiskCacheStrategy.ALL)
										.placeholder(R.drawable.ic_spinner)
										.centerCrop()
										.into(binding.userAvatar);

								binding.username.setText(
										getString(
												R.string.username_with_at_sign,
												user.getUsername()));
								binding.fullname.setText(user.getFullName());

								if (user.getPublicEmail() != null) {
									if (!user.getPublicEmail().isEmpty()) {
										binding.userEmail.setVisibility(View.VISIBLE);
										binding.userEmail.setText(user.getPublicEmail());
									}
								}

								if (user.getPronouns() != null) {
									if (!user.getPronouns().isEmpty()) {
										binding.pronouns.setVisibility(View.VISIBLE);
										binding.pronouns.setText(user.getPronouns());
									}
								}

								binding.followers.setText(
										getString(
												R.string.user_followers,
												String.valueOf(user.getFollowers())));
								binding.following.setText(
										getString(
												R.string.user_following,
												String.valueOf(user.getFollowing())));

								if (user.getWebsiteUrl() != null) {
									if (!user.getWebsiteUrl().isEmpty()) {
										binding.websiteUrlInfo.setVisibility(View.VISIBLE);
										binding.websiteUrl.setText(user.getWebsiteUrl());
									}
								}

								if (user.getDiscord() != null && !user.getDiscord().isEmpty()
										|| user.getLinkedin() != null
												&& !user.getLinkedin().isEmpty()
										|| user.getTwitter() != null
												&& !user.getTwitter().isEmpty()) {
									binding.socialInfo.setVisibility(View.VISIBLE);

									if (user.getDiscord() != null && !user.getDiscord().isEmpty()) {
										binding.discord.setVisibility(View.VISIBLE);
										binding.discord.setText(
												getString(
														R.string.discord_user_profile_link,
														user.getDiscord()));
									}

									if (user.getLinkedin() != null
											&& !user.getLinkedin().isEmpty()) {
										binding.linkedin.setVisibility(View.VISIBLE);
										binding.linkedin.setText(user.getLinkedin());
									}

									if (user.getTwitter() != null && !user.getTwitter().isEmpty()) {
										binding.twitter.setVisibility(View.VISIBLE);
										binding.twitter.setText(user.getTwitter());
									}
								}

								if (user.getLocation() != null) {
									if (!user.getLocation().isEmpty()) {
										binding.locationInfo.setVisibility(View.VISIBLE);
										binding.location.setText(user.getLocation());
									}
								}

								if (user.getBio() != null) {
									if (!user.getBio().isEmpty()) {
										binding.userBioInfo.setVisibility(View.VISIBLE);
										Markdown.render(
												ctx,
												EmojiParser.parseToUnicode(user.getBio().trim()),
												binding.userBio);
									}
								}
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						Snackbar.info(
								ProfileActivity.this,
								findViewById(R.id.bottom_app_bar),
								getString(R.string.generic_server_response_error));
					}
				});
	}
}
