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
import com.labnex.app.adapters.MembersAdapter;
import com.labnex.app.adapters.StarsAdapter;
import com.labnex.app.databinding.BottomSheetProjectMembersBinding;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.viewmodels.MembersViewModel;
import com.labnex.app.viewmodels.ProjectStarsViewModel;

/**
 * @author mmarif
 */
public class ProjectMembersBottomSheet extends BottomSheetDialogFragment {

	private BottomSheetProjectMembersBinding bottomSheetProjectMembersBinding;
	private MembersViewModel membersViewModel;
	private ProjectStarsViewModel projectStarsViewModel;
	private MembersAdapter membersAdapter;
	private StarsAdapter starsAdapter;
	private int page = 1;
	private int resultLimit;
	private int projectId;
	private String type;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetProjectMembersBinding =
				BottomSheetProjectMembersBinding.inflate(inflater, container, false);

		membersViewModel = new ViewModelProvider(this).get(MembersViewModel.class);
		projectStarsViewModel = new ViewModelProvider(this).get(ProjectStarsViewModel.class);

		projectId = requireArguments().getInt("projectId", 0);
		type = requireArguments().getString("type");
		resultLimit = ((BaseActivity) requireContext()).getAccount().getMaxPageLimit();

		bottomSheetProjectMembersBinding.closeBs.setOnClickListener(close -> dismiss());
		bottomSheetProjectMembersBinding.closeBs.setOnClickListener(close -> dismiss());

		bottomSheetProjectMembersBinding.membersList.setHasFixedSize(true);
		bottomSheetProjectMembersBinding.membersList.setLayoutManager(
				new LinearLayoutManager(getContext()));

		if (type.equalsIgnoreCase("stars")) {
			fetchProjectStarrers();
		} else {
			fetchProjectMembers();
		}

		return bottomSheetProjectMembersBinding.getRoot();
	}

	public void fetchProjectStarrers() {

		projectStarsViewModel
				.getProjectStarrers(
						getContext(),
						projectId,
						resultLimit,
						page,
						getActivity(),
						bottomSheetProjectMembersBinding)
				.observe(
						this,
						listMain -> {
							starsAdapter = new StarsAdapter(getContext(), listMain);
							starsAdapter.setLoadMoreListener(
									new StarsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											projectStarsViewModel.loadMore(
													getContext(),
													projectId,
													resultLimit,
													page,
													starsAdapter,
													getActivity(),
													bottomSheetProjectMembersBinding);
											bottomSheetProjectMembersBinding.progressBar
													.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											bottomSheetProjectMembersBinding.progressBar
													.setVisibility(View.GONE);
										}
									});

							if (starsAdapter.getItemCount() > 0) {

								bottomSheetProjectMembersBinding.membersList.setAdapter(
										starsAdapter);
								bottomSheetProjectMembersBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.GONE);
							} else {

								starsAdapter.notifyDataChanged();
								bottomSheetProjectMembersBinding.membersList.setAdapter(
										starsAdapter);
								bottomSheetProjectMembersBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.VISIBLE);
							}

							bottomSheetProjectMembersBinding.progressBar.setVisibility(View.GONE);
						});
	}

	public void fetchProjectMembers() {

		membersViewModel
				.getProjectMembers(
						getContext(),
						projectId,
						resultLimit,
						page,
						getActivity(),
						bottomSheetProjectMembersBinding)
				.observe(
						this,
						listMain -> {
							membersAdapter = new MembersAdapter(getContext(), listMain, projectId);
							membersAdapter.setLoadMoreListener(
									new MembersAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											membersViewModel.loadMoreProjectMembers(
													getContext(),
													projectId,
													resultLimit,
													page,
													membersAdapter,
													getActivity(),
													bottomSheetProjectMembersBinding);
											bottomSheetProjectMembersBinding.progressBar
													.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											bottomSheetProjectMembersBinding.progressBar
													.setVisibility(View.GONE);
										}
									});

							if (membersAdapter.getItemCount() > 0) {

								bottomSheetProjectMembersBinding.membersList.setAdapter(
										membersAdapter);
								bottomSheetProjectMembersBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.GONE);
							} else {

								membersAdapter.notifyDataChanged();
								bottomSheetProjectMembersBinding.membersList.setAdapter(
										membersAdapter);
								bottomSheetProjectMembersBinding
										.nothingFoundFrame
										.getRoot()
										.setVisibility(View.VISIBLE);
							}

							bottomSheetProjectMembersBinding.progressBar.setVisibility(View.GONE);
						});
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_project_members);

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
