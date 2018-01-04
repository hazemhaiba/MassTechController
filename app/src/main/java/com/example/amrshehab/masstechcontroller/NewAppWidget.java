package com.example.amrshehab.masstechcontroller;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.loopj.android.http.*;
import org.json.JSONObject;
import java.util.List;
import cz.msebera.android.httpclient.Header;


public class NewAppWidget extends AppWidgetProvider {

    static int ids;
    private static AppWidgetManager appWidgetManager1;
    static SharedPreferences credentials;
    static RemoteViews rv;
    static Boolean opened=false;

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            NewAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.i("Scenario","onEnabled");
        // Enter relevant functionality for when the first widget is created
        ids=0;
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        ids-=1;
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.i("lastLog","updateAppWidget");
        ids=appWidgetId;
        appWidgetManager1=appWidgetManager;


        rv = new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

        if(opened) {
            rv.setImageViewResource(R.id.gatePreview, R.drawable.openedgate);}
        else{
            rv.setImageViewResource(R.id.gatePreview, R.drawable.closedgate);}

        rv.setOnClickPendingIntent(R.id.openBtb, getSelfPendingIntent(context, "OPEN"));
        rv.setOnClickPendingIntent(R.id.closeBtb, getSelfPendingIntent(context, "CLOSE"));
        rv.setOnClickPendingIntent(R.id.settingsBtb, getSelfPendingIntent(context, "SETTINGS"));

        appWidgetManager.updateAppWidget(ids, rv);


    }

    public static PendingIntent getSelfPendingIntent(Context context, String command){
        Log.i("lastLog","getSelfPendingIntent");
        Intent actionPerformed = new Intent(context, NewAppWidget.class);
        actionPerformed.setAction(command);
        actionPerformed.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getBroadcast(context, 0, actionPerformed, 0);
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.i("lastLog", "onUpdate");
        final int N = appWidgetIds.length;

        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i("lastLog","onReceive: "+intent.getAction().toString());
        String passKey=intent.getAction().toString() + context.getSharedPreferences("PassKey", 0);

        credentials = context.getSharedPreferences("MainStorage", 0);
        rv= new RemoteViews(context.getPackageName(), R.layout.new_app_widget);

        if (intent.getAction().toString().equals("OPEN")){
            opened=true;
        }
        else{
            opened=false;
        }
//      updating the widget's layout drawing
        if (appWidgetManager1!=null)
        {updateAppWidget(context,appWidgetManager1, ids);}
//       end of layout drawing update

        wifi_action(context, intent);

        super.onReceive(context, intent);
    }
    public void wifi_action(final Context context, Intent intent){

        //       start sending the HTTP REQUEST
        if (intent.getAction().toString().equals("OPEN") || intent.getAction().toString().equals("CLOSE")){
            Log.i("lastLog","1");
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            AsyncHttpClient client = new AsyncHttpClient();

            String storedSSID=credentials.getString("SSID","");
            String storedNetworkPass=credentials.getString("NetworkPassword","");
            String storedPassKey=credentials.getString("PassKey","");
            String storedUrl=credentials.getString("URL","")+storedPassKey+intent.getAction().toString();
            Log.i("lastLog","2");
//            Log.i("SSID",storedSSID);
//            Log.i("Network Password",storedNetworkPass);
//            Log.i("Action PassKey",storedPassKey);
//            Log.i("URL",storedUrl);

            if (wifiManager.isWifiEnabled()){
                wifiManager.setWifiEnabled(false);
                while(wifiManager.isWifiEnabled());
            }
            if (!wifiManager.isWifiEnabled()){
                wifiManager.setWifiEnabled(true);
                while (!wifiManager.isWifiEnabled());
            }
            Log.i("lastLog","3");
            if (wifiManager.isWifiEnabled()){
                for (WifiConfiguration wifiConfiguration: wifiManager.getConfiguredNetworks()) {
                    wifiManager.disableNetwork(wifiConfiguration.networkId);
                }
                WifiConfiguration configuration= new WifiConfiguration();
                configuration.SSID="\"" + "ESP32ap" + "\"";
                configuration.preSharedKey="\""+ "12345678"  + "\"";
                configuration.priority = 100000;
                int res = wifiManager.addNetwork(configuration);
                Log.d("WifiPreference", "add Network returned " + res);
                wifiManager.disconnect();
                boolean isEnable = wifiManager.enableNetwork(res, true);
                Log.d("WifiPreference", "enable Network returned " + isEnable);
                wifiManager.reconnect();
                client.get(context, "http://192.168.4.1/HOPEN", null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        wifiManager.setWifiEnabled(false);
                        for (int i = 1; i < ids; i++) {
                            updateAppWidget(context, appWidgetManager1, ids);
                            Log.i("lastLog", "Command Communicated Successfully!");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.i("lastLog", "Command Communicated Failed");
                    }
                });

                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

                for (int count=0;count<list.size();count++) {
                    Log.    i("lastLog",list.get(count).SSID);
                    if (list.get(count).SSID != null && list.get(count).SSID.equals("\"" + "ESP32ap" + "\"")) {
                        Log.i("lastLog", "Network Found, #"+list.get(count));
                        wifiManager.disconnect();
                        wifiManager.removeNetwork(list.get(count).networkId);
                        list=wifiManager.getConfiguredNetworks();
                        count=0;
                    }
                }


                for (WifiConfiguration config: wifiManager.getConfiguredNetworks()) {
                    wifiManager.enableNetwork(config.networkId, true);

                }
                /*List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

                Log.i("lastLog","4");
                for (WifiConfiguration i : list) {
                    Log.i("lastLog",i.SSID);
                    if (i.SSID != null && i.SSID.equals("\"" + "ESP32ap" + "\"")) {
                        Log.i("lastLog", "Network Found, #"+i);
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        Log.i("lastLog", "supposed to be connected by now");
                        client.get(context, "http://192.168.4.1/HOPEN", null, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                super.onSuccess(statusCode, headers, response);
                                wifiManager.setWifiEnabled(false);
                                for (int i = 1; i < ids; i++) {
                                    updateAppWidget(context, appWidgetManager1, ids);
                                    Log.i("lastLog", "Command Communicated Successfully!");
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                super.onFailure(statusCode, headers, responseString, throwable);
                                Log.i("lastLog", "Command Communicated Failed");
                            }
                        });
                        break;
                    }
                }*/

            }

        }

        if  (intent.getAction().equals("SETTINGS")){
            Intent startSettings = new Intent(context, MainActivity.class);
            startSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(startSettings);
        }
    }
}


