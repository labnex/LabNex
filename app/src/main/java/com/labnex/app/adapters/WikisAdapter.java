package com.labnex.app.adapters;

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.labnex.app.R;
import com.labnex.app.bottomsheets.WikiActionsBottomSheet;
import com.labnex.app.clients.RetrofitClient;
import com.labnex.app.databinding.BottomSheetProjectWikisBinding;
import com.labnex.app.helpers.Snackbar;
import com.labnex.app.models.wikis.Wiki;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class WikisAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Wiki> list;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private int projectId;
	private BottomSheetProjectWikisBinding binding;
	Bundle bundle = new Bundle();

	public WikisAdapter(
			Context ctx, List<Wiki> list, int projectId, BottomSheetProjectWikisBinding binding) {
		this.context = ctx;
		this.list = list;
		this.projectId = projectId;
		this.binding = binding;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new WikisHolder(inflater.inflate(R.layout.list_wikis, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		((WikisHolder) holder).bindData(list.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Wiki> list_) {
		list = list_;
	}

	private void updateAdapter(int position) {
		list.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, list.size());
	}

	public void clearAdapter() {
		list.clear();
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	public class WikisHolder extends RecyclerView.ViewHolder {

		private final TextView title;
		private final TextView content;
		private final TextView slug;
		private Wiki wikis;

		WikisHolder(View itemView) {

			super(itemView);
			title = itemView.findViewById(R.id.title);
			content = itemView.findViewById(R.id.content);
			slug = itemView.findViewById(R.id.slug);
			ImageView delete = itemView.findViewById(R.id.delete);

			itemView.setOnClickListener(
					v -> {
						bundle.putString("source", "view");
						bundle.putString("slug", wikis.getSlug());
						bundle.putString("content", wikis.getContent());
						bundle.putString("title", wikis.getTitle());
						bundle.putInt("projectId", projectId);

						WikiActionsBottomSheet bottomSheet = new WikiActionsBottomSheet();
						bottomSheet.setArguments(bundle);
						bottomSheet.show(
								((FragmentActivity) context).getSupportFragmentManager(),
								"wikiActionsBottomSheet");
					});

			delete.setOnClickListener(
					deletePage -> {
						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(
										context,
										com.google.android.material.R.style
												.ThemeOverlay_Material3_Dialog_Alert);

						materialAlertDialogBuilder
								.setTitle(
										context.getString(
												R.string.delete_dialog_title, wikis.getTitle()))
								.setMessage(R.string.delete_wiki_dialog_message)
								.setPositiveButton(
										R.string.delete,
										(dialog, whichButton) -> {
											deleteWiki(
													wikis.getSlug(), getBindingAdapterPosition());
										})
								.setNeutralButton(R.string.cancel, null)
								.show();
					});
		}

		void bindData(Wiki wiki) {

			this.wikis = wiki;

			title.setText(wiki.getTitle());
			content.setText(
					wiki.getContent().substring(0, Math.min(wiki.getContent().length(), 54)));
			// Markdown.render(context, EmojiParser.parseToUnicode(wiki.getContent()), content);
			slug.setText(wiki.getSlug());
		}
	}

	private void deleteWiki(String slug, int position) {

		RetrofitClient.getApiInterface(context)
				.deleteWikiPage(projectId, slug)
				.enqueue(
						new Callback<>() {

							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {

								if (response.code() == 204) {

									updateAdapter(position);
									Snackbar.info(
											context,
											binding.getRoot(),
											context.getResources()
													.getString(R.string.wiki_page_deleted));

								} else if (response.code() == 401) {

									Snackbar.info(
											context,
											binding.getRoot(),
											context.getResources()
													.getString(R.string.not_authorized));
								} else if (response.code() == 403) {

									Snackbar.info(
											context,
											binding.getRoot(),
											context.getResources()
													.getString(R.string.access_forbidden_403));
								} else {

									Snackbar.info(
											context,
											binding.getRoot(),
											context.getResources()
													.getString(R.string.generic_error));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

								Snackbar.info(
										context,
										binding.getRoot(),
										context.getResources()
												.getString(R.string.generic_server_response_error));
							}
						});
	}
}
