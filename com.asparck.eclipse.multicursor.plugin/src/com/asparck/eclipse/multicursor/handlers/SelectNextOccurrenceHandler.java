package com.asparck.eclipse.multicursor.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
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

public class SelectNextOccurrenceHandler extends AbstractHandlerWithState {
	private static final String ID_SELECTS_IN_PROGRESS = "SELECTS_IN_PROGRESS";

	private static final class SelectInProgress {
		public final String searchText;
		public final List<IRegion> existingSelections;
		public final int nextOffset;

		public SelectInProgress(String selectedText, List<IRegion> existingSelections, int nextOffset) {
			this.searchText = selectedText;
			this.existingSelections = Collections.unmodifiableList(new ArrayList<IRegion>(existingSelections));
			this.nextOffset = nextOffset;
		}

		@Override
		public String toString() {
			return "[Find " + searchText + " at " + nextOffset + "]";
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);

		ISourceViewer viewer = ISourceViewerFinder.fromEditorPart(editor);
		if (viewer != null) {
			startEditing(viewer);
		}

		return null;
	}

	private void startEditing(ISourceViewer viewer) throws ExecutionException {
		final Point selOffsetAndLen = viewer.getSelectedRange();
		final IDocument document = viewer.getDocument();

		try {
			final String searchText;
			final int candidateSearchOffset;
			final int selStart = CoordinatesUtil.fromOffsetAndLengthToStartAndEnd(selOffsetAndLen).x;
			if (selOffsetAndLen.y == 0) { // no characters selected
				String documentText = document.get();
				Point wordOffsetAndLen = TextUtil.findWordSurrounding(documentText, selStart);
				if (wordOffsetAndLen != null) {
					searchText = document.get(wordOffsetAndLen.x, wordOffsetAndLen.y);
					candidateSearchOffset = wordOffsetAndLen.x;
				} else {
					IRegion selectedLine = document.getLineInformationOfOffset(selStart);
					searchText = document.get(selectedLine.getOffset(), selectedLine.getLength());
					candidateSearchOffset = selectedLine.getOffset();
				}
			} else {
				searchText = document.get(selOffsetAndLen.x, selOffsetAndLen.y);
				candidateSearchOffset = selOffsetAndLen.x;
			}

			final int searchOffset;
			final List<IRegion> selections;

			final SelectInProgress currentState = getCurrentState();
			//FIXME need more robust detection of whether this is still the same search
			// (should probably track the viewer, and the initial search offset, then compare those)
			if (currentState != null && searchText.equals(currentState.searchText)) {
				selections = new ArrayList<IRegion>(currentState.existingSelections);
				searchOffset = currentState.nextOffset;
			} else {
				selections = new ArrayList<IRegion>();
				searchOffset = candidateSearchOffset;
			}

			IRegion matchingRegion = new FindReplaceDocumentAdapter(document).find(searchOffset, searchText, true,
					true, false, false);
			if (matchingRegion != null) {
				selections.add(matchingRegion);
				saveCurrentState(new SelectInProgress(searchText, selections, matchingRegion.getOffset()
						+ matchingRegion.getLength()));

				startLinkedEdit(selections, viewer, selOffsetAndLen);
			}
		} catch (BadLocationException e) {
			throw new ExecutionException("Editing failed", e);
		}
	}

	private void startLinkedEdit(List<IRegion> selections, ITextViewer viewer, Point originalSelection)
			throws BadLocationException {
		final LinkedPositionGroup linkedPositionGroup = new LinkedPositionGroup();
		for (IRegion selection : selections) {
			linkedPositionGroup.addPosition(new LinkedPosition(viewer.getDocument(), selection.getOffset(), selection
					.getLength()));
		}

		LinkedModeModel model = new LinkedModeModel();
		model.addGroup(linkedPositionGroup);
		model.forceInstall();

		LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
		ui.setExitPolicy(new DeleteBlockingExitPolicy(viewer.getDocument()));
		ui.enter();

		// by default the text being edited is selected so restore original selection
		viewer.setSelectedRange(originalSelection.x, originalSelection.y);
	}

	private SelectInProgress getCurrentState() {
		State state = getState(ID_SELECTS_IN_PROGRESS);
		if (state == null) {
			return null;
		} else {
			return (SelectInProgress) state.getValue();
		}
	}

	private void saveCurrentState(SelectInProgress selectInProgress) {
		State state = new State();
		state.setValue(selectInProgress);
		state.setId(ID_SELECTS_IN_PROGRESS);
		addState(ID_SELECTS_IN_PROGRESS, state);
	}

	@Override
	public void handleStateChange(State state, Object oldValue) {
//		logger.debug("State changed; new value=" + state.getId() + ":" + state.getValue() + " and old value="
//				+ oldValue);
	}

}
