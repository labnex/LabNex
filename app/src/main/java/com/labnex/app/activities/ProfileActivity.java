package com.labnex.app.activities;

import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ActivityProfileBinding;
import com.labnex.app.databinding.ItemProfileDetailBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.TimeHelper;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;
import com.labnex.app.models.user.User;
import com.labnex.app.viewmodels.ProfileViewModel;
import com.vdurmont.emoji.EmojiParser;
import java.util.Date;
import java.util.Locale;

/**
 * @author mmarif
 */
public class ProfileActivity extends BaseActivity {

	private ActivityProfileBinding binding;
	private ProfileViewModel viewModel;
	private UserAccountsApi userAccountsApi;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityProfileBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, binding.mainView, null, null);

		viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
		userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);

		binding.btnBack.setOnClickListener(v -> finish());
		observeViewModel();

		String userIdStr = getIntent().getStringExtra("userId");
		long profileUserId = userIdStr != null ? Long.parseLong(userIdStr) : 0;
		String username = getIntent().getStringExtra("username");

		if (profileUserId > 0) {
			initLoading(profileUserId);
		} else if (username != null && !username.isEmpty()) {
			viewModel.loadUserByUsername(ctx, username);
		} else {
			finish();
		}
	}

	private void initLoading(long profileUserId) {
		int activeAccountId = sharedPrefDB.getInt("currentActiveAccountId", 0);
		UserAccount activeAccount = userAccountsApi.getAccountById(activeAccountId);
		long myUserId = (activeAccount != null) ? activeAccount.getUserId() : 0;
		viewModel.loadUser(ctx, profileUserId, myUserId);
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							int showView = Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE;
							binding.progressBar.setVisibility(showView);
							binding.mainView.setAlpha(showView);
						});

		viewModel
				.getUserInfo()
				.observe(
						this,
						user -> {
							if (user != null) populateUI(user);
						});

		viewModel
				.getError()
				.observe(
						this,
						errorMsg -> {
							if (errorMsg != null) {
								Toasty.show(ctx, getString(R.string.generic_server_response_error));
								viewModel.clearError();
							}
						});
	}

	private void populateUI(User user) {
		Glide.with(ctx)
				.load(user.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.ic_spinner)
				.centerCrop()
				.into(binding.userAvatar);

		binding.fullname.setText(user.getFullName());
		binding.username.setText(getString(R.string.username_with_at_sign, user.getUsername()));

		if (user.getFollowers() > 0 || user.getFollowing() > 0) {
			binding.socialMetricsCard.setVisibility(View.VISIBLE);
			binding.followers.setText(
					getString(R.string.user_followers, Utils.numberFormatter(user.getFollowers())));
			binding.following.setText(
					getString(R.string.user_following, Utils.numberFormatter(user.getFollowing())));
		} else {
			binding.socialMetricsCard.setVisibility(View.GONE);
		}

		if (user.getJobTitle() != null && !user.getJobTitle().isEmpty()) {
			binding.chipJobTitle.setVisibility(View.VISIBLE);
			binding.chipJobTitle.setText(user.getJobTitle());
		}

		binding.chipAdmin.setVisibility(user.isIsAdmin() ? View.VISIBLE : View.GONE);

		if (user.getBio() != null && !user.getBio().trim().isEmpty()) {
			binding.userBioInfo.setVisibility(View.VISIBLE);
			Markdown.render(ctx, EmojiParser.parseToUnicode(user.getBio().trim()), binding.userBio);
		} else {
			binding.userBioInfo.setVisibility(View.GONE);
		}

		setDetail(binding.detailLocation, R.drawable.ic_location, user.getLocation());
		setDetail(binding.detailWebsite, R.drawable.ic_browser, user.getWebsiteUrl());
		setDetail(binding.detailEmail, R.drawable.ic_email, user.getPublicEmail());
		setDetail(binding.detailPronouns, R.drawable.ic_person, user.getPronouns());

		if (user.getCreatedAt() != null) {
			Date d = TimeHelper.parseIso8601(user.getCreatedAt());
			setDetail(
					binding.detailJoined,
					R.drawable.ic_calendar,
					getString(
							R.string.member_since,
							TimeHelper.getAbsoluteDate(d, Locale.getDefault())));
		}

		boolean hasSocial = setupSocialLinks(user);
		binding.socialLinksCard.setVisibility(hasSocial ? View.VISIBLE : View.GONE);
	}

	private boolean setupSocialLinks(User user) {
		boolean hasDiscord = false;
		boolean hasLinkedin = false;
		boolean hasTwitter = false;

		if (user.getDiscord() != null && !user.getDiscord().isEmpty()) {
			setDetail(
					binding.detailDiscord,
					R.drawable.ic_discord,
					getString(R.string.discord_user_profile_link, user.getDiscord()));
			hasDiscord = true;
		}

		if (user.getLinkedin() != null && !user.getLinkedin().isEmpty()) {
			setDetail(binding.detailLinkedin, R.drawable.ic_linkedin, user.getLinkedin());
			hasLinkedin = true;
		}

		if (user.getTwitter() != null && !user.getTwitter().isEmpty()) {
			setDetail(binding.detailTwitter, R.drawable.ic_x, user.getTwitter());
			hasTwitter = true;
		}

		return hasDiscord || hasLinkedin || hasTwitter;
	}

	private void setDetail(ItemProfileDetailBinding b, int icon, String text) {
		if (text == null || text.isEmpty()) {
			b.getRoot().setVisibility(View.GONE);
			return;
		}
		b.getRoot().setVisibility(View.VISIBLE);
		b.detailIcon.setImageResource(icon);
		b.detailText.setText(text);
	}
}
