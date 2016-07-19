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

    static final int MY_REQUEST_CODE = 12345;
    static final float SCREEN_RATIO = 0.6f;
    private List<ImageView> mImageList;
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
                intent.putExtra("MESSAGE_position", mPosition);
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

            // Vibrate device
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(500);

            // Start chooser
            Intent intent = new Intent(getBaseContext(), Chooser.class);
            intent.putExtra("MESSAGE_position", mPosition);
            startActivityForResult(intent, MY_REQUEST_CODE);
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
                mPackageNames.set(position, data.getStringExtra("MESSAGE_package_name"));
                try {
                    mImageList.get(position).setImageDrawable(mPm.getApplicationIcon(mPackageNames.get(position)));
                } catch (PackageManager.NameNotFoundException e) {
                    Log.e("package", "package name " + mPackageNames.get(position) + " not found");
                    mPackageNames.set(position, "");
                    mImageList.get(position).setImageResource(R.drawable.plussign);
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
