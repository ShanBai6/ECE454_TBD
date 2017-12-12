package com.projecttango.examples.java.floorplanreconstruction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.List;

import static java.lang.Thread.sleep;

public class MainActivity extends Activity {

    private static int numOfClick = 0;
    private static String startingPoint;
    private static String destination;
    private Button speechBtn;
    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 500;
    private final int SHORT_DURATION = 120;
    private static final int SPEECH_REQUEST_CODE = 200;
    private Speaker speaker;
    private boolean askForConfirm=false;
    private boolean confirmed=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speechBtn=(Button)findViewById(R.id.speechBtn);
        checkTTS();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (numOfClick < 2 && !confirmed) {
                    try {
                        sleep(1);
                    } catch (InterruptedException e) {
                    }
                }
                Intent intent = new Intent(MainActivity.this,
                        FloorPlanReconstructionActivity.class);
                intent.putExtra("start",startingPoint);
                intent.putExtra("destination", destination);
                startActivity(intent);

            }
        }).start();


    }
    @Override
    protected  void onResume(){
        super.onResume();

        /*Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent,SPEECH_REQUEST_CODE);
    */
    }
    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }
    public void onSpeechButtonClicked(View v){
        //check if permission is granted
       /* if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            ==PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},SPEECH_REQUEST_CODE);
        }else{
            Intent intent = new Intent(RecognizerIntent.ACTION_VOICE_SEARCH_HANDS_FREE);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizer.startListening(intent);
        }*/
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent,SPEECH_REQUEST_CODE);
    }
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            String[]  text= results.get(0).toLowerCase().split("\\s+");
            if(!askForConfirm){
                for(int i=0; i<text.length;i++){
                    if(numOfClick==2)
                        break;

                    switch(text[i]){
                        case "first": {
                            if(numOfClick==0)
                                startingPoint = "1";
                            else
                                destination="1";
                            numOfClick++;
                            break;
                        }
                        case "second":
                        {
                            if(numOfClick==0)
                                startingPoint = "2";
                            else
                                destination="2";
                            numOfClick++;
                            break;
                        }

                        case "third":
                        {
                            if(numOfClick==0)
                                startingPoint = "3";
                            else
                                destination="3";
                            numOfClick++;
                            break;
                        }
                        case "fourth":
                        {
                            if(numOfClick==0)
                                startingPoint = "4";
                            else
                                destination="4";
                            numOfClick++;
                            break;
                        }
                        default:
                            break;
                    }
                }
                //confirm
                speaker.speak("From "+startingPoint+ " to "+destination +", is that what you want?");
                askForConfirm=true;
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent,SPEECH_REQUEST_CODE);
            }else{
                askForConfirm=false;
                if(results.get(0).equals("Yes")){
                    confirmed=true;
                    return;
                }else{
                    numOfClick=0;
                    confirmed=false;
                    requestCode=CHECK_CODE;
                }
            }

        }

        // Text to speech
        if(requestCode == CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                speaker = new Speaker(this);
                speaker.speak("My lord, which floor do you want to go to?");
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                startActivityForResult(intent,SPEECH_REQUEST_CODE);

            }else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
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
