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
import com.labnex.app.adapters.TagsAdapter;
import com.labnex.app.databinding.BottomsheetTagsBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.viewmodels.TagsViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class TagsBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetTagsBinding binding;
	private TagsViewModel viewModel;
	private TagsAdapter adapter;
	private long projectId;

	public static TagsBottomSheet newInstance(long projectId) {
		TagsBottomSheet sheet = new TagsBottomSheet();
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
		binding = BottomsheetTagsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(TagsViewModel.class);

		setupRecyclerView();
		observeViewModel();
		viewModel.loadTags(requireContext(), projectId);

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter =
				new TagsAdapter(
						requireContext(),
						new ArrayList<>(),
						tag -> {
							List<GenericMenuItemModel> items = new ArrayList<>();
							items.add(
									new GenericMenuItemModel(
											"delete",
											R.string.delete,
											R.drawable.ic_trash,
											com.google.android.material.R.attr.colorErrorContainer,
											com.google.android.material.R.attr
													.colorOnErrorContainer));

							GenericMenuBottomSheet sheet =
									GenericMenuBottomSheet.newInstance(tag.getName(), null, items);
							sheet.setOnMenuItemClickListener(
									menuId -> {
										if ("delete".equals(menuId)) {
											new MaterialAlertDialogBuilder(requireContext())
													.setTitle(R.string.delete_tag)
													.setMessage(
															getString(
																	R.string
																			.delete_tag_confirmation,
																	tag.getName()))
													.setPositiveButton(
															R.string.delete,
															(dialog, which) ->
																	viewModel.deleteTag(
																			requireContext(),
																			projectId,
																			tag.getName()))
													.setNegativeButton(R.string.cancel, null)
													.show();
										}
									});
							sheet.show(getParentFragmentManager(), "tagMenuSheet");
						});

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.tagsList.setLayoutManager(layoutManager);
		binding.tagsList.setAdapter(adapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(requireContext());
					}
				};
		binding.tagsList.addOnScrollListener(scrollListener);
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
				.getTagList()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;
							if (list == null || list.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.tagsList.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.tagsList.setVisibility(View.VISIBLE);
								adapter.updateList(list);
							}
						});

		viewModel
				.getActionSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								viewModel.loadTags(requireContext(), projectId);
								viewModel.clearActionSuccess();
							}
						});

		viewModel
				.getDeleteSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(requireContext(), R.string.tag_deleted);
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
