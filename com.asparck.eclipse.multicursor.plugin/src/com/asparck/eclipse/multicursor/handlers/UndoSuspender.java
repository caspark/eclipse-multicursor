package com.asparck.eclipse.multicursor.handlers;

import java.util.EventObject;

import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocument;
import org.eclipse.wst.sse.core.internal.undo.IStructuredTextUndoManager;

public class UndoSuspender implements ILinkedModeListener {

	private final IOperationApprover operationApprover = new IOperationApprover() {

		@Override
		public IStatus proceedUndoing(IUndoableOperation operation,
				IOperationHistory history, IAdaptable info) {
			return Status.CANCEL_STATUS;
		}

		@Override
		public IStatus proceedRedoing(IUndoableOperation operation,
				IOperationHistory history, IAdaptable info) {
			return Status.CANCEL_STATUS;
		}
	};

	private final CommandStackListener commandStackListener = new CommandStackListener() {

		@Override
		public void commandStackChanged(EventObject event) {
			if (event.getSource() instanceof CommandStack) {
				CommandStack aa = (CommandStack) event.getSource();
				compoundCommand.append(aa.getMostRecentCommand());
			}
		}
	};

	private final IOperationHistory operationHistory;
	private IStructuredTextUndoManager undoManager;
	private CommandStack oldCommandStack;
	private final CompoundCommand compoundCommand = new CompoundCommand();

	public UndoSuspender(IDocument document) {
		this.operationHistory = OperationHistoryFactory.getOperationHistory();
		this.operationHistory.addOperationApprover(operationApprover);

		if (isStructuredDocument(document)) {
			IStructuredDocument structuredDocument = (IStructuredDocument) document;
			CommandStack basicCommandStack = new BasicCommandStack() {
				@Override
				public boolean canUndo() {
					return false;
				}
			};
			basicCommandStack.addCommandStackListener(commandStackListener);
			undoManager = structuredDocument.getUndoManager();
			oldCommandStack = undoManager.getCommandStack();
			undoManager.setCommandStack(basicCommandStack);
		}
	}

	private boolean isStructuredDocument(IDocument document) {
		try {
			return document instanceof IStructuredDocument;
		} catch (NoClassDefFoundError e) {
			return false;
		}
	}

	@Override
	public void left(LinkedModeModel model, int flags) {
		operationHistory.removeOperationApprover(operationApprover);
		if (undoManager != null) {
			oldCommandStack.execute(compoundCommand);
			undoManager.setCommandStack(oldCommandStack);
		}
	}

	@Override
	public void suspend(LinkedModeModel model) {
	}

	@Override
	public void resume(LinkedModeModel model, int flags) {
	}

}
