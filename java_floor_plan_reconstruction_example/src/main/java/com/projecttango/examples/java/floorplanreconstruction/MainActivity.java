package com.projecttango.examples.java.floorplanreconstruction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import static java.lang.Thread.sleep;

public class MainActivity extends Activity {

    private static int numOfClick = 0;
    private static String startingPoint;
    private static String destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (numOfClick < 2) {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                    }
                }

                Intent intent = new Intent(MainActivity.this,
                        FloorPlanReconstructionActivity.class);
                startActivity(intent);

            }
        }).start();


    }

    public void onFirstFloorClicked(View v) {
        if (numOfClick == 0) {
            startingPoint = "1";
        } else {
            destination = "1";
        }
        numOfClick++;
    }

    public void onSecondFloorClicked(View v) {
        if (numOfClick == 0) {
            startingPoint = "2";
        } else {
            destination = "2";
        }
        numOfClick++;
    }

    public void onThirdFloorClicked(View v) {
        if (numOfClick == 0) {
            startingPoint = "3";
        } else {
            destination = "3";
        }
        numOfClick++;
    }

    public void onFourthFloorClicked(View v) {
        if (numOfClick == 0) {
            startingPoint = "4";
        } else {
            destination = "4";
        }
        numOfClick++;
    }

    public static  String getStartingPoint() {
        return startingPoint;
    }

    public static String getDestination() {
        return destination;
    }

}
