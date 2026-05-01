package com.labnex.app.helpers;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author mmarif
 */
public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

	private int currentPage = 1;
	private int previousTotalItemCount = 0;
	private boolean loading = true;
	private final RecyclerView.LayoutManager mLayoutManager;

	public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
		this.mLayoutManager = layoutManager;
	}

	@Override
	public void onScrolled(@NonNull RecyclerView view, int dx, int dy) {
		int lastVisibleItemPosition =
				((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
		int totalItemCount = mLayoutManager.getItemCount();

		if (totalItemCount < previousTotalItemCount) {
			this.currentPage = 1;
			this.previousTotalItemCount = totalItemCount;
			if (totalItemCount == 0) {
				this.loading = true;
			}
		}

		if (loading && (totalItemCount > previousTotalItemCount)) {
			loading = false;
			previousTotalItemCount = totalItemCount;
		}

		int visibleThreshold = 15;
		if (!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
			currentPage++;
			onLoadMore(currentPage, totalItemCount, view);
			loading = true;
		}
	}

	public abstract void onLoadMore(int page, int totalItemsCount, RecyclerView view);

	public void resetState() {
		this.currentPage = 1;
		this.previousTotalItemCount = 0;
		this.loading = true;
	}

	public int getCurrentPage() {
		return currentPage;
	}
}
