package com.labnex.app.helpers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.labnex.app.R;

/**
 * @author mmarif
 */
public class UIHelper {

	private static final int DIMEN_EXTRA_MARGIN =
			R.dimen.dimen12dp; // Top/Bottom spacing (fragments)
	private static final int DIMEN_DOCK_CLEARANCE = R.dimen.dimen12dp; // RV padding for bottom
	private static final int DIMEN_PULL_DISTANCE = R.dimen.dimen48dp; // SwipeRefresh

	// For activities - call in onCreate
	public static void applyEdgeToEdge(
			ComponentActivity activity,
			View dockedToolbar,
			View scrollableView,
			SwipeRefreshLayout swipeRefresh,
			View headerView) {

		EdgeToEdge.enable(activity);
		applyInsets(
				activity.findViewById(android.R.id.content),
				dockedToolbar,
				scrollableView,
				swipeRefresh,
				headerView);
	}

	// For activities and fragments
	public static void applyInsets(
			View rootView,
			View dockedToolbar,
			View scrollableView,
			SwipeRefreshLayout swipeRefresh,
			View headerView) {

		final Context context = rootView.getContext();
		final int extraMargin = (int) context.getResources().getDimension(DIMEN_EXTRA_MARGIN);
		final int pullDistance = (int) context.getResources().getDimension(DIMEN_PULL_DISTANCE);

		final int staticBottomClearance =
				(int) (72 * context.getResources().getDisplayMetrics().density);

		ViewCompat.setOnApplyWindowInsetsListener(
				rootView,
				(v, windowInsets) -> {
					Insets systemBars =
							windowInsets.getInsets(
									WindowInsetsCompat.Type.systemBars()
											| WindowInsetsCompat.Type.ime());

					int topP =
							(headerView == null)
									? (systemBars.top + extraMargin)
									: (scrollableView != null ? scrollableView.getPaddingTop() : 0);

					int bottomP = systemBars.bottom + staticBottomClearance;

					if (headerView != null) {
						headerView.setPadding(
								headerView.getPaddingLeft(),
								systemBars.top + extraMargin,
								headerView.getPaddingRight(),
								headerView.getPaddingBottom());
					}

					if (scrollableView != null) {
						if (scrollableView instanceof android.view.ViewGroup group) {
							group.setClipToPadding(false);
						}

						if (scrollableView instanceof androidx.core.widget.NestedScrollView nsv) {
							nsv.setPadding(nsv.getPaddingLeft(), topP, nsv.getPaddingRight(), 0);
							if (nsv.getChildCount() > 0) {
								View child = nsv.getChildAt(0);
								child.setPadding(
										child.getPaddingLeft(),
										child.getPaddingTop(),
										child.getPaddingRight(),
										bottomP);
							}
						} else {
							scrollableView.setPadding(
									scrollableView.getPaddingLeft(),
									topP,
									scrollableView.getPaddingRight(),
									bottomP);
						}
					}

					if (swipeRefresh != null) {
						int start = systemBars.top;
						int end = start + pullDistance;
						swipeRefresh.setProgressViewOffset(false, start, end);
					}

					if (dockedToolbar != null) {
						if (dockedToolbar.getLayoutParams()
								instanceof CoordinatorLayout.LayoutParams params) {
							params.bottomMargin = systemBars.bottom + extraMargin;
							dockedToolbar.setLayoutParams(params);
						}
					}

					return windowInsets;
				});
	}

	public static void applySheetStyle(@NonNull BottomSheetDialog dialog, boolean isDraggable) {
		View bottomSheet =
				dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (bottomSheet != null) {
			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
			behavior.setFitToContents(true);
			behavior.setSkipCollapsed(true);
			behavior.setExpandedOffset(0);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			behavior.setDraggable(isDraggable);
		}
	}

	public static void applyFullScreenSheetStyle(
			@NonNull BottomSheetDialog dialog, boolean isDraggable) {
		View bottomSheet =
				dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
		if (bottomSheet != null) {
			ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
			layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
			bottomSheet.setLayoutParams(layoutParams);

			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
			behavior.setFitToContents(false);
			behavior.setSkipCollapsed(true);
			behavior.setExpandedOffset(0);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			behavior.setDraggable(isDraggable);
		}
	}
}
