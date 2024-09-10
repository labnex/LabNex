package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.NotesBottomSheet;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.NotesApi;
import com.labnex.app.database.models.Notes;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.TimeUtils;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author mmarif
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {

	private List<Notes> notesList;
	private final Context ctx;
	private final String insert;
	private final String source;
	private View view;

	private final BottomAppBar bottomAppBar;

	public NotesAdapter(
			Context ctx,
			List<Notes> notesListMain,
			String insert,
			String source,
			BottomAppBar bottomAppBar) {
		this.ctx = ctx;
		this.notesList = notesListMain;
		this.insert = insert;
		this.source = source;
		this.bottomAppBar = bottomAppBar;
	}

	public class NotesViewHolder extends RecyclerView.ViewHolder {

		private Notes notes;
		private final TextView content;
		private final TextView datetime;

		private NotesViewHolder(View itemView) {

			super(itemView);

			content = itemView.findViewById(R.id.content);
			datetime = itemView.findViewById(R.id.datetime);
			ImageView deleteNote = itemView.findViewById(R.id.delete_note);

			itemView.setOnClickListener(
					cardView -> {
						Bundle bundle = new Bundle();
						bundle.putString("source", "edit");
						bundle.putInt("noteId", notes.getNoteId());

						NotesBottomSheet bottomSheet = new NotesBottomSheet();
						bottomSheet.setArguments(bundle);
						bottomSheet.show(
								((FragmentActivity) ctx).getSupportFragmentManager(),
								"notesBottomSheet");
					});

			deleteNote.setOnClickListener(
					itemDelete -> {
						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(
										ctx,
										com.google.android.material.R.style
												.ThemeOverlay_Material3_Dialog_Alert);

						materialAlertDialogBuilder
								.setMessage(ctx.getString(R.string.delete_note_dialog_message))
								.setPositiveButton(
										R.string.delete,
										(dialog, whichButton) ->
												deleteNote(
														getBindingAdapterPosition(),
														notes.getNoteId()))
								.setNeutralButton(R.string.cancel, null)
								.show();
					});

			/*if (insert.equalsIgnoreCase("insert") && source.equalsIgnoreCase("issue")) {

			deleteNote.setVisibility(View.GONE);

			itemView.setOnClickListener(
					view -> {
						CreateIssueActivity parentActivity = (CreateIssueActivity) ctx;
						EditText text = parentActivity.findViewById(R.id.newIssueDescription);
						text.append(notes.getContent());

						parentActivity.dialogNotes.dismiss();
					});*/

			/*if (insert.equalsIgnoreCase("insert") && source.equalsIgnoreCase("release")) {

				deleteNote.setVisibility(View.GONE);

				itemView.setOnClickListener(
						view -> {
							CreateReleaseActivity parentActivity = (CreateReleaseActivity) ctx;
							EditText text = parentActivity.findViewById(R.id.releaseContent);
							text.append(notes.getContent());

							parentActivity.dialogNotes.dismiss();
						});
			}*/

			/*if (insert.equalsIgnoreCase("insert") && source.equalsIgnoreCase("pr")) {

				deleteNote.setVisibility(View.GONE);

				itemView.setOnClickListener(
						view -> {
							CreatePullRequestActivity parentActivity =
									(CreatePullRequestActivity) ctx;
							EditText text = parentActivity.findViewById(R.id.prBody);
							text.append(notes.getContent());

							parentActivity.dialogNotes.dismiss();
						});
			}
			}*/
		}
	}

	private void deleteNote(int position, int noteId) {

		NotesApi notesApi = BaseApi.getInstance(ctx, NotesApi.class);
		assert notesApi != null;
		notesApi.deleteNote(noteId);
		notesList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, notesList.size());
		if (source.equalsIgnoreCase("search")) {
			Snackbar.info(
					ctx,
					view,
					ctx.getResources().getQuantityString(R.plurals.note_delete_message, 1));
		} else {
			Snackbar.info(
					ctx,
					view,
					bottomAppBar,
					ctx.getResources().getQuantityString(R.plurals.note_delete_message, 1));
		}
	}

	@NonNull @Override
	public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_notes, parent, false);
		return new NotesViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {

		Locale locale = ctx.getResources().getConfiguration().getLocales().get(0);
		Notes currentItem = notesList.get(position);
		holder.notes = currentItem;

		assert currentItem.getContent() != null;
		String contentTrimmed =
				currentItem
						.getContent()
						.substring(0, Math.min(currentItem.getContent().length(), 54));
		// Markdown.render(ctx, EmojiParser.parseToUnicode(contentTrimmed), holder.content);
		holder.content.setText(contentTrimmed);

		if (currentItem.getModified() != null) {
			String modifiedTime =
					TimeUtils.formatTime(
							Date.from(Instant.ofEpochSecond(currentItem.getModified())), locale);
			holder.datetime.setText(
					ctx.getResources().getString(R.string.updated_with, modifiedTime));
		} else {
			String createdTime =
					TimeUtils.formatTime(
							Date.from(Instant.ofEpochSecond(currentItem.getDatetime())), locale);
			holder.datetime.setText(
					ctx.getResources().getString(R.string.created_with, createdTime));
		}
	}

	@Override
	public int getItemCount() {
		return notesList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
	}

	public void updateList(List<Notes> list) {

		notesList = list;
		notifyDataChanged();
	}
}
