package com.example.luvikaser.assistivetouch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static final int MY_REQUEST_CODE = 12345;
    private static final float SCREEN_RATIO = 0.6f;
    private ArrayList<ImageView> mImageList;
    private ArrayList<String> mPackageNames;
    private PackageManager mPm;

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
        mPackageNames = intent.getStringArrayListExtra(Constants.MESSAGE_PACKAGE_NAMES);

        for (int i = 0; i < mImageList.size(); ++i) {
            if (mPackageNames.get(i).length() != 0) {
                try {
                    mImageList.get(i).setImageDrawable(mPm.getApplicationIcon(mPackageNames.get(i)));
                } catch (PackageManager.NameNotFoundException e) {
                    mPackageNames.set(i, "");
                }
            }

            mImageList.get(i).setOnClickListener(new MyOnClickListener(i));

            mImageList.get(i).setOnLongClickListener(new MyOnLongClickListener(i));
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
                intent.putExtra(Constants.MESSAGE_POSITION, mPosition);
                intent.putStringArrayListExtra(Constants.MESSAGE_EXISTED_PACKAGES, mPackageNames);
                startActivityForResult(intent, MY_REQUEST_CODE);
            }
        }
    }

    private class MyOnLongClickListener implements View.OnLongClickListener {

        private int mPosition;
        MyOnLongClickListener(int pos) {
            mPosition = pos;
        }

        @Override
        public boolean onLongClick(View v) {

            // TODO: move app icon -> rearrange/remove

            // Vibrate device
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);

            // Start chooser
            Intent intent = new Intent(getBaseContext(), Chooser.class);
            intent.putExtra(Constants.MESSAGE_POSITION, mPosition);
            startActivityForResult(intent, MY_REQUEST_CODE);
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                int position = data.getIntExtra(Constants.MESSAGE_POSITION, 0);
                ArrayList<String> newPackages = data.getStringArrayListExtra(Constants.MESSAGE_NEW_PACKAGES);

                mPackageNames.set(position, newPackages.get(0));
                try {
                    mImageList.get(position).setImageDrawable(mPm.getApplicationIcon(mPackageNames.get(position)));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("package", "package name " + mPackageNames.get(position) + " not found");
                    mPackageNames.set(position, "");
                    mImageList.get(position).setImageResource(R.drawable.plussign);
                }

                int i = 1;
                for(int pos = 0; i < Constants.PACKAGE_NUMBER; ++pos) {
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
        intent.putStringArrayListExtra(Constants.MESSAGE_PACKAGE_NAMES, mPackageNames);
        startService(intent);
    }
}
