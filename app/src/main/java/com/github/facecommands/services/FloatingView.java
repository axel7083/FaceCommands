package com.github.facecommands.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.github.facecommands.R;
import com.github.facecommands.activities.MainActivity;
import com.github.facecommands.services.AutoService;

public class FloatingView extends Service implements View.OnClickListener {
    private WindowManager mWindowManager;
    private View myFloatingView;

    private static final String TAG = "FloatingView";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"onCreate");

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
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layout_parms,
                 WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);


        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(myFloatingView, params);



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

        //Button startButton = (Button) myFloatingView.findViewById(R.id.start);
        //startButton.setOnClickListener(this);

        int[] values = new int[] {R.id.start_layout, R.id.settings_layout, R.id.exit_layout};
        for(int i = 0 ;i < values.length; i++) {
            myFloatingView.findViewById(values[i]).setOnClickListener(this);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
        if (myFloatingView != null) mWindowManager.removeView(myFloatingView);
    }


    @Override
    public void onClick(View v) {
        //Log.d("onClick","THIS IS CLICKED");
        Intent intent = new Intent(getApplicationContext(), AutoService.class);

        if(v.getId() == R.id.start_layout) {
            // TODO: start camera (following the settings)
        }
        else if(v.getId() == R.id.start) {
            //Log.d("START","THIS IS STARTED");
            int[] location = new int[2];
            myFloatingView.getLocationOnScreen(location);
            intent.putExtra("action", "play");
            intent.putExtra("x", location[0] - 1);
            intent.putExtra("y", location[1] - 1);
        }
        else if(v.getId() == R.id.settings_layout) {
            /*Intent i = new Intent(this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);*/
        }
        else if(v.getId() == R.id.exit_layout) {
            intent.putExtra("action", "stop");
            mWindowManager.removeView(myFloatingView);
            Intent appMain = new Intent(getApplicationContext(), MainActivity.class);
        }

        getApplication().startService(intent);
    }

}