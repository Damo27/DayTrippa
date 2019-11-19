package com.stuff.squishy.daytrippa;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mapbox.mapboxsdk.maps.MapView;

import java.util.List;

public class RoutesAdaptor extends RecyclerView.Adapter<RoutesAdaptor.MyViewHolder>
{
    Context context;
    List<Model_RouteHistory> model_routeHistoryList;

    public RoutesAdaptor(Context context, List<Model_RouteHistory> model_routeHistoryList)
    {
        this.context = context;
        this.model_routeHistoryList = model_routeHistoryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_route, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i)
    {
        myViewHolder.txt_origin.setText("Origin: "+ model_routeHistoryList.get(i).getOrigin());
        myViewHolder.txt_destination.setText("Destination: "+model_routeHistoryList.get(i).getDestination());
        myViewHolder.txt_distance.setText("Distance: "+ model_routeHistoryList.get(i).getDistance().toString());
        myViewHolder.txt_duration.setText("Duration: "+ model_routeHistoryList.get(i).getDuration().toString());
    }

    @Override
    public int getItemCount() {
        return model_routeHistoryList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {

        TextView txt_origin, txt_destination, txt_duration, txt_distance;
        MapView map;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);

            txt_origin = (TextView)itemView.findViewById(R.id.txt_origin);
            txt_destination = (TextView)itemView.findViewById(R.id.txt_destination);
            txt_duration = (TextView)itemView.findViewById(R.id.txt_duration);
            txt_distance = (TextView)itemView.findViewById(R.id.txt_distance);
        }
    }
}
