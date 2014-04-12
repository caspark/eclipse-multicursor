package com.asparck.eclipse.multicursor.editing;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;

import com.asparck.eclipse.multicursor.Logger;

public class SelectionManager {
	private static final Logger logger = Logger.create(SelectionManager.class);

	private final Map<ISourceViewer, CursorSelections> viewerToSelectionState = new HashMap<ISourceViewer, CursorSelections>();
	private final Map<ISourceViewer, DisposeListener> viewerToDisposeListener = new HashMap<ISourceViewer, DisposeListener>();

	public CursorSelections getOrCreateSelectionState(final ISourceViewer viewer) {
		final CursorSelections existingSelectionState = viewerToSelectionState.get(viewer);
		if (existingSelectionState != null) {
			return existingSelectionState;
		} else {
			final Point selectedRange = viewer.getSelectedRange();
			final IRegion selectedRegion = new Region(selectedRange.x, selectedRange.y);
			final CursorSelections createdSelectionState = new CursorSelections(viewer, Collections.singletonList(selectedRegion));
			setSelectionState(createdSelectionState);
			return createdSelectionState;
		}
	}

	public void setSelectionState(final CursorSelections selectionState) {
		final ISourceViewer sourceViewer = selectionState.getViewer();
		viewerToSelectionState.put(sourceViewer, selectionState);
		sourceViewer.getTextWidget().addDisposeListener(getOrCreateDisposeListenerForViewer(sourceViewer));
	}

	private DisposeListener getOrCreateDisposeListenerForViewer(final ISourceViewer viewer) {
		final DisposeListener existingDisposeListener = viewerToDisposeListener.get(viewer);
		if (existingDisposeListener != null) {
			return existingDisposeListener;
		} else {
			final DisposeListener createdDisposeListener = new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					logger.debug("Viewer " + viewer + " is getting disposed, clearing selection state for it");
					clearSelectionState(viewer);
				}
			};
			viewerToDisposeListener.put(viewer, createdDisposeListener);
			return createdDisposeListener;
		}
	}

	public void clearSelectionState(final ISourceViewer viewer) {
		viewerToSelectionState.remove(viewer);
		viewerToDisposeListener.remove(viewer);
	}
}
