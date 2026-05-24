package com.labnex.app.views.reactions;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.databinding.BottomsheetEmojiPickerBinding;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.models.app.Reactions;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mmarif
 */
public class EmojiPickerSheet extends BottomSheetDialogFragment {

	private BottomsheetEmojiPickerBinding binding;
	private EmojiPickedListener listener;

	public interface EmojiPickedListener {
		void onEmojiPicked(String name);
	}

	public static EmojiPickerSheet newInstance() {
		return new EmojiPickerSheet();
	}

	public void setOnEmojiPicked(EmojiPickedListener listener) {
		this.listener = listener;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetEmojiPickerBinding.inflate(inflater, container, false);

		binding.emojiGrid.setLayoutManager(new GridLayoutManager(getContext(), 8));

		List<Map.Entry<String, String>> emojiList = new ArrayList<>(Reactions.EMOJIS.entrySet());
		EmojiAdapter adapter =
				new EmojiAdapter(
						emojiList,
						name -> {
							if (listener != null) listener.onEmojiPicked(name);
							dismiss();
						});
		binding.emojiGrid.setAdapter(adapter);

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

	private static class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {

		private final List<Map.Entry<String, String>> emojis;
		private final EmojiPickedListener listener;

		EmojiAdapter(List<Map.Entry<String, String>> emojis, EmojiPickedListener listener) {
			this.emojis = emojis;
			this.listener = listener;
		}

		@NonNull @Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			View view =
					LayoutInflater.from(parent.getContext())
							.inflate(R.layout.item_emoji, parent, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			Map.Entry<String, String> entry = emojis.get(position);
			holder.textView.setText(entry.getValue());
			holder.textView.setOnClickListener(v -> listener.onEmojiPicked(entry.getKey()));
		}

		@Override
		public int getItemCount() {
			return emojis.size();
		}

		static class ViewHolder extends RecyclerView.ViewHolder {
			final TextView textView;

			ViewHolder(View itemView) {
				super(itemView);
				textView = itemView.findViewById(R.id.emoji_text);
			}
		}
	}
}
