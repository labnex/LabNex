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
import com.labnex.app.adapters.LabelsAdapter;
import com.labnex.app.adapters.MembersAdapter;
import com.labnex.app.databinding.BottomSheetGroupDetailBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.LabelsViewModel;
import com.labnex.app.viewmodels.MembersViewModel;
import java.util.Objects;

/**
 * @author mmarif
 */
public class GroupDetailBottomSheet extends BottomSheetDialogFragment
		implements LabelActionsBottomSheet.UpdateInterface {

	private BottomSheetGroupDetailBinding bottomSheetGroupDetailBinding;
	private String source;
	private LabelsViewModel labelsViewModel;
	private MembersViewModel membersViewModel;
	private LabelsAdapter adapter;
	private MembersAdapter membersAdapter;
	private int page = 1;
	private int resultLimit;
	private int groupId;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetGroupDetailBinding =
				BottomSheetGroupDetailBinding.inflate(inflater, container, false);

		labelsViewModel = new ViewModelProvider(this).get(LabelsViewModel.class);
		membersViewModel = new ViewModelProvider(this).get(MembersViewModel.class);

		groupId = requireArguments().getInt("groupId", 0);
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();

		bottomSheetGroupDetailBinding.bottomSheetGroupLabels.closeBs.setOnClickListener(
				close -> dismiss());
		bottomSheetGroupDetailBinding.bottomSheetGroupMembers.closeBs.setOnClickListener(
				close -> dismiss());

		LabelActionsBottomSheet.setUpdateListener(this);

		if (!Objects.requireNonNull(requireArguments().getString("source")).isEmpty()) {

			if (Objects.requireNonNull(requireArguments().getString("source"))
					.equalsIgnoreCase("labels")) {

				bottomSheetGroupDetailBinding
						.bottomSheetGroupLabels
						.getRoot()
						.setVisibility(View.VISIBLE);

				bottomSheetGroupDetailBinding.bottomSheetGroupLabels.createNewLabel
						.setOnClickListener(
								v1 -> {
									Bundle bsBundle = new Bundle();
									bsBundle.putString("source", "labels");
									bsBundle.putInt("groupId", groupId);
									LabelActionsBottomSheet bottomSheet =
											new LabelActionsBottomSheet();
									bottomSheet.setArguments(bsBundle);
									bottomSheet.show(
											getParentFragmentManager(), "labelActionsBottomSheet");
								});

				bottomSheetGroupDetailBinding.bottomSheetGroupLabels.labelsList.setHasFixedSize(
						true);
				bottomSheetGroupDetailBinding.bottomSheetGroupLabels.labelsList.setLayoutManager(
						new LinearLayoutManager(getContext()));
				fetchGroupLabels();
			} else if (Objects.requireNonNull(requireArguments().getString("source"))
					.equalsIgnoreCase("members")) {

				bottomSheetGroupDetailBinding
						.bottomSheetGroupMembers
						.getRoot()
						.setVisibility(View.VISIBLE);

				bottomSheetGroupDetailBinding.bottomSheetGroupMembers.membersList.setHasFixedSize(
						true);
				bottomSheetGroupDetailBinding.bottomSheetGroupMembers.membersList.setLayoutManager(
						new LinearLayoutManager(getContext()));
				fetchGroupMembers();
			}
		} else {
			dismiss();
		}

		return bottomSheetGroupDetailBinding.getRoot();
	}

	@Override
	public void updateDataListener(String str) {

		if (str.equalsIgnoreCase("created")) {
			Snackbar.info(
					requireContext(),
					bottomSheetGroupDetailBinding.bottomSheetGroupLabels.labelsLayout,
					getString(R.string.label_created));
		}
		if (str.equalsIgnoreCase("updated")) {
			Snackbar.info(
					requireContext(),
					bottomSheetGroupDetailBinding.bottomSheetGroupLabels.labelsLayout,
					getString(R.string.label_updated));
		}

		adapter.clearAdapter();
		page = 1;
		fetchGroupLabels();
	}

	public void fetchGroupLabels() {

		bottomSheetGroupDetailBinding.bottomSheetGroupLabels.progressBar.setVisibility(
				View.VISIBLE);

		labelsViewModel
				.getLabels(
						getContext(),
						groupId,
						resultLimit,
						page,
						getActivity(),
						bottomSheetGroupDetailBinding)
				.observe(
						this,
						listMain -> {
							adapter =
									new LabelsAdapter(
											getContext(),
											listMain,
											groupId,
											bottomSheetGroupDetailBinding);
							adapter.setLoadMoreListener(
									new LabelsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											labelsViewModel.loadMore(
													getContext(),
													groupId,
													resultLimit,
													page,
													adapter,
													getActivity(),
													bottomSheetGroupDetailBinding);
											bottomSheetGroupDetailBinding.bottomSheetGroupLabels
													.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											bottomSheetGroupDetailBinding.bottomSheetGroupLabels
													.progressBar.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {

								bottomSheetGroupDetailBinding.bottomSheetGroupLabels.labelsList
										.setAdapter(adapter);
								bottomSheetGroupDetailBinding
										.bottomSheetGroupLabels
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.GONE);
							} else {

								adapter.notifyDataChanged();
								bottomSheetGroupDetailBinding.bottomSheetGroupLabels.labelsList
										.setAdapter(adapter);
								bottomSheetGroupDetailBinding
										.bottomSheetGroupLabels
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.VISIBLE);
							}

							bottomSheetGroupDetailBinding.bottomSheetGroupLabels.progressBar
									.setVisibility(View.GONE);
						});
	}

	public void fetchGroupMembers() {

		membersViewModel
				.getMembers(
						getContext(),
						groupId,
						resultLimit,
						page,
						getActivity(),
						bottomSheetGroupDetailBinding)
				.observe(
						this,
						listMain -> {
							membersAdapter = new MembersAdapter(getContext(), listMain, groupId);
							membersAdapter.setLoadMoreListener(
									new MembersAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											membersViewModel.loadMore(
													getContext(),
													groupId,
													resultLimit,
													page,
													membersAdapter,
													getActivity(),
													bottomSheetGroupDetailBinding);
											bottomSheetGroupDetailBinding.bottomSheetGroupLabels
													.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											bottomSheetGroupDetailBinding.bottomSheetGroupMembers
													.progressBar.setVisibility(View.GONE);
										}
									});

							if (membersAdapter.getItemCount() > 0) {

								bottomSheetGroupDetailBinding.bottomSheetGroupMembers.membersList
										.setAdapter(membersAdapter);
								bottomSheetGroupDetailBinding
										.bottomSheetGroupMembers
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.GONE);
							} else {

								membersAdapter.notifyDataChanged();
								bottomSheetGroupDetailBinding.bottomSheetGroupMembers.membersList
										.setAdapter(membersAdapter);
								bottomSheetGroupDetailBinding
										.bottomSheetGroupMembers
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.VISIBLE);
							}

							bottomSheetGroupDetailBinding.bottomSheetGroupMembers.progressBar
									.setVisibility(View.GONE);
						});
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_group_detail);

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
