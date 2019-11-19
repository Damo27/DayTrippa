package com.stuff.squishy.daytrippa;

import android.content.Context;
import android.content.Intent;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.widget.Toast;

import com.mapbox.geojson.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class AppAdaptor extends RecyclerView.Adapter<AppAdaptor.ContactViewHolder>
{
    private List<ApplicationInfo> apps;
    //private ArrayList<PInfo> apps;
    private Context context;
    private PackageManager pm;
    private Point orig;


        public AppAdaptor(Context context, List<ApplicationInfo> apps, PackageManager pm, Point orig)
        {
            this.apps = apps;
            this.context = context;
            this.pm = pm;
            this.orig = orig;
        }


        @Override
        public int getItemCount()
        {
            return apps.size();
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int pos)
        {
            holder.icon.setImageDrawable(apps.get(pos).loadIcon(pm));
            holder.appName = apps.get(pos).loadLabel(pm).toString();
            holder.packageName = apps.get(pos).packageName;

        }

        @Override
        public ContactViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
        {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.app_route, viewGroup, false);
            return new ContactViewHolder(itemView);
        }


        public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
        {
            ImageView icon;
            String packageName;
            String appName;
            public ContactViewHolder(View v)
            {
                super(v);
                icon = (ImageView) v.findViewById(R.id.app_icon);
                icon.setOnClickListener(this);
            }

            @Override
            public void onClick(View v)
            {
                //Toast.makeText(context, "App name: "+appName+" Package Name: "+packageName, Toast.LENGTH_SHORT).show();
                sendAppMsg(v, packageName);
            }

            public void sendAppMsg(View view, String app) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String text = "Trip I am taking.";
                // change with required  application package

                intent.setPackage(app);
                if (intent != null)
                {
                    //StringBuilder stringBuilder = new StringBuilder("http://maps.google.com/maps?saddr=");
                    StringBuilder stringBuilder = new StringBuilder("http://www.daytrippa.co.za/");
                    stringBuilder.append(orig.latitude());
                    stringBuilder.append("/");
                    stringBuilder.append(orig.longitude());
                    intent.putExtra(Intent.EXTRA_SUBJECT, text);
                    intent.putExtra(android.content.Intent.EXTRA_TEXT, stringBuilder.toString());

                    context.startActivity(Intent.createChooser(intent, text));
                } else {

                    Toast.makeText(context, "App not found", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        }



    }

