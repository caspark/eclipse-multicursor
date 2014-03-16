package com.asparck.eclipse.multicursor.util;

import org.eclipse.swt.graphics.Point;

public class CoordinatesUtil {
	/**
	 * Converts offset and (possibly negative) length to absolute start and end positions.
	 */
	public static Point fromOffsetAndLengthToStartAndEnd(Point startAndOffset) {
		int selStart = Math.min(startAndOffset.x, startAndOffset.x + startAndOffset.y);
		int selEnd = Math.max(startAndOffset.x, startAndOffset.x + startAndOffset.y);
		return new Point(selStart, selEnd);
	}
}
