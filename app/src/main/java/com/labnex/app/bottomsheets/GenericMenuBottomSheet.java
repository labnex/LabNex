package com.labnex.app.bottomsheets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.color.MaterialColors;
import com.labnex.app.databinding.BottomsheetGenericMenuBinding;
import com.labnex.app.databinding.ItemGenericMenuCardBinding;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.app.GenericMenuItemModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class GenericMenuBottomSheet extends BottomSheetDialogFragment {

	private BottomsheetGenericMenuBinding binding;
	private List<GenericMenuItemModel> items;
	private String title;
	private String subtitle;
	private OnMenuItemClickListener listener;

	public interface OnMenuItemClickListener {
		void onMenuItemClick(String id);
	}

	public static GenericMenuBottomSheet newInstance(
			String title, String subtitle, List<GenericMenuItemModel> items) {
		GenericMenuBottomSheet sheet = new GenericMenuBottomSheet();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("subtitle", subtitle);
		args.putParcelableArrayList("items", new ArrayList<>(items));
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnMenuItemClickListener) {
			listener = (OnMenuItemClickListener) context;
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetGenericMenuBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			title = args.getString("title");
			subtitle = args.getString("subtitle");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				items = args.getParcelableArrayList("items", GenericMenuItemModel.class);
			} else {
				items = args.getParcelableArrayList("items");
			}
		}

		binding.sheetTitle.setText(title);
		if (subtitle != null && !subtitle.isEmpty()) {
			binding.sheetSubtitle.setVisibility(View.VISIBLE);
			binding.sheetSubtitle.setText(subtitle);
		} else {
			binding.sheetSubtitle.setVisibility(View.GONE);
		}

		binding.menuGrid.setColumnCount(3);
		binding.menuGrid.removeAllViews();

		for (GenericMenuItemModel item : items) {
			ItemGenericMenuCardBinding cardBinding =
					ItemGenericMenuCardBinding.inflate(
							getLayoutInflater(), binding.menuGrid, false);

			int bgColor =
					MaterialColors.getColor(requireContext(), item.getBackgroundAttr(), Color.GRAY);
			int contentColor =
					MaterialColors.getColor(
							requireContext(), item.getContentColorAttr(), Color.WHITE);

			cardBinding.menuCard.setCardBackgroundColor(bgColor);
			cardBinding.menuIcon.setImageResource(item.getIconRes());
			cardBinding.menuIcon.setImageTintList(ColorStateList.valueOf(contentColor));
			cardBinding.menuText.setText(item.getLabelRes());
			cardBinding.menuText.setTextColor(contentColor);

			cardBinding.menuCard.setOnClickListener(
					v -> {
						if (listener != null) {
							listener.onMenuItemClick(item.getId());
						}
						dismiss();
					});

			binding.menuGrid.addView(cardBinding.getRoot());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getDialog() instanceof BottomSheetDialog dialog) {
			UIHelper.applySheetStyle(dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
