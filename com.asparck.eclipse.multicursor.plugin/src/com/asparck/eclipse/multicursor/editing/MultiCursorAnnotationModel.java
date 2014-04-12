package com.asparck.eclipse.multicursor.editing;

import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;

public class MultiCursorAnnotationModel extends AnnotationModel {

	/**
	 * There's only 1 type of annotation that should be added to this model, and that's this type.
	 */
	public static final String ANNOTATION_TYPE = MultiCursorAnnotationModel.class.getCanonicalName()
			+ "#ANNOTATION_TYPE";

	public static final String ANNOTATION_MODEL_KEY = MultiCursorAnnotationModel.class.getCanonicalName()
			+ "#ANNOTATION_MODEL_KEY";

	public static MultiCursorAnnotationModel getFromOrCreateForViewer(ISourceViewer viewer) {
		final IAnnotationModelExtension baseAnnotationModelExt = getExtendedBaseAnnotationModel(viewer);
		final IAnnotationModel ourExistingModel = baseAnnotationModelExt.getAnnotationModel(ANNOTATION_MODEL_KEY);
		if (ourExistingModel != null) {
			return (MultiCursorAnnotationModel) ourExistingModel; // assume cast succeeds; should be our model
		} else {
			final MultiCursorAnnotationModel ourNewModel = new MultiCursorAnnotationModel();
			baseAnnotationModelExt.addAnnotationModel(ANNOTATION_MODEL_KEY, ourNewModel);
			return ourNewModel;
		}
	}

	public static void removeFromViewer(ISourceViewer viewer) {
		final IAnnotationModelExtension baseAnnotationModelExt = getExtendedBaseAnnotationModel(viewer);
		baseAnnotationModelExt.removeAnnotationModel(ANNOTATION_MODEL_KEY);
	}

	private static IAnnotationModelExtension getExtendedBaseAnnotationModel(ISourceViewer viewer) {
		final IAnnotationModel baseAnnotationModel = viewer.getAnnotationModel();
		if (baseAnnotationModel instanceof IAnnotationModelExtension) {
			return (IAnnotationModelExtension) baseAnnotationModel;
		} else {
			throw new IllegalStateException("Annotation model " + baseAnnotationModel + " on sourceviewer " + viewer
					+ " does not have expected extension");
		}
	}

	// We may want to use InclusivePositionUpdater to update registered positions like LinkedPositionAnnotations does.
	// (it *seems like* this will give us better handling of changes inside an annotation, but then do we really expect
	// that to happen? Most likely we'll only support our own changes, and bail out of multi cursor edit if something
	// other than us modifies the document.)

	@Override
	//overriden to make it public
	public void fireModelChanged() {
		super.fireModelChanged();
	}
}
