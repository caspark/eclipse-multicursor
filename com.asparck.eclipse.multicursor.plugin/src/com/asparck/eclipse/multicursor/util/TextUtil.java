package com.asparck.eclipse.multicursor.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.Point;

public class TextUtil {
	private static final Pattern WORD_RE = Pattern.compile("\\w+");

	/**
	 * Scans backward from the given offset to identify a word which contains {@code offset}.
	 * 
	 * @return position (x) and length (y) of the last word in {@code text} which contains {@code offset}, or null if no
	 *         word containing {@code offset} could be identified (i.e. offset is in whitespace)
	 */
	public static Point findWordSurrounding(String text, int offset) {
		int wordOffset = offset == 0 ? 0 : offset - 1;
		Point lastWordFound = null;
		Matcher matcher = WORD_RE.matcher(text);
		while (wordOffset >= 0) {
			boolean found = matcher.find(wordOffset);
			if (found && matcher.start() <= offset && matcher.end() >= offset) {
				lastWordFound = new Point(matcher.start(), matcher.end() - matcher.start());
				wordOffset--; // see if there's another match behind
			} else {
				break;
			}
		}

		return lastWordFound;
	}
}
