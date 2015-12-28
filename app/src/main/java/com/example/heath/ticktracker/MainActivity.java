package com.example.heath.ticktracker;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button mTestButtonButton;
    private Button mShowInfoButton;
    private TextView mTestInfoTextView;
    private Map<String, AppUsage> watchingList;
    private TickTrackerService.UsageBinder usageBinder;
    private String TAG = "xyz";
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            usageBinder = (TickTrackerService.UsageBinder) service;
            //watchingList = usageBinder.getWatchingList();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTestButtonButton = (Button) findViewById(R.id.test_button);
        mShowInfoButton = (Button) findViewById(R.id.show_info);
        mTestInfoTextView = (TextView) findViewById(R.id.test_info);

        /**
         * 如果Service是在绑定创建的，解除绑定时，service也会被销毁
         * 通过startService创建的服务必须通过stopService才能停止
         */
        Intent intent = new Intent(MainActivity.this,
                TickTrackerService.class);
        startService(intent);

        mTestButtonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this,
//                        TickTrackerService.class);
//                bindService(intent, connection, BIND_AUTO_CREATE);
            }
        });

        mShowInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watchingList = usageBinder.getWatchingList();
                Log.d(TAG, "onClick: " + watchingList.toString());
                mTestInfoTextView.setText(watchingList.get("com.tencent.mm")
                        .getPackageName() + watchingList.get("com.tencent.mm")
                        .getTotalTimeUsed() + "");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        Intent intent = new Intent(MainActivity.this,
                TickTrackerService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }



    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
        unbindService(connection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
