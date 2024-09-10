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
import com.labnex.app.adapters.ProjectLabelsAdapter;
import com.labnex.app.databinding.BottomSheetProjectLabelsBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.LabelsViewModel;

/**
 * @author mmarif
 */
public class ProjectLabelsBottomSheet extends BottomSheetDialogFragment
		implements LabelActionsBottomSheet.UpdateInterface {

	private BottomSheetProjectLabelsBinding bottomSheetProjectLabelsBinding;
	private LabelsViewModel labelsViewModel;
	private ProjectLabelsAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private int projectId;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetProjectLabelsBinding =
				BottomSheetProjectLabelsBinding.inflate(inflater, container, false);

		labelsViewModel = new ViewModelProvider(this).get(LabelsViewModel.class);

		projectId = requireArguments().getInt("projectId", 0);
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();

		bottomSheetProjectLabelsBinding.closeBs.setOnClickListener(close -> dismiss());

		LabelActionsBottomSheet.setUpdateListener(this);

		bottomSheetProjectLabelsBinding.getRoot().setVisibility(View.VISIBLE);

		bottomSheetProjectLabelsBinding.createNewLabel.setOnClickListener(
				v1 -> {
					Bundle bsBundle = new Bundle();
					bsBundle.putString("type", "project");
					bsBundle.putString("source", "labels");
					bsBundle.putInt("projectId", projectId);
					LabelActionsBottomSheet bottomSheet = new LabelActionsBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getParentFragmentManager(), "labelActionsBottomSheet");
				});

		bottomSheetProjectLabelsBinding.labelsList.setHasFixedSize(true);
		bottomSheetProjectLabelsBinding.labelsList.setLayoutManager(
				new LinearLayoutManager(getContext()));
		fetchProjectLabels();

		return bottomSheetProjectLabelsBinding.getRoot();
	}

	@Override
	public void updateDataListener(String str) {

		if (str.equalsIgnoreCase("created")) {
			Snackbar.info(
					requireContext(),
					bottomSheetProjectLabelsBinding.labelsLayout,
					getString(R.string.label_created));
		}
		if (str.equalsIgnoreCase("updated")) {
			Snackbar.info(
					requireContext(),
					bottomSheetProjectLabelsBinding.labelsLayout,
					getString(R.string.label_updated));
		}

		adapter.clearAdapter();
		page = 1;
		fetchProjectLabels();
	}

	public void fetchProjectLabels() {

		bottomSheetProjectLabelsBinding.progressBar.setVisibility(View.VISIBLE);

		labelsViewModel
				.getProjectLabels(
						getContext(),
						projectId,
						resultLimit,
						page,
						getActivity(),
						bottomSheetProjectLabelsBinding)
				.observe(
						this,
						listMain -> {
							adapter =
									new ProjectLabelsAdapter(
											getContext(),
											listMain,
											projectId,
											bottomSheetProjectLabelsBinding);
							adapter.setLoadMoreListener(
									new ProjectLabelsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											labelsViewModel.loadMoreProjectLabels(
													getContext(),
													projectId,
													resultLimit,
													page,
													adapter,
													getActivity(),
													bottomSheetProjectLabelsBinding);
											bottomSheetProjectLabelsBinding.progressBar
													.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											bottomSheetProjectLabelsBinding.progressBar
													.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {

								bottomSheetProjectLabelsBinding.labelsList.setAdapter(adapter);
								bottomSheetProjectLabelsBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.GONE);
							} else {

								adapter.notifyDataChanged();
								bottomSheetProjectLabelsBinding.labelsList.setAdapter(adapter);
								bottomSheetProjectLabelsBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.VISIBLE);
							}

							bottomSheetProjectLabelsBinding.progressBar.setVisibility(View.GONE);
						});
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_project_labels);

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
