package edu.wisc.ece.ece454minilab7;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
//import org.apache.commons.compress.utils.IOUtils;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;


public class MainActivity extends Activity {
    private static Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        checkTTS();
        Button storeButton = (Button)findViewById(R.id.button1);
        storeButton.setOnClickListener(new View.OnClickListener() {
                                           public void onClick(View v) {
                                               //Store
                                               String[ ] aStr = new String[2] ;
                                               aStr[0] = ((EditText)findViewById(R.id.fieldInKey)).getText().toString();
                                               aStr[1] = ((EditText)findViewById(R.id.fieldInVal)).getText().toString();

                                               //Query
                                               String[ ] rStr = new String[1] ;
                                               rStr[0] = ((EditText)findViewById(R.id.inQKey)).getText().toString();

                                               if (!aStr[0].isEmpty() && !aStr[1].isEmpty())
                                               {
                                                   //Execute store request
                                                   httpStore hS = new httpStore();
                                                   hS.execute(aStr);
                                               }
                                               else if (!rStr[0].isEmpty())
                                               {
                                                   //Execute query
                                                   httpQuery hQ = new httpQuery();
                                                   hQ.execute(rStr);
                                               }
                                           }

            }
        );

    }



    protected class httpStore extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strs) {
            String reply = null;
            String temp1=""; //capture acknowledgement from server, if any

            //Construct an HTTP POST
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost storeVal = new HttpPost("http://ece454f15lab7.appspot.com/storeavalue");

            // Values to be sent from android app to server
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            // "tag" is the name of the text form on the webserver
            // "value" is the value that the client is submitting to the server
            // These two are specified by the server. The cilent side program must respect.
            nameValuePairs.add(new BasicNameValuePair("tag", strs[0]));
            nameValuePairs.add(new BasicNameValuePair("value", strs[1]));


            try {
                UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                storeVal.setEntity(httpEntity);

                //Execute HTTP POST
                HttpResponse response = httpclient.execute(storeVal);
                //Capture acknowledgement from server
                // In this demo app, the server returns "Update" if the tag already exists;
                // Otherwise, the server returns "New"
                temp1 = EntityUtils.toString(response.getEntity());
            }
            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.out.println("HTTP IO Exception.");
                e.printStackTrace();
            }


            // Decompose the server's acknowledgement into a JSON array
            try {
                JSONArray jsonArray = new JSONArray(temp1);
                reply = jsonArray.getString(0);

            } catch (JSONException e) {
                System.out.println("Error in JSON decoding");
                e.printStackTrace();
            }

            return reply;
        }
        // Process the server's acknowledgement
        @Override
        protected void onPostExecute(String res) {

            if (res.equalsIgnoreCase("Store")) {
                Toast.makeText(getApplicationContext(),
                        "Store new value!", Toast.LENGTH_SHORT).show();
            }
            else if(res.equalsIgnoreCase("Update")) {
                Toast.makeText(getApplicationContext(),
                        "Update existing value!", Toast.LENGTH_SHORT).show();
            }
            // Clean the text field
            ((EditText)findViewById(R.id.fieldInKey)).setText("");
            ((EditText)findViewById(R.id.fieldInVal)).setText("");
        }
    }

    protected class httpQuery extends AsyncTask<String, Void, String> {


            @Override
            protected String doInBackground(String... strs) {
                String reply = null;
                String temp1=""; //capture acknowledgement from server, if any

                //Construct an HTTP POST
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost getVal = new HttpPost("http://ece454f15lab7.appspot.com/getvalue");

                // Values to be sent from android app to server
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

                // "tag" is the name of the text form on the webserver
                // "value" is the value that the client is submitting to the server
                // These two are specified by the server. The cilent side program must respect.
                //nameValuePairs.add(new BasicNameValuePair("tag", strs[0]));
                //nameValuePairs.add(new BasicNameValuePair("value", strs[1]));
                nameValuePairs.add(new BasicNameValuePair("tag",strs[0]));

                try {
                    UrlEncodedFormEntity httpEntity = new UrlEncodedFormEntity(nameValuePairs);
                    getVal.setEntity(httpEntity);

                    //Execute HTTP POST
                    HttpResponse response = httpclient.execute(getVal);
                    //Capture acknowledgement from server
                    // In this demo app, the server returns "Update" if the tag already exists;
                    // Otherwise, the server returns "New"
                    temp1 = EntityUtils.toString(response.getEntity());
                }
                catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    System.out.println("HTTP IO Exception.");
                    e.printStackTrace();
                }
                // Decompose the server's acknowledgement into a JSON array
                try {
                    JSONArray jsonArray = new JSONArray(temp1);
                    reply = jsonArray.getString(2);

                } catch (JSONException e) {
                    System.out.println("Error in JSON decoding");
                    e.printStackTrace();
                }
                return reply;
        }

        @Override
        protected void onPostExecute(String res) {

//            if (res.equalsIgnoreCase("Value")) {
//                Toast.makeText(getApplicationContext(),
//                        "Querying!!!", Toast.LENGTH_SHORT).show();
//            }

            // Clean the text field
            //((TextView)(findViewById(R.id.outVal1))).setText("");
            ((TextView)(findViewById(R.id.outVal1))).setText(res);

            //((EditText)findViewById(R.id.fieldInVal)).setText("");
        }
    }
    public void onVoiceClick(View v) {
        displaySpeechRecognizer();
    }
    private static final int SPEECH_REQUEST_CODE = 0;

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // The text to speech
    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 500;
    private final int SHORT_DURATION = 120;
    private Speaker speaker;

    private void checkTTS(){
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            ((TextView)(findViewById(R.id.outVal1))).setText(spokenText);
        }

        // Text to speech
        if(requestCode == CHECK_CODE){
            if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
                speaker = new Speaker(this);
            }else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }

    public void saySomething(View v){
        //speaker.allow(true);
        speaker.allow(true);
        //speaker.pause(LONG_DURATION);
        speaker.speak("this is a test FUCK");
       // speaker.pause(SHORT_DURATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speaker.destroy();
    }






}

