package com.labnex.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.GroupsAdapter;
import com.labnex.app.bottomsheets.GroupsBottomSheet;
import com.labnex.app.databinding.ActivityGroupsBinding;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.GroupsViewModel;

/**
 * @author mmarif
 */
public class GroupsActivity extends BaseActivity implements BottomSheetListener {

	private ActivityGroupsBinding binding;
	private GroupsViewModel groupsViewModel;
	private GroupsAdapter adapter;
	private int page = 1;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityGroupsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		groupsViewModel = new ViewModelProvider(this).get(GroupsViewModel.class);

		resultLimit = getAccount().getMaxPageLimit();

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		Bundle bsBundle = new Bundle();

		binding.newGroup.setOnClickListener(
				accounts -> {
					if (getAccount().getAccount().getInstanceUrl().contains("gitlab.com")) {
						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(ctx)
										.setTitle(getString(R.string.gitlab_saas))
										.setMessage(
												getString(R.string.gitlab_saas_message)
														+ "\n\n"
														+ getString(
																R.string.gitlab_saas_message_extra))
										.setNegativeButton(getString(R.string.close), null);

						materialAlertDialogBuilder.create().show();
					} else {
						bsBundle.putString("source", "new");
						GroupsBottomSheet bottomSheet = new GroupsBottomSheet();
						bottomSheet.setArguments(bsBundle);
						bottomSheet.show(getSupportFragmentManager(), "groupsBottomSheet");
					}
				});

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											binding.pullToRefresh.setRefreshing(false);
											fetchDataAsync();
											binding.progressBar.setVisibility(View.VISIBLE);
										},
										250));

		fetchDataAsync();
	}

	private void fetchDataAsync() {

		groupsViewModel
				.getGroups(ctx, resultLimit, page, GroupsActivity.this, binding)
				.observe(
						GroupsActivity.this,
						orgListMain -> {
							adapter = new GroupsAdapter(GroupsActivity.this, orgListMain);
							adapter.setLoadMoreListener(
									new GroupsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											groupsViewModel.loadMoreGroups(
													ctx,
													resultLimit,
													page,
													adapter,
													GroupsActivity.this,
													binding);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {

								binding.recyclerView.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {

								adapter.notifyDataChanged();
								binding.recyclerView.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	@Override
	public void onButtonClicked(String text) {}
}
