package com.nettel.maritimo.next.data;

import com.nettel.maritimo.next.util.DateTimeUtils;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Device {
    public int id;
    public String series, name, registration, status, located;
    public String messageType, loadStatus, batteryStatus;
    public double lat, lng, speed, heading;
    public boolean alert, theft;

    public static Device from(Map<String, String> m) {
        Device d = new Device();
        d.id = i(m, "IdDispositivo", "id_dispositivo", "id", "device_id");
        d.series = s(m, "Serie", "serie", "series", "serial", "imei");
        d.name = s(m, "Nombre", "nombre", "name", "nave", "barco", "embarcacion");
        d.registration = s(m, "Matricula", "matricula", "registration", "placa");
        d.status = s(m, "Estado", "estado", "status");
        d.located = s(m, "Ubicado", "ubicado", "fecha", "fecha_gps", "last_seen", "located");
        d.lat = x(m, "Latitud", "latitud", "lat", "latitude");
        d.lng = x(m, "Longitud", "longitud", "lng", "lon", "longitude");
        d.speed = x(m, "Velocidad", "velocidad", "speed", "knots");
        d.heading = x(m,
                "Rumbo", "rumbo", "last_rumbo", "lastRumbo",
                "heading", "Heading", "last_heading", "lastHeading",
                "course", "Course", "course_over_ground", "courseOverGround",
                "direccion", "Direccion", "Direction", "bearing", "Bearing");
        d.messageType = normalizedMessageType(m);
        d.loadStatus = s(m,
                "batteryCharging", "BatteryCharging", "battery_charge", "batteryCharge",
                "EstadoCarga", "estado_carga", "estadoCarga", "load_status", "loadStatus",
                "last_bateria", "lastBateria", "Carga", "carga");
        d.batteryStatus = s(m,
                "batteryLevel", "BatteryLevel", "battery_voltage", "batteryVoltage",
                "EstadoBateria", "estado_bateria", "estadoBateria", "battery_status",
                "batteryStatus", "last_bateria_level", "lastBateriaLevel",
                "Bateria", "Batería", "bateria", "battery", "Voltaje", "voltaje", "Voltage", "voltage");
        d.alert = i(m, "TieneAlerta", "tiene_alerta", "alerta", "alert") != 0
                || isCriticalSosMessageType(d.messageType);
        d.theft = i(m, "AlertaRobo", "alerta_robo", "robo", "theft") != 0;
        return d;
    }

    public String toString() {
        return text(name, "Sin nombre")
                + " (" + text(registration, "S/M") + ")\n"
                + text(series, "Sin serie")
                + " · " + text(status, "Sin estado")
                + " · " + speed + " kn";
    }

    public String mapInfo() {
        return "Latitud: " + String.format(Locale.US, "%.4f", lat)
                + "\nLongitud: " + String.format(Locale.US, "%.4f", lng)
                + "\nRumbo: " + formatHeading()
                + "\nEstado: " + text(status, "N/D")
                + "\nTipo: " + displayMessageType()
                + "\nEstado Batería: " + batteryText()
                + "\nUbicado: " + DateTimeUtils.utcToEcuador(located);
    }

    public String formatHeading() {
        return Double.isNaN(heading) ? "N/D" : String.valueOf(Math.round(heading)) + "°";
    }

    private String displayMessageType() {
        return isCriticalSosMessageType(messageType) ? "SOS" : text(messageType, "N/D");
    }

    public String batteryText() {
        String battery = text(batteryStatus, "");
        String charge = text(loadStatus, "");
        String upper = charge.toUpperCase(Locale.US);
        if ("BC".equals(upper)) charge = "Batería cargada";
        else if ("BD".equals(upper)) charge = "Batería descargada";
        else if ("CHARGING".equals(upper)) charge = "Cargando";
        else if ("NOT CHARGING".equals(upper)) charge = "No Carga";

        boolean knownBattery = known(battery);
        boolean knownCharge = known(charge);
        if (knownBattery && battery.matches(".*\\[.*\\].*")) return battery;
        if (knownCharge && knownBattery) return charge + " [" + battery + "]";
        if (knownCharge) return charge;
        return knownBattery ? "[" + battery + "]" : "N/D";
    }

    public double batteryVoltage() {
        String value = text(batteryStatus, "");
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("-?\\d+(?:[\\.,]\\d+)?")
                .matcher(value);
        if (!matcher.find()) return Double.NaN;
        try {
            return Double.parseDouble(matcher.group().replace(",", "."));
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    public boolean lowBattery() {
        double volts = batteryVoltage();
        return !Double.isNaN(volts) && volts <= 3.7d;
    }

    static String s(Map<String, String> m, String... keys) {
        for (String k : keys) {
            String v = m.get(k);
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        Set<String> normalizedKeys = new LinkedHashSet<>();
        for (String k : keys) normalizedKeys.add(normalizeFieldName(k));
        for (Map.Entry<String, String> entry : m.entrySet()) {
            if (normalizedKeys.contains(normalizeFieldName(entry.getKey()))) {
                String v = entry.getValue();
                if (v != null && !v.trim().isEmpty()) return v.trim();
            }
        }
        return "";
    }

    static int i(Map<String, String> m, String... keys) {
        try {
            return Integer.parseInt(s(m, keys));
        } catch (Exception e) {
            return 0;
        }
    }

    static double x(Map<String, String> m, String... keys) {
        try {
            return Double.parseDouble(s(m, keys).replace(",", "."));
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private static String normalizedMessageType(Map<String, String> m) {
        String typeRaw = s(m,
                "Tipo", "tipo", "TipoPosicion", "tipo_posicion",
                "TipoMensaje", "tipo_mensaje", "tipoMensaje", "message_type", "messageType");
        String stateRaw = s(m,
                "EstadoPosicion", "estado_posicion",
                "EstadoPosicionDispositivo", "estado_posicion_dispositivo");
        String variant = sosVariant(typeRaw, stateRaw);
        if (!variant.isEmpty()) return sosBase(typeRaw) + " " + variant;

        String type = typeRaw == null ? "" : typeRaw.trim().toUpperCase(Locale.US);
        String state = stateRaw == null ? "" : stateRaw.trim().toUpperCase(Locale.US);
        if ("A".equals(state) || "W".equals(state) || "B".equals(state) || "P".equals(state)) {
            return (type.isEmpty() ? "QTH" : type) + " " + state;
        }
        return type.isEmpty() ? "QTH" : type;
    }

    public static boolean isSosMessageType(String value) {
        return isCriticalSosMessageType(value);
    }

    public static boolean isCriticalSosMessageType(String value) {
        String raw = value == null ? "" : value.trim().toUpperCase(Locale.US);
        String[] pieces = raw.split("[\\s,;:/_-]+");
        String base = pieces.length == 0 ? "" : pieces[0];
        if (!("SOS".equals(base) || "PT1".equals(base) || "TP1".equals(base))) return false;
        if (pieces.length < 2) return "SOS".equals(base);
        return isCriticalSosVariant(pieces[1]);
    }

    private static String sosVariant(String typeRaw, String stateRaw) {
        String base = sosBase(typeRaw);
        if (base.isEmpty()) return "";
        String[] pieces = (typeRaw == null ? "" : typeRaw.trim().toUpperCase(Locale.US)).split("[\\s,;:/_-]+");
        if (pieces.length > 1 && isCriticalSosVariant(pieces[1])) return pieces[1];
        String state = stateRaw == null ? "" : stateRaw.trim().toUpperCase(Locale.US);
        return isCriticalSosVariant(state) ? state : "";
    }

    private static String sosBase(String typeRaw) {
        String raw = typeRaw == null ? "" : typeRaw.trim().toUpperCase(Locale.US);
        String[] pieces = raw.split("[\\s,;:/_-]+");
        String base = pieces.length == 0 ? "" : pieces[0];
        if ("SOS".equals(base)) return "SOS";
        if ("PT1".equals(base) || "TP1".equals(base)) return "PT1";
        return "";
    }

    private static boolean isCriticalSosVariant(String value) {
        return "A".equals(value) || "W".equals(value) || "P".equals(value);
    }

    private static String normalizeFieldName(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.US);
    }

    private static boolean known(String value) {
        return value != null && !value.trim().isEmpty() && !value.trim().matches("(?i)n/?d");
    }

    private static String text(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
