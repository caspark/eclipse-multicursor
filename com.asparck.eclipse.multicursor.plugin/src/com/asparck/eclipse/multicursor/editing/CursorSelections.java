package com.asparck.eclipse.multicursor.editing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.ISourceViewer;

/** Tracks what the selection is for a given viewer */
public class CursorSelections {

	private final ISourceViewer viewer;
	private final List<IRegion> selections;

	public CursorSelections(ISourceViewer viewer, List<IRegion> selections) {
		this.viewer = viewer;
		this.selections = Collections.unmodifiableList(new ArrayList<IRegion>(selections));
	}

	public ISourceViewer getViewer() {
		return viewer;
	}

	public List<IRegion> getSelections() {
		return selections;
	}

	@Override
	public String toString() {
		return selections + " (" + viewer + ")";
	}
}
