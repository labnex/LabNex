package com.labnex.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.database.models.UserAccount;
import com.labnex.app.databinding.ListUserAccountsBinding;
import com.labnex.app.helpers.SharedPrefDB;
import io.mikael.urlbuilder.UrlBuilder;
import java.util.List;

/**
 * @author mmarif
 */
public class UserAccountsAdapter
		extends RecyclerView.Adapter<UserAccountsAdapter.UserAccountsViewHolder> {

	private final List<UserAccount> userAccountsList;
	private final Context context;
	private final SharedPrefDB sharedPrefDB;
	private final OnAccountClickListener listener;

	public interface OnAccountClickListener {
		void onAccountClick(UserAccount account);

		void onEditClick(UserAccount account);

		void onDeleteClick(UserAccount account, int position);
	}

	public UserAccountsAdapter(Context ctx, OnAccountClickListener listener) {
		this.context = ctx;
		this.listener = listener;
		this.userAccountsList =
				java.util.Objects.requireNonNull(
								com.labnex.app.database.api.BaseApi.getInstance(
										context, com.labnex.app.database.api.UserAccountsApi.class))
						.usersAccounts();
		this.sharedPrefDB = SharedPrefDB.getInstance(context);
	}

	@NonNull @Override
	public UserAccountsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListUserAccountsBinding binding =
				ListUserAccountsBinding.inflate(LayoutInflater.from(context), parent, false);
		return new UserAccountsViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull UserAccountsViewHolder holder, int position) {
		holder.bind(userAccountsList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return userAccountsList.size();
	}

	public void removeAccount(int position) {
		userAccountsList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, userAccountsList.size());
	}

	public class UserAccountsViewHolder extends RecyclerView.ViewHolder {

		final ListUserAccountsBinding binding;

		UserAccountsViewHolder(ListUserAccountsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onAccountClick(userAccountsList.get(pos));
						}
					});

			binding.editAccount.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onEditClick(userAccountsList.get(pos));
						}
					});

			binding.deleteAccount.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onDeleteClick(userAccountsList.get(pos), pos);
						}
					});
		}

		void bind(UserAccount currentItem) {
			String url =
					UrlBuilder.fromString(currentItem.getInstanceUrl()).withPath("/").toString();
			binding.userId.setText(currentItem.getUserName());
			binding.accountUrl.setText(url);

			boolean isActive =
					sharedPrefDB.getInt("currentActiveAccountId") == currentItem.getAccountId();
			binding.activeAccount.setVisibility(isActive ? View.VISIBLE : View.GONE);
			binding.deleteAccount.setVisibility(isActive ? View.GONE : View.VISIBLE);

			binding.tokenExpiresAt.setText(
					context.getString(
							com.labnex.app.R.string.account_token_expires_at,
							currentItem.getTokenExpiry()));
		}
	}
}
