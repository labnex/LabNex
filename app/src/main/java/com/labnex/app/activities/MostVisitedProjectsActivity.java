package com.labnex.app.activities;

import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.MostVisitedAdapter;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.ProjectsApi;
import com.labnex.app.databinding.ActivityMostVisitedProjectsBinding;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import java.util.ArrayList;

/**
 * @author mmarif
 */
public class MostVisitedProjectsActivity extends BaseActivity {

	private ActivityMostVisitedProjectsBinding binding;
	private MostVisitedAdapter adapter;
	private ProjectsApi projectsApi;
	private int accountId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMostVisitedProjectsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, binding.recyclerView, null, null);

		accountId = SharedPrefDB.getInstance(ctx).getInt("currentActiveAccountId");
		projectsApi = BaseApi.getInstance(ctx, ProjectsApi.class);

		binding.btnBack.setOnClickListener(v -> finish());

		binding.btnDeleteAll.setOnClickListener(
				v ->
						new MaterialAlertDialogBuilder(ctx)
								.setTitle(R.string.remove_all_projects)
								.setMessage(R.string.remove_all_projects_message)
								.setNeutralButton(R.string.cancel, null)
								.setPositiveButton(
										R.string.remove,
										(dialog, which) -> {
											if (projectsApi != null) {
												projectsApi.deleteProjectsByAccount(accountId);
												adapter.updateList(new ArrayList<>());
												showEmptyState();
												Toasty.show(
														ctx,
														getString(R.string.all_projects_removed));
											}
										})
								.show());

		adapter =
				new MostVisitedAdapter(
						ctx,
						new ArrayList<>(),
						(project, position) -> {
							if (projectsApi != null) {
								projectsApi.deleteProject(project.getProjectId());
								adapter.removeItem(position);
								showEmptyState();
								Toasty.show(ctx, getString(R.string.project_removed));
							}
						});

		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		binding.recyclerView.setAdapter(adapter);

		loadData();
	}

	private void loadData() {
		if (projectsApi == null) return;

		projectsApi
				.fetchAllMostVisited(accountId)
				.observe(
						this,
						projects -> {
							if (projects == null || projects.isEmpty()) {
								showEmptyState();
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
								adapter.updateList(projects);
							}
						});
	}

	private void showEmptyState() {
		if (adapter.getItemCount() == 0) {
			binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		}
	}
}
