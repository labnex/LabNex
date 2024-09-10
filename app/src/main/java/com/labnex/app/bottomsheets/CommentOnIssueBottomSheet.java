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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.labnex.app.R;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetCommentOnIssueBinding;
import com.labnex.app.helpers.Markdown;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.interfaces.BottomSheetListener;
import com.labnex.app.models.notes.CreateNote;
import com.labnex.app.models.notes.Notes;
import com.vdurmont.emoji.EmojiParser;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class CommentOnIssueBottomSheet extends BottomSheetDialogFragment {

	private int projectId;
	private int issueIid;
	private int mergeRequestIid;
	private String source;
	private BottomSheetCommentOnIssueBinding bottomSheetCommentOnIssueBinding;

	private static UpdateInterface UpdateInterface;

	public interface UpdateInterface {
		void updateDataListener(String str);
	}

	public static void setUpdateListener(UpdateInterface updateInterface) {
		UpdateInterface = updateInterface;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		bottomSheetCommentOnIssueBinding =
				BottomSheetCommentOnIssueBinding.inflate(inflater, container, false);

		Bundle bundle = getArguments();
		assert bundle != null;

		if (bundle.getString("source") != null) {
			source = bundle.getString("source");
		} else {
			source = "";
		}

		bottomSheetCommentOnIssueBinding.view.setOnClickListener(
				view -> {
					bottomSheetCommentOnIssueBinding.commentText.setVisibility(View.GONE);
					bottomSheetCommentOnIssueBinding.renderContents.setVisibility(View.VISIBLE);

					bottomSheetCommentOnIssueBinding.view.setVisibility(View.GONE);
					bottomSheetCommentOnIssueBinding.edit.setVisibility(View.VISIBLE);

					Markdown.render(
							requireContext(),
							EmojiParser.parseToUnicode(
									bottomSheetCommentOnIssueBinding
											.commentText
											.getText()
											.toString()),
							bottomSheetCommentOnIssueBinding.renderContents);
				});

		bottomSheetCommentOnIssueBinding.edit.setOnClickListener(
				edit -> {
					bottomSheetCommentOnIssueBinding.commentText.setVisibility(View.VISIBLE);
					bottomSheetCommentOnIssueBinding.renderContents.setVisibility(View.GONE);

					bottomSheetCommentOnIssueBinding.view.setVisibility(View.VISIBLE);
					bottomSheetCommentOnIssueBinding.edit.setVisibility(View.GONE);
				});

		bottomSheetCommentOnIssueBinding.closeBs.setOnClickListener(close -> dismiss());

		if (!Objects.requireNonNull(requireArguments().getString("source")).isEmpty()) {

			if (Objects.requireNonNull(requireArguments().getString("source"))
					.equalsIgnoreCase("comment")) {

				projectId = requireArguments().getInt("projectId");
				issueIid = requireArguments().getInt("issueIid");

				bottomSheetCommentOnIssueBinding.comment.setOnClickListener(
						comment -> {
							if (!bottomSheetCommentOnIssueBinding
									.commentText
									.getText()
									.toString()
									.isEmpty()) {
								disableButton();
								createNote(
										bottomSheetCommentOnIssueBinding
												.commentText
												.getText()
												.toString());
							} else {
								Snackbar.info(
										requireContext(),
										bottomSheetCommentOnIssueBinding.mainBsFrame,
										getString(R.string.comment_is_empty));
							}
						});
			} else if (Objects.requireNonNull(requireArguments().getString("source"))
					.equalsIgnoreCase("mr_comment")) {

				projectId = requireArguments().getInt("projectId");
				mergeRequestIid = requireArguments().getInt("mergeRequestIid");

				bottomSheetCommentOnIssueBinding.comment.setOnClickListener(
						mr_comment -> {
							if (!bottomSheetCommentOnIssueBinding
									.commentText
									.getText()
									.toString()
									.isEmpty()) {
								disableButton();
								createMergeRequestNote(
										bottomSheetCommentOnIssueBinding
												.commentText
												.getText()
												.toString());
							} else {
								Snackbar.info(
										requireContext(),
										bottomSheetCommentOnIssueBinding.mainBsFrame,
										getString(R.string.comment_is_empty));
							}
						});
			}
		} else {
			dismiss();
		}

		return bottomSheetCommentOnIssueBinding.getRoot();
	}

	private void createNote(String body) {

		CreateNote createNote = new CreateNote();
		createNote.setBody(body);

		Call<Notes> call =
				RetrofitClient.getApiInterface(requireContext())
						.createIssueNote(projectId, issueIid, createNote);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Notes> call,
							@NonNull retrofit2.Response<Notes> response) {

						if (response.code() == 201) {

							dismiss();
							UpdateInterface.updateDataListener("created");
						} else if (response.code() == 401) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetCommentOnIssueBinding.mainBsFrame,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetCommentOnIssueBinding.mainBsFrame,
									getString(R.string.access_forbidden_403));
						} else {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetCommentOnIssueBinding.mainBsFrame,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Notes> call, @NonNull Throwable t) {

						enableButton();
						Snackbar.info(
								requireContext(),
								bottomSheetCommentOnIssueBinding.mainBsFrame,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void createMergeRequestNote(String body) {

		CreateNote createNote = new CreateNote();
		createNote.setBody(body);

		Call<Notes> call =
				RetrofitClient.getApiInterface(requireContext())
						.createMergeRequestNote(projectId, mergeRequestIid, createNote);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Notes> call,
							@NonNull retrofit2.Response<Notes> response) {

						if (response.code() == 201) {

							dismiss();
							UpdateInterface.updateDataListener("created");
						} else if (response.code() == 401) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetCommentOnIssueBinding.mainBsFrame,
									getString(R.string.not_authorized));
						} else if (response.code() == 403) {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetCommentOnIssueBinding.mainBsFrame,
									getString(R.string.access_forbidden_403));
						} else {

							enableButton();
							Snackbar.info(
									requireContext(),
									bottomSheetCommentOnIssueBinding.mainBsFrame,
									getString(R.string.generic_error));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Notes> call, @NonNull Throwable t) {

						enableButton();
						Snackbar.info(
								requireContext(),
								bottomSheetCommentOnIssueBinding.mainBsFrame,
								getString(R.string.generic_server_response_error));
					}
				});
	}

	private void disableButton() {
		bottomSheetCommentOnIssueBinding.comment.setEnabled(false);
		bottomSheetCommentOnIssueBinding.comment.setAlpha(.5F);
	}

	private void enableButton() {
		bottomSheetCommentOnIssueBinding.comment.setEnabled(true);
		bottomSheetCommentOnIssueBinding.comment.setAlpha(1F);
	}

	@NonNull @Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
		dialog.setContentView(R.layout.bottom_sheet_comment_on_issue);

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
