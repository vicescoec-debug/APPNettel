package com.nettel.maritimo.next;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.nettel.maritimo.next.work.AlertWorker;

import java.util.concurrent.TimeUnit;

public class NettelApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Constraints network = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest immediate = new OneTimeWorkRequest.Builder(AlertWorker.class)
                .setConstraints(network)
                .build();

        PeriodicWorkRequest periodic = new PeriodicWorkRequest.Builder(AlertWorker.class, 15, TimeUnit.MINUTES)
                .setConstraints(network)
                .build();

        WorkManager work = WorkManager.getInstance(this);
        work.enqueueUniqueWork("nettel-alerts-now", ExistingWorkPolicy.REPLACE, immediate);
        work.enqueueUniquePeriodicWork("nettel-alerts", ExistingPeriodicWorkPolicy.UPDATE, periodic);
    }
}
