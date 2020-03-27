package com;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.utils.PrefManager;

public class WelcomeActivity extends AppCompatActivity {

    private Button mAccpetButton, mCancelButton;
    private TextView mLink;
    private PrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checking for first time launch - before calling setContentView()
        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch()) {
            launchHomeScreen();
            finish();
        }

        setContentView(R.layout.welcome_activity);

        mAccpetButton = (Button) findViewById(R.id.accept);
        mCancelButton = (Button) findViewById(R.id.cancel);

        mAccpetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });

        //mLink = (TextView) findViewById(R.id.link);
        //mLink.setMovementMethod(LinkMovementMethod.getInstance());
        //mLink.setText(Html.fromHtml("Click on this link to visit my Website <br />" +
           //     "<a href='http://www.google.com'>Read terms and conditions here</a>"));
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }

}

