package com.example.heath.ticktracker;

import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by heath on 15-12-25.
 */

public class TickTrackerService extends Service {
    public UsageStatsManager usageStatsManager;
    private boolean running = true;
    private  UsageStats oldAppStatus;
    private Map<String, AppUsage> wathcingList;
    private UsageBinder usageBinder = new UsageBinder();
    private String TAG = "xyz";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return usageBinder;
    }

    @Override
    public void onCreate() {
        oldAppStatus = null;
        Log.d(TAG, "onCreate: Service Created");
        wathcingList = new HashMap<>();
        wathcingList.put("com.tencent.mm", new AppUsage());
        wathcingList.get("com.tencent.mm").setPackageName("com.tencent.mm");
        usageStatsManager = (UsageStatsManager)getSystemService("usagestats");
        new WatchingForegroundAppThread().start();
    }

    private  void onAppSwitched() {
        AppUsage appUsage = wathcingList.get(oldAppStatus.getPackageName());
        appUsage.setLastTimeUsed(oldAppStatus.getLastTimeUsed());
        appUsage.setLastTimeQuit(System.currentTimeMillis());
        long total = appUsage.getTotalTimeUsed() + System
                .currentTimeMillis() - oldAppStatus.getLastTimeUsed();
        appUsage.setTotalTimeUsed(total);
        Log.d(TAG, "onAppSwitched: 上次启动时间" + oldAppStatus.getLastTimeUsed());
        Log.d(TAG, "onAppSwitched: 上次用时" + (System.currentTimeMillis() -
                oldAppStatus.getLastTimeUsed()));
        Log.d(TAG, "onAppSwitched: 总用时" + total);
    }

    private class WatchingForegroundAppThread extends Thread {
        @Override
        public void run() {
            while (running) {
                long ts = System.currentTimeMillis();
                List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats
                        (UsageStatsManager.INTERVAL_BEST, ts - 2000, ts);
                if (queryUsageStats != null && !queryUsageStats.isEmpty()) {
                    UsageStats recentStats = null;

                    for (UsageStats usageStats : queryUsageStats) {
                        if(recentStats == null || recentStats.getLastTimeUsed() <
                                usageStats.getLastTimeUsed()) {
                            recentStats = usageStats;
                        }
                    }
                    if (recentStats != null && (oldAppStatus == null ||
                            !oldAppStatus.getPackageName().equals(recentStats
                                    .getPackageName()))) {
                        if (oldAppStatus != null && oldAppStatus
                                .getPackageName().equals("com.tencent.mm")) {
                            onAppSwitched();
                        }
                        oldAppStatus = recentStats;
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Service");
        super.onDestroy();
        //TODO 在销毁之前把数据写入数据库
    }

    class UsageBinder extends Binder {
        public Map<String, AppUsage> getWatchingList() {
            return wathcingList;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: Unbind Service");
        return super.onUnbind(intent);
    }
}
