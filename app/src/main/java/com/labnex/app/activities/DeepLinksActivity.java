package com.labnex.app.activities;

import android.os.Bundle;
import com.labnex.app.databinding.ActivityDeeplinksBinding;

/**
 * @author @mmarif
 */
public class DeepLinksActivity extends BaseActivity {

	private ActivityDeeplinksBinding viewBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityDeeplinksBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());
	}
}
