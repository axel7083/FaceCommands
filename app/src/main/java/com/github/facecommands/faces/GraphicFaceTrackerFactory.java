package com.github.facecommands.faces;

import com.github.facecommands.camera.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

public class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {

    private GraphicOverlay mGraphicOverlay;
    private OnFaceGesture onFaceGesture;

    public GraphicFaceTrackerFactory(GraphicOverlay mGraphicOverlay, OnFaceGesture onFaceGesture) {
        this.mGraphicOverlay = mGraphicOverlay;
        this.onFaceGesture = onFaceGesture;
    }

    @Override
    public Tracker<Face> create(Face face) {
        return new GraphicFaceTracker(mGraphicOverlay, onFaceGesture);
    }
}

