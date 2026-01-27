package com.labnex.app.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.labnex.app.R;
import com.labnex.app.adapters.ProjectsAdapter;
import com.labnex.app.bottomsheets.GroupDetailBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.ActivityGroupDetailsBinding;
import com.labnex.app.databinding.BottomSheetGroupActionsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.TextDrawable.ColorGenerator;
import com.labnex.app.helpers.TextDrawable.TextDrawable;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.groups.GroupsItem;
import com.labnex.app.viewmodels.ProjectsViewModel;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class GroupDetailActivity extends BaseActivity implements BottomSheetListener {

	private ActivityGroupDetailsBinding binding;
	private int groupId;
	private ProjectsViewModel projectsViewModel;
	private ProjectsAdapter projectsAdapter;
	private final int page = 1;
	private int resultLimit;
	private BottomSheetGroupActionsBinding sheetBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityGroupDetailsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		resultLimit = getAccount().getMaxPageLimit();
		projectsViewModel = new ViewModelProvider(this).get(ProjectsViewModel.class);

		groupId = getIntent().getIntExtra("groupId", 0);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

		binding.bottomAppBar.setNavigationOnClickListener(bottomAppBar -> finish());

		setupNavigationClickListener(binding.issuesMainFrame, IssuesActivity.class, groupId);

		setupNavigationClickListener(
				binding.mergeRequestsMainFrame, MergeRequestsActivity.class, groupId);

		binding.bottomAppBar.setOnMenuItemClickListener(
				menuItem -> {
					if (menuItem.getItemId() == R.id.menu) {
						showGroupActionsBottomSheet();
						return true;
					}
					return false;
				});

		getGroupDetails();
		getGroupProjects();
	}

	private void setupNavigationClickListener(View view, Class<?> targetActivity, int id) {
		view.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, targetActivity);
					intent.putExtra("source", "group");
					intent.putExtra("id", id);
					ctx.startActivity(intent);
				});
	}

	private void showGroupActionsBottomSheet() {

		sheetBinding =
				BottomSheetGroupActionsBinding.inflate(LayoutInflater.from(this), null, false);
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		sheetBinding.labelsItem.setOnClickListener(
				v -> {
					Bundle bsBundle = new Bundle();
					bsBundle.putString("source", "labels");
					bsBundle.putInt("groupId", groupId);
					GroupDetailBottomSheet bottomSheet = new GroupDetailBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "groupDetailBottomSheet");
					bottomSheetDialog.dismiss();
				});

		sheetBinding.membersItem.setOnClickListener(
				v -> {
					Bundle bsBundle = new Bundle();
					bsBundle.putString("source", "members");
					bsBundle.putInt("groupId", groupId);
					GroupDetailBottomSheet bottomSheet = new GroupDetailBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getSupportFragmentManager(), "groupDetailBottomSheet");
					bottomSheetDialog.dismiss();
				});

		bottomSheetDialog.show();
	}

	private void getGroupDetails() {

		Call<GroupsItem> call = RetrofitClient.getApiInterface(ctx).getGroup(groupId);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<GroupsItem> call,
							@NonNull retrofit2.Response<GroupsItem> response) {

						GroupsItem groupDetails = response.body();

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								assert groupDetails != null;

								ColorGenerator generator = ColorGenerator.MATERIAL;
								int color = generator.getColor(groupDetails.getName());
								String firstCharacter =
										String.valueOf(groupDetails.getName().charAt(0));

								TextDrawable drawable =
										TextDrawable.builder()
												.beginConfig()
												.useFont(Typeface.DEFAULT)
												.fontSize(16)
												.toUpperCase()
												.width(28)
												.height(28)
												.endConfig()
												.buildRoundRect(firstCharacter, color, 8);

								binding.infoFrame.setVisibility(View.VISIBLE);
								binding.progressBar.setVisibility(View.GONE);

								binding.groupName.setText(groupDetails.getName());
								binding.groupPath.setText(groupDetails.getFullPath());

								if (!groupDetails.getDescription().isEmpty()) {
									binding.groupDescription.setVisibility(View.VISIBLE);
									binding.groupDescription.setText(groupDetails.getDescription());
								}

								if (!groupDetails.getVisibility().isEmpty()) {
									binding.visibilityFrame.setVisibility(View.VISIBLE);
									binding.groupVisibility.setText(
											groupDetails.getVisibility().toUpperCase());
								}

								if (groupDetails.getAvatarUrl() != null) {

									Glide.with(ctx)
											.load(groupDetails.getAvatarUrl())
											.diskCacheStrategy(DiskCacheStrategy.ALL)
											.placeholder(R.drawable.ic_spinner)
											.centerCrop()
											.into(binding.groupAvatar);
								} else {
									binding.groupAvatar.setImageDrawable(drawable);
								}
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<GroupsItem> call, @NonNull Throwable t) {

						binding.progressBar.setVisibility(View.GONE);
						Snackbar.info(
								ctx,
								findViewById(R.id.content),
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void getGroupProjects() {

		projectsViewModel
				.getProjects(
						ctx,
						"group",
						"single",
						groupId,
						resultLimit,
						page,
						GroupDetailActivity.this,
						binding.bottomAppBar)
				.observe(
						GroupDetailActivity.this,
						projectsListMain -> {
							projectsAdapter =
									new ProjectsAdapter(
											GroupDetailActivity.this, projectsListMain, "");

							if (projectsAdapter.getItemCount() > 0) {

								binding.groupProjectsFrame.setVisibility(View.VISIBLE);
								binding.recyclerView.setAdapter(projectsAdapter);
							} else {

								binding.recyclerView.setAdapter(projectsAdapter);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	@Override
	public void onButtonClicked(String text) {}
}
