package edu.wisc.ece.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class NavigationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        ImageView myImage = (ImageView) findViewById(R.id.imageView4);
        myImage.setAlpha(0.0f);



    }
}