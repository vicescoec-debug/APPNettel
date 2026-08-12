package com.nettel.maritimo.next.ui;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nettel.maritimo.next.R;
import com.nettel.maritimo.next.data.Alert;
import com.nettel.maritimo.next.data.AlertCache;
import com.nettel.maritimo.next.data.NettelRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AlertsActivity extends BaseActivity {
    private static final String TAG = "NettelAlerts";
    private final List<Alert> items = new ArrayList<>();
    private ListView list;
    private TextView summary;
    private ArrayAdapter<Alert> adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.page(this, "Alertas");
        View refresh = Ui.button(this, "Actualizar");
        View read = Ui.button(this, "Marcar todas como leídas");

        summary = new TextView(this);
        summary.setTextColor(getColor(R.color.navy));
        summary.setTextSize(15);
        summary.setPadding(Ui.dp(this, 14), Ui.dp(this, 8), Ui.dp(this, 14), Ui.dp(this, 8));

        list = new ListView(this);
        adapter = new AlertAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> openAlertOnMap(items.get(position)));

        root.addView(refresh);
        root.addView(read);
        root.addView(summary);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        refresh.setOnClickListener(v -> load());
        read.setOnClickListener(v -> markAllRead());

        load();
    }

    private void openAlertOnMap(Alert alert) {
        if (alert == null) return;
        Intent intent = new Intent(this, MapActivity.class);
        if (alert.series != null && !alert.series.trim().isEmpty()) {
            intent.putExtra("series", alert.series.trim());
        }
        if (!Double.isNaN(alert.lat) && !Double.isNaN(alert.lng) && (alert.lat != 0 || alert.lng != 0)) {
            intent.putExtra("focus_lat", alert.lat);
            intent.putExtra("focus_lng", alert.lng);
            intent.putExtra("focus_title", alert.title());
        }
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        async(
                () -> {
                    List<Alert> server = new NettelRepository(this).alerts(0);
                    try {
                        AlertCache cache = new AlertCache(this);
                        cache.saveAll(server);
                        return cache.merge(server);
                    } catch (Exception cacheError) {
                        Log.w(TAG, "No se pudo mezclar cache de alertas: " + cacheError.getMessage());
                        return server;
                    }
                },
                alerts -> {
                    items.clear();
                    items.addAll(alerts);
                    Log.i(TAG, "pantalla alertas count=" + items.size());
                    adapter.notifyDataSetChanged();
                    summary.setText(items.isEmpty()
                            ? "No hay alertas para mostrar."
                            : "Mostrando " + items.size() + " alertas recibidas por servidor/notificaciones.");
                    if (items.isEmpty()) {
                        Ui.toast(this, "No hay alertas disponibles.", Toast.LENGTH_SHORT);
                    }
                }
        );
    }

    private void markAllRead() {
        Set<Integer> unique = new LinkedHashSet<>();
        for (Alert a : items) {
            if (a.id != 0) unique.add(a.id);
        }
        if (unique.isEmpty()) {
            Ui.toast(this, "No hay alertas válidas para marcar como leídas.", Toast.LENGTH_LONG);
            return;
        }

        List<Integer> ids = new ArrayList<>(unique);
        Ui.toast(this, "Marcando " + ids.size() + " alertas como leídas...", Toast.LENGTH_SHORT);
        async(
                () -> {
                    new NettelRepository(this).markRead(ids);
                    AlertCache cache = new AlertCache(this);
                    cache.markRead(ids);
                    cache.clear();
                    return ids.size();
                },
                count -> {
                    Ui.toast(this, count + " alertas marcadas como leídas.", Toast.LENGTH_LONG);
                    items.clear();
                    adapter.notifyDataSetChanged();
                    summary.setText("No hay alertas para mostrar.");
                }
        );
    }

    private class AlertAdapter extends ArrayAdapter<Alert> {
        AlertAdapter() {
            super(AlertsActivity.this, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LinearLayout row = convertView instanceof LinearLayout ? (LinearLayout) convertView : new LinearLayout(AlertsActivity.this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(Ui.dp(AlertsActivity.this, 14), Ui.dp(AlertsActivity.this, 10), Ui.dp(AlertsActivity.this, 14), Ui.dp(AlertsActivity.this, 10));
            row.removeAllViews();

            Alert alert = getItem(position);
            TextView title = new TextView(AlertsActivity.this);
            title.setText(alert == null ? "Alerta" : alert.title());
            title.setTextColor(getColor(R.color.navy));
            title.setTextSize(16);
            title.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

            TextView detail = new TextView(AlertsActivity.this);
            detail.setText(alert == null ? "" : alert.detail());
            detail.setTextColor(Color.rgb(35, 45, 55));
            detail.setTextSize(14);
            detail.setPadding(0, Ui.dp(AlertsActivity.this, 4), 0, 0);

            View divider = new View(AlertsActivity.this);
            divider.setBackgroundColor(Color.rgb(225, 231, 238));

            row.addView(title);
            row.addView(detail);
            row.addView(divider, new LinearLayout.LayoutParams(-1, Ui.dp(AlertsActivity.this, 1)));
            return row;
        }
    }
}
