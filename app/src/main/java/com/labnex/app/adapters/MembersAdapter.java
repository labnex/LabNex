package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.labnex.app.R;
import com.labnex.app.databinding.ListMembersBinding;
import com.labnex.app.helpers.AvatarGenerator;
import com.labnex.app.models.user.User;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MembersHolder> {

	private final Context context;
	private final List<User> list;
	private final OnMemberClickListener listener;

	public interface OnMemberClickListener {
		void onMemberClick(User user);
	}

	public MembersAdapter(Context ctx, List<User> listMain, OnMemberClickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (listMain != null) this.list.addAll(listMain);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<User> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public MembersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListMembersBinding binding =
				ListMembersBinding.inflate(LayoutInflater.from(context), parent, false);
		return new MembersHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull MembersHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class MembersHolder extends RecyclerView.ViewHolder {

		final ListMembersBinding binding;

		MembersHolder(ListMembersBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onMemberClick(list.get(pos));
						}
					});
		}

		@SuppressLint("SetTextI18n")
		void bind(User user) {

			binding.userName.setText(
					user.getFullName() != null && !user.getFullName().isEmpty()
							? user.getFullName()
							: "@" + user.getUsername());
			binding.userId.setText("@" + user.getUsername());

			if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
				Glide.with(itemView.getContext())
						.load(user.getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.ic_spinner)
						.centerCrop()
						.into(binding.userAvatar);
			} else {
				binding.userAvatar.setImageDrawable(
						AvatarGenerator.getLetterAvatar(
								context,
								user.getFullName() != null
										? user.getFullName()
										: user.getUsername(),
								40));
			}
		}
	}
}
