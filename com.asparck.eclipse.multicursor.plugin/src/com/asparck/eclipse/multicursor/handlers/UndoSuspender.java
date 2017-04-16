package com.asparck.eclipse.multicursor.handlers;

import org.eclipse.core.commands.operations.IOperationApprover;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;

public class UndoSuspender implements ILinkedModeListener {

	IOperationApprover operationApprover = new IOperationApprover() {

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

	private IOperationHistory operationHistory;

	public UndoSuspender() {
		this.operationHistory = OperationHistoryFactory.getOperationHistory();
		this.operationHistory.addOperationApprover(operationApprover);
	}

	@Override
	public void left(LinkedModeModel model, int flags) {
		operationHistory.removeOperationApprover(operationApprover);
	}

	@Override
	public void suspend(LinkedModeModel model) {
	}

	@Override
	public void resume(LinkedModeModel model, int flags) {
	}

}
