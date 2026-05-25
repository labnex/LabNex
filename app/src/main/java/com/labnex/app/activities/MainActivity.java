package com.labnex.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;
import com.labnex.app.R;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.databinding.ActivityMainBinding;
import com.labnex.app.fragments.ActivitiesFragment;
import com.labnex.app.fragments.ExploreFragment;
import com.labnex.app.fragments.HomeFragment;
import com.labnex.app.fragments.TodoFragment;
import com.labnex.app.helpers.AppSettingsInit;
import com.labnex.app.helpers.BadgeHelper;
import com.labnex.app.helpers.CheckAuthorizationStatus;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.MainActivityViewModel;
import java.util.Objects;

/**
 * @author mmarif
 */
public class MainActivity extends BaseActivity {

	private ActivityMainBinding binding;
	private SharedPrefDB sharedPrefDB;
	private MainActivityViewModel viewModel;

	private static final String STATE_ACTIVE_TAB = "active_tab";
	private static final String TAB_HOME = "home";
	private static final String TAB_EXPLORE = "explore";
	private static final String TAB_TODO = "todo";
	private static final String TAB_ACTIVITIES = "activities";

	public static boolean closeActivity = false;

	public final Fragment homeFrag = new HomeFragment();
	public final Fragment exploreFrag = new ExploreFragment();
	public final Fragment todoFrag = new TodoFragment();
	public final Fragment activitiesFrag = new ActivitiesFragment();
	private Fragment activeFragment = homeFrag;
	private String currentActiveTab = TAB_HOME;
	private final FragmentManager fm = getSupportFragmentManager();
	private BadgeDrawable todoBadge;

	private View detachedDivider;
	private View detachedMenuBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		sharedPrefDB = SharedPrefDB.getInstance(ctx);

		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
		assert userAccountsApi != null;
		if (userAccountsApi.getCount() == 0) {
			startActivity(new Intent(ctx, SignInActivity.class));
			finish();
			return;
		}

		viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		detachedDivider = binding.dockDivider;
		detachedMenuBtn = binding.btnDockMenu;

		if (savedInstanceState != null) {
			currentActiveTab = savedInstanceState.getString(STATE_ACTIVE_TAB, TAB_HOME);
		}

		observeViewModel();
		CheckAuthorizationStatus.checkTokenExpiryWarning(this);

		if (ctx != null) {
			viewModel.checkPersonalAccessToken(ctx);
			viewModel.fetchGitlabVersion(ctx);
		}

		setupFragments();
		setupDockListeners();

		if (savedInstanceState != null) {
			restoreFromSavedTab();
		} else {
			setDefaultFragment();
		}

		getOnBackPressedDispatcher()
				.addCallback(
						this,
						new OnBackPressedCallback(true) {
							@Override
							public void handleOnBackPressed() {
								if (activeFragment != homeFrag) {
									switchTab(homeFrag, R.id.btn_nav_home);
								} else {
									finish();
								}
							}
						});
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_ACTIVE_TAB, currentActiveTab);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (closeActivity) {
			finishAndRemoveTask();
			closeActivity = false;
		}
		viewModel.fetchPendingTodoCount(ctx);
	}

	private void observeViewModel() {
		viewModel
				.getTokenCheckResult()
				.observe(
						this,
						code -> {
							if (code != null && code == 401) {
								CheckAuthorizationStatus.authorizationErrorDialog(ctx);
							}
						});

		viewModel
				.getVersionCheckDone()
				.observe(
						this,
						done -> {
							if (Boolean.TRUE.equals(done)) {
								String version = viewModel.getServerVersion().getValue();
								if (version != null) {
									getAccount()
											.setAccount(
													Objects.requireNonNull(
																	BaseApi.getInstance(
																			ctx,
																			UserAccountsApi.class))
															.getAccountById(
																	sharedPrefDB.getInt(
																			"currentActiveAccountId")));
									viewModel.fetchPendingTodoCount(ctx);
								}
							}
						});

		viewModel
				.getPendingTodoCount()
				.observe(
						this,
						count -> {
							if (count != null && binding != null) {
								updateTodoBadge(count);
							}
						});

		viewModel
				.getError()
				.observe(
						this,
						errorMsg -> {
							if (errorMsg != null && !errorMsg.isEmpty()) {
								Toasty.show(ctx, errorMsg);
							}
						});
	}

	private void setupFragments() {
		fm.beginTransaction()
				.add(R.id.nav_host_fragment, activitiesFrag, TAB_ACTIVITIES)
				.hide(activitiesFrag)
				.add(R.id.nav_host_fragment, todoFrag, TAB_TODO)
				.hide(todoFrag)
				.add(R.id.nav_host_fragment, exploreFrag, TAB_EXPLORE)
				.hide(exploreFrag)
				.add(R.id.nav_host_fragment, homeFrag, TAB_HOME)
				.commitNow();
	}

	private void setupDockListeners() {
		prepareNavButton(binding.btnNavHome);
		prepareNavButton(binding.btnNavExplore);
		prepareNavButton(binding.btnNavTodo);
		prepareNavButton(binding.btnNavActivities);

		binding.btnNavHome.setOnClickListener(v -> switchTab(homeFrag, R.id.btn_nav_home));
		binding.btnNavExplore.setOnClickListener(v -> switchTab(exploreFrag, R.id.btn_nav_explore));
		binding.btnNavTodo.setOnClickListener(v -> switchTab(todoFrag, R.id.btn_nav_todo));
		binding.btnNavActivities.setOnClickListener(
				v -> switchTab(activitiesFrag, R.id.btn_nav_activities));

		binding.btnDockMenu.setOnClickListener(
				v -> {
					if (activeFragment instanceof ExploreFragment) {
						((ExploreFragment) activeFragment).openContextMenu();
					} else if (activeFragment instanceof TodoFragment) {
						((TodoFragment) activeFragment).openContextMenu();
					} else if (activeFragment instanceof ActivitiesFragment) {
						((ActivitiesFragment) activeFragment).openContextMenu();
					}
				});

		LinearLayout.LayoutParams params =
				(LinearLayout.LayoutParams) binding.btnNavActivities.getLayoutParams();
		params.setMarginEnd((int) getResources().getDimension(R.dimen.dimen12dp));
		binding.btnNavActivities.setLayoutParams(params);
	}

	private void prepareNavButton(MaterialButton btn) {
		btn.setBackgroundResource(R.drawable.nav_pill_background);
		btn.setBackgroundTintList(null);
		btn.getBackground().setAlpha(0);
	}

	private void activatePill(MaterialButton btn) {
		btn.setSelected(true);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(255);
	}

	private void resetPill(MaterialButton btn) {
		btn.setSelected(false);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(0);
	}

	public void switchTab(Fragment target, int btnId) {
		if (target.isVisible() && activeFragment == target) {
			updateDockUI(btnId);
			return;
		}

		fm.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.hide(activeFragment)
				.show(target)
				.commit();

		activeFragment = target;
		currentActiveTab = tabForFragment(target);
		updateDockUI(btnId);
	}

	private String tabForFragment(Fragment f) {
		if (f == homeFrag) return TAB_HOME;
		if (f == exploreFrag) return TAB_EXPLORE;
		if (f == todoFrag) return TAB_TODO;
		if (f == activitiesFrag) return TAB_ACTIVITIES;
		return TAB_HOME;
	}

	private void updateDockUI(int activeBtnId) {
		resetPill(binding.btnNavHome);
		resetPill(binding.btnNavExplore);
		resetPill(binding.btnNavTodo);
		resetPill(binding.btnNavActivities);

		int[] btnIds = {
			R.id.btn_nav_home, R.id.btn_nav_explore, R.id.btn_nav_todo, R.id.btn_nav_activities
		};
		MaterialButton[] buttons = {
			binding.btnNavHome, binding.btnNavExplore, binding.btnNavTodo, binding.btnNavActivities
		};

		for (int i = 0; i < btnIds.length; i++) {
			if (activeBtnId == btnIds[i]) activatePill(buttons[i]);
		}

		updateContextualDockActions(activeBtnId);
	}

	private void updateTodoBadge(int count) {
		runOnUiThread(
				() -> {
					todoBadge = BadgeHelper.updateBadge(this, binding.btnNavTodo, todoBadge, count);

					if (activeFragment == todoFrag || activeFragment == exploreFrag) {
						updateContextualDockActions(
								activeFragment == todoFrag
										? R.id.btn_nav_todo
										: R.id.btn_nav_explore);
					}
				});
	}

	public void refreshTodoBadge() {
		viewModel.fetchPendingTodoCount(ctx);
	}

	private void updateContextualDockActions(int activeBtnId) {
		ViewGroup parent = binding.dockContainer;
		parent.removeView(detachedDivider);
		parent.removeView(detachedMenuBtn);

		if (activeBtnId == R.id.btn_nav_explore
				|| activeBtnId == R.id.btn_nav_todo
				|| activeBtnId == R.id.btn_nav_activities) {

			int badgeMargin = getResources().getDimensionPixelSize(R.dimen.dimen16dp);
			if (todoBadge != null && todoBadge.isVisible()) {
				badgeMargin = getResources().getDimensionPixelSize(R.dimen.dimen8dp);
			}

			ViewGroup.MarginLayoutParams params =
					(ViewGroup.MarginLayoutParams) detachedDivider.getLayoutParams();
			params.setMarginStart(badgeMargin);
			detachedDivider.setLayoutParams(params);

			parent.addView(detachedDivider);
			parent.addView(detachedMenuBtn);
		}

		binding.dockedToolbar.requestLayout();
	}

	private void setDefaultFragment() {
		int defaultScreen =
				Integer.parseInt(
						AppSettingsInit.getSettingsValue(ctx, AppSettingsInit.APP_HOME_SCREEN_KEY));

		switch (defaultScreen) {
			case 3:
				switchTab(activitiesFrag, R.id.btn_nav_activities);
				break;
			case 1:
				switchTab(exploreFrag, R.id.btn_nav_explore);
				break;
			case 2:
				switchTab(todoFrag, R.id.btn_nav_todo);
				break;
			default:
				updateDockUI(R.id.btn_nav_home);
				break;
		}
	}

	private void restoreFromSavedTab() {
		switch (currentActiveTab) {
			case TAB_EXPLORE:
				fm.beginTransaction()
						.hide(homeFrag)
						.hide(todoFrag)
						.hide(activitiesFrag)
						.show(exploreFrag)
						.commitNow();
				activeFragment = exploreFrag;
				updateDockUI(R.id.btn_nav_explore);
				break;
			case TAB_TODO:
				fm.beginTransaction()
						.hide(homeFrag)
						.hide(exploreFrag)
						.hide(activitiesFrag)
						.show(todoFrag)
						.commitNow();
				activeFragment = todoFrag;
				updateDockUI(R.id.btn_nav_todo);
				break;
			case TAB_ACTIVITIES:
				fm.beginTransaction()
						.hide(homeFrag)
						.hide(exploreFrag)
						.hide(todoFrag)
						.show(activitiesFrag)
						.commitNow();
				activeFragment = activitiesFrag;
				updateDockUI(R.id.btn_nav_activities);
				break;
			default:
				activeFragment = homeFrag;
				updateDockUI(R.id.btn_nav_home);
				break;
		}
	}
}
