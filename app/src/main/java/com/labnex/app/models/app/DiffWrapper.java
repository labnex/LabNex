package com.labnex.app.models.app;

import com.labnex.app.models.commits.Diff;

/**
 * @author mmarif
 */
public class DiffWrapper {

	private final Diff diff;
	private DiffData diffData;

	public DiffWrapper(Diff diff) {
		this.diff = diff;
	}

	public Diff getDiff() {
		return diff;
	}

	public DiffData getDiffData() {
		return diffData;
	}

	public void setDiffData(DiffData diffData) {
		this.diffData = diffData;
	}
}
