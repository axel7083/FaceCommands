package com.github.facecommands;

import android.app.ActivityManager;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.github.facecommands.camera.GraphicOverlay;
import com.github.facecommands.faces.GraphicFaceTrackerFactory;
import com.github.facecommands.faces.OnFaceGesture;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.FaceDetector;

public class Utils {

    private final static String TAG = "Utils";

    public static boolean isAccessServiceEnabled(Context context, Class accessibilityServiceClass)
    {
        String prefString = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return prefString!= null && prefString.contains(context.getPackageName() + "/" + accessibilityServiceClass.getName());
    }


    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    public static CameraSource createCameraSource(Context context, GraphicOverlay graphicOverlay, OnFaceGesture onFaceGesture) {

        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory(graphicOverlay, onFaceGesture))
                        .build());

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        return new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(25.0f)
                .build();
    }
}
