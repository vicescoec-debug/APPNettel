package com.nettel.maritimo.next.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.nettel.maritimo.next.R;
import com.nettel.maritimo.next.data.Device;
import com.nettel.maritimo.next.data.NettelRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends BaseActivity {
    private final List<Device> devices = new ArrayList<>();
    private final List<String> deviceLabels = new ArrayList<>();
    private DeviceSearchAdapter deviceAdapter;
    private Device selectedDeviceValue;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.page(this, "Histórico de posiciones");
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        Ui.pad(form, 12);

        TextView deviceLabel = new TextView(this);
        deviceLabel.setText("Buscar dispositivo");
        deviceLabel.setTextSize(15);
        deviceLabel.setTextColor(getColor(R.color.navy));
        form.addView(deviceLabel);

        TextInputLayout deviceSearchLayout = new TextInputLayout(this);
        deviceSearchLayout.setHintEnabled(false);
        deviceSearchLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        deviceSearchLayout.setBoxBackgroundColor(android.graphics.Color.WHITE);
        deviceSearchLayout.setBoxStrokeColor(getColor(R.color.navy));
        deviceSearchLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        deviceSearchLayout.setEndIconDrawable(R.drawable.ic_clear_x);
        deviceSearchLayout.setEndIconContentDescription("Borrar búsqueda");

        AutoCompleteTextView deviceSearch = new AutoCompleteTextView(this);
        deviceSearch.setHint("Escriba nombre, matrícula o serial");
        deviceSearch.setSingleLine(true);
        deviceSearch.setThreshold(1);
        deviceSearch.setTextColor(android.graphics.Color.rgb(20, 31, 43));
        deviceSearch.setHintTextColor(android.graphics.Color.rgb(59, 80, 102));
        deviceSearch.setTextSize(16);
        deviceSearchLayout.addView(deviceSearch, new LinearLayout.LayoutParams(-1, Ui.dp(this, 56)));
        form.addView(deviceSearchLayout, new LinearLayout.LayoutParams(-1, Ui.dp(this, 72)));

        TextView selectedDevice = new TextView(this);
        selectedDevice.setText("Seleccione un dispositivo");
        selectedDevice.setTextSize(16);
        selectedDevice.setTextColor(getColor(R.color.navy));
        selectedDevice.setPadding(0, 0, 0, Ui.dp(this, 10));
        form.addView(selectedDevice);

        deviceAdapter = new DeviceSearchAdapter();
        deviceSearch.setAdapter(deviceAdapter);
        deviceSearch.setOnClickListener(v -> deviceSearch.showDropDown());
        deviceSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) deviceSearch.showDropDown();
        });
        deviceSearchLayout.setEndIconOnClickListener(v -> {
            deviceSearch.setText("", false);
            selectedDeviceValue = null;
            selectedDevice.setText("Seleccione un dispositivo");
            deviceSearch.requestFocus();
            deviceSearch.showDropDown();
        });
        deviceSearch.setOnItemClickListener((parent, view, position, id) -> {
            String label = (String) parent.getItemAtPosition(position);
            selectedDeviceValue = findDeviceByLabel(label);
            if (selectedDeviceValue != null) {
                selectedDevice.setText("Seleccionado: " + labelFor(selectedDeviceValue));
            }
        });

        TextInputLayout from = Ui.field(this, "Fecha desde (AAAA-MM-DD)", false);
        TextInputLayout to = Ui.field(this, "Fecha hasta (AAAA-MM-DD)", false);
        setupDatePicker(from);
        setupDatePicker(to);
        form.addView(from);
        form.addView(to);

        View search = Ui.button(this, "Consultar histórico");
        form.addView(search);

        ListView list = new ListView(this);
        form.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        root.addView(form, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        async(
                () -> new NettelRepository(this).devicesForFleet(),
                items -> {
                    devices.clear();
                    devices.addAll(items);
                    reloadLabels();
                    if (devices.isEmpty()) {
                        selectedDevice.setText("No hay dispositivos disponibles");
                        Ui.toast(this, "No hay dispositivos asignados para consultar histórico.", Toast.LENGTH_LONG);
                    } else {
                        selectedDeviceValue = null;
                        deviceSearch.setText("", false);
                        selectedDevice.setText("Seleccione un dispositivo");
                    }
                }
        );

        search.setOnClickListener(v -> {
            Device device = selectedDeviceValue;
            if (device == null) {
                String typed = deviceSearch.getText() == null ? "" : deviceSearch.getText().toString();
                device = findDeviceByLabel(typed);
            }
            if (device == null) {
                Ui.toast(this, "Seleccione un dispositivo válido.", Toast.LENGTH_SHORT);
                return;
            }

            String fromText = Ui.text(from);
            String toText = Ui.text(to);
            Device finalDevice = device;
            async(
                    () -> new NettelRepository(this).history(finalDevice, fromText, toText, "500"),
                    items -> {
                        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
                        if (items.isEmpty()) {
                            Ui.toast(this, "La consulta histórica no devolvió posiciones.", Toast.LENGTH_LONG);
                            return;
                        }
                        startActivity(new Intent(this, MapActivity.class)
                                .putExtra("history_series", finalDevice.series)
                                .putExtra("history_name", finalDevice.name)
                                .putExtra("history_from", fromText)
                                .putExtra("history_to", toText));
                    }
            );
        });
    }

    private void reloadLabels() {
        deviceLabels.clear();
        for (Device d : devices) deviceLabels.add(labelFor(d));
        deviceAdapter.notifyDataSetChanged();
    }

    private String labelFor(Device d) {
        String name = d.name == null || d.name.trim().isEmpty() ? "Sin nombre" : d.name.trim();
        String registration = d.registration == null || d.registration.trim().isEmpty() ? "S/M" : d.registration.trim();
        String serial = d.series == null || d.series.trim().isEmpty() ? "S/S" : d.series.trim();
        return name + " · " + registration + " · " + serial;
    }

    private Device findDeviceByLabel(String label) {
        for (Device d : devices) {
            if (labelFor(d).equals(label)) return d;
        }
        return null;
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
        return value.toLowerCase(Locale.US)
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ñ", "n")
                .trim();
    }

    private class DeviceSearchAdapter extends ArrayAdapter<String> {
        private final List<String> filtered = new ArrayList<>();

        DeviceSearchAdapter() {
            super(HistoryActivity.this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        }

        @Override
        public int getCount() {
            return filtered.size();
        }

        @Override
        public String getItem(int position) {
            return filtered.get(position);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    List<String> values = new ArrayList<>();
                    String q = constraint == null ? "" : constraint.toString();
                    for (Device d : devices) {
                        if (matchesSearch(d, q)) values.add(labelFor(d));
                    }
                    results.values = values;
                    results.count = values.size();
                    return results;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    filtered.clear();
                    if (results != null && results.values instanceof List) {
                        filtered.addAll((List<String>) results.values);
                    }
                    DeviceSearchAdapter.super.notifyDataSetChanged();
                }
            };
        }

        @Override
        public void notifyDataSetChanged() {
            filtered.clear();
            for (Device d : devices) filtered.add(labelFor(d));
            super.notifyDataSetChanged();
        }
    }

    private void setupDatePicker(TextInputLayout field) {
        if (field.getEditText() == null) return;
        field.getEditText().setFocusable(false);
        field.getEditText().setCursorVisible(false);
        field.getEditText().setOnClickListener(v -> showCalendar(field));
        field.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);
        field.setEndIconDrawable(android.R.drawable.ic_menu_month);
        field.setEndIconOnClickListener(v -> showCalendar(field));
    }

    private void showCalendar(TextInputLayout field) {
        Calendar c = Calendar.getInstance();
        String current = Ui.text(field);
        if (current.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                c.set(
                        Integer.parseInt(current.substring(0, 4)),
                        Integer.parseInt(current.substring(5, 7)) - 1,
                        Integer.parseInt(current.substring(8, 10))
                );
            } catch (Exception ignored) {}
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    field.getEditText().setText(date);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dialog.setOnShowListener(d -> {
            dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.WHITE);
            dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.WHITE);
        });
        dialog.show();
    }
}
