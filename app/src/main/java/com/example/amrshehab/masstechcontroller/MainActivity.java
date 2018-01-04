package com.example.amrshehab.masstechcontroller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import static com.example.amrshehab.masstechcontroller.R.*;


// main shared preference: "MainStorage"
// wifi password stored in "NetworkPassword"
// URL stored in "URL"
// Password for actions (open & close) stored in "PassKey"
// if not set, the default URL is "http://192.168.4.1/"
// the final sent URL is to be the url + the password + the action (OPEN/CLOSE)


public class MainActivity extends Activity {

    SharedPreferences credentials;
    private Button changePassKey;
    EditText passKey;

    private  Button changeURLBtn;
    EditText URLnewValue;

    private Button changeNetworkPass;
    EditText networkPass;

    private Button changeSSIDBtn;
    EditText newSSID;

    Button resetURLBtn;


    static Boolean networkPasswordChanged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_main);

//      need to instantiate the onCreate method so that the context is ready
        networkPasswordChanged =false;

        this.changeSSIDBtn=(Button)findViewById(id.changeSSIDBtn);
        newSSID=(EditText)findViewById(id.newSSID);

        this.resetURLBtn = (Button)findViewById(id.resetURL);

        this.changeNetworkPass=(Button)findViewById(id.changeNetworkPasswordBtn);
        networkPass=(EditText)findViewById(id.networkNewPass);

        this.changePassKey=(Button)findViewById(id.changePasskey);
        passKey=(EditText)findViewById(R.id.passkeyText);

        this.changeURLBtn =(Button)findViewById(id.changeURLBtn);
        URLnewValue=(EditText)findViewById(id.URLnewValue);

        credentials = getSharedPreferences("MainStorage",0);


        this.changeNetworkPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor= credentials.edit();
                editor.putString("NetworkPassword",networkPass.getText().toString());
                editor.commit();
                networkPasswordChanged=true;
            }
        });

        this.changeSSIDBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=credentials.edit();
                editor.putString("SSID",newSSID.getText().toString());
                editor.commit();
            }
        });

        this.changePassKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = credentials.edit();
                editor.putString("PassKey",passKey.getText().toString());
                editor.commit();
            }
        });

        this.changeURLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor urlChanging = credentials.edit();
                urlChanging.putString("URL", URLnewValue.getText().toString());
                urlChanging.commit();
            }
        });

        this.resetURLBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor=credentials.edit();
                editor.putString("URL", "http://192.168.4.1/");
                editor.commit();
            }
        });

            if (!networkPasswordChanged) {
                SharedPreferences.Editor editor = credentials.edit();
                editor.putString("networkPassword","123456");
                editor.commit();
            }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
