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
import com.labnex.app.adapters.WikisAdapter;
import com.labnex.app.databinding.BottomSheetProjectWikisBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.WikisViewModel;

/**
 * @author mmarif
 */
public class ProjectWikisBottomSheet extends BottomSheetDialogFragment
		implements WikiActionsBottomSheet.UpdateInterface {

	private BottomSheetProjectWikisBinding bottomSheetProjectWikisBinding;
	private WikisViewModel wikisViewModel;
	private WikisAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private int projectId;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetProjectWikisBinding =
				BottomSheetProjectWikisBinding.inflate(inflater, container, false);

		wikisViewModel = new ViewModelProvider(this).get(WikisViewModel.class);

		projectId = requireArguments().getInt("projectId", 0);
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();

		bottomSheetProjectWikisBinding.closeBs.setOnClickListener(close -> dismiss());
		bottomSheetProjectWikisBinding.closeBs.setOnClickListener(close -> dismiss());

		WikiActionsBottomSheet.setUpdateListener(this);

		bottomSheetProjectWikisBinding.getRoot().setVisibility(View.VISIBLE);

		bottomSheetProjectWikisBinding.createNew.setOnClickListener(
				v1 -> {
					Bundle bsBundle = new Bundle();
					bsBundle.putString("source", "new");
					bsBundle.putInt("projectId", projectId);
					WikiActionsBottomSheet bottomSheet = new WikiActionsBottomSheet();
					bottomSheet.setArguments(bsBundle);
					bottomSheet.show(getParentFragmentManager(), "wikiActionsBottomSheet");
				});

		bottomSheetProjectWikisBinding.recyclerView.setHasFixedSize(true);
		bottomSheetProjectWikisBinding.recyclerView.setLayoutManager(
				new LinearLayoutManager(getContext()));
		fetchProjectWikis();

		return bottomSheetProjectWikisBinding.getRoot();
	}

	@Override
	public void updateDataListener(String str) {

		if (str.equalsIgnoreCase("created")) {
			Snackbar.info(
					requireContext(),
					bottomSheetProjectWikisBinding.mainLayout,
					getString(R.string.wiki_page_created));
		}
		if (str.equalsIgnoreCase("updated")) {
			Snackbar.info(
					requireContext(),
					bottomSheetProjectWikisBinding.mainLayout,
					getString(R.string.wiki_page_updated));
		}

		adapter.clearAdapter();
		page = 1;
		fetchProjectWikis();
	}

	public void fetchProjectWikis() {

		bottomSheetProjectWikisBinding.progressBar.setVisibility(View.VISIBLE);

		wikisViewModel
				.getWikis(
						getContext(),
						projectId,
						resultLimit,
						page,
						getActivity(),
						bottomSheetProjectWikisBinding)
				.observe(
						this,
						listMain -> {
							adapter =
									new WikisAdapter(
											getContext(),
											listMain,
											projectId,
											bottomSheetProjectWikisBinding);
							/*adapter.setLoadMoreListener(
							new WikisAdapter.OnLoadMoreListener() {

								@Override
								public void onLoadMore() {

									page += 1;
									wikisViewModel.loadMore(
											getContext(),
											projectId,
											resultLimit,
											page,
											adapter,
											getActivity(),
											bottomSheetProjectWikisBinding);
									bottomSheetProjectWikisBinding.progressBar
											.setVisibility(View.VISIBLE);
								}

								@Override
								public void onLoadFinished() {

									bottomSheetProjectWikisBinding.progressBar
											.setVisibility(View.GONE);
								}
							});*/

							if (adapter.getItemCount() > 0) {

								bottomSheetProjectWikisBinding.recyclerView.setAdapter(adapter);
								bottomSheetProjectWikisBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.GONE);
							} else {

								bottomSheetProjectWikisBinding.recyclerView.setAdapter(adapter);
								bottomSheetProjectWikisBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.VISIBLE);
							}

							bottomSheetProjectWikisBinding.progressBar.setVisibility(View.GONE);
						});
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_project_wikis);

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
