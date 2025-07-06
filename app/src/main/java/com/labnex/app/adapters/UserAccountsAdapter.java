package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.activities.AppSettingsActivity;
import com.labnex.app.database.api.BaseApi;
import com.labnex.app.database.api.UserAccountsApi;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.helpers.CheckAuthorizationStatus;
import com.labnex.app.helpers.SharedPrefDB;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.helpers.Utils;
import io.mikael.urlbuilder.UrlBuilder;
import java.util.List;
import java.util.Objects;

/**
 * @author mmarif
 */
public class UserAccountsAdapter
		extends RecyclerView.Adapter<UserAccountsAdapter.UserAccountsViewHolder> {

	private final List<UserAccount> userAccountsList;
	private final Context context;
	private final SharedPrefDB sharedPrefDB;
	private View view;

	public UserAccountsAdapter(Context ctx) {
		this.context = ctx;
		this.userAccountsList =
				Objects.requireNonNull(BaseApi.getInstance(context, UserAccountsApi.class))
						.usersAccounts();
		this.sharedPrefDB = SharedPrefDB.getInstance(context);
	}

	private void updateLayoutByPosition(int position) {

		userAccountsList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, userAccountsList.size());
		Snackbar.info(
				context,
				view,
				context.getResources().getString(R.string.remove_account_from_app_success));
	}

	@NonNull @Override
	public UserAccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		view =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_user_accounts, parent, false);
		return new UserAccountsViewHolder(view);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onBindViewHolder(@NonNull UserAccountsViewHolder holder, int position) {

		UserAccount currentItem = userAccountsList.get(position);

		String url = UrlBuilder.fromString(currentItem.getInstanceUrl()).withPath("/").toString();

		holder.accountId = currentItem.getAccountId();
		holder.accountName = currentItem.getAccountName();

		holder.userId.setText(currentItem.getUserName());
		holder.accountUrl.setText(url);

		if (sharedPrefDB.getInt("currentActiveAccountId") == currentItem.getAccountId()) {
			holder.activeAccount.setVisibility(View.VISIBLE);
		} else {
			holder.deleteAccount.setVisibility(View.VISIBLE);
		}

		holder.tokenExpiresAt.setText(
				String.format(
						context.getResources().getString(R.string.account_token_expires_at),
						currentItem.getTokenExpiry()));
	}

	@Override
	public int getItemCount() {
		return userAccountsList.size();
	}

	public class UserAccountsViewHolder extends RecyclerView.ViewHolder {

		private final TextView accountUrl;
		private final TextView userId;
		private final ImageView activeAccount;
		private final ImageView deleteAccount;
		private final TextView tokenExpiresAt;
		private int accountId;
		private String accountName;

		private UserAccountsViewHolder(View itemView) {

			super(itemView);
			Context itemCtx = itemView.getContext();

			accountUrl = itemView.findViewById(R.id.account_url);
			userId = itemView.findViewById(R.id.user_id);
			activeAccount = itemView.findViewById(R.id.active_account);
			deleteAccount = itemView.findViewById(R.id.delete_account);
			ImageView editAccount = itemView.findViewById(R.id.edit_account);
			tokenExpiresAt = itemView.findViewById(R.id.token_expires_at);

			editAccount.setOnClickListener(
					edit -> {
						BottomSheetDialogFragment bottomSheet =
								(BottomSheetDialogFragment)
										((AppSettingsActivity) context)
												.getSupportFragmentManager()
												.findFragmentByTag("accountsBottomSheet");
						if (bottomSheet != null) {
							bottomSheet.dismiss();
							new Handler(Looper.getMainLooper())
									.postDelayed(
											() ->
													CheckAuthorizationStatus.showUpdateTokenDialog(
															context, accountId, true, false),
											300);
						} else {
							CheckAuthorizationStatus.showUpdateTokenDialog(
									context, accountId, true, false);
						}
					});

			deleteAccount.setOnClickListener(
					itemDelete -> {
						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(itemCtx)
										.setTitle(
												itemCtx.getResources()
														.getString(
																R.string
																		.remove_account_from_app_title))
										.setMessage(
												itemCtx.getResources()
														.getString(
																R.string
																		.remove_account_from_app_message))
										.setNeutralButton(
												itemCtx.getResources().getString(R.string.cancel),
												null)
										.setPositiveButton(
												itemCtx.getResources().getString(R.string.remove),
												(dialog, which) -> {
													updateLayoutByPosition(
															getBindingAdapterPosition());
													UserAccountsApi userAccountsApi =
															BaseApi.getInstance(
																	itemCtx, UserAccountsApi.class);
													assert userAccountsApi != null;
													userAccountsApi.deleteAccount(
															Integer.parseInt(
																	String.valueOf(accountId)));
												});

						materialAlertDialogBuilder.create().show();
					});

			itemView.setOnClickListener(
					switchAccount -> {
						UserAccountsApi userAccountsApi =
								BaseApi.getInstance(context, UserAccountsApi.class);
						assert userAccountsApi != null;
						UserAccount userAccount = userAccountsApi.getAccountByName(accountName);

						if (sharedPrefDB.getInt("currentActiveAccountId")
								!= userAccount.getAccountId()) {

							if (Utils.switchToAccount(context, userAccount)) {

								new Handler()
										.postDelayed(
												() -> {
													Intent i =
															context.getPackageManager()
																	.getLaunchIntentForPackage(
																			context
																					.getPackageName());
													assert i != null;
													context.startActivity(
															Intent.makeRestartActivityTask(
																	i.getComponent()));
													Runtime.getRuntime().exit(0);
												},
												150);
							}
						}
					});
		}
	}
}
