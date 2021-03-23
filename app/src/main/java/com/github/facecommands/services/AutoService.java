package com.github.facecommands.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class AutoService extends AccessibilityService {
    private Handler mHandler;
    private int mX;
    private int mY;

    private static final String TAG = "AutoService";

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
        startService(new Intent(this, FloatingView.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service","SERVICE STARTED");
        if(intent!=null){
            String action = intent.getStringExtra("action");
            if (action.equals("play")) {
                mX = intent.getIntExtra("x", 0);
                mY = intent.getIntExtra("y", 0);

                Log.d("x_value",Integer.toString(mX));
                Log.d("y_value",Integer.toString(mY));

                if (mRunnable == null) {
                    mRunnable = new IntervalRunnable();
                }
                //playTap(mX,mY);
                //mHandler.postDelayed(mRunnable, 1000);
                mHandler.post(mRunnable);
                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_SHORT).show();
            }
            else if(action.equals("stop")){
                mHandler.removeCallbacksAndMessages(null);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    //@RequiresApi(api = Build.VERSION_CODES.N)
    private void playTap(int x, int y) {
       //Log.d("TAPPED","STARTED TAPpING");
        Path swipePath = new Path();
        swipePath.moveTo(x, y);
        swipePath.lineTo(x+20, y+20);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 10));
        //dispatchGesture(gestureBuilder.build(), null, null);
        //Log.d("hello","hello?");
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d("Gesture Completed","Gesture Completed");
                super.onCompleted(gestureDescription);
                //mHandler.postDelayed(mRunnable, 1);
                mHandler.post(mRunnable);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                //Log.d("Gesture Cancelled","Gesture Cancelled");
                super.onCancelled(gestureDescription);
            }
        }, null);
        //Log.d("hi","hi?");
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
        stopService(new Intent(this, FloatingView.class));
        super.onDestroy();
    }

    private IntervalRunnable mRunnable;

    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
            Log.d("clicked","click");
            //playTap(mX, mY);
            touchTo(mX, mY, mX, mY);
        }
    }
}
