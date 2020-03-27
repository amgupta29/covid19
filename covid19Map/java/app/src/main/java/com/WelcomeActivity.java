package com;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

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
        mLink = (TextView) findViewById(R.id.link);

        mAccpetButton.setOnClickListener(v -> launchHomeScreen());
        mCancelButton.setOnClickListener(v -> finishAffinity());
        mLink.setOnClickListener(v -> {
            // inflate the layout of the popup window
            LayoutInflater inflater = (LayoutInflater)
                    getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_window, null);
            // create the popup window
            int width = LinearLayout.LayoutParams.FILL_PARENT;
            int height = LinearLayout.LayoutParams.FILL_PARENT;
            boolean focusable = true; // lets taps outside the popup also dismiss it
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

            // show the popup window
            // which view you pass in doesn't matter, it is only used for the window tolken
            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            Toast toast = Toast.makeText(WelcomeActivity.this, "Touch anywhere on the screen to dismiss.",
                    Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 200);
            toast.show();

            // dismiss the popup window when touched
            //Button mOkButton = (Button) findViewById(R.id.ok);
            popupView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    popupWindow.dismiss();
                    return true;
                }
            });
        });
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
        finish();
    }

}

