package edu.wisc.ece.myapplication;

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
                        sleep(5);
                    } catch (InterruptedException e) {
                    }
                }
                Intent intent = new Intent(MainActivity.this,
                        NavigationActivity.class);
                startActivity(intent);
            }
        }).start();
    }

    public void onFirstFloorClicked(View v) {
        if (numOfClick == 0) {
            startingPoint = "firstFloor";
        } else {
            destination = "firstFloor";
        }
        numOfClick++;
    }

    public void onSecondFloorClicked(View v) {
        if (numOfClick == 0) {
            startingPoint = "secondFloor";
        } else {
            destination = "secondFloor";
        }
        numOfClick++;
    }

    public void onThirdFloorClicked(View v) {
        if (numOfClick == 0) {
            startingPoint = "thirdFloor";
        } else {
            destination = "thirdFloor";
        }
        numOfClick++;
    }

    public void onFourthFloorClicked(View v) {
        if (numOfClick == 0) {
            startingPoint = "FourthFloor";
        } else {
            destination = "FourthFloor";
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
