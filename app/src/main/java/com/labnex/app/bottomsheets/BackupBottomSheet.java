package com.labnex.app.bottomsheets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.helpers.Snackbar;

/**
 * @author mmarif
 */
public class BackupBottomSheet extends BottomSheetDialogFragment {

	public interface BackupCallback {
		void onExport();

		void onImport();
	}

	private BackupCallback callback;

	public static BackupBottomSheet newInstance(BackupCallback callback) {
		BackupBottomSheet fragment = new BackupBottomSheet();
		fragment.callback = callback;
		return fragment;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.bottom_sheet_backup, container, false);

		View exportButton = view.findViewById(R.id.export_button);
		View importButton = view.findViewById(R.id.import_button);

		exportButton.setOnClickListener(
				v ->
						new MaterialAlertDialogBuilder(requireContext())
								.setTitle(R.string.backup_dialog_title)
								.setMessage(R.string.backup_dialog_message)
								.setPositiveButton(
										R.string.export_button,
										(dialog, which) -> {
											if (callback != null) {
												callback.onExport();
											} else {
												requireActivity()
														.findViewById(R.id.nav_view)
														.post(
																() ->
																		Snackbar.info(
																				requireActivity(),
																				requireActivity()
																						.findViewById(
																								R.id
																										.nav_view),
																				getString(
																						R.string
																								.backup_failed)));
											}
											dialog.dismiss();
											dismiss();
										})
								.setNeutralButton(
										R.string.cancel,
										(dialog, which) -> {
											dialog.dismiss();
											dismiss();
										})
								.show());

		importButton.setOnClickListener(
				v ->
						new MaterialAlertDialogBuilder(requireContext())
								.setTitle(R.string.import_dialog_title)
								.setMessage(R.string.import_dialog_message)
								.setPositiveButton(
										R.string.import_button,
										(dialog, which) -> {
											if (callback != null) {
												callback.onImport();
											} else {
												requireActivity()
														.findViewById(R.id.nav_view)
														.post(
																() ->
																		Snackbar.info(
																				requireActivity(),
																				requireActivity()
																						.findViewById(
																								R.id
																										.nav_view),
																				getString(
																						R.string
																								.import_failed)));
											}
											dialog.dismiss();
											dismiss();
										})
								.setNeutralButton(
										R.string.cancel,
										(dialog, which) -> {
											dialog.dismiss();
											dismiss();
										})
								.show());

		return view;
	}
}
