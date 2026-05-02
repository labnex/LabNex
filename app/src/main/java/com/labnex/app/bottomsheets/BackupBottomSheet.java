package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.databinding.BottomsheetBackupBinding;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;

/**
 * @author mmarif
 */
public class BackupBottomSheet extends BottomSheetDialogFragment {

	public interface BackupCallback {
		void onExport();

		void onImport();
	}

	private BottomsheetBackupBinding binding;
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
		binding = BottomsheetBackupBinding.inflate(inflater, container, false);

		binding.exportButton.setOnClickListener(
				v ->
						new MaterialAlertDialogBuilder(requireContext())
								.setTitle(R.string.backup_dialog_title)
								.setMessage(R.string.backup_dialog_message)
								.setPositiveButton(
										R.string.export_button,
										(dialog, which) -> {
											if (callback != null) callback.onExport();
											else
												Toasty.show(
														requireContext(),
														getString(R.string.backup_failed));
											dismiss();
										})
								.setNeutralButton(R.string.cancel, (dialog, which) -> dismiss())
								.show());

		binding.importButton.setOnClickListener(
				v ->
						new MaterialAlertDialogBuilder(requireContext())
								.setTitle(R.string.import_dialog_title)
								.setMessage(R.string.import_dialog_message)
								.setPositiveButton(
										R.string.import_button,
										(dialog, which) -> {
											if (callback != null) callback.onImport();
											else
												Toasty.show(
														requireContext(),
														getString(R.string.import_failed));
											dismiss();
										})
								.setNeutralButton(R.string.cancel, (dialog, which) -> dismiss())
								.show());

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
}
