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
import com.labnex.app.adapters.LabelsAdapter;
import com.labnex.app.databinding.BottomsheetLabelsBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.viewmodels.LabelsViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class LabelsBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetLabelsBinding binding;
	private LabelsViewModel viewModel;
	private LabelsAdapter adapter;
	private String type;
	private long id;
	private boolean canModify;

	public static LabelsBottomSheet newInstance(String type, long id, boolean canModify) {
		LabelsBottomSheet sheet = new LabelsBottomSheet();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putLong("id", id);
		args.putBoolean("canModify", canModify);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			type = getArguments().getString("type", "project");
			id = getArguments().getLong("id", 0);
			canModify = getArguments().getBoolean("canModify");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetLabelsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(LabelsViewModel.class);

		setupRecyclerView();
		observeViewModel();
		viewModel.loadLabels(requireContext(), type, id);

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter =
				new LabelsAdapter(
						requireContext(),
						new ArrayList<>(),
						label -> {
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
											label.getName(), null, items);
							sheet.setOnMenuItemClickListener(
									menuId -> {
										switch (menuId) {
											case "edit":
												CreateLabelBottomSheet.newInstance(type, id, label)
														.show(
																getParentFragmentManager(),
																"editLabelSheet");
												break;
											case "delete":
												new MaterialAlertDialogBuilder(requireContext())
														.setTitle(
																getString(
																		R.string
																				.delete_dialog_title,
																		label.getName()))
														.setMessage(
																R.string
																		.delete_label_dialog_message)
														.setPositiveButton(
																R.string.delete,
																(dialog, which) ->
																		viewModel.deleteLabel(
																				requireContext(),
																				type,
																				id,
																				label.getId()))
														.setNeutralButton(R.string.cancel, null)
														.show();
												break;
										}
									});
							sheet.show(getParentFragmentManager(), "labelMenuSheet");
						});

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.labelsList.setLayoutManager(layoutManager);
		binding.labelsList.setAdapter(adapter);

		adapter.setCanModify(canModify);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(requireContext());
					}
				};
		binding.labelsList.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.progressBar.setVisibility(
									Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getLabelList()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;
							if (list == null || list.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.labelsList.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.labelsList.setVisibility(View.VISIBLE);
								adapter.updateList(list);
							}
						});

		viewModel
				.getDeleteSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(requireContext(), R.string.label_deleted);
								viewModel.clearDeleteSuccess();
							}
						});

		viewModel
				.getActionSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								viewModel.loadLabels(requireContext(), type, id);
								viewModel.clearActionSuccess();
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
