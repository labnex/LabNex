package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.R;
import com.labnex.app.database.models.Notes;
import com.labnex.app.databinding.ListNotesBinding;
import com.labnex.app.helpers.TimeHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author mmarif
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

	private final Context ctx;
	private List<Notes> notesList;
	private final OnNoteClickListener listener;
	private OnNoteInsertListener insertListener;
	private boolean useSheetStyle = false;

	public void setSheetStyle(boolean sheetStyle) {
		this.useSheetStyle = sheetStyle;
	}

	public interface OnNoteInsertListener {
		void onNoteSelected(String content);
	}

	public void setOnNoteInsertListener(OnNoteInsertListener listener) {
		this.insertListener = listener;
	}

	public interface OnNoteClickListener {
		void onNoteClick(Notes note);

		void onNoteDelete(Notes note, int position);
	}

	public NotesAdapter(Context ctx, List<Notes> notesList, OnNoteClickListener listener) {
		this.ctx = ctx;
		this.notesList = new ArrayList<>(notesList);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Notes> newList) {
		this.notesList = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	public void removeItem(int position) {
		notesList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, notesList.size());
	}

	@NonNull @Override
	public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListNotesBinding binding =
				ListNotesBinding.inflate(LayoutInflater.from(ctx), parent, false);
		return new NotesViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
		holder.bind(notesList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
		TypedValue tv = new TypedValue();
		if (useSheetStyle) {
			holder.itemView
					.getContext()
					.getTheme()
					.resolveAttribute(
							com.google.android.material.R.attr.colorSurfaceContainerHigh, tv, true);
		} else {
			holder.itemView
					.getContext()
					.getTheme()
					.resolveAttribute(
							com.google.android.material.R.attr.colorSurfaceContainerLow, tv, true);
		}
		holder.binding.card.setCardBackgroundColor(tv.data);
	}

	@Override
	public int getItemCount() {
		return notesList.size();
	}

	public class NotesViewHolder extends RecyclerView.ViewHolder {

		final ListNotesBinding binding;

		NotesViewHolder(ListNotesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos == RecyclerView.NO_POSITION) return;

						if (insertListener != null) {
							Notes note = notesList.get(pos);
							if (note.getContent() != null) {
								insertListener.onNoteSelected(note.getContent());
							}
						} else if (listener != null) {
							listener.onNoteClick(notesList.get(pos));
						}
					});

			binding.btnDelete.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onNoteDelete(notesList.get(pos), pos);
						}
					});
		}

		void bind(Notes note) {
			String content = note.getContent();
			if (content != null && content.length() > 120) {
				content = content.substring(0, 120) + "…";
			}
			binding.content.setText(content);

			if (note.getModified() != null) {
				Date date = new Date(note.getModified() * 1000L);
				binding.datetime.setText(
						ctx.getString(R.string.updated_with, TimeHelper.formatTime(date)));
			} else {
				Date date = new Date(note.getDatetime() * 1000L);
				binding.datetime.setText(
						ctx.getString(R.string.created_with, TimeHelper.formatTime(date)));
			}

			binding.btnDelete.setVisibility(insertListener != null ? View.GONE : View.VISIBLE);
		}
	}
}
