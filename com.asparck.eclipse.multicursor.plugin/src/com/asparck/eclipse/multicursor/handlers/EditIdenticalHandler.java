package com.asparck.eclipse.multicursor.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import com.asparck.eclipse.multicursor.copied.DeleteBlockingExitPolicy;
import com.asparck.eclipse.multicursor.hacks.ISourceViewerFinder;
import com.asparck.eclipse.multicursor.util.CoordinatesUtil;
import com.asparck.eclipse.multicursor.util.TextUtil;

/** When triggered, any lines which are identical to the current line will start being edited. */
public class EditIdenticalHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);

		ISourceViewer viewer = ISourceViewerFinder.fromEditorPart(editor);
		if (viewer != null) {
			startEditing(viewer);
		}

		return null;
	}

	/**
	 * Mostly based on code from {@link org.eclipse.jdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal}
	 */
	private void startEditing(ISourceViewer viewer) throws ExecutionException {
		Point selOffsetAndLen = viewer.getSelectedRange();
		int selStart = CoordinatesUtil.fromOffsetAndLengthToStartAndEnd(selOffsetAndLen).x;

		IDocument document = viewer.getDocument();
		try {
			String selectedText;
			if (selOffsetAndLen.y == 0) { // no characters selected
				String documentText = document.get();
				Point wordOffsetAndLen = TextUtil.findWordSurrounding(documentText, selStart);
				if (wordOffsetAndLen != null) {
					selectedText = document.get(wordOffsetAndLen.x, wordOffsetAndLen.y);
				} else {
					IRegion selectedLine = document.getLineInformationOfOffset(selStart);
					selectedText = document.get(selectedLine.getOffset(), selectedLine.getLength());
				}
			} else {
				selectedText = document.get(selOffsetAndLen.x, selOffsetAndLen.y);
			}

			LinkedPositionGroup linkedPositionGroup = new LinkedPositionGroup();

			FindReplaceDocumentAdapter findReplaceAdaptor = new FindReplaceDocumentAdapter(document);
			IRegion matchingRegion = findReplaceAdaptor.find(0, selectedText, true, true, false, false);
			while (matchingRegion != null) {
				linkedPositionGroup.addPosition(new LinkedPosition(document, matchingRegion.getOffset(), matchingRegion
						.getLength()));

				matchingRegion = findReplaceAdaptor.find(matchingRegion.getOffset() + matchingRegion.getLength(),
						selectedText, true, true, false, false);
			}

			LinkedModeModel model = new LinkedModeModel();
			model.addGroup(linkedPositionGroup);
			model.forceInstall();

			LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
			ui.setExitPolicy(new DeleteBlockingExitPolicy(document));
			ui.enter();

			// by default the text being edited is selected so restore original selection
			viewer.setSelectedRange(selOffsetAndLen.x, selOffsetAndLen.y);
		} catch (BadLocationException e) {
			throw new ExecutionException("Editing failed", e);
		}
	}

}
