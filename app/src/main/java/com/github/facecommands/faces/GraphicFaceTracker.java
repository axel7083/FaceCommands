package com.github.facecommands.faces;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.github.facecommands.camera.GraphicOverlay;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

/**
 * Face tracker for each detected individual. This maintains a face graphic within the app's
 * associated face overlay.
 */
class GraphicFaceTracker extends Tracker<Face> {

    private static final String TAG = "GraphicFaceTracker";
    private GraphicOverlay mOverlay;
    private FaceGraphic mFaceGraphic;
    private OnFaceGesture onFaceGesture;

    GraphicFaceTracker(GraphicOverlay overlay, OnFaceGesture onFaceGesture) {
        mOverlay = overlay;
        this.onFaceGesture = onFaceGesture;
        mFaceGraphic = new FaceGraphic(overlay);
    }

    /**
     * Start tracking the detected face instance within the face overlay.
     */
    @Override
    public void onNewItem(int faceId, Face item) {
        mFaceGraphic.setId(faceId);
    }

    /**
     * Update the position/characteristics of the face within the overlay.
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        mOverlay.add(mFaceGraphic);
        mFaceGraphic.updateFace(face);

        if(face == null || onFaceGesture == null)
            return;

        new Handler(Looper.getMainLooper()).post(() -> {

            if(face.getIsSmilingProbability() >= 0.98) {
                onFaceGesture.onSmile();
            }

            if(face.getIsRightEyeOpenProbability() <= 0.1 && face.getIsLeftEyeOpenProbability() >= 0.9) {
                onFaceGesture.onRightBlink();
            }

            if(face.getIsLeftEyeOpenProbability() <= 0.1 && face.getIsRightEyeOpenProbability() >= 0.9) {
                onFaceGesture.onLeftBlink();
            }
        });

    }

    /**
     * Hide the graphic when the corresponding face was not detected.  This can happen for
     * intermediate frames temporarily (e.g., if the face was momentarily blocked from
     * view).
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        Log.d(TAG,"onMissing");
        mOverlay.remove(mFaceGraphic);
    }

    /**
     * Called when the face is assumed to be gone for good. Remove the graphic annotation from
     * the overlay.
     */
    @Override
    public void onDone() {
        Log.d(TAG,"onDone");
        mOverlay.remove(mFaceGraphic);
    }



}
