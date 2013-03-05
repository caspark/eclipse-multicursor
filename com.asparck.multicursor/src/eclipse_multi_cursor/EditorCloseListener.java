package eclipse_multi_cursor;

import java.util.Map;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

public class EditorCloseListener implements IPartListener2 {

	private final Map<AbstractTextEditor, MultiCursor> multicursors;

	public EditorCloseListener(Map<AbstractTextEditor, MultiCursor> multicursors2) {
		this.multicursors = multicursors2;
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		IWorkbenchPart part = partRef.getPart(false);
		if (part instanceof ITextEditor) {
			multicursors.remove(((ITextEditor) part).getEditorInput());
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
	}
}
