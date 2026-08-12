package com.nettel.maritimo.next.data;

import com.google.gson.Gson;
import com.nettel.maritimo.next.BuildConfig;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RestClient {
    private static final Gson GSON = new Gson();
    private static final String TAG = "NettelREST";
    private static final String LEGACY_DEVICES_URL = "https://spot2.nettelcorp.com/gps/Mapa.2.0.Dispositivos.php";

    private RestClient() {}

    public static ApiResult postResult(String endpoint, Map<String, ?> body) throws Exception {
        return parseResult(postJson(endpoint, body));
    }

    public static JSONObject postJson(String endpoint, Map<String, ?> body) throws Exception {
        String url = BuildConfig.API_BASE_URL + endpoint;
        byte[] payload = GSON.toJson(body == null ? new LinkedHashMap<>() : body).getBytes(StandardCharsets.UTF_8);
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(20000);
        c.setReadTimeout(30000);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        try (OutputStream os = c.getOutputStream()) {
            os.write(payload);
        }
        int status = c.getResponseCode();
        InputStream is = status >= 400 ? c.getErrorStream() : c.getInputStream();
        byte[] bytes = read(is);
        String text = new String(bytes, StandardCharsets.UTF_8).trim();
        Log.i(TAG, endpoint + " HTTP " + status + " " + preview(text));
        if (status >= 400) throw new IllegalStateException("REST HTTP " + status + ": " + text);
        return text.isEmpty() ? new JSONObject() : new JSONObject(text);
    }

    public static List<Map<String, String>> postLegacyDevices(String user) throws Exception {
        String form = "usuario=" + URLEncoder.encode(user == null ? "" : user, "UTF-8");
        byte[] payload = form.getBytes(StandardCharsets.UTF_8);
        HttpURLConnection c = (HttpURLConnection) new URL(LEGACY_DEVICES_URL).openConnection();
        c.setConnectTimeout(4000);
        c.setReadTimeout(8000);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("Accept", "application/json");
        c.setRequestProperty("User-Agent", "NettelMaritimo-Android/1.0");
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        try (OutputStream os = c.getOutputStream()) {
            os.write(payload);
        }
        int status = c.getResponseCode();
        InputStream is = status >= 400 ? c.getErrorStream() : c.getInputStream();
        String text = new String(read(is), StandardCharsets.UTF_8).trim();
        Log.i(TAG, "legacy devices HTTP " + status + " " + preview(text));
        if (status >= 400 || text.isEmpty()) return new ArrayList<>();

        List<Map<String, String>> out = new ArrayList<>();
        Object parsed = text.startsWith("[") ? new JSONArray(text) : new JSONObject(text);
        append(out, parsed);
        return out;
    }

    private static ApiResult parseResult(JSONObject json) {
        ApiResult r = new ApiResult();
        r.action = json.optInt("action", json.optBoolean("success", false) ? 0 : json.optInt("code", 2));
        r.message = json.optString("message", json.optString("mensaje", r.action == 0 ? "OK" : "Error"));
        r.token = json.optString("token", json.optString("session_token", ""));
        r.scalar = json.optString("value", json.optString("result", ""));

        Object data = json.opt("data");
        if (data == null) data = json.opt("items");
        if (data == null) data = json.opt("result");
        append(r.primary, data);
        append(r.secondary, json.opt("secondary"));
        return r;
    }

    private static void append(java.util.List<Map<String, String>> out, Object data) {
        if (data instanceof JSONArray) {
            JSONArray array = (JSONArray) data;
            for (int i = 0; i < array.length(); i++) {
                Object item = array.opt(i);
                if (item instanceof JSONObject) out.add(flatten((JSONObject) item));
            }
        } else if (data instanceof JSONObject) {
            out.add(flatten((JSONObject) data));
        }
    }

    private static Map<String, String> flatten(JSONObject object) {
        Map<String, String> map = new LinkedHashMap<>();
        Iterator<String> keys = object.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = object.opt(key);
            map.put(key, value == null || value == JSONObject.NULL ? "" : String.valueOf(value));
        }
        return map;
    }

    private static byte[] read(InputStream is) throws Exception {
        if (is == null) return new byte[0];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) out.write(buf, 0, n);
        return out.toByteArray();
    }

    private static String preview(String text) {
        if (text == null) return "";
        String compact = text.replace('\n', ' ').replace('\r', ' ');
        return compact.length() > 600 ? compact.substring(0, 600) + "..." : compact;
    }
}
