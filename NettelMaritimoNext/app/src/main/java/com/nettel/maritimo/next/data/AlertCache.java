package com.nettel.maritimo.next.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlertCache {
    private static final String PREFS = "alert_cache";
    private static final String KEY_ITEMS = "items";
    private static final String KEY_READ_IDS = "read_ids";
    private static final int MAX_ITEMS = 300;
    private final SharedPreferences prefs;
    private final String scope;

    public AlertCache(Context context) {
        Context app = context.getApplicationContext();
        prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        scope = scope(app);
        prefs.edit().remove(KEY_ITEMS).remove(KEY_READ_IDS).apply();
    }

    public synchronized void saveAll(List<Alert> alerts) {
        Map<Integer, Alert> merged = new LinkedHashMap<>();
        Set<Integer> readIds = readIds();
        for (Alert a : load()) {
            if (a.id != 0 && !a.isInformationalSpotTrace()) merged.put(a.id, a);
        }
        for (Alert a : alerts) {
            if (a.id != 0 && !readIds.contains(a.id) && !a.isInformationalSpotTrace()) merged.put(a.id, a);
        }

        JSONArray array = new JSONArray();
        int count = 0;
        List<Alert> values = new ArrayList<>(merged.values());
        for (int i = values.size() - 1; i >= 0 && count < MAX_ITEMS; i--, count++) {
            array.put(toJson(values.get(i)));
        }
        prefs.edit().putString(itemsKey(), array.toString()).apply();
    }

    public synchronized List<Alert> load() {
        List<Alert> alerts = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(itemsKey(), "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject o = array.optJSONObject(i);
                if (o != null) {
                    Alert a = fromJson(o);
                    if (!a.isInformationalSpotTrace()) alerts.add(a);
                }
            }
        } catch (Exception ignored) {
        }
        return alerts;
    }

    public synchronized void clear() {
        prefs.edit().remove(itemsKey()).apply();
    }

    public List<Alert> merge(List<Alert> server) {
        Set<Integer> readIds = readIds();
        Map<Integer, Alert> merged = new LinkedHashMap<>();
        for (Alert a : load()) if (a.id != 0 && !readIds.contains(a.id) && !a.isInformationalSpotTrace()) merged.put(a.id, a);
        for (Alert a : server) if (a.id != 0 && !readIds.contains(a.id) && !a.isInformationalSpotTrace()) merged.put(a.id, a);
        return new ArrayList<>(merged.values());
    }

    public List<Alert> unreadOnly(List<Alert> alerts) {
        Set<Integer> readIds = readIds();
        List<Alert> unread = new ArrayList<>();
        for (Alert a : alerts) {
            if (a.id != 0 && !readIds.contains(a.id) && !a.isInformationalSpotTrace()) unread.add(a);
        }
        return unread;
    }

    public synchronized void markRead(List<Integer> ids) {
        Set<Integer> readIds = readIds();
        readIds.addAll(ids);
        JSONArray array = new JSONArray();
        for (Integer id : readIds) array.put(id);
        prefs.edit().putString(readIdsKey(), array.toString()).apply();
        removeReadFromCache(readIds);
    }

    private void removeReadFromCache(Set<Integer> readIds) {
        List<Alert> current = load();
        JSONArray array = new JSONArray();
        for (Alert a : current) {
            if (a.id != 0 && !readIds.contains(a.id) && !a.isInformationalSpotTrace()) array.put(toJson(a));
        }
        prefs.edit().putString(itemsKey(), array.toString()).apply();
    }

    private Set<Integer> readIds() {
        Set<Integer> ids = new HashSet<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(readIdsKey(), "[]"));
            for (int i = 0; i < array.length(); i++) ids.add(array.optInt(i));
        } catch (Exception ignored) {
        }
        return ids;
    }

    private String itemsKey() {
        return KEY_ITEMS + "_" + scope;
    }

    private String readIdsKey() {
        return KEY_READ_IDS + "_" + scope;
    }

    private String scope(Context context) {
        try {
            SessionStore session = new SessionStore(context);
            String user = clean(session.user());
            String fleet = clean(session.fleetUser());
            if (fleet.isEmpty() && "gfanny".equalsIgnoreCase(user)) fleet = "rdiego";
            String active = fleet.isEmpty() ? user : fleet;
            if (active.isEmpty()) active = "anonymous";
            return active;
        } catch (Exception ignored) {
            return "anonymous";
        }
    }

    private String clean(String value) {
        return value == null ? "" : value.replaceAll("[^A-Za-z0-9_@.-]", "_").toLowerCase(java.util.Locale.US);
    }

    private JSONObject toJson(Alert a) {
        JSONObject o = new JSONObject();
        try {
            o.put("id", a.id);
            o.put("type", a.type);
            o.put("message", a.message);
            o.put("date", a.date);
            o.put("boat", a.boat);
            o.put("registration", a.registration);
            o.put("client", a.client);
            o.put("series", a.series);
            o.put("lat", a.lat);
            o.put("lng", a.lng);
        } catch (Exception ignored) {
        }
        return o;
    }

    private Alert fromJson(JSONObject o) {
        Alert a = new Alert();
        a.id = o.optInt("id");
        a.type = o.optString("type");
        a.message = o.optString("message");
        a.date = o.optString("date");
        a.boat = o.optString("boat");
        a.registration = o.optString("registration");
        a.client = o.optString("client");
        a.series = o.optString("series");
        a.lat = o.optDouble("lat");
        a.lng = o.optDouble("lng");
        return a;
    }
}
