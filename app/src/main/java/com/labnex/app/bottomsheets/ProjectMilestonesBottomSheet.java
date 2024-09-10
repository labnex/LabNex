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
import com.labnex.app.adapters.ProjectMilestonesAdapter;
import com.labnex.app.databinding.BottomSheetProjectMilestonesBinding;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.MilestonesViewModel;

/**
 * @author mmarif
 */
public class ProjectMilestonesBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetProjectMilestonesBinding bottomSheetProjectMilestonesBinding;
	private MilestonesViewModel milestonesViewModel;
	private ProjectMilestonesAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private int projectId;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetProjectMilestonesBinding =
				BottomSheetProjectMilestonesBinding.inflate(inflater, container, false);

		milestonesViewModel = new ViewModelProvider(this).get(MilestonesViewModel.class);

		projectId = requireArguments().getInt("projectId", 0);
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();

		bottomSheetProjectMilestonesBinding.closeBs.setOnClickListener(close -> dismiss());
		bottomSheetProjectMilestonesBinding.closeBs.setOnClickListener(close -> dismiss());

		bottomSheetProjectMilestonesBinding.recyclerView.setHasFixedSize(true);
		bottomSheetProjectMilestonesBinding.recyclerView.setLayoutManager(
				new LinearLayoutManager(getContext()));
		fetchProjectMilestones();

		return bottomSheetProjectMilestonesBinding.getRoot();
	}

	public void fetchProjectMilestones() {

		bottomSheetProjectMilestonesBinding.progressBar.setVisibility(View.VISIBLE);

		milestonesViewModel
				.getMilestones(
						getContext(),
						projectId,
						resultLimit,
						page,
						getActivity(),
						bottomSheetProjectMilestonesBinding)
				.observe(
						this,
						listMain -> {
							adapter =
									new ProjectMilestonesAdapter(
											getContext(),
											listMain,
											projectId,
											bottomSheetProjectMilestonesBinding);
							adapter.setLoadMoreListener(
									new ProjectMilestonesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											milestonesViewModel.loadMore(
													getContext(),
													projectId,
													resultLimit,
													page,
													adapter,
													getActivity(),
													bottomSheetProjectMilestonesBinding);
											bottomSheetProjectMilestonesBinding.progressBar
													.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											bottomSheetProjectMilestonesBinding.progressBar
													.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {

								bottomSheetProjectMilestonesBinding.recyclerView.setAdapter(
										adapter);
								bottomSheetProjectMilestonesBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.GONE);
							} else {

								bottomSheetProjectMilestonesBinding.recyclerView.setAdapter(
										adapter);
								bottomSheetProjectMilestonesBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.VISIBLE);
							}

							bottomSheetProjectMilestonesBinding.progressBar.setVisibility(
									View.GONE);
						});
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_project_milestones);

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
						behavior.setHideable(false);
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
