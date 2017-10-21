package edu.wisc.ece.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    private static int numOfClick = 0;
    private String startingPoint;
    private String destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(numOfClick == 2){
            Intent intent = new Intent(MainActivity.this,
                    NavigationActivity.class);
            startActivity(intent);
        }
    }

    public void onFirstFloorClicked(View v) {
        if(numOfClick == 0){
            startingPoint = "firstFloor";
        }
        else{
            destination = "firstFloor";
        }
        numOfClick ++;
    }

    public void onSecondFloorClicked(View v) {
        if(numOfClick == 0){
            startingPoint = "secondFloor";
        }
        else{
            destination = "secondFloor";
        }
        numOfClick ++;
    }

    public void onThirdFloorClicked(View v) {
        if(numOfClick == 0){
            startingPoint = "thirdFloor";
        }
        else{
            destination = "thirdFloor";
        }
        numOfClick ++;
    }

    public void onFourthFloorClicked(View v) {
        if(numOfClick == 0){
            startingPoint = "FourthFloor";
        }
        else{
            destination = "FourthFloor";
        }
        numOfClick ++;
    }

    public String getStartingPoint(){
        return startingPoint;
    }

    public String getDestination(){
        return destination;
    }

}
