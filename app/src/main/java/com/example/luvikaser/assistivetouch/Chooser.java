package com.example.luvikaser.assistivetouch;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by haibt on 7/15/2016.
 */
public class Chooser extends Activity {
    private PackageManager pm;
    private List<ResolveInfo> launchables = null;
    private AppAdapter adapter = null;
    private boolean[] itemCheckeds = null;
    private Intent intent = null;
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        itemCheckeds = savedInstanceState.getBooleanArray("itemChecked");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chooser_layout);

        intent = getIntent();
        pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN, null);

        main.addCategory(Intent.CATEGORY_LAUNCHER);
        launchables = pm.queryIntentActivities(main, 0);
        Collections.sort(launchables,
                new ResolveInfo.DisplayNameComparator(pm));


        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent newIntent = new Intent();
                newIntent.putExtra("MESSAGE_position", intent.getIntExtra("MESSAGE_position", 0));

                ArrayList<String> listPackageChoose = new ArrayList<String>();
                for(int i = 0; i < launchables.size(); ++i)
                    if (AppAdapter.itemCheckeds[i]) {
                        listPackageChoose.add(launchables.get(i).activityInfo.packageName);
                    }
                newIntent.putStringArrayListExtra("MESSAGE_itemCheckeds", listPackageChoose);
                setResult(MainActivity.RESULT_OK, newIntent);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (itemCheckeds == null)
            itemCheckeds = new boolean[launchables.size()];

        adapter = new AppAdapter(this, launchables, pm, itemCheckeds, intent.getIntExtra("MESSAGE_nItem", 0));

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBooleanArray("itemChecked", AppAdapter.itemCheckeds);
    }
}