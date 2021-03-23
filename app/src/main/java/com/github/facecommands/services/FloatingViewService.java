package com.github.facecommands.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.core.app.ActivityCompat;

import com.github.facecommands.R;
import com.github.facecommands.Utils;
import com.github.facecommands.activities.MainActivity;
import com.github.facecommands.camera.CameraSourcePreview;
import com.github.facecommands.camera.GraphicOverlay;
import com.github.facecommands.faces.OnFaceGesture;
import com.github.facecommands.services.AutoService;
import com.google.android.gms.vision.CameraSource;

import java.io.IOException;

import static com.github.facecommands.activities.MainActivity.MY_PREFS;

public class FloatingViewService extends Service implements View.OnClickListener, OnFaceGesture {

    private static final String TAG = "FloatingView";


    private WindowManager mWindowManager;
    private WindowManager.LayoutParams params;
    private View myFloatingView;

    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;



    private int rightEyeCommand;
    private int leftEyeCommand;

    int[] swipeUp = new int[4];
    int[] swipeDown = new int[4];

    int posX = -1;
    int posY = -1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"onCreate");

        // Load settings
        loadSettings();


        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        int width = display.getMode().getPhysicalWidth();
        int height = display.getMode().getPhysicalHeight();

        Log.d(TAG,"Screen dimension " + width + "x" + height);

        int x_center = width/2;
        int y_center = height/2;

        swipeUp[0] = x_center;
        swipeUp[1] = y_center-50;
        swipeUp[2] = x_center;
        swipeUp[3] = y_center+50;

        swipeDown[0] = x_center;
        swipeDown[1] = y_center+50;
        swipeDown[2] = x_center;
        swipeDown[3] = y_center-50;


        //getting the widget layout from xml using layout inflater
        myFloatingView = LayoutInflater.from(this).inflate(R.layout.floating_view, null);

        int layout_parms;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            layout_parms = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        else {

            layout_parms = WindowManager.LayoutParams.TYPE_PHONE;

        }

        //setting the layout parameters
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_parms,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.START;

        if(posX != -1 & posY != -1) {
            params.x = posX;
            params.y = posY;
        }

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(myFloatingView, params);


        mPreview = (CameraSourcePreview) myFloatingView.findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) myFloatingView.findViewById(R.id.faceOverlay);

        //adding an touchlistener to make drag movement of the floating widget
        myFloatingView.findViewById(R.id.move).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("TOUCH","THIS IS TOUCHED");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        //this code is helping the widget to move around the screen with fingers
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(myFloatingView, params);
                        return true;
                }
                return false;
            }
        });


        myFloatingView.findViewById(R.id.stop).setOnClickListener(this);

        try {
            setupCamera();
            mPreview.start(mCameraSource, mGraphicOverlay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS, MODE_PRIVATE);
        rightEyeCommand = prefs.getInt("rightEyeCommand",-1);
        leftEyeCommand = prefs.getInt("leftEyeCommand",-1);
        posX = prefs.getInt("params_x",-1);
        posY = prefs.getInt("params_y",-1);
    }

    private void savePosition() {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS, MODE_PRIVATE).edit();
        editor.putInt("params_x",params.x);
        editor.putInt("params_y",params.y);
        editor.apply();
    }


    private void setupCamera() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            mCameraSource = Utils.createCameraSource(getApplicationContext(),mGraphicOverlay,this);
        } else {
            //requestCameraPermission();
            throw new Error("Missing permission");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        savePosition();
        Log.d(TAG,"onDestroy");

        if (myFloatingView != null) mWindowManager.removeView(myFloatingView);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.stop) {
            Intent intent = new Intent(getApplicationContext(), AutoService.class);
            intent.putExtra("action", "stop");
            getApplication().startService(intent);
            stopSelf();
        }
    }

    @Override
    public void onRightBlink() {
        onCommand (rightEyeCommand);
    }

    @Override
    public void onLeftBlink() {
        onCommand (leftEyeCommand);
    }

    @Override
    public void onSmile() {

    }


    private void onCommand(int val) {
        switch (val)
        {
            case 0:
                startService(getSwipeDownIntent());
                break;
            case 1:
                startService(getSwipeUpIntent());
                break;
            default:

                break;
        }
    }

    private Intent getSwipeUpIntent() {
        Intent intent = new Intent(this, AutoService.class);
        intent.putExtra("action","swipe");
        intent.putExtra("x1", swipeUp[0]);
        intent.putExtra("y1",  swipeUp[1]);
        intent.putExtra("x2",  swipeUp[2]);
        intent.putExtra("y2",  swipeUp[3]);
        return intent;
    }

    private Intent getSwipeDownIntent() {
        Intent intent = new Intent(this, AutoService.class);
        intent.putExtra("action","swipe");
        intent.putExtra("x1", swipeDown[0]);
        intent.putExtra("y1",  swipeDown[1]);
        intent.putExtra("x2",  swipeDown[2]);
        intent.putExtra("y2",  swipeDown[3]);
        return intent;
    }
}