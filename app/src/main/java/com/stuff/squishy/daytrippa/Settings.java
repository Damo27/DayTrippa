package com.stuff.squishy.daytrippa;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Settings extends AppCompatActivity {

    private ImageButton menu;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Switch sw_units;

    private Connections conn = new Connections();
    private FirebaseAuth fb_auth;
    private FirebaseDatabase fb_database;
    private DatabaseReference ref;
    private boolean imperial = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        drawerLayout = findViewById(R.id.drawer_settings);
        navigationView = findViewById(R.id.settings_nav);
        menu = findViewById(R.id.img_menu_settings);
        sw_units = findViewById(R.id.switch_units);

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
                        intent = new Intent(Settings.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Share:
                    {
                        Connections.setShareON(true);
                        intent = new Intent(Settings.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Profile:
                    {
                        intent = new Intent(Settings.this, Profile.class);
                        startActivity(intent);
                        finish();
                        break;
                    }
                    case R.id.Settings:
                    {
                        drawerLayout.closeDrawers();
                        break;
                    }
                    case R.id.Logout:
                    {
                        //Logout
                        fb_auth.signOut();
                        intent = new Intent(Settings.this, Login.class);
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
                imperial = dataSnapshot.child("Imperial").getValue(boolean.class);
                if(imperial)
                {
                    sw_units.setChecked(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        sw_units.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {

                    if (isChecked)
                    {
                        imperial = true;
                    }
                    else
                        if(!isChecked)
                        {
                            imperial = false;
                        }
                    ref.child("Imperial").setValue(imperial);
            }
        });
    }
}
