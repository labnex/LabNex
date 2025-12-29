package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.content.Context;
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
import com.labnex.app.adapters.ProjectReleasesAdapter;
import com.labnex.app.databinding.BottomSheetProjectReleasesBinding;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.ReleasesViewModel;

/**
 * @author mmarif
 */
public class ProjectReleasesBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetProjectReleasesBinding bottomSheetProjectReleasesBinding;
	private ReleasesViewModel releasesViewModel;
	private ProjectReleasesAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private int projectId;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetProjectReleasesBinding =
				BottomSheetProjectReleasesBinding.inflate(inflater, container, false);

		releasesViewModel = new ViewModelProvider(this).get(ReleasesViewModel.class);

		projectId = requireArguments().getInt("projectId", 0);
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();

		bottomSheetProjectReleasesBinding.closeBs.setOnClickListener(close -> dismiss());
		bottomSheetProjectReleasesBinding.closeBs.setOnClickListener(close -> dismiss());

		bottomSheetProjectReleasesBinding.recyclerView.setHasFixedSize(true);
		bottomSheetProjectReleasesBinding.recyclerView.setLayoutManager(
				new LinearLayoutManager(getContext()));
		fetchProjectReleases();

		return bottomSheetProjectReleasesBinding.getRoot();
	}

	public void fetchProjectReleases() {

		bottomSheetProjectReleasesBinding.progressBar.setVisibility(View.VISIBLE);

		releasesViewModel
				.getReleases(
						getContext(),
						projectId,
						resultLimit,
						page,
						getActivity(),
						bottomSheetProjectReleasesBinding)
				.observe(
						this,
						listMain -> {
							adapter =
									new ProjectReleasesAdapter(
											getContext(),
											listMain,
											projectId,
											bottomSheetProjectReleasesBinding);
							adapter.setLoadMoreListener(
									new ProjectReleasesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											releasesViewModel.loadMore(
													getContext(),
													projectId,
													resultLimit,
													page,
													adapter,
													getActivity(),
													bottomSheetProjectReleasesBinding);
											bottomSheetProjectReleasesBinding.progressBar
													.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											bottomSheetProjectReleasesBinding.progressBar
													.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {

								bottomSheetProjectReleasesBinding.recyclerView.setAdapter(adapter);
								bottomSheetProjectReleasesBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.GONE);
							} else {

								bottomSheetProjectReleasesBinding.recyclerView.setAdapter(adapter);
								bottomSheetProjectReleasesBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.VISIBLE);
							}

							bottomSheetProjectReleasesBinding.progressBar.setVisibility(View.GONE);
						});
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_project_releases);

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

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		try {
			BottomSheetListener bottomSheetListener = (BottomSheetListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}
}
