package com.labnex.app.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * @author @mmarif
 */
public class LoadingDotsTextView extends AppCompatTextView {

	private final Handler handler = new Handler();
	private Runnable dotAnimationRunnable;
	private String baseText;
	private int dotCount = 0;
	private boolean isAnimating = false;
	private long animationDelay = 500; // Time between dot changes

	public LoadingDotsTextView(Context context) {
		super(context);
	}

	public LoadingDotsTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LoadingDotsTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void startAnimation() {
		if (getText() != null) {
			this.baseText = getText().toString();
			this.dotCount = 0;
			this.isAnimating = true;

			handler.removeCallbacks(dotAnimationRunnable);
			updateText();

			dotAnimationRunnable =
					new Runnable() {
						@Override
						public void run() {
							dotCount =
									(dotCount + 1) % 4; // 0, 1, 2, 3 (0 = no dots, 3 = three dots)
							updateText();
							handler.postDelayed(this, animationDelay);
						}
					};
			handler.postDelayed(dotAnimationRunnable, animationDelay);
		}
	}

	public void stopAnimation() {
		handler.removeCallbacks(dotAnimationRunnable);
		isAnimating = false;
		if (baseText != null) {
			setText(baseText);
		}
	}

	@SuppressLint("SetTextI18n")
	private void updateText() {
		StringBuilder dots = new StringBuilder();
		for (int i = 0; i < dotCount; i++) {
			dots.append(".");
		}
		setText(baseText + dots);
	}

	public boolean isAnimating() {
		return isAnimating;
	}

	public void setAnimationDelay(long millis) {
		animationDelay = millis;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopAnimation();
	}
}
