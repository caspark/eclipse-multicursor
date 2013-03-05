package eclipse_multicursor.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
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
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import eclipse_multi_cursor.MultiCursorPlugin;

public class SelectNextHandler extends AbstractHandler {

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		System.err.println("Booomboom");
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);

		if (!(editor instanceof AbstractTextEditor)) {
			return null;
		}
		AbstractTextEditor textEditor = (AbstractTextEditor) editor;
		ISourceViewer viewer = MultiCursorPlugin.getViewer(textEditor);
		if (viewer == null) {
			MultiCursorPlugin.log("Viewer returned was null", null);
			return null;
		}
		try {
			goGoMultiCursor(viewer);
		} catch (BadLocationException e) {
			MultiCursorPlugin.log("Bad location error", e);
		}
		return null;
	}

	private void goGoMultiCursor(ISourceViewer viewer) throws BadLocationException {
		Point selection = viewer.getSelectedRange();
		int selStart = Math.min(selection.x, selection.y);
		int selEnd = Math.max(selection.x, selection.y);

		IDocument document = viewer.getDocument();

		List<IRegion> lines = new ArrayList<IRegion>();
		IRegion startLine = document.getLineInformationOfOffset(selStart);
		IRegion currLine = startLine;
		lines.add(currLine);
		IRegion endLine = document.getLineInformationOfOffset(selEnd);
		while (!currLine.equals(endLine) && currLine.getLength() + currLine.getOffset() + 1 < document.getLength()) {
			currLine = document.getLineInformationOfOffset(currLine.getLength() + currLine.getOffset() + 2);
			lines.add(currLine);
		}

		LinkedPositionGroup group = new LinkedPositionGroup();
		for (int i = 0; i < lines.size(); i++) {
			IRegion line = lines.get(i);
			//FIXME next line fails if the contents aren't the same
			group.addPosition(new LinkedPosition(document, line.getOffset(), line.getLength(), i));
		}

		LinkedModeModel model = new LinkedModeModel();
		model.addGroup(group);
		model.forceInstall();
//		if (fContext instanceof AssistContext) {
//			IEditorPart editor = ((AssistContext) fContext).getEditor();
//			if (editor instanceof JavaEditor) {
//				model.addLinkingListener(new EditorHighlightingSynchronizer((JavaEditor) editor));
//			}
//		}

		LinkedModeUI ui = new EditorLinkedModeUI(model, viewer);
		ui.setExitPolicy(new DeleteBlockingExitPolicy(document));
//		ui.setExitPosition(viewer, selEnd, 0, LinkedPositionGroup.NO_STOP);
		ui.enter();

		viewer.setSelectedRange(startLine.getOffset(), startLine.getLength()); // by default full word is selected, restore original selection

	}
}
