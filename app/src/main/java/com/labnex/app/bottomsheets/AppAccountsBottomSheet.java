package com.labnex.app.bottomsheets;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.SignInActivity;
import com.labnex.app.adapters.UserAccountsAdapter;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.NotesApi;
import com.labnex.app.database.api.ProjectsApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.BottomsheetAppAccountsBinding;
import com.labnex.app.helpers.CheckAuthorizationStatus;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Toasty;
import com.labnex.app.helpers.UIHelper;
import com.labnex.app.helpers.Utils;

/**
 * @author mmarif
 */
public class AppAccountsBottomSheet extends BottomSheetDialogFragment
		implements UserAccountsAdapter.OnAccountClickListener {

	private BottomsheetAppAccountsBinding binding;
	private UserAccountsAdapter adapter;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetAppAccountsBinding.inflate(inflater, container, false);

		binding.addNewAccount.setOnClickListener(
				v -> {
					startActivity(
							new Intent(requireContext(), SignInActivity.class)
									.putExtra("source", "add_account"));
					dismiss();
				});

		binding.removeAllAccounts.setOnClickListener(
				v ->
						new MaterialAlertDialogBuilder(requireContext())
								.setTitle(R.string.remove_all_accounts)
								.setMessage(R.string.remove_all_accounts_from_app_message)
								.setNeutralButton(R.string.cancel, null)
								.setPositiveButton(
										R.string.remove,
										(dialog, which) -> {
											UserAccountsApi ua =
													BaseApi.getInstance(
															requireContext(),
															UserAccountsApi.class);
											NotesApi na =
													BaseApi.getInstance(
															requireContext(), NotesApi.class);
											ProjectsApi pa =
													BaseApi.getInstance(
															requireContext(), ProjectsApi.class);
											if (ua != null) ua.deleteAllAccounts();
											if (na != null) na.deleteAllNotes();
											if (pa != null) pa.deleteAllProjects();
											new Handler()
													.postDelayed(
															() -> {
																Intent intent =
																		new Intent(
																				requireContext(),
																				SignInActivity
																						.class);
																intent.setFlags(
																		Intent
																						.FLAG_ACTIVITY_NEW_TASK
																				| Intent
																						.FLAG_ACTIVITY_CLEAR_TASK);
																requireContext()
																		.startActivity(intent);
																dismiss();
															},
															500);
										})
								.show());

		adapter = new UserAccountsAdapter(requireContext(), this);
		binding.accountsList.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.accountsList.setAdapter(adapter);

		return binding.getRoot();
	}

	@Override
	public void onAccountClick(UserAccount account) {
		UserAccountsApi api = BaseApi.getInstance(requireContext(), UserAccountsApi.class);
		if (api == null) return;
		UserAccount current = api.getAccountByName(account.getAccountName());
		if (current != null
				&& SharedPrefDB.getInstance(requireContext()).getInt("currentActiveAccountId")
						!= current.getAccountId()) {
			if (Utils.switchToAccount(requireContext(), current)) {
				dismiss();
				new Handler()
						.postDelayed(
								() -> {
									Intent i =
											requireContext()
													.getPackageManager()
													.getLaunchIntentForPackage(
															requireContext().getPackageName());
									if (i != null)
										requireContext()
												.startActivity(
														Intent.makeRestartActivityTask(
																i.getComponent()));
									Runtime.getRuntime().exit(0);
								},
								150);
			}
		}
	}

	@Override
	public void onEditClick(UserAccount account) {
		Context ctx = requireContext();
		int id = account.getAccountId();
		dismiss();
		new Handler(Looper.getMainLooper())
				.postDelayed(
						() -> CheckAuthorizationStatus.showUpdateTokenBottomSheet(ctx, id), 200);
	}

	@Override
	public void onDeleteClick(UserAccount account, int position) {
		new MaterialAlertDialogBuilder(requireContext())
				.setTitle(R.string.remove_account_from_app_title)
				.setMessage(R.string.remove_account_from_app_message)
				.setNeutralButton(R.string.cancel, null)
				.setPositiveButton(
						R.string.remove,
						(dialog, which) -> {
							adapter.removeAccount(position);
							UserAccountsApi api =
									BaseApi.getInstance(requireContext(), UserAccountsApi.class);
							if (api != null) api.deleteAccount(account.getAccountId());
							Toasty.show(
									requireContext(),
									getString(R.string.remove_account_from_app_success));
						})
				.show();
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
