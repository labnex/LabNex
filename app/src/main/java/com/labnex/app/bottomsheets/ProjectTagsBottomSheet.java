package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.activities.BaseActivity;
import com.labnex.app.adapters.ProjectTagsAdapter;
import com.labnex.app.databinding.BottomSheetProjectTagsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.viewmodels.TagsViewModel;

/**
 * @author mmarif
 */
public class ProjectTagsBottomSheet extends BottomSheetDialogFragment
		implements TagActionsBottomSheet.UpdateInterface {

	private BottomSheetProjectTagsBinding binding;
	private TagsViewModel tagsViewModel;
	private ProjectTagsAdapter adapter;
	private int page = 1;
	private int projectId;
	private int resultLimit;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetProjectTagsBinding.inflate(inflater, container, false);

		tagsViewModel = new ViewModelProvider(this).get(TagsViewModel.class);
		projectId = requireArguments().getInt("projectId", 0);
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();

		TagActionsBottomSheet.setUpdateListener(this);

		binding.closeBs.setOnClickListener(close -> dismiss());

		binding.getRoot().setVisibility(View.VISIBLE);

		binding.tagsList.setHasFixedSize(true);
		binding.tagsList.setLayoutManager(new LinearLayoutManager(getContext()));
		fetchProjectTags();

		binding.createNew.setOnClickListener(
				v -> {
					Bundle bsBundle = new Bundle();
					bsBundle.putInt("projectId", projectId);
					TagActionsBottomSheet bottomSheet = new TagActionsBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getParentFragmentManager(), "tagActionsBottomSheet");
				});

		return binding.getRoot();
	}

	@Override
	public void updateDataListener(String str) {
		if (str.equalsIgnoreCase("created")) {
			Snackbar.info(requireContext(), binding.tagsLayout, getString(R.string.tag_created));
		}
		adapter.clearAdapter();
		page = 1;
		fetchProjectTags();
	}

	public void fetchProjectTags() {
		binding.progressBar.setVisibility(View.VISIBLE);

		tagsViewModel
				.getProjectTags(getContext(), projectId, resultLimit, page, getActivity(), binding)
				.observe(
						this,
						tags -> {
							if (adapter != null) {
								adapter.clearAdapter();
							}
							adapter =
									new ProjectTagsAdapter(getContext(), tags, projectId, binding);
							adapter.setLoadMoreListener(
									new ProjectTagsAdapter.OnLoadMoreListener() {
										@Override
										public void onLoadMore() {
											page += 1;
											tagsViewModel.loadMoreTags(
													getContext(),
													projectId,
													resultLimit,
													page,
													adapter,
													getActivity(),
													binding);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {
											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								binding.tagsList.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								binding.tagsList.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_project_tags);

		dialog.setOnShowListener(
				dialogInterface -> {
					BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
					View bottomSheet =
							bottomSheetDialog.findViewById(
									com.google.android.material.R.id.design_bottom_sheet);

					if (bottomSheet != null) {
						BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
						behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
						behavior.setPeekHeight(bottomSheet.getHeight());
						behavior.setHideable(true);
						behavior.setSkipCollapsed(true);
					}
				});

		if (dialog.getWindow() != null) {
			WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
			params.height = WindowManager.LayoutParams.MATCH_PARENT;
			dialog.getWindow().setAttributes(params);
		}

		return dialog;
	}
}
