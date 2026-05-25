package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.databinding.BottomsheetMergeMrBinding;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.MrDetailViewModel;

/**
 * @author mmarif
 */
public class MergeMrBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetMergeMrBinding binding;
	private long projectId;
	private long mrIid;
	private MrDetailViewModel viewModel;

	public static MergeMrBottomSheet newInstance(
			long projectId, long mrIid, MrDetailViewModel viewModel) {
		MergeMrBottomSheet sheet = new MergeMrBottomSheet();
		Bundle args = new Bundle();
		args.putLong("projectId", projectId);
		args.putLong("mrIid", mrIid);
		sheet.viewModel = viewModel;
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			projectId = getArguments().getLong("projectId");
			mrIid = getArguments().getLong("mrIid");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetMergeMrBinding.inflate(inflater, container, false);

		binding.btnMerge.setOnClickListener(
				v -> {
					boolean removeSource = binding.switchRemoveSource.isChecked();
					boolean squash = binding.switchSquash.isChecked();
					viewModel.mergeMr(requireContext(), projectId, mrIid, removeSource, squash);
					dismiss();
				});

		return binding.getRoot();
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
