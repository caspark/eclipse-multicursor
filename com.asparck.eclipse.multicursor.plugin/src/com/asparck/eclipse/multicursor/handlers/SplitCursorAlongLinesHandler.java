package com.asparck.eclipse.multicursor.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.asparck.eclipse.multicursor.Logger;
import com.asparck.eclipse.multicursor.editing.MultiCursorController;
import com.asparck.eclipse.multicursor.hacks.ISourceViewerFinder;

public class SplitCursorAlongLinesHandler extends AbstractHandler {
	private static final Logger logger = Logger.create(SplitCursorAlongLinesHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);

		ISourceViewer viewer = ISourceViewerFinder.fromEditorPart(editor);
		if (viewer != null) {
			try {
				new MultiCursorController().splitToLines(viewer);;
			} catch (BadLocationException e) {
				logger.error("Failed to split selection to lines", e);
			}
		}

		return null;
	}
}
