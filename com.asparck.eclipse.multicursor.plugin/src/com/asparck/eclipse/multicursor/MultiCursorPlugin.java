package com.asparck.eclipse.multicursor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MultiCursorPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.asparck.eclipse.multicursor.plugin"; //$NON-NLS-1$

	// The shared instance
	private static MultiCursorPlugin plugin;
	
	/**
	 * The constructor
	 */
	public MultiCursorPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MultiCursorPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	private final Map<AbstractTextEditor, MultiCursor> multicursors = new HashMap<AbstractTextEditor, MultiCursor>();
	private final EditorCloseListener listener = new EditorCloseListener(multicursors);

	public MultiCursor getCursorFor(AbstractTextEditor editor) {
		MultiCursor multiCursor;
		synchronized (multicursors) {
			multiCursor = multicursors.get(editor);
			if (multiCursor == null) {
				multiCursor = new MultiCursor();
				editor.getSite().getPage().addPartListener(listener);
				multicursors.put(editor, multiCursor);
			}
		}
		return multiCursor;
	}

	/**
	 * Uses hacky reflection techniques to get at the source viewer of a given abstract text editor.
	 * Relies on protected final method {@link AbstractTextEditor#getSourceViewer()}.
	 */
	public static ISourceViewer getViewer(AbstractTextEditor editor) {
		try {
			Method getSourceViewer = null;
			Class<?> clazz = editor.getClass();
			while (clazz != null && getSourceViewer == null) {
				if (clazz.equals(AbstractTextEditor.class)) {
					getSourceViewer = clazz.getDeclaredMethod("getSourceViewer");
				} else {
					clazz = clazz.getSuperclass();
				}
			}
			if (getSourceViewer == null) {
				throw new RuntimeException();
			}
			getSourceViewer.setAccessible(true);
			ISourceViewer result = (ISourceViewer) getSourceViewer.invoke(editor);
			return result;
		} catch (Exception e) {
			log("Failed to get viewer", e);
			return null;
		}
	}

	public static void log(String msg, Exception e) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, msg, e));
	}
}
