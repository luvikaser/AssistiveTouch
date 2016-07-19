package com.example.luvikaser.assistivetouch;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;

public class FloatViewService extends Service {

    private static final String DATA = "PackageNames" ;
    private static final int nPACKAGENAMES = 9;
    private WindowManager mWindowManager;
    private ImageView mImageView = null;
    private WindowManager.LayoutParams mParams;
    private GestureDetector mGestureDetector;
    private ArrayList<String> mPackageNames;
    private SharedPreferences mSharedPreferences;
    private static final String CONFIGURATION_CHANGED = "android.intent.action.CONFIGURATION_CHANGED";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPackageNames = new ArrayList<>(Collections.nCopies(9, ""));
        mSharedPreferences = getSharedPreferences(DATA, Context.MODE_PRIVATE);
        if (mSharedPreferences != null) {
            for (int i = 0; i < nPACKAGENAMES; ++i){
                mPackageNames.set(i, mSharedPreferences.getString(i + "", ""));
            }
        }

        // Register broadcast receiver for configuration changed event
        IntentFilter filter = new IntentFilter();
        filter.addAction(CONFIGURATION_CHANGED);
        this.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ArrayList<String> tmpArray = null;

        if (intent != null) {
            tmpArray = intent.getStringArrayListExtra("package_names");
        }

        if (tmpArray != null) {
            mPackageNames = tmpArray;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            for(int i = 0; i < nPACKAGENAMES; ++i)
                editor.putString(i + "", mPackageNames.get(i));
            editor.apply();
        }
        if (mImageView != null) {
            return START_STICKY;
        }

        mGestureDetector = new GestureDetector(this, new SingleTapConfirm());
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mImageView = new ImageView(this);
        mImageView.setImageResource(R.mipmap.ic_launcher);

        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        mParams.gravity = Gravity.TOP | Gravity.LEFT;
        mParams.x = 0;
        mParams.y = 0;

        mImageView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            private DisplayMetrics mDisplayMetrics;

            @Override public boolean onTouch(View v, MotionEvent event) {

                mDisplayMetrics = new DisplayMetrics();
                mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);

                if (mGestureDetector.onTouchEvent(event)) {
                    // On click event, start the main activity
                    Intent intent = new Intent(FloatViewService.this, MainActivity.class);

                    // Use FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS to hide app from recent apps
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.putStringArrayListExtra("package_names", mPackageNames);
                    startActivity(intent);
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = mParams.x;
                        initialY = mParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_UP:
                        moveIconToSides();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        mParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mImageView, mParams);
                        return true;
                }
                return false;
            }
        });

        mWindowManager.addView(mImageView, mParams);

        return START_STICKY;
    }

    /** Listen to the screen rotation */
    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent myIntent) {

            if ( myIntent.getAction().equals(CONFIGURATION_CHANGED) ) {
                moveIconToSides();
            }
        }
    };

    /** Move the icon to a side of the screen */
    private void moveIconToSides() {
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);

        // Standardize the coordinates
        mParams.x = Math.min(Math.max(0, mParams.x), mDisplayMetrics.widthPixels - mImageView.getWidth());
        mParams.y = Math.min(Math.max(0, mParams.y), mDisplayMetrics.heightPixels - mImageView.getHeight());

        // Distance to 2 sides of screen
        int d1, d2;
        d1 = mParams.x;
        d2 = mDisplayMetrics.widthPixels - mParams.x - mImageView.getWidth();

        // Set the x-coordinate to the nearer side
        if (d1 < d2) {
            mParams.x = 0;
        } else {
            mParams.x = mDisplayMetrics.widthPixels - mImageView.getWidth();
        }

        mWindowManager.updateViewLayout(mImageView, mParams);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mImageView != null) {
            mWindowManager.removeView(mImageView);
            mImageView = null;
        }

        unregisterReceiver(mBroadcastReceiver);
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
    }
}
