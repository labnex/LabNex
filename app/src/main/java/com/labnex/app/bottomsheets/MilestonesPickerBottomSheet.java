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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.adapters.MilestonePickerAdapter;
import com.labnex.app.databinding.BottomsheetMilestonePickerBinding;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.milestone.Milestones;
import com.labnex.app.viewmodels.MilestonesViewModel;
import java.util.ArrayList;

/**
 * @author mmarif
 */
public class MilestonesPickerBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetMilestonePickerBinding binding;
	private MilestonePickerAdapter adapter;
	private String type;
	private long id;
	private Milestones selected;
	private OnMilestoneSelectedListener listener;

	public interface OnMilestoneSelectedListener {
		void onMilestoneSelected(Milestones milestone);
	}

	public static MilestonesPickerBottomSheet newInstance(
			String type, long id, Milestones preSelected) {
		MilestonesPickerBottomSheet sheet = new MilestonesPickerBottomSheet();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putLong("id", id);
		if (preSelected != null) args.putSerializable("selected", preSelected);
		sheet.setArguments(args);
		return sheet;
	}

	public void setOnMilestoneSelectedListener(OnMilestoneSelectedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			type = getArguments().getString("type", "project");
			id = getArguments().getLong("id", 0);
			selected = (Milestones) getArguments().getSerializable("selected");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetMilestonePickerBinding.inflate(inflater, container, false);
		MilestonesViewModel viewModel = new ViewModelProvider(this).get(MilestonesViewModel.class);

		adapter =
				new MilestonePickerAdapter(
						requireContext(),
						new ArrayList<>(),
						selected,
						milestone -> selected = milestone);

		binding.milestonesList.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.milestonesList.setAdapter(adapter);

		binding.btnDone.setOnClickListener(
				v -> {
					if (listener != null && selected != null) {
						listener.onMilestoneSelected(selected);
					}
					dismiss();
				});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.progressBar.setVisibility(
									Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getMilestoneList()
				.observe(
						getViewLifecycleOwner(),
						milestones -> {
							if (milestones != null) {
								adapter.updateList(milestones);
							}
						});

		viewModel.loadMilestones(requireContext(), type, id);

		return binding.getRoot();
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
