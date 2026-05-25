package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Pair;
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
import com.labnex.app.adapters.CommitDiffsAdapter;
import com.labnex.app.databinding.BottomsheetCommitDiffsBinding;
import com.labnex.app.helpers.DiffParser;
import com.labnex.app.helpers.EndlessRecyclerViewScrollListener;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.commits.Diff;
import com.labnex.app.viewmodels.CommitDiffsViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author mmarif
 */
public class CommitDiffsBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetCommitDiffsBinding binding;
	private CommitDiffsViewModel viewModel;
	private CommitDiffsAdapter adapter;

	private long projectId;
	private String sha;
	private long mrIid;

	private final List<Pair<Diff, Pair<SpannableStringBuilder, SpannableStringBuilder>>>
			masterAdapterList = new ArrayList<>();
	private ExecutorService diffProcessingExecutor;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCommitDiffsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(CommitDiffsViewModel.class);

		diffProcessingExecutor =
				Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

		Bundle args = getArguments();
		if (args != null) {
			projectId = args.getLong("projectId");
			sha = args.getString("sha");
			mrIid = args.getLong("mrIid", 0);
		}

		setupRecyclerView();
		observeViewModel();

		if (mrIid > 0) {
			viewModel.loadMrChanges(requireContext(), projectId, mrIid);
			binding.sheetTitle.setText(getString(R.string.files));
		} else {
			viewModel.loadDiffs(requireContext(), projectId, sha);
		}

		binding.btnClose.setOnClickListener(v -> dismiss());

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter = new CommitDiffsAdapter(requireContext(), new ArrayList<>());

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.list.setLayoutManager(layoutManager);
		binding.list.setAdapter(adapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.loadNextPage(requireContext());
					}
				};
		binding.list.addOnScrollListener(scrollListener);
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
				.getDiffsList()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (Boolean.TRUE.equals(viewModel.getIsLoading().getValue())) return;
							if (list == null || list.isEmpty()) {
								if (masterAdapterList.isEmpty()) {
									binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
									binding.list.setVisibility(View.GONE);
								}
							} else {
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
								binding.list.setVisibility(View.VISIBLE);

								processDiffsAndBind(list);
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

	private void processDiffsAndBind(List<Diff> incomingRawList) {
		if (incomingRawList == null
				|| getContext() == null
				|| diffProcessingExecutor.isShutdown()) {
			return;
		}

		binding.progressBar.setVisibility(View.VISIBLE);
		final float textPixelSize = getResources().getDimension(R.dimen.dimen14sp);

		List<Diff> newElementsToProcess = new ArrayList<>();
		for (Diff rawItem : incomingRawList) {
			boolean alreadyTracked = false;
			for (Pair<Diff, Pair<SpannableStringBuilder, SpannableStringBuilder>> trackedPair :
					masterAdapterList) {
				if (trackedPair.first == rawItem) {
					alreadyTracked = true;
					break;
				}
			}
			if (!alreadyTracked) {
				newElementsToProcess.add(rawItem);
			}
		}

		diffProcessingExecutor.execute(
				() -> {
					List<Pair<Diff, Pair<SpannableStringBuilder, SpannableStringBuilder>>>
							processedBatch = new ArrayList<>();

					for (Diff rawItem : newElementsToProcess) {
						Pair<SpannableStringBuilder, SpannableStringBuilder> parsedSpans =
								DiffParser.parseInBackground(
										requireContext(), rawItem.getDiff(), textPixelSize);

						processedBatch.add(new Pair<>(rawItem, parsedSpans));
					}

					if (getActivity() != null) {
						getActivity()
								.runOnUiThread(
										() -> {
											if (binding != null) {
												masterAdapterList.addAll(processedBatch);
												binding.progressBar.setVisibility(View.GONE);
												adapter.updateList(masterAdapterList);
											}
										});
					}
				});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (diffProcessingExecutor != null) {
			diffProcessingExecutor.shutdownNow();
		}
		binding = null;
	}
}
