package com.blackwood.cryptohack;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

/**
 * Created by blackwood on 2/5/18.
 */

public class RequestHandling {
    private final Context context;
    private OkHttpClient client;
    private Request kRequest,bRequest;
    private static final String kUrl = "https://koinex.in/api/ticker";
    private static final String bUrl = "https://api.binance.com/api/v3/ticker/price";
    JSONObject kJSONObject;
    JSONArray bJSONArray;
    private static int i=0;
    private ArbitraryCheck arbitraryCheck;
    private DecimalFormat formatter;
    private SharedPreferences sharedPreferences;
    public static final String TAG = "Request Handling";

    public RequestHandling(Context context){
        client = new OkHttpClient();
        kRequest = new Request.Builder().url(kUrl).build();
        bRequest = new Request.Builder().url(bUrl).build();
        formatter = new DecimalFormat("#.00", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));
        formatter.setRoundingMode( RoundingMode.DOWN );
        sharedPreferences = context.getSharedPreferences("CryptoHack",Context.MODE_PRIVATE);
        arbitraryCheck = new ArbitraryCheck(sharedPreferences);
        this.context = context;
    }


    public void executeTask(){


        client.newCall(kRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {
                    String jsonData = response.body().string();
                    try {
                        kJSONObject = new JSONObject(jsonData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    client.newCall(bRequest).enqueue(new Callback() {
                       @Override
                       public void onFailure(Request request, IOException e) {
                           e.printStackTrace();
                       }

                       @Override
                       public void onResponse(Response response) throws IOException {
                           if (!response.isSuccessful()) {
                               throw new IOException("Unexpected code " + response);
                           } else {
                               String jsonData = response.body().string();
                               try {
                                   bJSONArray = new JSONArray(jsonData);
                                   createNotification(arbitraryCheck.getArbitrary(bJSONArray,kJSONObject));
                                   EventBus.getDefault().post(arbitraryCheck);
                               } catch (JSONException e) {
                                   e.printStackTrace();
                               }
                           }
                       }
                   });
                }
            }
        });
    }


    private void createNotification(List<MassageEvent> messageList){
        if (messageList.size()>1){
            String message = "";
            String messages = "";
            MassageEvent massageEvent;
            for (int i=0;i<messageList.size();i++){
                massageEvent = messageList.get(i);
                //                    String message = formatter.format(value)+"  "+kCurrencyList[j]+" -> "+kCurrencyList[i];
                message = formatter.format(massageEvent.getValue()) +"  " + massageEvent.getFromCurrency()
                        + " -> "+ massageEvent.getToCurrency();
                if (i==0)
                    messages += message;
                else
                    messages += "\n" + message;

            }
            massageEvent = messageList.get(0);
            message = formatter.format(massageEvent.getValue()) +"  " + massageEvent.getFromCurrency()
                    + " -> "+ massageEvent.getToCurrency();
            EventBus.getDefault().post(messages);
            boolean notify = sharedPreferences.getBoolean("notify",true);
            Log.e(TAG,""+notify);
            if (notify)
                addNotification("Crypto Hack",message, messages);
        }

    }

    private void addNotification(String title, String smallContent, String largeContent) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.crypto_icon)
                        .setContentTitle(title)
                        .setContentText(smallContent)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(largeContent));
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }
}
