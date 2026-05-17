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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.adapters.ReleasesAdapter;
import com.labnex.app.databinding.BottomsheetReleasesBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.viewmodels.ReleasesViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class ReleasesBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetReleasesBinding binding;
	private ReleasesViewModel viewModel;
	private ReleasesAdapter adapter;
	private long projectId;
	private boolean canModify;

	public static ReleasesBottomSheet newInstance(long projectId, boolean canModify) {
		ReleasesBottomSheet sheet = new ReleasesBottomSheet();
		Bundle args = new Bundle();
		args.putLong("projectId", projectId);
		args.putBoolean("canModify", canModify);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			projectId = getArguments().getLong("projectId", 0);
			canModify = getArguments().getBoolean("canModify");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetReleasesBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(ReleasesViewModel.class);

		setupRecyclerView();
		observeViewModel();
		viewModel.loadReleases(requireContext(), projectId);

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter =
				new ReleasesAdapter(
						requireContext(),
						new ArrayList<>(),
						release -> {
							List<GenericMenuItemModel> items = new ArrayList<>();
							items.add(
									new GenericMenuItemModel(
											"edit",
											R.string.edit,
											R.drawable.ic_edit,
											com.google.android.material.R.attr
													.colorPrimaryContainer,
											com.google.android.material.R.attr
													.colorOnPrimaryContainer));
							items.add(
									new GenericMenuItemModel(
											"delete",
											R.string.delete,
											R.drawable.ic_trash,
											com.google.android.material.R.attr.colorErrorContainer,
											com.google.android.material.R.attr
													.colorOnErrorContainer));

							GenericMenuBottomSheet sheet =
									GenericMenuBottomSheet.newInstance(
											release.getName(), release.getTagName(), items);
							sheet.setOnMenuItemClickListener(
									id -> {
										switch (id) {
											case "edit":
												CreateReleaseBottomSheet.newInstance(
																projectId, release)
														.show(
																getParentFragmentManager(),
																"editReleaseSheet");
												break;
											case "delete":
												new MaterialAlertDialogBuilder(requireContext())
														.setTitle(
																getString(
																		R.string
																				.delete_dialog_title,
																		release.getTagName()))
														.setMessage(R.string.delete_release_message)
														.setPositiveButton(
																R.string.delete,
																(dialog, which) ->
																		viewModel.deleteRelease(
																				requireContext(),
																				projectId,
																				release
																						.getTagName()))
														.setNeutralButton(R.string.cancel, null)
														.show();
												break;
										}
									});
							sheet.show(getParentFragmentManager(), "releaseMenuSheet");
						});

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.releasesList.setLayoutManager(layoutManager);
		binding.releasesList.setAdapter(adapter);

		adapter.setCanModify(canModify);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(requireContext());
					}
				};
		binding.releasesList.addOnScrollListener(scrollListener);
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
				.getReleaseList()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;
							if (list == null || list.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.releasesList.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.releasesList.setVisibility(View.VISIBLE);
								adapter.updateList(list);
							}
						});

		viewModel
				.getActionSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								viewModel.loadReleases(requireContext(), projectId);
								viewModel.clearActionSuccess();
							}
						});

		viewModel
				.getDeleteSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(requireContext(), R.string.release_deleted);
								viewModel.clearDeleteSuccess();
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
