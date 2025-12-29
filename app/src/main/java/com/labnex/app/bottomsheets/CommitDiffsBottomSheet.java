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
import com.labnex.app.adapters.CommitDiffsAdapter;
import com.labnex.app.databinding.BottomSheetCommitDiffsBinding;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.CommitDiffsViewModel;

/**
 * @author mmarif
 */
public class CommitDiffsBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetCommitDiffsBinding binding;
	private CommitDiffsViewModel commitDiffsViewModel;
	private CommitDiffsAdapter adapter;
	private int projectId;
	private String sha;
	private int page = 1;
	private int resultLimit;
	private String source;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		binding = BottomSheetCommitDiffsBinding.inflate(inflater, container, false);
		commitDiffsViewModel = new ViewModelProvider(this).get(CommitDiffsViewModel.class);
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();

		binding.closeBs.setOnClickListener(close -> dismiss());

		Bundle bundle = getArguments();
		assert bundle != null;

		projectId = bundle.getInt("projectId");
		sha = bundle.getString("sha");

		if (bundle.getString("source") != null) {
			source = bundle.getString("source");
		} else {
			source = "";
		}

		binding.list.setHasFixedSize(true);
		binding.list.setLayoutManager(new LinearLayoutManager(getContext()));
		fetchCommitDiffs();

		return binding.getRoot();
	}

	public void fetchCommitDiffs() {

		binding.progressBar.setVisibility(View.VISIBLE);

		commitDiffsViewModel
				.getCommitDiffs(
						getContext(),
						source,
						projectId,
						sha,
						resultLimit,
						page,
						getActivity(),
						binding)
				.observe(
						this,
						listMain -> {
							adapter =
									new CommitDiffsAdapter(
											getContext(), listMain, projectId, binding);
							adapter.setLoadMoreListener(
									new CommitDiffsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											commitDiffsViewModel.loadMore(
													getContext(),
													source,
													projectId,
													sha,
													resultLimit,
													page,
													adapter,
													getActivity(),
													binding);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {

								binding.list.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.GONE);
							} else {

								adapter.notifyDataChanged();
								binding.list.setAdapter(adapter);
								binding.nothingFoundFrame.getRoot().setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_commit_diffs);

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
