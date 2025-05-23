package com.labnex.app.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.labnex.app.fragments.SnippetFileFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarif
 */
public class SnippetFilePagerAdapter extends FragmentStateAdapter {

	private final List<SnippetFileFragment> fragments = new ArrayList<>();
	private final List<String> fileNames = new ArrayList<>();
	private final SnippetFileFragment.OnFileNameChangedListener listener;

	public SnippetFilePagerAdapter(
			@NonNull FragmentActivity fragmentActivity,
			SnippetFileFragment.OnFileNameChangedListener listener) {
		super(fragmentActivity);
		this.listener = listener;
		addFile("file1.txt", "");
	}

	public void addFile(String fileName, String fileContent) {
		if (fragments.size() < 10) {
			SnippetFileFragment fragment =
					SnippetFileFragment.newInstance(fileName, fileContent, fragments.size());
			fragment.setOnFileNameChangedListener(listener);
			fragments.add(fragment);
			fileNames.add(fileName);
			notifyItemInserted(fragments.size() - 1);
		}
	}

	public void removeFile(int position) {
		if (position >= 0 && position < fragments.size()) {
			fragments.remove(position);
			fileNames.remove(position);
			notifyItemRemoved(position);
		}
	}

	public void updateFileName(int position, String newFileName) {
		if (position >= 0 && position < fileNames.size()) {
			fileNames.set(position, newFileName);
		}
	}

	public List<SnippetFileFragment> getFragments() {
		return fragments;
	}

	public String getFileName(int position) {
		return fileNames.get(position);
	}

	@NonNull @Override
	public Fragment createFragment(int position) {
		return fragments.get(position);
	}

	@Override
	public int getItemCount() {
		return fragments.size();
	}
}
