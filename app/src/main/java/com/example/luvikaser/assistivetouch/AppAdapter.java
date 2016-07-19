package com.example.luvikaser.assistivetouch;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Luvi Kaser on 7/18/2016.
 */
class AppAdapter extends BaseAdapter {
    private PackageManager pm = null;
    private Activity context;
    private int nItem;
    private List<ResolveInfo> apps;
    public static boolean[] itemCheckeds;

    AppAdapter(Activity context, List<ResolveInfo> apps, PackageManager pm, boolean[] itemCheckeds, int nItem) {
        super();
        this.pm = pm;
        this.context = context;
        this.apps = apps;
        this.itemCheckeds = itemCheckeds;
        this.nItem = nItem;
    }

    private class ViewHolder {
        ImageView apkIcon;
        TextView apkName;
        CheckBox ck1;
    }

    @Override
    public int getCount() {
        return apps.size();
    }

    @Override
    public ResolveInfo getItem(int i) {
        return apps.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView,
                        ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.row, null);

            holder = new ViewHolder();
            holder.apkIcon = (ImageView) convertView.findViewById(R.id.icon);
            holder.apkName = (TextView) convertView.findViewById(R.id.label);
            holder.ck1 = (CheckBox) convertView.findViewById(R.id.checkBox);

            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        holder.apkName.setText(getItem(position).loadLabel(pm));
        holder.ck1.setChecked(itemCheckeds[position]);
        holder.apkIcon.setImageDrawable(getItem(position).loadIcon(pm));

        holder.ck1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (holder.ck1.isChecked()) {
                    if (nItem == 0) {
                        holder.ck1.setChecked(false);
                        Toast.makeText(v.getContext(), "...", Toast.LENGTH_SHORT).show();
                    } else {
                        --nItem;
                        itemCheckeds[position] = true;
                    }
                } else {
                    ++nItem;
                    itemCheckeds[position] = false;

                }
            }
        });

        return convertView;
    }

}