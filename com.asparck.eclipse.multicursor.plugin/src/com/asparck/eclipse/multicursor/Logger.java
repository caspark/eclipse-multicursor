package com.asparck.eclipse.multicursor;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class Logger {
	public static Logger create(Class<?> clazz) {
		return new Logger(clazz.getCanonicalName());
	}

	private final ILog pluginLog = MultiCursorPlugin.getDefault().getLog();

	private final String name;

	public Logger(String name) {
		this.name = name;
	}

	public void debug(String text) {
		System.out.println("DEBUG: " + formatMessage(text));
	}

	public void info(String text) {
		logToEclipseLog(IStatus.INFO, text, null);
	}

	public void warning(String text) {
		logToEclipseLog(IStatus.WARNING, text, null);
	}

	public void error(String text, Throwable error) {
		logToEclipseLog(IStatus.ERROR, text, error);
	}

	private String formatMessage(String message) {
		return name + " " + message;
	}

	private void logToEclipseLog(int iStatusLogLevel, String message, Throwable error) {
		pluginLog.log(new Status(iStatusLogLevel, MultiCursorPlugin.PLUGIN_ID, formatMessage(message), error));
	}
}
