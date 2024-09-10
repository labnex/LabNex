package com.labnex.app.helpers.codeeditor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.amrdeveloper.codeview.Code;
import com.amrdeveloper.codeview.CodeViewAdapter;
import com.labnex.app.R;
import java.util.List;

/**
 * @author AmrDeveloper
 * @author mmarif
 */
public class CustomCodeViewAdapter extends CodeViewAdapter {

	private final LayoutInflater layoutInflater;

	public CustomCodeViewAdapter(@NonNull Context context, @NonNull List<Code> codes) {
		super(context, R.layout.list_items_autocomplete, 0, codes);
		this.layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = layoutInflater.inflate(R.layout.list_items_autocomplete, parent, false);
		}

		// ImageView codeType = convertView.findViewById(R.id.code_type);
		TextView codeTitle = convertView.findViewById(R.id.code_title);
		Code currentCode = (Code) getItem(position);
		if (currentCode != null) {
			codeTitle.setText(currentCode.getCodeTitle());
			/*if (currentCode instanceof Snippet) {
				//codeType.setImageResource(R.drawable.ic_snippet);
			} else {
				//codeType.setImageResource(R.drawable.ic_keyword);
			}*/
		}

		return convertView;
	}
}
