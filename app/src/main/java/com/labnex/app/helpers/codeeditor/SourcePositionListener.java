package com.labnex.app.helpers.codeeditor;

import android.text.Layout;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;
import androidx.annotation.NonNull;

/**
 * @author AmrDeveloper
 * @author mmarif
 */
public class SourcePositionListener {

	private OnPositionChanged onPositionChanged;

	public SourcePositionListener(EditText editText) {
		View.AccessibilityDelegate viewAccessibility =
				new View.AccessibilityDelegate() {

					@Override
					public void sendAccessibilityEvent(@NonNull View host, int eventType) {
						super.sendAccessibilityEvent(host, eventType);
						if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
								&& onPositionChanged != null) {
							int selectionStart = editText.getSelectionStart();
							Layout layout = editText.getLayout();
							if (layout == null) {
								return;
							}
							int line = editText.getLayout().getLineForOffset(selectionStart);
							int column = selectionStart - editText.getLayout().getLineStart(line);
							onPositionChanged.onPositionChange(line + 1, column + 1);
						}
					}
				};
		editText.setAccessibilityDelegate(viewAccessibility);
	}

	public void setOnPositionChanged(OnPositionChanged listener) {
		onPositionChanged = listener;
	}

	@FunctionalInterface
	public interface OnPositionChanged {

		void onPositionChange(int line, int column);
	}
}
