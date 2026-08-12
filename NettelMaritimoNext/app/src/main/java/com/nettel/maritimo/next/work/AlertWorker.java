package com.nettel.maritimo.next.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.nettel.maritimo.next.data.Alert;
import com.nettel.maritimo.next.data.AlertCache;
import com.nettel.maritimo.next.data.NettelRepository;
import com.nettel.maritimo.next.data.SessionStore;

import java.util.List;

public class AlertWorker extends Worker {
    private static final String TAG = "NettelAlertWorker";

    public AlertWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SessionStore session = new SessionStore(context);
        if (!session.active()) return Result.success();

        try {
            AlertCache cache = new AlertCache(context);
            List<Alert> alerts = cache.unreadOnly(new NettelRepository(context).alerts(0));
            cache.saveAll(alerts);
            Log.i(TAG, "alertas recibidas=" + alerts.size());
            for (Alert alert : alerts) {
                NotificationHelper.show(context, alert);
            }
            Log.i(TAG, "notificaciones enviadas=" + alerts.size());
            return Result.success();
        } catch (Exception e) {
            Log.w(TAG, "fallo sincronizando alertas: " + e.getMessage());
            return Result.retry();
        }
    }
}
