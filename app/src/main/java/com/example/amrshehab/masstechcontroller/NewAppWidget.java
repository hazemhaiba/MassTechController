package com.example.amrshehab.masstechcontroller;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.List;

import cz.msebera.android.httpclient.Header;


public class NewAppWidget extends AppWidgetProvider {

    static int ids;
    static SharedPreferences credentials;
    static RemoteViews rv;
    static Boolean opened=false;
    private static AppWidgetManager appWidgetManager1;
    NetworkRequest.Builder builder;

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
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            NewAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        Log.i("Scenario", "onEnabled");
        // Enter relevant functionality for when the first widget is created
        ids = 0;
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        ids -= 1;
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

        opened = intent.getAction().toString().equals("OPEN");
//      updating the widget's layout drawing
        if (appWidgetManager1!=null)
        {updateAppWidget(context,appWidgetManager1, ids);}
//       end of layout drawing update

        wifi_action(context, intent);

        super.onReceive(context, intent);
    }

    public void wifi_action(final Context context, final Intent intent) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
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
                WifiConfiguration configuration= new WifiConfiguration();
                configuration.SSID = "\"" + "GarageDoor" + "\"";
                configuration.preSharedKey="\""+ "12345678"  + "\"";
                configuration.priority = 99999;
                wifiManager.addNetwork(configuration);

                List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();

                Log.i("lastLog","4");
                for (final WifiConfiguration i : list) {
                    Log.i("lastLog",i.SSID);
                    if (i.SSID != null && i.SSID.equals("\"" + "GarageDoor" + "\"")) {
                        Log.i("lastLog", "Network Found, #"+i);
                        wifiManager.disconnect();
                        wifiManager.enableNetwork(i.networkId, true);
                        wifiManager.reconnect();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new NetworkRequest.Builder();
                            //set the transport type do WIFI
                            builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
                            connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
                                @Override
                                public void onAvailable(Network network) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        if (Build.VERSION.RELEASE.equalsIgnoreCase("6.0")) {
                                            if (!Settings.System.canWrite(context)) {
                                                Intent goToSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                                goToSettings.setData(Uri.parse("package:" + context.getPackageName()));
                                                context.startActivity(goToSettings);
                                            }
                                        }
                                        connectivityManager.bindProcessToNetwork(null);
                                        if (wifiManager.getConnectionInfo().getSSID() != null && wifiManager.getConnectionInfo().getSSID().equals("\"" + "GarageDoor" + "\"")) {
                                            connectivityManager.bindProcessToNetwork(network);
                                            Log.i("bind_process", "process  binded to network");

                                        } else {
                                            Log.i("bind_process", "process not binded to network");
                                        }

                                    } else {
                                        //This method was deprecated in API level 23
                                        ConnectivityManager.setProcessDefaultNetwork(null);
                                        if (wifiManager.getConnectionInfo().getSSID() != null && wifiManager.getConnectionInfo().getSSID().equals("\"" + "GarageDoor" + "\"")) {
                                            ConnectivityManager.setProcessDefaultNetwork(network);
                                            Log.i("bind_process", "process  binded to network");

                                        } else {
                                            Log.i("bind_process", "process not binded to network");

                                        }

                                    }
                                    try {
                                        //do a callback or something else to alert your code that it's ok to send the message through socket now
                                    } catch (Exception e) {
                                        Log.e(null, "onAvailable: ", e);
                                        e.printStackTrace();
                                    }
                                    connectivityManager.unregisterNetworkCallback(this);
                                }
                            });
                        }
                        Log.i("lastLog", "supposed to be connected by now");

                        client.get(context, "http://192.168.4.1/HOPEN", null, new AsyncHttpResponseHandler() {
                            @Override
                            public void onStart() {
                                Log.i("onstart", "onStart: Starting the request");
                                super.onStart();
                            }


                            @Override
                            public void onFinish() {
                                super.onFinish();
                                wifiManager.disconnect();
                                boolean deleted = wifiManager.removeNetwork(i.networkId);
                                Log.i("onfinish", "onFinish:  " + deleted);
                            }

                            @Override
                            public void onSuccess(int m, Header[] headers, byte[] bytes) {
                                wifiManager.setWifiEnabled(false);
                                Log.i("onsuccess", "onSuccess: in the success method");
                                for (int i = 1; i < ids; i++) {
                                    updateAppWidget(context, appWidgetManager1, ids);
                                    Log.i("lastLog", "Command Communicated Successfully!");
                                }
                                wifiManager.disconnect();
                                wifiManager.removeNetwork(i.networkId);

                            }

                            @Override
                            public void onFailure(int m, Header[] headers, byte[] bytes, Throwable throwable) {
                                Log.i("lastLog", "Command Communicated Failed");
                                wifiManager.disconnect();
                                wifiManager.removeNetwork(i.networkId);

                            }
                        });
                        break;
                    }
                }

            }

        }

        if  (intent.getAction().equals("SETTINGS")){
            Intent startSettings = new Intent(context, MainActivity.class);
            startSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(startSettings);
        }
    }
}