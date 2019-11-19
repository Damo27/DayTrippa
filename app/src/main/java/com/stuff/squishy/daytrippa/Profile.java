package com.stuff.squishy.daytrippa;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.api.directions.v5.DirectionsCriteria;

import java.util.ArrayList;
import java.util.List;

public class Profile extends AppCompatActivity
{
    private ImageButton menu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView routes;

    private Connections conn;
    private FirebaseAuth fb_auth;
    private FirebaseDatabase fb_database;
    private DatabaseReference ref;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        drawerLayout = findViewById(R.id.drawer_profile);
        navigationView = findViewById(R.id.profile_Nav);
        menu = findViewById(R.id.img_menu_profile);
        routes = findViewById(R.id.recycler_routes);
        routes.setHasFixedSize(true);
        routes.setLayoutManager(new LinearLayoutManager(Profile.this, LinearLayoutManager.HORIZONTAL, false));

        conn = Connections.getInstance();
        fb_auth = conn.getFb_authInstance();
        fb_database = conn.getFb_database();
        ref = fb_database.getReference(fb_auth.getCurrentUser().getUid());

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                Intent intent;
                switch (menuItem.getItemId())
                {
                    case R.id.Travel:
                    {
                        Connections.setShareON(false);
                        intent = new Intent(Profile.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Share:
                    {
                        Connections.setShareON(true);
                        intent = new Intent(Profile.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Profile:
                    {
                        drawerLayout.closeDrawers();
                        break;
                    }
                    case R.id.Settings:
                    {
                        intent = new Intent(Profile.this, Settings.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Logout:
                    {
                        //Logout
                        fb_auth.signOut();
                        intent = new Intent(Profile.this, Login.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                }
                return false;
            }
        });

        menu.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                drawerLayout.openDrawer(Gravity.LEFT);
                return false;
            }
        });

        ref.addValueEventListener(new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> routesSnapshots = dataSnapshot.child("Routes").getChildren();
                List<Model_RouteHistory> routesList = new ArrayList<>();

                for(DataSnapshot route: routesSnapshots)
                {
                    routesList.add(route.getValue(Model_RouteHistory.class));
                }

                RoutesAdaptor adaptor = new RoutesAdaptor(Profile.this,routesList);
                routes.setAdapter(adaptor);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
