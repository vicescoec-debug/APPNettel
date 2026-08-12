package com.nettel.maritimo.next.data;

import com.nettel.maritimo.next.util.DateTimeUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

public class Alert {
    public int id;
    public String type, message, date, boat, registration, client, series;
    public double lat, lng;

    public static Alert from(Map<String, String> m) {
        Alert a = new Alert();
        a.id = Device.i(m, "id_alarma", "IdAlarma", "id", "alert_id");
        a.type = Device.s(m, "tipo_alarma", "TipoAlarma", "tipo", "type");
        a.message = Device.s(m, "mensaje", "Mensaje", "message", "descripcion", "description");
        a.date = Device.s(m, "ubicado", "Ubicado", "fecha", "fecha_alarma", "date", "created_at");
        a.boat = Device.s(m, "nave", "Nave", "embarcacion", "barco", "nombre", "boat");
        a.registration = Device.s(m, "matricula", "Matricula", "registration", "placa");
        a.client = Device.s(m, "cliente", "Cliente", "customer", "client");
        a.series = Device.s(m, "Serie", "serie", "series", "serial", "imei");
        a.lat = Device.x(m, "latitud", "Latitud", "lat", "latitude");
        a.lng = Device.x(m, "longitud", "Longitud", "lng", "lon", "longitude");
        return a;
    }

    public String title() {
        String t = text(type, "Alerta");
        String b = text(boat, "Embarcación");
        String r = text(registration, "S/M");
        return t + " · " + b + " (" + r + ")";
    }

    public String detail() {
        StringBuilder out = new StringBuilder();
        append(out, text(message, "Sin mensaje"));
        append(out, "Fecha: " + DateTimeUtils.utcToEcuador(date));
        append(out, "Cliente: " + text(client, "N/D"));
        if (lat != 0 || lng != 0) {
            append(out, "Ubicación: "
                    + String.format(Locale.US, "%.4f", lat)
                    + ", "
                    + String.format(Locale.US, "%.4f", lng));
        }
        return out.toString();
    }

    @Override
    public String toString() {
        return title() + "\n" + detail();
    }

    public boolean isInformationalSpotTrace() {
        String normalized = normalize(message);
        return normalized.equals("spot trace is functioning properly")
                || normalized.equals("spt trace is functioning properly")
                || normalized.equals("spot trace is functioning propoerly")
                || normalized.equals("spt trace is functioning propoerly")
                || normalized.equals("spot trace has detected that the asset has moved");
    }

    private void append(StringBuilder out, String value) {
        if (value == null || value.trim().isEmpty()) return;
        if (out.length() > 0) out.append('\n');
        out.append(value);
    }

    private static String text(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String text = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        return text.replaceAll("\\s+", " ");
    }
}
