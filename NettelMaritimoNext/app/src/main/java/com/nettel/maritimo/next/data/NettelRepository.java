package com.nettel.maritimo.next.data;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NettelRepository {
    private static final String TAG = "NettelRepo";
    private final Context context;
    private final SessionStore session;

    public NettelRepository(Context c) {
        context = c.getApplicationContext();
        session = new SessionStore(context);
    }

    private Map<String, String> p(String... values) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) m.put(values[i], values[i + 1]);
        return m;
    }

    public ApiResult login(String user, String password) throws Exception {
        String id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        ApiResult r = RestClient.postResult("auth/login", p("usuario", user, "clave", password, "device_id", id));
        if (r.success()) session.save(user, r.token.isEmpty() ? id : r.token);
        return r;
    }

    public void logout() throws Exception {
        try {
            if (session.active()) call("auth/logout", p("token", session.token()));
        } catch (Exception e) {
            Log.w(TAG, "logout remoto falló; se cerrará la sesión local: " + e.getMessage());
        } finally {
            session.clear();
        }
    }

    public ApiResult role() throws Exception {
        return call("auth/role", p("token", session.token()));
    }

    public List<User> users() throws Exception {
        ApiResult r = call("users", p("token", session.token()));
        ensure(r);
        List<User> x = new ArrayList<>();
        for (Map<String, String> m : r.primary) x.add(User.from(m));
        return x;
    }

    public User loggedUserProfile() throws Exception {
        String logged = session.user();
        User fallback = new User();
        fallback.id = logged;
        for (User user : users()) {
            if (same(user.id, logged)) return user;
        }
        return fallback;
    }

    public void updateLoggedUserProfile(String email, String phone) throws Exception {
        String user = session.user();
        Map<String, String> body = p(
                "token", session.token(),
                "usuario", user,
                "IdUsuario", user,
                "email", email,
                "EmailPrincipal", email,
                "correo", email,
                "telefono", phone,
                "Telefono", phone,
                "celular", phone
        );
        String[] endpoints = {
                "users/update",
                "users/profile/update",
                "user/update",
                "profile/update",
                "auth/profile/update"
        };
        Exception last = null;
        for (String endpoint : endpoints) {
            try {
                ApiResult r = call(endpoint, body);
                ensure(r);
                return;
            } catch (Exception e) {
                last = e;
                Log.w(TAG, "update profile endpoint=" + endpoint + " error=" + e.getMessage());
            }
        }
        throw new IllegalStateException("El servidor aún no tiene habilitado el servicio para actualizar correo y celular del usuario.");
    }

    public List<Device> devices(String user) throws Exception {
        ApiResult r = call("devices", p("token", session.token(), "usuario", user));
        ensure(r);
        enrichWithLegacyTelemetry(user, r.primary);
        List<Device> x = new ArrayList<>();
        for (Map<String, String> m : r.primary) x.add(Device.from(m));
        Log.i(TAG, "devices usuario=" + user + " count=" + x.size() + " names=" + names(x));
        return x;
    }

    private void enrichWithLegacyTelemetry(String user, List<Map<String, String>> devices) {
        if (devices == null || devices.isEmpty()) return;
        try {
            List<Map<String, String>> telemetry = RestClient.postLegacyDevices(user);
            Map<String, Map<String, String>> bySeries = new LinkedHashMap<>();
            for (Map<String, String> row : telemetry) {
                String key = seriesKey(row);
                if (!key.isEmpty()) bySeries.put(key, row);
            }
            int merged = 0;
            String[] fields = {
                    "rumbo", "last_rumbo", "last_bateria", "last_bateria_level",
                    "last_energia", "tipo_posicion", "estado_posicion", "tiene_alarma", "alerta_robo"
            };
            for (Map<String, String> device : devices) {
                Map<String, String> legacy = bySeries.get(seriesKey(device));
                if (legacy == null) continue;
                for (String field : fields) {
                    String value = legacy.get(field);
                    if (value != null && !value.trim().isEmpty()) device.put(field, value.trim());
                }
                merged++;
            }
            Log.i(TAG, "legacy telemetry usuario=" + user + " recibida=" + telemetry.size() + " mezclada=" + merged);
        } catch (Exception e) {
            Log.w(TAG, "legacy telemetry no disponible para " + user + ": " + e.getMessage());
        }
    }

    private String seriesKey(Map<String, String> row) {
        if (row == null) return "";
        String value = Device.s(row, "Serie", "serie", "series", "serial", "imei");
        return value == null ? "" : value.replaceAll("\\s+", "").toUpperCase(java.util.Locale.US);
    }

    public List<Device> devicesForFleet() throws Exception {
        return devices(activeFleetUser());
    }

    public String activeFleetUser() {
        String selected = session.fleetUser();
        String active;
        if (selected != null && !selected.trim().isEmpty()) active = selected;
        else if ("gfanny".equalsIgnoreCase(session.user())) active = "rdiego";
        else active = session.user();
        Log.i(TAG, "activeFleetUser login=" + session.user() + " selected=" + selected + " active=" + active);
        return active;
    }

    public List<Device> history(String series, String from, String to, String limit) throws Exception {
        Log.i(TAG, "history usuario=" + activeFleetUser() + " serie=" + series + " desde=" + from + " hasta=" + to + " limite=" + limit);
        ApiResult r = call("devices/history", p("token", session.token(), "usuario", activeFleetUser(), "serie", series, "fechaDesde", from, "fechaHasta", to, "numReg", limit));
        ensure(r);
        List<Device> x = new ArrayList<>();
        for (Map<String, String> m : r.primary) x.add(Device.from(m));
        applyHistoryContext(x, findDeviceBySeries(series));
        Log.i(TAG, "history result serie=" + series + " count=" + x.size());
        return x;
    }

    public List<Device> history(Device device, String from, String to, String limit) throws Exception {
        String fleetUser = activeFleetUser();
        String loginUser = session.user();
        String nextTo = nextDate(to);
        List<Map<String, String>> attempts = new ArrayList<>();

        addHistoryAttempt(attempts, "flota-serie-id", fleetUser, device, true, true, from, to, limit);
        addHistoryAttempt(attempts, "flota-serie", fleetUser, device, true, false, from, to, limit);
        addHistoryAttempt(attempts, "flota-id", fleetUser, device, false, true, from, to, limit);
        if (!same(loginUser, fleetUser)) {
            addHistoryAttempt(attempts, "login-serie-id", loginUser, device, true, true, from, to, limit);
            addHistoryAttempt(attempts, "login-serie", loginUser, device, true, false, from, to, limit);
            addHistoryAttempt(attempts, "login-id", loginUser, device, false, true, from, to, limit);
        }
        if (same(from, to) && nextTo != null) {
            addHistoryAttempt(attempts, "flota-serie-dia-siguiente", fleetUser, device, true, false, from, nextTo, limit);
            addHistoryAttempt(attempts, "flota-id-dia-siguiente", fleetUser, device, false, true, from, nextTo, limit);
            if (!same(loginUser, fleetUser)) {
                addHistoryAttempt(attempts, "login-serie-dia-siguiente", loginUser, device, true, false, from, nextTo, limit);
                addHistoryAttempt(attempts, "login-id-dia-siguiente", loginUser, device, false, true, from, nextTo, limit);
            }
        }

        Exception lastError = null;
        List<Device> lastEmpty = new ArrayList<>();
        for (Map<String, String> body : attempts) {
            String attempt = body.remove("_attempt");
            try {
                Log.i(TAG, "history attempt=" + attempt + " usuario=" + body.get("usuario") + " id=" + body.get("id_dispositivo") + " serie=" + body.get("serie") + " desde=" + body.get("fechaDesde") + " hasta=" + body.get("fechaHasta") + " limite=" + body.get("numReg"));
                ApiResult r = call("devices/history", body);
                if (!r.success()) {
                    Log.w(TAG, "history attempt=" + attempt + " rejected action=" + r.action + " message=" + r.message);
                    lastError = new IllegalStateException(r.message);
                    continue;
                }
                List<Device> x = new ArrayList<>();
                for (Map<String, String> m : r.primary) x.add(Device.from(m));
                applyHistoryContext(x, device);
                Log.i(TAG, "history attempt=" + attempt + " count=" + x.size());
                if (!x.isEmpty()) return x;
                lastEmpty = x;
            } catch (Exception e) {
                Log.w(TAG, "history attempt=" + attempt + " error=" + e.getMessage());
                lastError = e;
            }
        }
        if (lastError != null && attempts.isEmpty()) throw lastError;
        return lastEmpty;
    }

    private void addHistoryAttempt(List<Map<String, String>> attempts, String label, String user, Device device, boolean includeSeries, boolean includeId, String from, String to, String limit) {
        if ((user == null || user.trim().isEmpty()) || (!includeSeries && !includeId)) return;
        Map<String, String> m = p("token", session.token(), "usuario", user, "fechaDesde", from, "fechaHasta", to, "numReg", limit);
        m.put("_attempt", label);
        if (includeSeries && device.series != null && !device.series.trim().isEmpty()) m.put("serie", device.series);
        if (includeId && device.id > 0) {
            m.put("id_dispositivo", String.valueOf(device.id));
            m.put("IdDispositivo", String.valueOf(device.id));
        }
        attempts.add(m);
    }

    private Device findDeviceBySeries(String series) {
        if (series == null || series.trim().isEmpty()) return null;
        try {
            for (Device d : devicesForFleet()) {
                if (same(cleanSeries(d.series), cleanSeries(series))) return d;
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudo buscar contexto histórico para serie=" + series + ": " + e.getMessage());
        }
        return null;
    }

    private void applyHistoryContext(List<Device> history, Device source) {
        if (history == null || history.isEmpty() || source == null) return;
        int enriched = 0;
        for (Device point : history) {
            if (point == null) continue;
            if (empty(point.series)) point.series = source.series;
            if (empty(point.name)) point.name = source.name;
            if (empty(point.registration)) point.registration = source.registration;
            if (empty(point.status)) point.status = source.status;
            if (empty(point.messageType)) point.messageType = source.messageType;
            if (empty(point.loadStatus)) point.loadStatus = source.loadStatus;
            if (empty(point.batteryStatus)) point.batteryStatus = source.batteryStatus;
            if (Double.isNaN(point.heading)) point.heading = source.heading;
            enriched++;
        }
        Log.i(TAG, "history telemetry context applied=" + enriched
                + " serie=" + source.series
                + " rumbo=" + source.formatHeading()
                + " bateria=" + source.batteryText());
    }

    private String cleanSeries(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toUpperCase(java.util.Locale.US);
    }

    private boolean empty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean same(String a, String b) {
        if (a == null) return b == null;
        return a.equalsIgnoreCase(b == null ? "" : b);
    }

    private String nextDate(String date) {
        try {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            f.setLenient(false);
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(f.parse(date));
            c.add(java.util.Calendar.DATE, 1);
            return f.format(c.getTime());
        } catch (Exception ignored) {
            return null;
        }
    }

    public List<Alert> alerts(int after) throws Exception {
        ApiResult r = call("alerts", p("token", session.token(), "ultimaAlarma", String.valueOf(after)));
        ensure(r);
        List<Alert> x = new ArrayList<>();
        for (Map<String, String> m : r.primary) addServerAlert(x, Alert.from(m));
        for (Map<String, String> m : r.secondary) addServerAlert(x, Alert.from(m));
        x.addAll(localDeviceAlerts());
        Log.i(TAG, "alerts after=" + after + " count=" + x.size());
        return x;
    }

    private void addServerAlert(List<Alert> alerts, Alert alert) {
        if (alert.isInformationalSpotTrace()) {
            Log.i(TAG, "Alerta informativa SPOT Trace filtrada: " + alert.message);
            return;
        }
        alerts.add(alert);
    }

    private List<Alert> localDeviceAlerts() {
        List<Alert> alerts = new ArrayList<>();
        try {
            for (Device d : devicesForFleet()) {
                if (d.lowBattery()) alerts.add(lowBatteryAlert(d));
                if (Device.isCriticalSosMessageType(d.messageType)) alerts.add(sosTelemetryAlert(d));
            }
        } catch (Exception e) {
            Log.w(TAG, "No se pudieron generar alertas locales de dispositivos: " + e.getMessage());
        }
        return alerts;
    }

    private Alert lowBatteryAlert(Device d) {
        Alert a = deviceAlert(d, "low-battery");
        a.type = "Alerta de batería baja";
        a.message = "Batería baja: " + String.format(java.util.Locale.US, "%.1f", d.batteryVoltage()) + " V (límite 3.7 V).";
        return a;
    }

    private Alert sosTelemetryAlert(Device d) {
        Alert a = deviceAlert(d, "sos:" + d.located);
        a.type = "Alerta crítica SOS";
        a.message = "Se recibió una señal SOS (" + (empty(d.messageType) ? "PT1" : d.messageType) + ") del dispositivo.";
        return a;
    }

    private Alert deviceAlert(Device d, String kind) {
        Alert a = new Alert();
        String key = (d.series == null || d.series.trim().isEmpty() ? d.name : d.series).trim();
        a.id = -Math.abs((kind + ":" + key).hashCode());
        if (a.id == 0) a.id = -3700;
        a.date = d.located;
        a.boat = d.name;
        a.registration = d.registration;
        a.series = d.series;
        a.client = activeFleetUser();
        a.lat = d.lat;
        a.lng = d.lng;
        return a;
    }

    public void markRead(List<Integer> ids) throws Exception {
        StringBuilder b = new StringBuilder();
        for (int id : ids) {
            if (id <= 0) continue;
            if (b.length() > 0) b.append(',');
            b.append(id);
        }
        if (b.length() > 0) {
            ApiResult r = call("alerts/read", p("token", session.token(), "alarmasLeidas", b.toString()));
            ensure(r);
        }
    }

    public org.json.JSONObject verifyPasswordReset(String user) throws Exception {
        return RestClient.postJson("auth/password/verify-temporary", p("id_usuario", user));
    }

    public org.json.JSONObject forgotPassword(String customerId, String user) throws Exception {
        return RestClient.postJson("auth/password/forgot", p("customer_id", customerId, "username", user));
    }

    public org.json.JSONObject changePassword(String user, String current, String next) throws Exception {
        return RestClient.postJson("auth/password/change", p("usuario", user, "claveactual", current, "clave", next));
    }

    private ApiResult call(String endpoint, Map<String, String> body) throws Exception {
        ApiResult r = RestClient.postResult(endpoint, body);
        session.updateToken(r.token);
        return r;
    }

    private void ensure(ApiResult r) {
        if (!r.success()) throw new IllegalStateException(r.message);
    }

    private String names(List<Device> devices) {
        StringBuilder b = new StringBuilder();
        for (Device d : devices) {
            if (b.length() > 0) b.append(", ");
            b.append(d.name);
            if (b.length() > 250) {
                b.append("...");
                break;
            }
        }
        return b.toString();
    }
}
