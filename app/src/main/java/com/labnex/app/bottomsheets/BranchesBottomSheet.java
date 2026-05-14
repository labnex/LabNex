package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.adapters.BranchesAdapter;
import com.labnex.app.databinding.BottomsheetBranchesBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.BranchesViewModel;
import java.util.ArrayList;

/**
 * @author mmarif
 */
public class BranchesBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetBranchesBinding binding;
	private BranchesViewModel viewModel;
	private BranchesAdapter adapter;
	private long projectId;

	public interface OnBranchSelectedListener {
		void onBranchSelected(String branch);
	}

	public static BranchesBottomSheet newInstance(long projectId) {
		BranchesBottomSheet sheet = new BranchesBottomSheet();
		Bundle args = new Bundle();
		args.putLong("projectId", projectId);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			projectId = getArguments().getLong("projectId", 0);
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetBranchesBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(BranchesViewModel.class);

		setupRecyclerView();
		observeViewModel();
		viewModel.loadBranches(requireContext(), projectId);

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter =
				new BranchesAdapter(
						requireContext(),
						new ArrayList<>(),
						branch -> {
							OnBranchSelectedListener parent =
									(OnBranchSelectedListener) getParentFragment();
							if (parent != null) {
								parent.onBranchSelected(branch);
							} else if (requireActivity() instanceof OnBranchSelectedListener) {
								((OnBranchSelectedListener) requireActivity())
										.onBranchSelected(branch);
							}
							dismiss();
						});

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.branchesList.setLayoutManager(layoutManager);
		binding.branchesList.setAdapter(adapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(requireContext());
					}
				};
		binding.branchesList.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading ->
								binding.progressBar.setVisibility(
										Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE));

		viewModel
				.getBranchList()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;
							if (list == null || list.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.branchesList.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.branchesList.setVisibility(View.VISIBLE);
								adapter.updateList(list);
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg == null) return;
							switch (errorMsg) {
								case "auth_error":
									Toasty.show(
											requireContext(), getString(R.string.not_authorized));
									break;
								case "access_forbidden_403":
									Toasty.show(
											requireContext(),
											getString(R.string.access_forbidden_403));
									break;
								case "not_found":
									Toasty.show(requireContext(), getString(R.string.not_found));
									break;
								case "generic_error":
									Toasty.show(
											requireContext(), getString(R.string.generic_error));
									break;
								default:
									Toasty.show(requireContext(), errorMsg);
									break;
							}
							viewModel.clearError();
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applyFullScreenSheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
