package com.projecttango.examples.java.floorplanreconstruction;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static java.lang.Thread.sleep;

/**
 * Created by ECE_STUDENT on 12/11/2017.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(SplashActivity.this,
                MainActivity.class);
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startActivity(intent);


        finish();
    }

}
