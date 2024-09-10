package com.labnex.app.activities;

import static com.labnex.app.helpers.CheckAuthorizationStatus.authorizationErrorDialog;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.databinding.ActivityMainBinding;
import com.labnex.app.fragments.ActivitiesFragment;
import com.labnex.app.fragments.ExploreFragment;
import com.labnex.app.fragments.HomeFragment;
import com.labnex.app.models.metadata.Metadata;
import com.labnex.app.models.personal_access_tokens.PersonalAccessTokens;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class MainActivity extends BaseActivity {

	private Fragment homeFragment;
	private Fragment activitiesFragment;
	private Fragment exploreFragment;
	public static boolean refActivity = false;
	public static boolean closeActivity = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);

		assert userAccountsApi != null;
		if (userAccountsApi.getCount() == 0) {
			Intent intent = new Intent(ctx, SignInActivity.class);
			ctx.startActivity(intent);
			finish();
			return;
		}

		checkPersonalAccessToken();
		gitlabVersion();
		homeFragment = new HomeFragment();
		activitiesFragment = new ActivitiesFragment();
		exploreFragment = new ExploreFragment();

		loadFragment(homeFragment);

		binding.navView.setOnItemSelectedListener(
				item -> {
					if (R.id.navigation_home_menu == item.getItemId()) {
						loadFragment(homeFragment);
						return true;
					} else if (R.id.navigation_activities_menu == item.getItemId()) {
						loadFragment(activitiesFragment);
						return true;
					} else if (R.id.navigation_explore_menu == item.getItemId()) {
						loadFragment(exploreFragment);
						return true;
					} else {
						return false;
					}
				});
	}

	@Override
	public void onResume() {
		super.onResume();

		if (closeActivity) {
			finishAndRemoveTask();
			closeActivity = false;
		}

		if (refActivity) {
			this.recreate();
			this.overridePendingTransition(0, 0);
			refActivity = false;
		}
	}

	private void loadFragment(Fragment fragment) {

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		if (fragment.isAdded()) {
			transaction.show(fragment);
		} else {
			transaction.add(R.id.nav_host_fragment_activity_main, fragment);
		}

		for (Fragment frag : getSupportFragmentManager().getFragments()) {
			if (frag != fragment) {
				transaction.hide(frag);
			}
		}

		transaction.commit();
	}

	private void checkPersonalAccessToken() {

		Call<PersonalAccessTokens> call =
				RetrofitClient.getApiInterface(ctx).getPersonalAccessTokenInfo();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<PersonalAccessTokens> call,
							@NonNull retrofit2.Response<PersonalAccessTokens> response) {

						if (response.code() == 401) {
							authorizationErrorDialog(ctx);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<PersonalAccessTokens> call, @NonNull Throwable t) {}
				});
	}

	private void gitlabVersion() {

		Call<Metadata> callVersion = RetrofitClient.getApiInterface(ctx).getMetadata();
		callVersion.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull final Call<Metadata> callVersion,
							@NonNull retrofit2.Response<Metadata> responseVersion) {

						if (responseVersion.code() == 200 && responseVersion.body() != null) {
							String version = responseVersion.body().getVersion();

							Objects.requireNonNull(BaseApi.getInstance(ctx, UserAccountsApi.class))
									.updateServerVersion(
											version, sharedPrefDB.getInt("currentActiveAccountId"));
							getAccount()
									.setAccount(
											Objects.requireNonNull(
															BaseApi.getInstance(
																	ctx, UserAccountsApi.class))
													.getAccountById(
															sharedPrefDB.getInt(
																	"currentActiveAccountId")));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<Metadata> callVersion, @NonNull Throwable t) {}
				});
	}
}
