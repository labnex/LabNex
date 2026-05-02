package com.labnex.app.helpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.labnex.app.databinding.ItemDropdownEntryBinding;

/**
 * @author mmarif
 */
public class DropdownHelper {

	public interface IconProvider<T> {
		@DrawableRes
		int getIcon(T item);
	}

	public interface TextProvider<T> {
		String getText(T item);
	}

	public static <T> View createItemView(
			int position,
			@Nullable View convertView,
			@NonNull ViewGroup parent,
			ArrayAdapter<T> adapter,
			IconProvider<T> iconProvider,
			TextProvider<T> textProvider) {
		ItemDropdownEntryBinding binding;
		if (convertView != null) {
			binding = ItemDropdownEntryBinding.bind(convertView);
		} else {
			binding =
					ItemDropdownEntryBinding.inflate(
							LayoutInflater.from(parent.getContext()), parent, false);
		}

		T item = adapter.getItem(position);
		if (item != null) {
			binding.dropdownText.setText(textProvider.getText(item));
			binding.dropdownIcon.setImageResource(iconProvider.getIcon(item));
		}

		return binding.getRoot();
	}
}
