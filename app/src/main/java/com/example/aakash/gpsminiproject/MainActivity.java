package com.example.aakash.gpsminiproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity
{
    public static final String TAG = "MainActivity";
    public static final int ERROR_DIALOG_REQUEST = 9001;

    //When app is opened for the first time
    public boolean firstTime = true;

    public String mMyNumber = "";
    public String mTrackingNumber = "";

    //UI elements
    public EditText mMyNumberInputField;
    public EditText mTrackingNumberInputField;
    public Button mStartTracking;
    public TextView mInfo;

    private void writeToFile(String filename, String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput
                    (filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(String filename, Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null )
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e)
        {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e)
        {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public boolean isNumber(String s)
    {
        if(s.length() != 10)
        {
            return false;
        }

        for(int i = 0; i < 10; i++)
        {
            if(!Character.isDigit(s.charAt(i)))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Read the config file to get the registered number if already registered before.
        String savedMyNumber = readFromFile("config.txt", this);
        mMyNumber = (savedMyNumber.split("\\r?\\n"))[0];

        //If the config file had a number in it then this is not the first time, else it is a
        // first time
        firstTime = !(isNumber(mMyNumber));

        //Initialize handle to all UI elements
        mMyNumberInputField = (EditText)findViewById(R.id.myNumber);
        mTrackingNumberInputField = (EditText)findViewById(R.id.trackingNumner);
        mStartTracking = (Button)findViewById(R.id.btnMap);
        mInfo = (TextView)findViewById(R.id.info);


        if(!firstTime)
        {
            //If this is not the first time then disable the input field
            //that asks for the user's number
            mMyNumberInputField.setEnabled(false);
            mInfo.setText("Enter the number of person you want to track");
        }
        else
        {
            mMyNumberInputField.setEnabled(true);
            mInfo.setText("This is your first time using the app, so enter your number in the" +
                    " first field." +
                    "\nEnter the " +
                    "number " +
                    "of " +
                    "person you want to track in second field.");
        }

        if(isServicesOK())
        {
            init();
        }
    }

    private void init()
    {

        Button btnMap = (Button) findViewById(R.id.btnMap);

        //When "TRACK" button is clicked, do this.
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String errorMessage = "";
                mTrackingNumber = mTrackingNumberInputField.getText().toString();
                if(firstTime)
                {
                    mMyNumber = mMyNumberInputField.getText().toString();
                }
                if(!isNumber(mMyNumber))
                {
                    errorMessage += "Please enter a valid number for yourself.\n";
                }
                else
                {
                    writeToFile("config.txt", mMyNumber, MainActivity.this);
                }
                if(!isNumber(mTrackingNumber))
                {
                    errorMessage += "Please enter the number you want to track.\n";
                }


                if(errorMessage.length() == 0)
                {
                    //If all fields entered had valid numbers then send the numbers to MapActivity
                    //and start it.
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("MY_NUMBER", mMyNumber);
                    intent.putExtra("TRACKING_NUMBER", mTrackingNumber);
                    startActivity(intent);
                }
                else
                {
                    mInfo.setText(errorMessage);
                }
            }
        });
    }

    public boolean isServicesOK()
    {

        //Boilerplate code used for getting permissions to use Google play services for
        // displaying app.

        Log.d(TAG, "isServicesOK(): checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else
        {
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

}
