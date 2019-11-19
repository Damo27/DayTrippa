package com.stuff.squishy.daytrippa;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity implements View.OnClickListener
{

    // variables needed for firebase
    private Connections conn;
    private FirebaseAuth fb_auth;
    private FirebaseDatabase fb_database;
    private DatabaseReference ref;

    private EditText ed_email;
    private EditText ed_password;
    private String userEmail, userPassword;
    private ImageView viewPass;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        conn = Connections.getInstance();
        fb_auth = conn.getFb_authInstance();
        fb_database = conn.getFb_database();

        ed_email = findViewById(R.id.txt_uName);
        ed_password = findViewById(R.id.txt_password);
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.txt_signUp).setOnClickListener(this);
        viewPass = findViewById(R.id.img_viewPassword);//.setOnClickListener(this);

        viewPass.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = MotionEventCompat.getActionMasked(event);

                switch (action)
                {
                    case (MotionEvent.ACTION_DOWN):
                        {
                            ed_password.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
                            return true;
                        }
                    case(MotionEvent.ACTION_UP):
                        {
                            ed_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            return true;
                        }
                }
                return false;
            }
        });

    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = fb_auth.getCurrentUser();
        loggedIn(currentUser);
    }

    private void loggedIn(FirebaseUser user)
    {
        boolean loggedIn = (user != null);
        if(loggedIn)
        {
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
        }
    }

    @Override
    public void onClick(View v)
    {
        int i = v.getId();

        if (i == R.id.btn_login)
        {
            userEmail = ed_email.getText().toString();
            userPassword = ed_password.getText().toString();
            signIn();
        }
        else
        if (i == R.id.txt_signUp)
        {
            userEmail = ed_email.getText().toString();
            userPassword = ed_password.getText().toString();

            if(userPassword.length() >= 6)
            {
                registerUser();
            }
            else
            {
                Toast.makeText(Login.this, "Password must be a minimum of 6 characters.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void signIn()
    {
        try
        {
            fb_auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                FirebaseUser user = fb_auth.getCurrentUser();
                                loggedIn(user);
                            }
                            else
                            {
                                Toast.makeText(Login.this, "Incorrect username and password.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        catch(IllegalArgumentException e)
        {
            Toast.makeText(this, "Email and Password cannot be empty", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Unable to login.", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Error message: " + e.getClass().toString(), Toast.LENGTH_LONG).show();
        }
    }

    //-----------------------------Method creates user with Firebase Auth email and password----------------
    private void registerUser()
    {
        try
        {
            fb_auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                FirebaseUser user = fb_auth.getCurrentUser();
                                ref = fb_database.getReference(fb_auth.getCurrentUser().getUid());
                                boolean tempImperial = false;
                                ref.child("Imperial").setValue(tempImperial);
                                Toast.makeText(Login.this, "Registration successful. Please Login.", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {

                                Toast.makeText(Login.this, "Registration failed: " + task.getException(), Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }
        catch(IllegalArgumentException e)
        {
            Toast.makeText(this, "Email field cannot be empty.", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Unable to login.", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Error message: " + e.getClass().toString(), Toast.LENGTH_LONG).show();
        }
    }
}
