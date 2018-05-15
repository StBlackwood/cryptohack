package com.blackwood.cryptohack;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private List<Button> buttons;
    private JobScheduler mJobScheduler;
    private int displayID;
    private ArbitraryCheck dataObject;
    private TextView currnecyName, currencyKoinex, currencyBinance;
    private TextView assetsName, assetsKoinex, assetsBinance;
    private DecimalFormat formatter;
    private SharedPreferences sharedPreferences;

    private EditText limitEditText;
    private Button showButtton,limitButton;
    private CheckBox notifyCheckBox;
    private TextView ledgerTextView;
    private LinearLayout assetsLayout;
    private String messages = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mJobScheduler = (JobScheduler)
                getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1,
                new ComponentName(getPackageName(),
                        JobSchedulerService.class.getName()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setMinimumLatency(5000);
        else
            builder.setPeriodic(5000);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);

        sharedPreferences = getSharedPreferences("CryptoHack",Context.MODE_PRIVATE);
        initializeViews();
        createButtons();
        formatter = new DecimalFormat("#.000000", DecimalFormatSymbols.getInstance( Locale.ENGLISH ));

        Log.e(TAG,"checccccc");
        if (mJobScheduler.schedule(builder.build()) <= 0) {
            Log.e(TAG, "onCreate: Some error while scheduling the job");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ArbitraryCheck event) {
        this.dataObject = event;
        displayData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String event) {
        this.messages = event;
    }

    private void displayData(){
        if (dataObject != null){

            currnecyName.setText(ArbitraryCheck.kCurrencyList[displayID]);
            currencyBinance.setText(Double.toString(dataObject.getbPrices()[displayID]));
            currencyKoinex.setText(Double.toString(dataObject.getkPrices()[displayID]));

            int n = ArbitraryCheck.n;
            String names="",kRatios = "",bRatios="";

            int length = 10;
            for (int i=0;i<n;i++){
                names+=ArbitraryCheck.kCurrencyList[i]+"\n";
                String a = (dataObject.getkRatios()[displayID][i])+"";
                if (a.length()>length)
                    a = a.substring(0,length);
                kRatios+= a+"\n";
                a = (dataObject.getbRatios()[displayID][i])+"";
                if (a.length()>length)
                    a = a.substring(0,length);
                bRatios+= a+"\n";
//                kRatios+= (dataObject.getkRatios()[displayID][i])+"\n";
//                bRatios+= (dataObject.getbRatios()[displayID][i])+"\n";
            }

            assetsName.setText(names);
            assetsKoinex.setText(kRatios);
            assetsBinance.setText(bRatios);

            ledgerTextView.setText(messages);
        }
    }

    private void initializeViews(){
        currnecyName = (TextView) findViewById(R.id.currency_name);
        currencyBinance = (TextView) findViewById(R.id.currency_binance);
        currencyKoinex = (TextView) findViewById(R.id.currency_koinex);
        assetsBinance = (TextView) findViewById(R.id.assets_binance);
        assetsKoinex = (TextView) findViewById(R.id.assets_koinex);
        assetsName = (TextView) findViewById(R.id.assets_names);

        limitButton = (Button) findViewById(R.id.limit_button);
        limitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float value = Float.parseFloat(limitEditText.getText().toString());
                sharedPreferences.edit().putFloat("limit",value).apply();
            }
        });
        limitEditText = (EditText) findViewById(R.id.limit_edit_text);
        limitEditText.setText(Float.toString(sharedPreferences.getFloat("limit",3.0f)));

        notifyCheckBox = (CheckBox) findViewById(R.id.notifications_check);
        boolean notify = sharedPreferences.getBoolean("notify",true);
        notifyCheckBox.setChecked(notify);
        notifyCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e(TAG,""+isChecked);
                sharedPreferences.edit().putBoolean("notify",isChecked).apply();
            }
        });
        ledgerTextView = (TextView) findViewById(R.id.ledger_text_view);
        showButtton = (Button) findViewById(R.id.show_btn);
        showButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assetsLayout.setVisibility(View.GONE);
                ledgerTextView.setVisibility(View.VISIBLE);
                removeColorButtons();
            }
        });
        assetsLayout = (LinearLayout) findViewById(R.id.assets_data);
    }


    private void createButtons(){
        int n = ArbitraryCheck.n;
        LinearLayout leftLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_left);
        LinearLayout rightLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_right);
        Button button;
        buttons = new ArrayList<>();
        for (int i =0 ; i < n/2; i++){
            button = new Button(this);

            assignOnClick(button, i);
            button.setText(ArbitraryCheck.kCurrencyList[i]);

            buttons.add(button);
            leftLinearLayout.addView(button);
        }
        for (int i =n/2 ; i < n; i++){
            button = new Button(this);

            assignOnClick(button,i);
            button.setText(ArbitraryCheck.kCurrencyList[i]);

            buttons.add(button);
            rightLinearLayout.addView(button);
        }
    }

    private void removeColorButtons(){
        for (Button button: buttons)
            button.setBackgroundResource(android.R.drawable.btn_default);
    }

    private void assignOnClick(Button button, final int id){
        button.setBackgroundResource(android.R.drawable.btn_default);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button tempButton = (Button) v;
                removeColorButtons();
                tempButton.setBackgroundColor(Color.YELLOW);
                displayID = id;
                displayData();
                assetsLayout.setVisibility(View.VISIBLE);
                ledgerTextView.setVisibility(View.GONE);
            }
        });
    }

}
