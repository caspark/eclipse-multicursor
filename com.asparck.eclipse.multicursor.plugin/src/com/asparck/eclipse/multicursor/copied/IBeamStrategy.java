package com.asparck.eclipse.multicursor.copied;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;


/**
 * Draws an iBeam at the given offset, the length is ignored.
 *
 * @since 3.0
 */
// Copied from org.eclipse.ui.texteditor.SourceViewerDecorationSupport.IBeamStrategy
public final class IBeamStrategy implements IDrawingStrategy {

	/*
	 * @see org.eclipse.jface.text.source.AnnotationPainter.IDrawingStrategy#draw(org.eclipse.jface.text.source.Annotation, org.eclipse.swt.graphics.GC, org.eclipse.swt.custom.StyledText, int, int, org.eclipse.swt.graphics.Color)
	 */
	@Override
	public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
		if (gc != null) {

			Point left= textWidget.getLocationAtOffset(offset);
			int x1= left.x;
			int y1= left.y;

			gc.setForeground(color);
			gc.drawLine(x1, y1, x1, left.y + textWidget.getLineHeight(offset) - 1);

		} else {
			/*
			 * The length for IBeam's is always 0, which causes no redraw to occur in
			 * StyledText#redraw(int, int, boolean). We try to normally redraw at length of one,
			 * and up to the line start of the next line if offset is at the end of line. If at
			 * the end of the document, we redraw the entire document as the offset is behind
			 * any content.
			 */
			final int contentLength= textWidget.getCharCount();
			if (offset >= contentLength) {
				textWidget.redraw();
				return;
			}

			char ch= textWidget.getTextRange(offset, 1).charAt(0);
			if (ch == '\r' || ch == '\n') {
				// at the end of a line, redraw up to the next line start
				int nextLine= textWidget.getLineAtOffset(offset) + 1;
				if (nextLine >= textWidget.getLineCount()) {
					/*
					 * Panic code: should not happen, as offset is not the last offset,
					 * and there is a delimiter character at offset.
					 */
					textWidget.redraw();
					return;
				}

				int nextLineOffset= textWidget.getOffsetAtLine(nextLine);
				length= nextLineOffset - offset;
			} else {
				length= 1;
			}

			textWidget.redrawRange(offset, length, true);
		}
	}
}