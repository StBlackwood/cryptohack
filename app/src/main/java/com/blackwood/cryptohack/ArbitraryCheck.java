package com.blackwood.cryptohack;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by blackwood on 2/5/18.
 */

public class ArbitraryCheck {
    public static final String TAG = "Arbitrary Check";
    private double bPrices[],kPrices[];
    private double bRatios[][],kRatios[][];
    private final SharedPreferences sharedPreferences;
    public static final String kCurrencyList[] = {"AE", "AION", "BAT", "BCH", "BTC", "EOS", "ETH", "GAS", "GNT", "LTC", "NCASH", "NEO", "OMG", "ONT",
            "REQ", "TRX", "XLM", "XRB", "XRP", "ZRX"};

    private static final String bCurrencyList[] = {"AE", "AION", "BAT", "BCC", "BTC", "EOS", "ETH", "GAS", "GNT", "LTC", "NCASH", "NEO", "OMG", "ONT",
            "REQ", "TRX", "XLM", "NANO", "XRP", "ZRX"};
    public static final int n = kCurrencyList.length;


    public ArbitraryCheck(SharedPreferences sharedPreferences){
        bPrices = new double[n];
        kPrices = new double[n];
        bRatios = new double[n][n];
        kRatios = new double[n][n];
        this.sharedPreferences = sharedPreferences;
    }


    public List<MassageEvent> getArbitrary(JSONArray bJsonArray, JSONObject kJsonObject){

        double limit = sharedPreferences.getFloat("limit",3.0f);
        List<MassageEvent> messageList = new ArrayList<>();

        for (int x = 0; x < n; x++){
            try {
                kPrices[x]= kJsonObject.getJSONObject("prices").getJSONObject("inr").getDouble(kCurrencyList[x]);
                for (int y =0; y<bJsonArray.length();y++){
                    if (bJsonArray.getJSONObject(y).getString("symbol").equals(bCurrencyList[x]+"BTC")){
                        bPrices[x] = bJsonArray.getJSONObject(y).getDouble("price");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        bPrices[4] = 1.0;

        for (int i=0;i<n;i++) {
            for (int j = 0; j < n; j++) {
                bRatios[i][j] = bPrices[j] / bPrices[i];
                kRatios[i][j] = kPrices[j] / kPrices[i];
            }
        }
        for (int i=0;i<n;i++){
            for (int j=i+1; j<n; j++){
                double Ktemp = kRatios[i][j];
                double Btemp = bRatios[i][j];
                double difference = Ktemp - Btemp;
                double value = 100 * difference / Math.min(Ktemp,Btemp);
                if (Math.abs(value)>limit){
//                    String message = formatter.format(value)+"  "+kCurrencyList[j]+" -> "+kCurrencyList[i];
                    MassageEvent massageEvent = new MassageEvent(value,kCurrencyList[j],kCurrencyList[i]);
                    messageList.add(massageEvent);
                }
            }
        }
        Collections.sort(messageList, new Comparator<MassageEvent>() {
            @Override
            public int compare(MassageEvent o1, MassageEvent o2) {
                if (Math.abs(o1.getValue())>Math.abs(o2.getValue()))
                    return -1;
                else
                    return 1;
            }
        });

        return messageList;
    }

    public double[] getbPrices() {
        return bPrices;
    }

    public double[] getkPrices() {
        return kPrices;
    }

    public double[][] getbRatios() {
        return bRatios;
    }

    public double[][] getkRatios() {
        return kRatios;
    }
}
