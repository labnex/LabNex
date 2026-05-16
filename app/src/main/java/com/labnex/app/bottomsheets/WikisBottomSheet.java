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
import com.labnex.app.adapters.WikisAdapter;
import com.labnex.app.databinding.BottomsheetWikisBinding;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.app.GenericMenuItemModel;
import com.labnex.app.models.wikis.Wiki;
import com.labnex.app.viewmodels.WikisViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mmarif
 */
public class WikisBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetWikisBinding binding;
	private WikisViewModel viewModel;
	private WikisAdapter adapter;
	private String type;
	private long id;
	private String webUrl;

	public static WikisBottomSheet newInstance(String type, long id, String webUrl) {
		WikisBottomSheet sheet = new WikisBottomSheet();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putLong("id", id);
		args.putString("webUrl", webUrl);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			type = getArguments().getString("type", "project");
			id = getArguments().getLong("id", 0);
			webUrl = getArguments().getString("webUrl", "");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetWikisBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(WikisViewModel.class);

		setupRecyclerView();
		observeViewModel();
		viewModel.loadWikis(requireContext(), type, id);

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter =
				new WikisAdapter(
						requireContext(),
						new ArrayList<>(),
						new WikisAdapter.OnWikiClickListener() {
							@Override
							public void onWikiClick(Wiki wiki) {
								Map<String, String> meta = new HashMap<>();
								if (webUrl != null && !webUrl.isEmpty()) {
									meta.put("URL", webUrl + "/-/wikis/" + wiki.getSlug());
								}

								ContentViewerBottomSheet viewer =
										ContentViewerBottomSheet.newInstance(
												wiki.getContent(),
												null,
												wiki.getTitle(),
												null,
												null,
												meta,
												ContentViewerBottomSheet.Feature.SHOW_TITLE,
												ContentViewerBottomSheet.Feature.MARKDOWN_PREVIEW,
												ContentViewerBottomSheet.Feature.START_IN_MARKDOWN,
												ContentViewerBottomSheet.Feature.ALLOW_COPY,
												ContentViewerBottomSheet.Feature.ALLOW_SHARE);
								viewer.show(getParentFragmentManager(), "wikiViewer");
							}

							@Override
							public void onMenuClick(Wiki wiki) {
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
												com.google.android.material.R.attr
														.colorErrorContainer,
												com.google.android.material.R.attr
														.colorOnErrorContainer));

								GenericMenuBottomSheet sheet =
										GenericMenuBottomSheet.newInstance(
												wiki.getTitle(), null, items);
								sheet.setOnMenuItemClickListener(
										menuId -> {
											switch (menuId) {
												case "edit":
													CreateWikiBottomSheet.newInstance(
																	type, id, wiki)
															.show(
																	getParentFragmentManager(),
																	"editWikiSheet");
													break;
												case "delete":
													new MaterialAlertDialogBuilder(requireContext())
															.setTitle(
																	getString(
																			R.string
																					.delete_dialog_title,
																			wiki.getTitle()))
															.setMessage(
																	R.string
																			.delete_wiki_dialog_message)
															.setPositiveButton(
																	R.string.delete,
																	(dialog, which) ->
																			viewModel.deleteWiki(
																					requireContext(),
																					type,
																					id,
																					wiki.getSlug()))
															.setNeutralButton(R.string.cancel, null)
															.show();
													break;
											}
										});
								sheet.show(getParentFragmentManager(), "wikiMenuSheet");
							}
						});

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.wikisList.setLayoutManager(layoutManager);
		binding.wikisList.setAdapter(adapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(requireContext());
					}
				};
		binding.wikisList.addOnScrollListener(scrollListener);
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
				.getWikiList()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;
							if (list == null || list.isEmpty()) {
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
								binding.wikisList.setVisibility(View.GONE);
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.wikisList.setVisibility(View.VISIBLE);
								adapter.updateList(list);
							}
						});

		viewModel
				.getActionSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								viewModel.loadWikis(requireContext(), type, id);
								viewModel.clearActionSuccess();
							}
						});

		viewModel
				.getDeleteSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (Boolean.TRUE.equals(success)) {
								Toasty.show(requireContext(), R.string.wiki_page_deleted);
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
