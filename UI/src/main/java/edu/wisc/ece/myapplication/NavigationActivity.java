package edu.wisc.ece.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class NavigationActivity extends Activity {

    private ImageView up;
    private ImageView left;
    private ImageView right;
    private ImageView down;
    private ImageView stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        up = findViewById(R.id.imageView8);
        left = findViewById(R.id.imageView7);
        right = findViewById(R.id.imageView6);
        down = findViewById(R.id.imageView5);
        stop = findViewById(R.id.imageView4);
        
        ShowPic("Up");

    }

    public void ShowPic(String direction){
        switch (direction.toUpperCase()){
            case "STOP":
                down.setAlpha(0.0f);
                right.setAlpha(0.0f);
                left.setAlpha(0.0f);
                up.setAlpha(0.0f);
                break;
            case "LEFT":
                down.setAlpha(0.0f);
                right.setAlpha(0.0f);
                stop.setAlpha(0.0f);
                up.setAlpha(0.0f);
                break;
            case "RIGHT":
                down.setAlpha(0.0f);
                left.setAlpha(0.0f);
                stop.setAlpha(0.0f);
                up.setAlpha(0.0f);
                break;
            case "UP":
                down.setAlpha(0.0f);
                left.setAlpha(0.0f);
                stop.setAlpha(0.0f);
                right.setAlpha(0.0f);
                break;
            default: //down
                right.setAlpha(0.0f);
                left.setAlpha(0.0f);
                stop.setAlpha(0.0f);
                up.setAlpha(0.0f);
                break;
        }
    }


}