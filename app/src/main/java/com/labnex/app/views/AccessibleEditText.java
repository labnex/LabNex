package com.labnex.app.views;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author mmarif
 */
public class AccessibleEditText extends com.google.android.material.textfield.TextInputEditText {

	public AccessibleEditText(@NonNull Context context) {
		super(context);
	}

	public AccessibleEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public AccessibleEditText(
			@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}
}
