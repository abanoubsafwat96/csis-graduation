package com.csis.social.app;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.csis.social.app.R;

public class MainActivity extends AppCompatActivity {

    //views
    Button mRegisterBtn, mLoginAsStudentBtn,mLoginAsAdminBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init views
        mRegisterBtn = findViewById(R.id.register_btn);
        mLoginAsStudentBtn = findViewById(R.id.loginAsStudent_btn);
        mLoginAsAdminBtn = findViewById(R.id.loginAsAdmin_btn);

        //handle register button click
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start RegisterActivity
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
        //handle login button click
        mLoginAsStudentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start LoginActivity
                Intent i=new Intent(MainActivity.this, LoginActivity.class);
                i.putExtra("userType","Student");
                startActivity(i);
            }
        });
        mLoginAsAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start LoginActivity
                Intent i=new Intent(MainActivity.this, LoginActivity.class);
                i.putExtra("userType","Admin");
                startActivity(i);
            }
        });
    }
}
/*In this Part(30):
 * ->Post Notifications:
 *   -We will use Firebase Topic Messaging
 *   -We will create settings screen where user can enable/disable post notification
 *   -Add menu item "Settings" in menu_main.xml to access Settings Activity
 *   -I'm only creating notifications for posts, but using this tutorial you can implement notifications for
 *    likes, comments etc too.
 * ->Require Libraries
 *   -Volley                    [Already added for chat notification]
 *   -Firebase cloud messaging  [Already added for chat notification]*/