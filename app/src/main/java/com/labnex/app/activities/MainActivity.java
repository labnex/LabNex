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
import com.labnex.app.helpers.AppSettingsInit;
import com.labnex.app.helpers.CheckAuthorizationStatus;
import com.labnex.app.models.metadata.Metadata;
import com.labnex.app.models.personal_access_tokens.PersonalAccessTokens;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class MainActivity extends BaseActivity {

	private ActivityMainBinding binding;
	private Fragment homeFragment;
	private Fragment activitiesFragment;
	private Fragment exploreFragment;
	public static boolean refActivity = false;
	public static boolean closeActivity = false;
	public static boolean homeScreen = true;
	private static final String LAST_FRAGMENT_KEY = "last_fragment_index";
	private int lastFragmentIndex = -1;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);

		assert userAccountsApi != null;
		if (userAccountsApi.getCount() == 0) {
			Intent intent = new Intent(ctx, SignInActivity.class);
			ctx.startActivity(intent);
			finish();
			return;
		}

		CheckAuthorizationStatus.checkTokenExpiryWarning(this);
		checkPersonalAccessToken();
		gitlabVersion();
		homeFragment = new HomeFragment();
		activitiesFragment = new ActivitiesFragment();
		exploreFragment = new ExploreFragment();

		binding.navView.setOnItemSelectedListener(
				item -> {
					if (R.id.navigation_home_menu == item.getItemId()) {
						lastFragmentIndex = 0;
						loadFragment(homeFragment);
						return true;
					} else if (R.id.navigation_activities_menu == item.getItemId()) {
						lastFragmentIndex = 1;
						loadFragment(activitiesFragment);
						return true;
					} else if (R.id.navigation_explore_menu == item.getItemId()) {
						lastFragmentIndex = 2;
						loadFragment(exploreFragment);
						return true;
					} else {
						return false;
					}
				});

		if (savedInstanceState != null) {
			lastFragmentIndex = savedInstanceState.getInt(LAST_FRAGMENT_KEY, -1);
			restoreLastFragment();
		} else {
			setDefaultFragment();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (closeActivity) {
			finishAndRemoveTask();
			closeActivity = false;
			return;
		}

		if (refActivity) {
			this.recreate();
			this.overridePendingTransition(0, 0);
			refActivity = false;
			return;
		}

		if (lastFragmentIndex != -1) {
			restoreLastFragment();
		} else if (homeScreen) {
			setDefaultFragment();
			homeScreen = false;
		}
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(LAST_FRAGMENT_KEY, lastFragmentIndex);
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

		if (fragment == homeFragment) {
			binding.navView.getMenu().getItem(0).setChecked(true);
		} else if (fragment == activitiesFragment) {
			binding.navView.getMenu().getItem(1).setChecked(true);
		} else if (fragment == exploreFragment) {
			binding.navView.getMenu().getItem(2).setChecked(true);
		}
	}

	private void setDefaultFragment() {
		int defaultScreen =
				Integer.parseInt(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_HOME_SCREEN_KEY));
		switch (defaultScreen) {
			case 0:
				lastFragmentIndex = 0;
				binding.navView.getMenu().getItem(0).setChecked(true);
				loadFragment(homeFragment);
				break;
			case 1:
				lastFragmentIndex = 1;
				binding.navView.getMenu().getItem(1).setChecked(true);
				loadFragment(activitiesFragment);
				break;
			case 2:
				lastFragmentIndex = 2;
				binding.navView.getMenu().getItem(2).setChecked(true);
				loadFragment(exploreFragment);
				break;
		}
	}

	private void restoreLastFragment() {
		switch (lastFragmentIndex) {
			case 0:
				binding.navView.getMenu().getItem(0).setChecked(true);
				loadFragment(homeFragment);
				break;
			case 1:
				binding.navView.getMenu().getItem(1).setChecked(true);
				loadFragment(activitiesFragment);
				break;
			case 2:
				binding.navView.getMenu().getItem(2).setChecked(true);
				loadFragment(exploreFragment);
				break;
			default:
				setDefaultFragment();
				break;
		}
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
