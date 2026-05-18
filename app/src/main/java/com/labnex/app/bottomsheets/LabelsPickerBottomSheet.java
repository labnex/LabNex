package com.labnex.app.bottomsheets;

import android.annotation.SuppressLint;
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
import com.labnex.app.adapters.LabelPickerAdapter;
import com.labnex.app.databinding.BottomsheetLabelPickerBinding;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.viewmodels.LabelsViewModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author mmarif
 */
public class LabelsPickerBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetLabelPickerBinding binding;
	private LabelPickerAdapter adapter;
	private String type;
	private long id;
	private final Set<String> selectedLabels = new HashSet<>();
	private OnLabelsSelectedListener listener;

	public interface OnLabelsSelectedListener {
		void onLabelsSelected(List<String> labels);
	}

	public static LabelsPickerBottomSheet newInstance(
			String type, long id, List<String> preSelected) {
		LabelsPickerBottomSheet sheet = new LabelsPickerBottomSheet();
		Bundle args = new Bundle();
		args.putString("type", type);
		args.putLong("id", id);
		args.putStringArrayList(
				"selected", new ArrayList<>(preSelected != null ? preSelected : new ArrayList<>()));
		sheet.setArguments(args);
		return sheet;
	}

	public void setOnLabelsSelectedListener(OnLabelsSelectedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			type = getArguments().getString("type", "project");
			id = getArguments().getLong("id", 0);
			List<String> preSelected = getArguments().getStringArrayList("selected");
			if (preSelected != null) selectedLabels.addAll(preSelected);
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetLabelPickerBinding.inflate(inflater, container, false);
		LabelsViewModel viewModel = new ViewModelProvider(this).get(LabelsViewModel.class);

		adapter =
				new LabelPickerAdapter(
						requireContext(),
						new ArrayList<>(),
						selectedLabels,
						label -> {
							if (selectedLabels.contains(label.getName())) {
								selectedLabels.remove(label.getName());
							} else {
								selectedLabels.add(label.getName());
							}
							adapter.notifyDataSetChanged();
						});

		binding.labelsList.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.labelsList.setAdapter(adapter);

		binding.btnDone.setOnClickListener(
				v -> {
					if (listener != null) {
						listener.onLabelsSelected(new ArrayList<>(selectedLabels));
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
				.getLabelList()
				.observe(
						getViewLifecycleOwner(),
						labels -> {
							if (labels != null) {
								adapter.updateList(labels);
							}
						});

		viewModel.loadLabels(requireContext(), type, id);

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
