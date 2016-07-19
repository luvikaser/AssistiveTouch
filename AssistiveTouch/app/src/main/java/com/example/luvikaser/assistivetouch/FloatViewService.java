package com.example.luvikaser.assistivetouch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
    private WindowManager.LayoutParams params;
    private GestureDetector mGestureDetector;
    private ArrayList<String> mPackageNames;
    private SharedPreferences mSharedPreferences;

    public FloatViewService() {
    }

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
            editor.commit();
        }
        if (mImageView != null) {
            return START_STICKY;
        }

        mGestureDetector = new GestureDetector(this, new SingleTapConfirm());
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mImageView = new ImageView(this);
        mImageView.setImageResource(R.mipmap.ic_launcher);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;

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
                    Intent intent = new Intent(FloatViewService.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.putStringArrayListExtra("package_names", mPackageNames);
                    startActivity(intent);
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:

                        params.x = Math.min(Math.max(0, params.x), mDisplayMetrics.widthPixels - mImageView.getWidth());
                        params.y = Math.min(Math.max(0, params.y), mDisplayMetrics.heightPixels - mImageView.getHeight());

                        // Distance to 2 sides of screen
                        int d1, d2;
                        d1 = params.x;
                        d2 = mDisplayMetrics.widthPixels - params.x - mImageView.getWidth();

                        if (d1 < d2) {
                            params.x = 0;
                        } else {
                            params.x = mDisplayMetrics.widthPixels - mImageView.getWidth();
                        }

                        mWindowManager.updateViewLayout(mImageView, params);

                        return true;

                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mImageView, params);
                        return true;
                }
                return false;
            }
        });

        mWindowManager.addView(mImageView, params);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mImageView != null) {
            mWindowManager.removeView(mImageView);
            mImageView = null;
        }
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return true;
        }
    }
}
