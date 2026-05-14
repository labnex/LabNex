package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.databinding.BottomsheetColorPickerBinding;
import com.labnex.app.databinding.ItemColorFamilyBinding;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import java.util.Objects;

/**
 * @author mmarif
 */
public class ColorPickerBottomSheet extends BottomSheetDialogFragment {

	private static final String[][] PALETTE_FAMILIES = {
		{"#B71C1C", "#D32F2F", "#E53935", "#F44336", "#EF5350", "#E57373"}, // Red
		{"#E65100", "#F57C00", "#FB8C00", "#FF9800", "#FFA726", "#FFB74D"}, // Orange
		{"#F9A825", "#FBC02D", "#FDD835", "#FFEB3B", "#FFEE58", "#FFF176"}, // Yellow
		{"#1B5E20", "#2E7D32", "#388E3C", "#43A047", "#4CAF50", "#66BB6A"}, // Green
		{"#0D47A1", "#1565C0", "#1976D2", "#1E88E5", "#2196F3", "#42A5F5"}, // Blue
		{"#4A148C", "#6A1B9A", "#7B1FA2", "#8E24AA", "#9C27B0", "#AB47BC"}, // Purple
		{"#212121", "#424242", "#616161", "#757575", "#9E9E9E", "#BDBDBD"} // Gray
	};

	private BottomsheetColorPickerBinding binding;
	private String selectedColor;
	private OnColorSelectedListener listener;

	public interface OnColorSelectedListener {
		void onColorSelected(String hexColor);
	}

	public static ColorPickerBottomSheet newInstance(String initialColor) {
		ColorPickerBottomSheet fragment = new ColorPickerBottomSheet();
		Bundle args = new Bundle();
		args.putString("initial_color", initialColor);
		fragment.setArguments(args);
		return fragment;
	}

	public void setOnColorSelectedListener(OnColorSelectedListener listener) {
		this.listener = listener;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetColorPickerBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (getArguments() != null) {
			selectedColor = getArguments().getString("initial_color", "#2E7D32");
		}

		setupUI();
		setupGrid();
	}

	private void setupUI() {
		binding.hexInput.setText(selectedColor.replace("#", ""));

		binding.hexInputLayout.setEndIconOnClickListener(
				v -> {
					String input =
							Objects.requireNonNull(binding.hexInput.getText()).toString().trim();
					String cleanHex = input.replaceAll("^#+", "");

					if (cleanHex.length() == 6) {
						try {
							String finalColor = "#" + cleanHex;
							Color.parseColor(finalColor);
							applyAndDismiss(finalColor);
						} catch (Exception e) {
							Toasty.show(requireContext(), getString(R.string.invalid_color));
						}
					} else {
						Toasty.show(requireContext(), getString(R.string.invalid_length));
					}
				});
	}

	private void setupGrid() {
		binding.colorGrid.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.colorGrid.setAdapter(
				new RecyclerView.Adapter<FamilyViewHolder>() {
					@NonNull @Override
					public FamilyViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
						return new FamilyViewHolder(
								ItemColorFamilyBinding.inflate(getLayoutInflater(), p, false));
					}

					@Override
					public void onBindViewHolder(@NonNull FamilyViewHolder holder, int pos) {
						String[] family = PALETTE_FAMILIES[pos];
						bindSegment(holder.binding.seg1, holder.binding.dot1, family[0]);
						bindSegment(holder.binding.seg2, holder.binding.dot2, family[1]);
						bindSegment(holder.binding.seg3, holder.binding.dot3, family[2]);
						bindSegment(holder.binding.seg4, holder.binding.dot4, family[3]);
						bindSegment(holder.binding.seg5, holder.binding.dot5, family[4]);
						bindSegment(holder.binding.seg6, holder.binding.dot6, family[5]);
					}

					@Override
					public int getItemCount() {
						return PALETTE_FAMILIES.length;
					}
				});
	}

	private void bindSegment(FrameLayout seg, View dot, String colorStr) {
		seg.setBackgroundColor(Color.parseColor(colorStr));
		dot.setVisibility(colorStr.equalsIgnoreCase(selectedColor) ? View.VISIBLE : View.GONE);
		seg.setOnClickListener(v -> applyAndDismiss(colorStr));
	}

	private void applyAndDismiss(String color) {
		if (listener != null) {
			listener.onColorSelected(color);
		}
		dismiss();
	}

	static class FamilyViewHolder extends RecyclerView.ViewHolder {
		final ItemColorFamilyBinding binding;

		FamilyViewHolder(ItemColorFamilyBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			UIHelper.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}
}
