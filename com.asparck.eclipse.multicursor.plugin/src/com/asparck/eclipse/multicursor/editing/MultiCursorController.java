package com.asparck.eclipse.multicursor.editing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.AnnotationPainter.HighlightingStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

import com.asparck.eclipse.multicursor.Logger;

/**
 * Coordinates starting and ending multi cursor editing.
 * 
 * Drawing of the selection / cursors is implemented using source viewer annotations.
 * 
 * How editing should work is undetermined so far; see {@link #setUpEditing(ISourceViewer, AnnotationModel)} for ideas.
 */
public class MultiCursorController {

	private static final Logger logger = Logger.create(MultiCursorController.class);

	private static final String ANNOTATION_DRAWING_STRATEGY = "cursorAnnotationDrawingStrategy";

	// FIXME allocate and dispose of color properly once edit is done
	private static final Color annotationColor = new Color(Display.getCurrent(), 255, 0, 0);

	public void splitToLines(ISourceViewer viewer) throws BadLocationException {
		final List<IRegion> selectionRegions = new ArrayList<IRegion>();

		//FIXME get the initial selected regions from the selection
//		final Point selOffsetAndLen = viewer.getSelectedRange();
//		final IDocument document = viewer.getDocument();
//		final int firstLineNum = document.getLineOfOffset(selOffsetAndLen.x);
//		final int lastLineNum = document.getLineOfOffset(selOffsetAndLen.x + selOffsetAndLen.y);
//		for (int line = firstLineNum; line <= lastLineNum; line++) {
//			selectionRegions.add(new Region(document.getLineOffset(line), document.getLineLength(line)));
//		}
		selectionRegions.add(new Region(5, 1)); //TODO remove once we have a proper set of selections

		startMultiCursorEdit(new CursorSelections(viewer, selectionRegions));
	}

	private void startMultiCursorEdit(CursorSelections cursorSelections) {
		final ISourceViewer viewer = cursorSelections.getViewer();


		final MultiCursorAnnotationModel annotationModel = MultiCursorAnnotationModel.getFromOrCreateForViewer(viewer);
		for (IRegion region : cursorSelections.getSelections()) {
			final Position position = new Position(region.getOffset(), region.getLength());
			final String description = "A secondary cursor from eclipse-multicursor";
			final Annotation annotation = new Annotation(MultiCursorAnnotationModel.ANNOTATION_TYPE, false, description);
			annotationModel.addAnnotation(annotation, position);
		}

		setUpAnnotationPainting(viewer, annotationModel);

		setUpEditing(viewer, annotationModel);
	}

	//TODO call this from somewhere, e.g. when Esc is hit when in a multi cursor edit
	private void endMultiCursorEdit(ISourceViewer viewer, AnnotationPainter annotationPainter) {
		MultiCursorAnnotationModel.removeFromViewer(viewer);

		tearDownAnnotationPainting(viewer, annotationPainter);
	}

	/**
	 * Creates and configures an annotation painter for the given viewer and annotation model.
	 * @param viewer
	 * @param annotationModel
	 */
	private void setUpAnnotationPainting(ISourceViewer viewer, MultiCursorAnnotationModel annotationModel) {
		// Some points on drawing the cursors using annotations:
		//  http://greensopinion.blogspot.com.au/2008/09/beyond-rich-text-tricks-using.html
		//  http://greensopinion.blogspot.com.au/2008/09/beyond-rich-text-displaying-images-in.html
		//  patches attached to https://bugs.eclipse.org/bugs/show_bug.cgi?id=246034
		//  http://rcpexperiments.blogspot.com.au/2009/07/annotationpainter-does-not-highlight.html

		//configure the annotation painter
		final AnnotationPainter annotationPainter = new AnnotationPainter(viewer, new DefaultMarkerAnnotationAccess());
		annotationPainter.addAnnotationType(MultiCursorAnnotationModel.ANNOTATION_TYPE, annotationPainter);
		annotationPainter.setAnnotationTypeColor(MultiCursorAnnotationModel.ANNOTATION_TYPE, annotationColor);
		annotationPainter.addAnnotationType(MultiCursorAnnotationModel.ANNOTATION_TYPE, MultiCursorController.ANNOTATION_DRAWING_STRATEGY);

//		annotationPainter.addDrawingStrategy(MultiCursorController.ANNOTATION_DRAWING_STRATEGY, new IBeamStrategy());
		annotationPainter.addTextStyleStrategy(MultiCursorController.ANNOTATION_DRAWING_STRATEGY, new HighlightingStrategy());
//				annotationPainter.addDrawingStrategy(MultiCursorController.ANNOTATION_DRAWING_STRATEGY, new IDrawingStrategy() {
//					@Override
//					public void draw(Annotation annotation, GC gc, StyledText textWidget, int offset, int length, Color color) {
//						logger.debug("Should now draw");
//					}
//				});

		logger.debug("Annotation painting configured added");

		final ITextViewerExtension4 viewerExt4 = (ITextViewerExtension4) viewer;
		viewerExt4.addTextPresentationListener(annotationPainter);
		final ITextViewerExtension2 viewerExt2 = (ITextViewerExtension2) viewer;
		viewerExt2.addPainter(annotationPainter); // causes redraw
	}

	private void tearDownAnnotationPainting(ISourceViewer viewer, AnnotationPainter annotationPainter) {
		final ITextViewerExtension4 viewerExt4 = (ITextViewerExtension4) viewer;
		viewerExt4.removeTextPresentationListener(annotationPainter);
		final ITextViewerExtension2 viewerExt2 = (ITextViewerExtension2) viewer;
		viewerExt2.removePainter(annotationPainter);
	}

	/** After changes to the model, it may be necessary to call this to get the annotation painter to repaint */
	//TODO figure out when (if) we need to call this manually
	private void triggerRedraw(AnnotationPainter annotationPainter, AnnotationModel annotationModel) {
		// TODO 3 approaches to trigger redraw; which should we use?

		// 1 - make the model tell its listeners that it changed
//		annotationModel.fireModelChanged(); //FIXME this doesn't always trigger the redraw for some reason?

		// 2 - tell the annotation that the annotation model changed completely (world change), and everything needs to
		// be redrawn.
		final boolean isWorldChange = true;
		annotationPainter.modelChanged(new AnnotationModelEvent(annotationModel, isWorldChange));

		// 3 call paint() on the painter directly
//		annotationPainter.paint(IPainter.INTERNAL); // untested!
	}

	// TODO make multi text editing actually do something
	private void setUpEditing(ISourceViewer viewer, AnnotationModel annotationModel) {
		//ITextViewerExtension has getRewriteTarget which lets us do compound changes so that undo / redo works
		//ITextViewerExtension2 has auto edit support that we might want

		final StyledText widget = viewer.getTextWidget();

		logger.debug("adding listener");

		widget.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				int start = e.start;
				int end = e.end;
				String text = e.text;
				logger.debug("start " + start + " end " + end + " text " + text);

				final Event copy = copyVerifyEvent(e);

				//FIXME this doesn't cause the copy to be re-evaluated.
				// How about we try hooking into the listener for the styled text directly instead?
				widget.getDisplay().post(copy);
			}
		});
	}

	private static Event copyVerifyEvent(VerifyEvent e) {
		final Event copy = new Event();
		copy.character = e.character;
		copy.data = e.data;
		copy.display = e.display;
		copy.doit = e.doit;
		copy.end = e.end;
		copy.keyCode = e.keyCode;
		copy.keyLocation =  e.keyLocation;
		copy.start = e.start;
		copy.stateMask = e.stateMask;
		copy.text = e.text;
		copy.time = e.time;
		copy.type = SWT.Verify;
		copy.widget = e.widget;
		return copy;
	}
}
