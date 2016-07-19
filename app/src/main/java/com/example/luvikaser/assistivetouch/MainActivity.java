package com.example.luvikaser.assistivetouch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static final int MY_REQUEST_CODE = 12345;
    private static final float SCREEN_RATIO = 0.6f;
    private ArrayList<ImageView> mImageList;
    private ArrayList<String> mPackageNames;
    private PackageManager mPm;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private ImageView mImageView = null;
    private ImageView mDeleteImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get screen size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        // Set window size from screen size
        int size = Math.min(dm.widthPixels, dm.heightPixels);
        getWindow().setLayout((int)(size*SCREEN_RATIO), (int)(size*SCREEN_RATIO));

        mPm = getPackageManager();

        mImageList = new ArrayList<>();
        mImageList.add((ImageView) findViewById(R.id.imageView11));
        mImageList.add((ImageView) findViewById(R.id.imageView12));
        mImageList.add((ImageView) findViewById(R.id.imageView13));
        mImageList.add((ImageView) findViewById(R.id.imageView21));
        mImageList.add((ImageView) findViewById(R.id.imageView22));
        mImageList.add((ImageView) findViewById(R.id.imageView23));
        mImageList.add((ImageView) findViewById(R.id.imageView31));
        mImageList.add((ImageView) findViewById(R.id.imageView32));
        mImageList.add((ImageView) findViewById(R.id.imageView33));

        Intent intent = getIntent();
        mPackageNames = intent.getStringArrayListExtra("package_names");

        for (int i = 0; i < mImageList.size(); ++i) {
            if (mPackageNames.get(i).length() != 0) {
                try {
                    mImageList.get(i).setImageDrawable(mPm.getApplicationIcon(mPackageNames.get(i)));
                } catch (PackageManager.NameNotFoundException e) {
                    mPackageNames.set(i, "");
                }
            }

            mImageList.get(i).setOnClickListener(new MyOnClickListener(i));

            MyOnLongClickListener myOnLongClickListener = new MyOnLongClickListener(i);
            mImageList.get(i).setOnLongClickListener(myOnLongClickListener);
            mImageList.get(i).setOnTouchListener(myOnLongClickListener);
        }
    }

    private class MyOnClickListener implements View.OnClickListener {

        private int mPosition;
        MyOnClickListener(int pos) {
            mPosition = pos;
        }

        @Override
        public void onClick(View v) {
            if (mPackageNames.get(mPosition).length() != 0) {
                Intent intent = mPm.getLaunchIntentForPackage(mPackageNames.get(mPosition));
                finish();
                startActivity(intent);
            } else {
                Intent intent = new Intent(getBaseContext(), Chooser.class);
                intent.putExtra("MESSAGE_position", mPosition);
                intent.putStringArrayListExtra("MESSAGE_existPackages", mPackageNames);
                startActivityForResult(intent, MY_REQUEST_CODE);
            }
        }
    }

    private static boolean isPointInsideView(float x, float y, View view){
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        if(( x > viewX && x < (viewX + view.getWidth())) &&
                ( y > viewY && y < (viewY + view.getHeight()))){
            return true;
        } else {
            return false;
        }
    }

    private class MyOnLongClickListener implements View.OnLongClickListener, View.OnTouchListener {

        private int mPosition;
        private boolean mIsOnDrag = false;
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;
        private DisplayMetrics mDisplayMetrics;
        MyOnTouchListener mListener;

        MyOnLongClickListener(int pos) {
            mPosition = pos;
            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            mDisplayMetrics = new DisplayMetrics();
            mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if (!mIsOnDrag) {
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
            } else if (mListener != null) {
                mListener.onTouch(mImageView, motionEvent);
            }
            return false;
        }

        private class MyOnTouchListener implements View.OnTouchListener{
            private ImageView image;

            MyOnTouchListener(View v) {image = (ImageView) v;}

            @Override public boolean onTouch(View v, MotionEvent event) {

                mDisplayMetrics = new DisplayMetrics();
                mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return true;

                    case MotionEvent.ACTION_UP:
                        Log.e("pointer", "X = " + event.getRawX() + "Y =" + event.getRawY());

                        boolean mOK = true;
                        int pos = 0;
                        for(ImageView mImage: mImageList){
                            Log.e("image", pos+"");
                            if (isPointInsideView(event.getRawX(), event.getRawY(), mImage)){
                                if (pos == mPosition)
                                    break;
                                Drawable drawable = mImage.getDrawable();
                                mImage.setImageDrawable(mImageView.getDrawable());
                                image.setImageDrawable(drawable);

                                String mPackageName = mPackageNames.get(mPosition);
                                mPackageNames.set(mPosition, mPackageNames.get(pos));
                                mPackageNames.set(pos, mPackageName);

                                mOK = false;
                                break;
                            }
                            ++pos;
                        }

                        if (isPointInsideView(event.getRawX(), event.getRawY(), mDeleteImage)){
                            image.setImageResource(R.drawable.plussign);
                            mPackageNames.set(mPosition, "");
                            mOK = false;
                        }

                        if (mOK)
                            image.setImageDrawable(mImageView.getDrawable());
                        mWindowManager.removeView(mImageView);
                        mWindowManager.removeView(mDeleteImage);

                        mIsOnDrag = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        mParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mImageView, mParams);
                        return true;
                }
                return false;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            // Vibrate device
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);

            mIsOnDrag = true;

            mImageView = new ImageView(getBaseContext());
            mImageView.setImageDrawable(((ImageView)v).getDrawable());
            ((ImageView)v).setImageDrawable(null);

            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            mParams.gravity = Gravity.TOP | Gravity.LEFT;

            mParams.x = (int)initialTouchX - v.getWidth() / 2;
            mParams.y = (int)initialTouchY - v.getHeight() / 2;

            initialX = mParams.x;
            initialY = mParams.y;

            mListener = new MyOnTouchListener(v);
            mImageView.setOnTouchListener(mListener);

            mWindowManager.addView(mImageView, mParams);

            mDeleteImage = new ImageView(getBaseContext());
            mDeleteImage.setImageResource(R.mipmap.remove);
            WindowManager.LayoutParams mParamsDelete;
            mParamsDelete = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            mParamsDelete.gravity = Gravity.TOP | Gravity.LEFT;

            mParamsDelete.x =  mDisplayMetrics.widthPixels / 2 - mDeleteImage.getWidth() / 2;
            mParamsDelete.y = mDisplayMetrics.heightPixels - mDeleteImage.getHeight();

            mWindowManager.addView(mDeleteImage, mParamsDelete);
            return true;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("MainActivity", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                int position = data.getIntExtra("MESSAGE_position", 0);
                ArrayList<String> newPackages = data.getStringArrayListExtra("MESSAGE_newPackages");

                mPackageNames.set(position, newPackages.get(0));
                try {
                    mImageList.get(position).setImageDrawable(mPm.getApplicationIcon(mPackageNames.get(position)));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("package", "package name " + mPackageNames.get(position) + " not found");
                    mPackageNames.set(position, "");
                    mImageList.get(position).setImageResource(R.drawable.plussign);
                }

                int i = 1;
                for(int pos = 0; i < 9; ++pos) {
                    if (i >= newPackages.size())
                        break;
                    if (mPackageNames.get(pos).length() == 0) {
                        mPackageNames.set(pos, newPackages.get(i));
                        try {
                            mImageList.get(pos).setImageDrawable(mPm.getApplicationIcon(mPackageNames.get(pos)));
                        } catch (PackageManager.NameNotFoundException e) {
                            Log.e("package", "package name " + mPackageNames.get(pos) + " not found");
                            mPackageNames.set(pos, "");
                            mImageList.get(pos).setImageResource(R.drawable.plussign);
                        }
                        ++i;
                    }
                }
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Start service
        Intent intent = new Intent(this, FloatViewService.class);
        intent.putStringArrayListExtra("package_names", mPackageNames);
        startService(intent);
    }
}
