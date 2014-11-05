package com.asparck.eclipse.multicursor.hacks;

import java.lang.reflect.Method;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.asparck.eclipse.multicursor.Logger;

/**
 * Uses hacky reflection techniques to find the source viewer for a given thing.
 */
public class ISourceViewerFinder {
	private static final Logger logger = Logger.create(ISourceViewerFinder.class);

	public static ISourceViewer fromEditorPart(IEditorPart editorPart) {
		Object activeEditor = editorPart;
		if (editorPart instanceof MultiPageEditorPart) {
			MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) editorPart;
			activeEditor = multiPageEditorPart.getSelectedPage();
		}
		if (activeEditor instanceof AbstractTextEditor) {
			return fromAbstractTextEditor((AbstractTextEditor) activeEditor);
		} else {
			logger.info("Unable to get ISourceViewer from " + editorPart
					+ " of type " + editorPart.getClass().getCanonicalName());
			return null;
		}
	}

	/**
	 * Relies on protected final method {@link AbstractTextEditor#getSourceViewer()}.
	 */
	private static ISourceViewer fromAbstractTextEditor(AbstractTextEditor editor) {
		try {
			Method getSourceViewerMethod = null;
			Class<?> clazz = editor.getClass();
			while (clazz != null && getSourceViewerMethod == null) {
				if (clazz.equals(AbstractTextEditor.class)) {
					getSourceViewerMethod = clazz.getDeclaredMethod("getSourceViewer");
				} else {
					clazz = clazz.getSuperclass();
				}
			}
			if (getSourceViewerMethod == null) {
				throw new RuntimeException();
			}
			getSourceViewerMethod.setAccessible(true);
			ISourceViewer result = (ISourceViewer) getSourceViewerMethod.invoke(editor);
			return result;
		} catch (Exception e) {
			logger.error("Failed to acquire ISourceViewer via reflection", e);
			return null;
		}
	}

}
