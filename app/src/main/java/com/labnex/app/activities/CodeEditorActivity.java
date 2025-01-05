package com.labnex.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import com.amrdeveloper.codeview.Code;
import com.labnex.app.R;
import com.labnex.app.core.MainGrammarLocator;
import com.labnex.app.databinding.ActivityCodeEditorBinding;
import com.labnex.app.helpers.codeeditor.CustomCodeViewAdapter;
import com.labnex.app.helpers.codeeditor.SourcePositionListener;
import com.labnex.app.helpers.codeeditor.languages.Language;
import com.labnex.app.helpers.codeeditor.languages.UnknownLanguage;
import com.labnex.app.helpers.codeeditor.theme.Theme;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author AmrDeveloper
 * @author mmarif
 */
public class CodeEditorActivity extends BaseActivity {

	private Theme currentTheme;
	private ActivityCodeEditorBinding binding;
	private Language currentLanguage = new UnknownLanguage();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityCodeEditorBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		String fileContent = getIntent().getStringExtra("fileContent");
		String fileExtension;
		currentTheme = Theme.getDefaultTheme(this);

		if (getIntent().getStringExtra("fileExtension") != null) {
			fileExtension =
					MainGrammarLocator.fromExtension(
							Objects.requireNonNull(getIntent().getStringExtra("fileExtension")));

			currentLanguage = Language.fromName(fileExtension);
		}

		OnBackPressedCallback onBackPressedCallback =
				new OnBackPressedCallback(true) {
					@Override
					public void handleOnBackPressed() {
						sendResults();
						finish();
					}
				};
		getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

		configCodeView(currentLanguage, fileContent);
		configCodeViewPlugins();
	}

	private void sendResults() {
		Intent intent = new Intent();
		intent.putExtra("fileContentFromActivity", binding.codeView.getText().toString());
		setResult(Activity.RESULT_OK, intent);
	}

	private void configCodeView(Language currentLanguage, String fileContent) {

		binding.codeView.setTypeface(
				Typeface.createFromAsset(ctx.getAssets(), "fonts/hackregular.ttf"));

		// Setup Line number feature
		binding.codeView.setEnableLineNumber(true);
		binding.codeView.setLineNumberTextColor(Color.GRAY);
		binding.codeView.setLineNumberTextSize(32f);

		// Setup Auto indenting feature
		/*if (Integer.parseInt(
				AppDatabaseSettings.getSettingsValue(
						ctx, AppDatabaseSettings.APP_CE_INDENTATION_KEY))
				== 0) {
			binding.codeView.setEnableAutoIndentation(true);
			switch (Integer.parseInt(
					AppDatabaseSettings.getSettingsValue(
							ctx, AppDatabaseSettings.APP_CE_TABS_WIDTH_KEY))) {
				case 0:
					binding.codeView.setTabLength(2);
					break;
				case 1:
					binding.codeView.setTabLength(4);
					break;
				case 2:
					binding.codeView.setTabLength(6);
					break;
				case 3:
					binding.codeView.setTabLength(8);
					break;
			}
		} else {
			binding.codeView.setEnableAutoIndentation(false);
		}*/

		binding.codeView.setEnableAutoIndentation(true);
		binding.codeView.setTabLength(4);

		// Set up the language and theme with SyntaxManager helper class
		currentLanguage.applyTheme(this, binding.codeView, currentTheme);

		// Setup auto pair complete
		final Map<Character, Character> pairCompleteMap = new HashMap<>();
		pairCompleteMap.put('{', '}');
		pairCompleteMap.put('[', ']');
		pairCompleteMap.put('(', ')');
		pairCompleteMap.put('<', '>');
		pairCompleteMap.put('"', '"');
		pairCompleteMap.put('\'', '\'');

		binding.codeView.setPairCompleteMap(pairCompleteMap);
		binding.codeView.enablePairComplete(true);
		binding.codeView.enablePairCompleteCenterCursor(true);
		binding.codeView.setText(fileContent);

		// Set up the auto complete and auto indenting for the current language
		configLanguageAutoComplete();
		configLanguageAutoIndentation();
	}

	private void configLanguageAutoComplete() {

		{
			List<Code> codeList = currentLanguage.getCodeList();

			CustomCodeViewAdapter adapter = new CustomCodeViewAdapter(this, codeList);

			binding.codeView.setAdapter(adapter);
		}
	}

	private void configLanguageAutoIndentation() {
		binding.codeView.setIndentationStarts(currentLanguage.getIndentationStarts());
		binding.codeView.setIndentationEnds(currentLanguage.getIndentationEnds());
	}

	private void configCodeViewPlugins() {
		configLanguageName();

		binding.sourcePosition.setText(getString(R.string.source_position, 0, 0));
		configSourcePositionListener();
	}

	private void configLanguageName() {
		binding.languageName.setText(currentLanguage.getName().toLowerCase());
	}

	private void configSourcePositionListener() {
		SourcePositionListener sourcePositionListener =
				new SourcePositionListener(binding.codeView);
		sourcePositionListener.setOnPositionChanged(
				(line, column) -> {
					binding.sourcePosition.setText(
							getString(R.string.source_position, line, column));
				});
	}
}
