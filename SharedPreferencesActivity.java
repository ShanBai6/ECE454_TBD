//Author : Kenny
//Internal storage for ObjectOutputStream

package edu.wisc.ece.ece454minilab5;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SharedPreferencesActivity extends Activity {

    private static final String TAG = "Minilab5";
    public static final String PREFS_MYRUNS = "MyPreferences";
    String FILENAME = " ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_shared_preferences);

        // Load user data to screen using the private helper function
        // loadProfile

        loadUserData();

    }

    public void onSaveClicked(View v) throws IOException {

        // Save all information from the screen into a "shared preferences"
        // using private helper function

        saveUserData();

        Toast.makeText(getApplicationContext(),
                getString(R.string.save_message), Toast.LENGTH_SHORT).show();

        Intent mIntent = new Intent(SharedPreferencesActivity.this,
                MainLayoutActivity.class);
        startActivity(mIntent);
    }

    public void onCancelClicked(View v) {

        Toast.makeText(getApplicationContext(),
                getString(R.string.cancel_message), Toast.LENGTH_SHORT).show();

        Intent mIntent = new Intent(SharedPreferencesActivity.this,
                MainLayoutActivity.class);
        startActivity(mIntent);
    }

    // ****************** private helper functions ***************************//

    // load the user data from shared preferences if there is no data make sure
    // that we set it to something reasonable
    private void loadUserData() {

        // We can also use log.d to print to the LogCat

        Log.d(TAG, "loadUserData()");

        // Load and update all profile views

        // Get the shared preferences - create or retrieve the activity
        // preference object

        String mKey = getString(R.string.preference_name);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);

        // Load the user email

        mKey = getString(R.string.preference_key_profile_email);
        String mValue = mPrefs.getString(mKey, " ");
        ((EditText) findViewById(R.id.editEmail)).setText(mValue);

        // Load gender info and set radio box

        mKey = getString(R.string.preference_key_profile_gender);

        int mIntValue = mPrefs.getInt(mKey, -1);
        // In case there isn't one saved before:
        if (mIntValue >= 0) {
            // Find the radio button that should be checked.
            RadioButton radioBtn = (RadioButton) ((RadioGroup) findViewById(R.id.radioGender))
                    .getChildAt(mIntValue);
            // Check the button.
            radioBtn.setChecked(true);
        }

    }

    // load the user data from shared preferences if there is no data make sure
    // that we set it to something reasonable
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void saveUserData() throws IOException {

        Log.d(TAG, "saveUserData()");

        // Getting the shared preferences editor
        String mKey = getString(R.string.student_names);
        SharedPreferences mPrefs = getSharedPreferences(mKey, MODE_PRIVATE);
        // Load name using email field
        mKey = getString(R.string.student_set);
        Set<String> mValue = mPrefs.getStringSet(mKey, new HashSet<String>());

        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.clear();


        // Save name
        String newStudent = (String) ((EditText) findViewById(R.id.editEmail))
                .getText().toString();
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("hello","kenny");
        //String FILENAME = " ";
        FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        ObjectOutputStream oout = new ObjectOutputStream(fos);
        oout.writeObject(map);
        oout.flush();
        fos.close();

        FileInputStream fin = openFileInput(FILENAME);
        ObjectInputStream oin = new ObjectInputStream(fin);

        HashMap<String, String> mapin = new HashMap<>();

        try {
            mapin = (HashMap<String, String>) oin.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("lol",mapin.get("hello"));


        String res = convertStreamToString(fin);
        Log.d("TEST",""+res);
      //  String studentName = new String(fin.read());

        fin.close();

       Log.d("!!!", newStudent);


        //mValue.add(newStudent);
        //mEditor.putStringSet(mKey, mValue);

        mEditor.commit();

        Toast.makeText(getApplicationContext(), "saved name: " + mValue,
                Toast.LENGTH_SHORT).show();

    }
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


}
