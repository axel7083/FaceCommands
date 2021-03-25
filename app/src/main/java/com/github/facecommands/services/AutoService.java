package com.github.facecommands.services;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.github.facecommands.activities.MainActivity;

import static com.github.facecommands.Utils.isServiceRunning;

public class AutoService extends AccessibilityService {

    private static final String TAG = "AutoService";

    private Handler mHandler;
    private int x1, x2, y1, y2;

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("auto-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    protected void onServiceConnected() {
        Log.d(TAG,"onServiceConnected");

        if(!checkPermission()) {
            Toast.makeText(this,"Please grant all permissions before use",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if(!isServiceRunning(FloatingViewService.class, this)) {
            Log.d(TAG,"FloatingViewService NOT running");
            startService(new Intent(this, FloatingViewService.class));
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        if(intent!=null){
            String action = intent.getStringExtra("action");
            if (action.equals("swipe")) {
                x1 = intent.getIntExtra("x1", 0);
                x2 = intent.getIntExtra("x2", 0);
                y1 = intent.getIntExtra("y1", 0);
                y2 = intent.getIntExtra("y2", 0);

                if (mRunnable == null) {
                    mRunnable = new IntervalRunnable();
                }

                mHandler.post(mRunnable);
            }
            else if(action.equals("stop")){
                mHandler.removeCallbacksAndMessages(null);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }




    private void touchTo(int x1, int y1, int x2, int y2) {
        Path swipePath = new Path();
        swipePath.moveTo(x1, y1);
        swipePath.lineTo(x2, y2);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 50));
        dispatchGesture(gestureBuilder.build(), null, null);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {
        Log.d(TAG,"onInterrupt");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        stopService(new Intent(this, FloatingViewService.class));
        super.onDestroy();
    }

    private IntervalRunnable mRunnable;

    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
            Log.d("IntervalRunnable","Runnable");
            touchTo(x1, y1, x2, y2);
        }
    }

    private boolean checkPermission() {
        return Settings.canDrawOverlays(this) &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED;
    }
}
