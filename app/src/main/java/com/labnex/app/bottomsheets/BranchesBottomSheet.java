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
import com.labnex.app.adapters.BranchesAdapter;
import com.labnex.app.databinding.BottomSheetBranchesBinding;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.BranchesViewModel;

/**
 * @author mmarif
 */
public class BranchesBottomSheet extends BottomSheetDialogFragment
		implements BranchesAdapter.BranchesAdapterListener {

	private BottomSheetBranchesBinding bottomSheetBranchesBinding;
	private BranchesViewModel branchesViewModel;
	private BranchesAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private int projectId;
	private String type;
	private String source;

	// project detail interface
	private static UpdateInterface UpdateInterface;

	public interface UpdateInterface {
		void updateDataListener(String str, String type);
	}

	public static void setUpdateListener(UpdateInterface updateInterface) {
		UpdateInterface = updateInterface;
	}

	// create mr interface
	private static MrUpdateInterface MrUpdateInterface;

	public interface MrUpdateInterface {
		void mrUpdateDataListener(String str, String type);
	}

	public static void setMrUpdateListener(MrUpdateInterface mrUpdateInterface) {
		MrUpdateInterface = mrUpdateInterface;
	}

	// create file interface
	private static CreateFileUpdateInterface CreateFileUpdateInterface;

	public interface CreateFileUpdateInterface {
		void createFileUpdateDataListener(String str, String type);
	}

	public static void setCreateFileUpdateListener(
			CreateFileUpdateInterface createFileUpdateInterface) {
		CreateFileUpdateInterface = createFileUpdateInterface;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetBranchesBinding = BottomSheetBranchesBinding.inflate(inflater, container, false);

		branchesViewModel = new ViewModelProvider(this).get(BranchesViewModel.class);

		projectId = requireArguments().getInt("projectId", 0);
		type = requireArguments().getString("type");
		source = requireArguments().getString("source");
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();

		bottomSheetBranchesBinding.closeBs.setOnClickListener(close -> dismiss());

		bottomSheetBranchesBinding.getRoot().setVisibility(View.VISIBLE);

		bottomSheetBranchesBinding.rvList.setHasFixedSize(true);
		bottomSheetBranchesBinding.rvList.setLayoutManager(new LinearLayoutManager(getContext()));
		fetchBranches();

		return bottomSheetBranchesBinding.getRoot();
	}

	@Override
	public void onClickItem(String branch) {

		if (source.equalsIgnoreCase("project_detail")) {
			UpdateInterface.updateDataListener(branch, type);
		} else if (source.equalsIgnoreCase("create_mr")) {
			MrUpdateInterface.mrUpdateDataListener(branch, type);
		} else if (source.equalsIgnoreCase("create_file")) {
			CreateFileUpdateInterface.createFileUpdateDataListener(branch, type);
		}
		dismiss();
	}

	public void fetchBranches() {

		bottomSheetBranchesBinding.progressBar.setVisibility(View.VISIBLE);

		branchesViewModel
				.getBranches(
						getContext(),
						projectId,
						resultLimit,
						page,
						getActivity(),
						bottomSheetBranchesBinding)
				.observe(
						this,
						listMain -> {
							adapter =
									new BranchesAdapter(
											getContext(),
											listMain,
											projectId,
											bottomSheetBranchesBinding,
											this);
							adapter.setLoadMoreListener(
									new BranchesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											branchesViewModel.loadMore(
													getContext(),
													projectId,
													resultLimit,
													page,
													adapter,
													getActivity(),
													bottomSheetBranchesBinding);
											bottomSheetBranchesBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											bottomSheetBranchesBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {

								bottomSheetBranchesBinding.rvList.setAdapter(adapter);
								bottomSheetBranchesBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.GONE);
							} else {

								adapter.notifyDataChanged();
								bottomSheetBranchesBinding.rvList.setAdapter(adapter);
								bottomSheetBranchesBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.VISIBLE);
							}

							bottomSheetBranchesBinding.progressBar.setVisibility(View.GONE);
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
