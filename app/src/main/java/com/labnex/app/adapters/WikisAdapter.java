package com.labnex.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.labnex.app.databinding.ListWikisBinding;
import com.labnex.app.models.wikis.Wiki;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class WikisAdapter extends RecyclerView.Adapter<WikisAdapter.WikisHolder> {

	private final Context context;
	private final List<Wiki> list;
	private final OnWikiClickListener listener;
	private boolean canModify = true;

	public void setCanModify(boolean canModify) {
		this.canModify = canModify;
	}

	public interface OnWikiClickListener {
		void onWikiClick(Wiki wiki);

		void onMenuClick(Wiki wiki);
	}

	public WikisAdapter(Context ctx, List<Wiki> listMain, OnWikiClickListener listener) {
		this.context = ctx;
		this.list = new ArrayList<>();
		if (listMain != null) this.list.addAll(listMain);
		this.listener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Wiki> newList) {
		list.clear();
		if (newList != null) list.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public WikisHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListWikisBinding binding =
				ListWikisBinding.inflate(LayoutInflater.from(context), parent, false);
		return new WikisHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull WikisHolder holder, int position) {
		holder.bind(list.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public class WikisHolder extends RecyclerView.ViewHolder {

		ListWikisBinding binding;

		WikisHolder(ListWikisBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			if (!canModify) {
				binding.btnMenu.setVisibility(View.GONE);
			}

			itemView.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onWikiClick(list.get(pos));
						}
					});

			binding.btnMenu.setOnClickListener(
					v -> {
						int pos = getBindingAdapterPosition();
						if (pos != RecyclerView.NO_POSITION && listener != null) {
							listener.onMenuClick(list.get(pos));
						}
					});
		}

		void bind(Wiki wiki) {
			binding.title.setText(wiki.getTitle());
			binding.slug.setText(wiki.getSlug());
			if (wiki.getContent() != null) {
				binding.content.setText(
						wiki.getContent().substring(0, Math.min(wiki.getContent().length(), 120)));
			}
		}
	}
}
