package com.projecttango.examples.java.floorplanreconstruction;

/**
 * Created by ECE_STUDENT on 11/15/2017.
 */

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;


public class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private String prevText;

    public Speaker(Context context){
        tts = new TextToSpeech(context, this);
        tts.setSpeechRate(1);
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            // Change this to match your
            // locale
            tts.setLanguage(Locale.US);
        }
    }

    public void speak(String text){
        if(!text.equals(prevText)){
            final String text1 = text;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tts.speak(text1, TextToSpeech.QUEUE_ADD, null);
                }},500);
        }
        prevText = text;
    }

    // Free up resources
    public void destroy(){
        tts.shutdown();
    }



}