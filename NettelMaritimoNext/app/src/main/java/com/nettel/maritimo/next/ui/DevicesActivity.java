package com.nettel.maritimo.next.ui;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;
import com.nettel.maritimo.next.R;
import com.nettel.maritimo.next.data.Device;
import com.nettel.maritimo.next.data.NettelRepository;
import com.nettel.maritimo.next.util.DateTimeUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DevicesActivity extends BaseActivity {
    private final List<Device> allDevices = new ArrayList<>();
    private final List<Device> devices = new ArrayList<>();
    private ArrayAdapter<Device> adapter;
    private TextView countLabel;
    private TextInputLayout searchLayout;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.page(this, "Dispositivos");
        countLabel = new TextView(this);
        countLabel.setTextColor(getColor(R.color.navy));
        countLabel.setTextSize(16);
        countLabel.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        countLabel.setPadding(Ui.dp(this, 14), Ui.dp(this, 8), Ui.dp(this, 14), Ui.dp(this, 8));

        searchLayout = Ui.field(this, "Buscar por embarcación, matrícula o serial", false);
        searchLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        searchLayout.setEndIconDrawable(R.drawable.ic_clear_x);
        searchLayout.setEndIconContentDescription("Borrar búsqueda");
        if (searchLayout.getEditText() != null) {
            EditText edit = searchLayout.getEditText();
            edit.setSingleLine(true);
            edit.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(String.valueOf(s)); }
                @Override public void afterTextChanged(Editable s) {}
            });
            searchLayout.setEndIconOnClickListener(v -> edit.setText(""));
        }

        ListView list = new ListView(this);
        adapter = new DeviceAdapter();
        list.setAdapter(adapter);

        root.addView(searchLayout);
        root.addView(countLabel);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        list.setOnItemClickListener((parent, view, position, id) -> {
            Device d = devices.get(position);
            startActivity(new Intent(this, MapActivity.class).putExtra("series", d.series));
        });

        load();
    }

    private void load() {
        async(
                () -> new NettelRepository(this).devicesForFleet(),
                items -> {
                    allDevices.clear();
                    allDevices.addAll(items);
                    devices.clear();
                    devices.addAll(allDevices);
                    Collections.sort(devices, (a, b) -> Long.compare(DateTimeUtils.utcMillis(b.located), DateTimeUtils.utcMillis(a.located)));
                    updateCount();
                    adapter.notifyDataSetChanged();
                    if (devices.isEmpty()) {
                        Ui.toast(this, "No hay dispositivos asignados para mostrar.", Toast.LENGTH_LONG);
                    }
                }
        );
    }

    private void applyFilter(String query) {
        devices.clear();
        for (Device d : allDevices) {
            if (matchesSearch(d, query)) devices.add(d);
        }
        Collections.sort(devices, (a, b) -> Long.compare(DateTimeUtils.utcMillis(b.located), DateTimeUtils.utcMillis(a.located)));
        updateCount();
        adapter.notifyDataSetChanged();
    }

    private boolean matchesSearch(Device d, String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String q = normalize(query);
        return normalize(d.name).contains(q)
                || normalize(d.registration).contains(q)
                || normalize(d.series).contains(q);
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.US)
                .trim();
    }

    private void updateCount() {
        if (searchLayout != null && searchLayout.getEditText() != null && searchLayout.getEditText().length() > 0) {
            countLabel.setText("Dispositivos enlistados: " + devices.size() + " de " + allDevices.size());
        } else {
            countLabel.setText("Dispositivos enlistados: " + devices.size());
        }
    }

    private class DeviceAdapter extends ArrayAdapter<Device> {
        DeviceAdapter() {
            super(DevicesActivity.this, 0, devices);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LinearLayout card = convertView instanceof LinearLayout ? (LinearLayout) convertView : new LinearLayout(DevicesActivity.this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(Ui.dp(DevicesActivity.this, 14), Ui.dp(DevicesActivity.this, 10), Ui.dp(DevicesActivity.this, 14), Ui.dp(DevicesActivity.this, 10));
            card.setBackgroundColor(Color.WHITE);
            card.removeAllViews();

            Device d = getItem(position);
            if (d == null) return card;

            TextView date = text(DateTimeUtils.utcToEcuador(d.located), 13, Color.BLACK, Typeface.BOLD);
            date.setGravity(Gravity.CENTER_HORIZONTAL);
            date.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC));
            card.addView(date);

            TextView series = text(value(d.series, "Sin serie"), 17, getColor(R.color.navy), Typeface.BOLD);
            series.setPaintFlags(series.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
            series.setPadding(0, Ui.dp(DevicesActivity.this, 8), 0, Ui.dp(DevicesActivity.this, 4));
            card.addView(series);

            card.addView(line("Embarcación:", value(d.name, "N/D")));
            card.addView(line("Matrícula:", value(d.registration, "N/D")));
            card.addView(line("Estado Batería:", d.batteryText()));

            LinearLayout location = new LinearLayout(DevicesActivity.this);
            location.setOrientation(LinearLayout.HORIZONTAL);
            location.addView(line("Lat:", coord(d.lat), 1));
            location.addView(line("Lng:", coord(d.lng), 1));
            card.addView(location);

            LinearLayout motion = new LinearLayout(DevicesActivity.this);
            motion.setOrientation(LinearLayout.HORIZONTAL);
            motion.addView(line("Rumbo:", d.formatHeading(), 1));
            motion.addView(line("Velocidad:", number(d.speed), 1));
            card.addView(motion);

            TextView type = text(value(d.messageType, value(d.status, "N/D")), 14, Color.BLACK, Typeface.BOLD);
            type.setPadding(0, Ui.dp(DevicesActivity.this, 4), 0, Ui.dp(DevicesActivity.this, 4));
            card.addView(type);

            View divider = new View(DevicesActivity.this);
            divider.setBackgroundColor(Color.BLACK);
            card.addView(divider, new LinearLayout.LayoutParams(-1, Ui.dp(DevicesActivity.this, 1)));
            return card;
        }

        private TextView line(String label, String value) {
            TextView t = text(label + " " + value, 14, Color.BLACK, Typeface.NORMAL);
            t.setPadding(0, Ui.dp(DevicesActivity.this, 2), 0, Ui.dp(DevicesActivity.this, 2));
            return t;
        }

        private TextView line(String label, String value, float weight) {
            TextView t = line(label, value);
            t.setLayoutParams(new LinearLayout.LayoutParams(0, -2, weight));
            return t;
        }

        private TextView text(String value, int sp, int color, int style) {
            TextView t = new TextView(DevicesActivity.this);
            t.setText(value);
            t.setTextSize(sp);
            t.setTextColor(color);
            t.setTypeface(Typeface.DEFAULT, style);
            return t;
        }
    }

    private String coord(double value) {
        if (Double.isNaN(value)) return "N/D";
        if (value == 0) return "0";
        return String.format(Locale.US, "%.5f", value);
    }

    private String number(double value) {
        if (Double.isNaN(value)) return "N/D";
        if (Math.abs(value - Math.round(value)) < 0.0001) return String.valueOf((int) Math.round(value));
        return String.format(Locale.US, "%.2f", value);
    }

    private String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
