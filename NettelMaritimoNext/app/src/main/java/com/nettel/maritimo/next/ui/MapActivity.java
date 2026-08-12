package com.nettel.maritimo.next.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.nettel.maritimo.next.R;
import com.nettel.maritimo.next.data.Device;
import com.nettel.maritimo.next.data.NettelRepository;
import com.nettel.maritimo.next.util.DateTimeUtils;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.TilesOverlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapActivity extends FragmentActivity {
    private static final String TAG = "NettelMap";
    private static final GeoPoint ECUADOR = new GeoPoint(-1.831239, -78.183406);
    private static final double MARINE_PROFILE_ZOOM_920K = 9.3;

    private MapView map;
    private TilesOverlay marineProfileOverlay;
    private TilesOverlay seaMarksOverlay;
    private ITileSource satelliteTileSource;
    private LinearLayout mapControls;
    private Button bathymetryButton;
    private Button fishingButton;
    private Button satelliteButton;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final List<Device> visibleFleetDevices = new ArrayList<>();
    private final List<Overlay> fishingOverlays = new ArrayList<>();
    private final Map<String, Drawable> clusterIconCache = new HashMap<>();
    private Drawable lowBatteryIcon;
    private Drawable criticalAlertIcon;
    private boolean showingFleetClusters;
    private boolean bathymetryVisible;
    private boolean fishingVisible;
    private boolean satelliteVisible;
    private boolean historyModeActive;
    private double lastClusterZoom = -1;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        configureOsmdroid();

        map = new MapView(this);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);
        map.setTilesScaledToDpi(true);
        map.setZoomRounding(true);
        map.addMapListener(new org.osmdroid.events.MapListener() {
            @Override
            public boolean onScroll(org.osmdroid.events.ScrollEvent event) {
                return false;
            }

            @Override
            public boolean onZoom(org.osmdroid.events.ZoomEvent event) {
                refreshFleetClustersIfNeeded();
                return false;
            }
        });
        map.getController().setZoom(MARINE_PROFILE_ZOOM_920K);
        map.getController().setCenter(ECUADOR);
        setupOpenSeaMapOverlay();

        if (getIntent().hasExtra("history_series")) {
            setContentView(map);
            loadHistory();
        } else {
            setupFleetMapContent();
            loadFleet();
        }
    }

    private void configureOsmdroid() {
        IConfigurationProvider config = Configuration.getInstance();
        config.setUserAgentValue(getPackageName());
        config.setTileDownloadThreads((short) 8);
        config.setTileFileSystemThreads((short) 4);
        config.setTileDownloadMaxQueueSize((short) 120);
        config.setTileFileSystemMaxQueueSize((short) 120);
        config.setCacheMapTileCount((short) 36);
        config.setCacheMapTileOvershoot((short) 12);
        config.setTileFileSystemCacheMaxBytes(320L * 1024L * 1024L);
        config.setTileFileSystemCacheTrimBytes(260L * 1024L * 1024L);
        config.setMapTileDownloaderFollowRedirects(true);
    }

    private void setupFleetMapContent() {
        FrameLayout frame = new FrameLayout(this);
        frame.addView(map, new FrameLayout.LayoutParams(-1, -1));

        mapControls = new LinearLayout(this);
        mapControls.setOrientation(LinearLayout.HORIZONTAL);
        mapControls.setGravity(Gravity.CENTER);
        mapControls.setPadding(Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8), Ui.dp(this, 8));
        mapControls.setBackgroundColor(Color.argb(180, 255, 255, 255));

        bathymetryButton = controlButton("BATIMETRIA");
        fishingButton = controlButton("PESCA");
        satelliteButton = controlButton("SATELITE");
        mapControls.addView(bathymetryButton);
        mapControls.addView(fishingButton);
        mapControls.addView(satelliteButton);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, -2);
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.topMargin = Ui.dp(this, 18);
        frame.addView(mapControls, params);
        setContentView(frame);

        bathymetryButton.setOnClickListener(v -> toggleBathymetry());
        fishingButton.setOnClickListener(v -> toggleFishing());
        satelliteButton.setOnClickListener(v -> toggleSatellite());
    }

    private Button controlButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(12);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(11, 46, 94));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, Ui.dp(this, 44));
        params.setMargins(Ui.dp(this, 4), 0, Ui.dp(this, 4), 0);
        button.setLayoutParams(params);
        return button;
    }

    private void loadHistory() {
        String series = getIntent().getStringExtra("history_series");
        String from = getIntent().getStringExtra("history_from");
        String to = getIntent().getStringExtra("history_to");
        io.execute(() -> {
            try {
                List<Device> devices = new NettelRepository(this).history(series, from == null ? "" : from, to == null ? "" : to, "500");
                runOnUiThread(() -> show(devices, true));
            } catch (Exception e) {
                Log.e(TAG, "Error cargando histÃ³rico", e);
                runOnUiThread(() -> Ui.toast(
                        this,
                        "No se pudo cargar el histÃ³rico: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ));
            }
        });
    }

    private void loadFleet() {
        io.execute(() -> {
            try {
                List<Device> devices = new NettelRepository(this).devicesForFleet();
                runOnUiThread(() -> show(devices, false));
            } catch (Exception e) {
                Log.e(TAG, "Error cargando flota", e);
                runOnUiThread(() -> Ui.toast(
                        this,
                        "No se pudo cargar la flota: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ));
            }
        });
    }

    private void show(List<Device> devices, boolean historyMode) {
        historyModeActive = historyMode;
        map.getOverlays().clear();
        addOpenSeaMapOverlay();
        visibleFleetDevices.clear();
        showingFleetClusters = false;
        lastClusterZoom = -1;
        if (historyMode) sortHistory(devices);
        List<GeoPoint> points = new ArrayList<>();
        int filtered = 0;
        String only = historyMode ? null : getIntent().getStringExtra("series");
        double focusLat = getIntent().getDoubleExtra("focus_lat", Double.NaN);
        double focusLng = getIntent().getDoubleExtra("focus_lng", Double.NaN);
        boolean hasFocus = !Double.isNaN(focusLat) && !Double.isNaN(focusLng) && (focusLat != 0 || focusLng != 0);
        List<Device> fleetPoints = new ArrayList<>();

        for (Device d : devices) {
            if (only != null && !only.equals(d.series)) continue;
            filtered++;
            if ((d.lat == 0 && d.lng == 0) || Double.isNaN(d.lat) || Double.isNaN(d.lng)) {
                Log.w(TAG, "Dispositivo sin coordenadas: " + d.series + " " + d.name);
                continue;
            }

            GeoPoint point = new GeoPoint(d.lat, d.lng);
            points.add(point);
            if (!historyMode && only == null) {
                fleetPoints.add(d);
                continue;
            }

            Marker marker = new Marker(map);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            if (!historyMode && isCriticalAlert(d)) map.getOverlays().add(new AlertPulseOverlay(point, true));
            else if (!historyMode && d.lowBattery()) map.getOverlays().add(new AlertPulseOverlay(point, false));
            Drawable icon = historyMode
                    ? ContextCompat.getDrawable(this, R.drawable.ic_history_pin_yellow)
                    : markerIcon(d);
            if (icon != null) marker.setIcon(icon);
            String label = historyMode ? historyLabel(d, points.size()) : (d.name == null || d.name.isEmpty() ? d.series : d.name);
            marker.setTitle(label + " (" + d.registration + ")");
            marker.setSnippet(d.mapInfo());
            map.getOverlays().add(marker);
            if (hasFocus && near(d.lat, d.lng, focusLat, focusLng)) marker.showInfoWindow();
            if (!historyMode || points.size() == 1 || points.size() % 10 == 0) map.getOverlays().add(new DeviceNameOverlay(point, label));
        }

        if (!historyMode && only == null) {
            visibleFleetDevices.clear();
            visibleFleetDevices.addAll(fleetPoints);
            addFleetClusters();
        }

        Log.i(TAG, (historyMode ? "HistÃ³rico" : "Flota") + " recibida=" + devices.size() + " filtrada=" + filtered + " puntos=" + points.size());
        if (historyMode && points.size() > 1) {
            Polyline route = new Polyline();
            route.setPoints(points);
            route.getOutlinePaint().setColor(Color.rgb(255, 193, 7));
            route.getOutlinePaint().setStrokeWidth(7f);
            map.getOverlays().add(Math.min(1, map.getOverlays().size()), route);
            map.getOverlays().add(new DirectionArrowOverlay(points));
        }

        if (hasFocus) {
            GeoPoint point = new GeoPoint(focusLat, focusLng);
            if (points.isEmpty()) {
                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                Drawable icon = markerIcon(null);
                if (icon != null) marker.setIcon(icon);
                String title = getIntent().getStringExtra("focus_title");
                marker.setTitle(title == null || title.trim().isEmpty() ? "Alerta" : title);
                marker.setSnippet("Ubicación de alerta\nLatitud: " + String.format(java.util.Locale.US, "%.4f", focusLat)
                        + "\nLongitud: " + String.format(java.util.Locale.US, "%.4f", focusLng));
                map.getOverlays().add(marker);
                marker.showInfoWindow();
            }
            map.post(() -> {
                map.getController().setZoom(Math.max(map.getZoomLevelDouble(), 13.5));
                map.getController().animateTo(point);
            });
        } else if (!points.isEmpty()) {
            map.post(() -> zoomTo(points));
        } else if (devices.isEmpty()) {
            Ui.toast(this, historyMode ? "La consulta histÃ³rica no devolviÃ³ posiciones." : "No hay dispositivos de flota para mostrar.", Toast.LENGTH_LONG);
        } else {
            Ui.toast(this, historyMode ? "El histÃ³rico fue recibido, pero no tiene coordenadas vÃ¡lidas." : "La flota fue recibida, pero no tiene coordenadas vÃ¡lidas para el mapa.", Toast.LENGTH_LONG);
        }
        map.invalidate();
    }

    private boolean near(double lat, double lng, double focusLat, double focusLng) {
        return Math.abs(lat - focusLat) < 0.0002 && Math.abs(lng - focusLng) < 0.0002;
    }

    private void refreshFleetClustersIfNeeded() {
        if (!showingFleetClusters || visibleFleetDevices.isEmpty()) return;
        double zoom = map.getZoomLevelDouble();
        if (Math.abs(zoom - lastClusterZoom) < 0.45) return;
        clearFleetMarkerOverlays();
        addFleetClusters();
        map.invalidate();
    }

    private void clearFleetMarkerOverlays() {
        List<Overlay> keep = new ArrayList<>();
        for (Overlay overlay : map.getOverlays()) {
            if (overlay == marineProfileOverlay || overlay == seaMarksOverlay || fishingOverlays.contains(overlay)) keep.add(overlay);
        }
        map.getOverlays().clear();
        map.getOverlays().addAll(keep);
    }

    private void addFleetClusters() {
        showingFleetClusters = true;
        lastClusterZoom = map.getZoomLevelDouble();
        List<Device> devices = visibleFleetDevices;
        List<DeviceCluster> clusters = new ArrayList<>();
        int thresholdPx = clusterThresholdPx(lastClusterZoom);
        for (Device d : devices) {
            if (isCriticalAlert(d)) continue;
            GeoPoint p = new GeoPoint(d.lat, d.lng);
            Point screen = map.getProjection().toPixels(p, null);
            DeviceCluster best = null;
            double bestDistance = Double.MAX_VALUE;
            for (DeviceCluster c : clusters) {
                double distance = screenDistance(screen, c.screenCenter());
                if (distance < thresholdPx && distance < bestDistance) {
                    best = c;
                    bestDistance = distance;
                }
            }
            if (best == null) {
                best = new DeviceCluster();
                clusters.add(best);
            }
            best.add(d, screen);
        }

        boolean showNames = shouldShowFleetNames(devices.size(), lastClusterZoom);
        for (Device d : devices) {
            if (!isCriticalAlert(d)) continue;
            addFleetMarker(d, true, true);
        }
        for (DeviceCluster cluster : clusters) {
            if (cluster.size() == 1) {
                Device d = cluster.items.get(0);
                addFleetMarker(d, showNames, false);
            } else {
                Marker marker = new Marker(map);
                marker.setPosition(cluster.center());
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                marker.setIcon(clusterIcon(cluster.size()));
                marker.setTitle(cluster.size() + " dispositivos agrupados");
                marker.setSnippet(cluster.names());
                map.getOverlays().add(marker);
            }
        }
        Log.i(TAG, "clusters flota dispositivos=" + devices.size() + " clusters=" + clusters.size());
    }

    private int clusterThresholdPx(double zoom) {
        if (zoom >= 13.0) return 0;
        if (zoom >= 12.0) return 28;
        if (zoom >= 11.0) return 44;
        if (zoom >= 10.0) return 62;
        if (zoom >= 9.0) return 78;
        return 96;
    }

    private boolean shouldShowFleetNames(int deviceCount, double zoom) {
        if (zoom < 11.5) return false;
        if (deviceCount > 80 && zoom < 12.5) return false;
        return true;
    }

    private double screenDistance(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.hypot(dx, dy);
    }

    private Drawable clusterIcon(int count) {
        String key = clusterIconKey(count);
        Drawable cached = clusterIconCache.get(key);
        if (cached != null) return cached;

        int size = 88;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
        circle.setColor(Color.rgb(11, 46, 94));
        circle.setStyle(Paint.Style.FILL);
        canvas.drawCircle(size / 2f, size / 2f, size * 0.42f, circle);

        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setColor(Color.WHITE);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(5f);
        canvas.drawCircle(size / 2f, size / 2f, size * 0.42f, border);

        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setColor(Color.WHITE);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(count > 99 ? 24f : 30f);
        text.setFakeBoldText(true);
        Paint.FontMetrics fm = text.getFontMetrics();
        float y = size / 2f - (fm.ascent + fm.descent) / 2f;
        canvas.drawText(String.valueOf(count), size / 2f, y, text);
        Drawable icon = new BitmapDrawable(getResources(), bitmap);
        clusterIconCache.put(key, icon);
        return icon;
    }

    private Drawable markerIcon(Device d) {
        if (isCriticalAlert(d)) return criticalAlertMarkerIcon();
        if (d != null && d.lowBattery()) return lowBatteryMarkerIcon();
        return ContextCompat.getDrawable(this, R.drawable.marker_boat);
    }

    private boolean isCriticalAlert(Device d) {
        return d != null && Device.isCriticalSosMessageType(d.messageType);
    }

    private void addFleetMarker(Device d, boolean showName, boolean forceInfoWindow) {
        GeoPoint point = new GeoPoint(d.lat, d.lng);
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (isCriticalAlert(d)) map.getOverlays().add(new AlertPulseOverlay(point, true));
        else if (d.lowBattery()) map.getOverlays().add(new AlertPulseOverlay(point, false));
        Drawable icon = markerIcon(d);
        if (icon != null) marker.setIcon(icon);
        String label = d.name == null || d.name.isEmpty() ? d.series : d.name;
        marker.setTitle(label + " (" + d.registration + ")");
        marker.setSnippet(d.mapInfo());
        map.getOverlays().add(marker);
        if (forceInfoWindow) marker.showInfoWindow();
        if (showName) map.getOverlays().add(new DeviceNameOverlay(point, label));
    }

    private Drawable criticalAlertMarkerIcon() {
        if (criticalAlertIcon != null) return criticalAlertIcon;
        criticalAlertIcon = coloredBoatMarkerIcon(Color.rgb(255, 40, 0));
        return criticalAlertIcon;
    }

    private Drawable lowBatteryMarkerIcon() {
        if (lowBatteryIcon != null) return lowBatteryIcon;
        lowBatteryIcon = coloredBoatMarkerIcon(Color.rgb(245, 124, 0));
        return lowBatteryIcon;
    }

    private Drawable coloredBoatMarkerIcon(int color) {
        int size = 108;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
        bg.setColor(color);
        bg.setStyle(Paint.Style.FILL);
        canvas.drawCircle(size / 2f, size / 2f, size * 0.44f, bg);

        Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
        border.setColor(Color.WHITE);
        border.setStyle(Paint.Style.STROKE);
        border.setStrokeWidth(5f);
        canvas.drawCircle(size / 2f, size / 2f, size * 0.44f, border);

        Paint boat = new Paint(Paint.ANTI_ALIAS_FLAG);
        boat.setColor(Color.WHITE);
        boat.setStyle(Paint.Style.FILL);
        Path hull = new Path();
        hull.moveTo(size * 0.22f, size * 0.58f);
        hull.lineTo(size * 0.78f, size * 0.58f);
        hull.lineTo(size * 0.66f, size * 0.75f);
        hull.lineTo(size * 0.34f, size * 0.75f);
        hull.close();
        canvas.drawPath(hull, boat);
        canvas.drawRect(size * 0.42f, size * 0.22f, size * 0.58f, size * 0.56f, boat);
        canvas.drawRect(size * 0.34f, size * 0.34f, size * 0.66f, size * 0.42f, boat);

        return new BitmapDrawable(getResources(), bitmap);
    }

    private String clusterIconKey(int count) {
        if (count < 10) return "2-9:" + count;
        if (count < 100) return "10-99:" + count;
        return "100+";
    }

    private static class DeviceCluster {
        final List<Device> items = new ArrayList<>();
        final List<Point> screens = new ArrayList<>();
        double lat;
        double lng;
        int screenX;
        int screenY;

        void add(Device d, Point screen) {
            items.add(d);
            screens.add(screen);
            lat = ((lat * (items.size() - 1)) + d.lat) / items.size();
            lng = ((lng * (items.size() - 1)) + d.lng) / items.size();
            screenX = (int) Math.round(((screenX * (items.size() - 1)) + screen.x) / (double) items.size());
            screenY = (int) Math.round(((screenY * (items.size() - 1)) + screen.y) / (double) items.size());
        }

        int size() {
            return items.size();
        }

        GeoPoint center() {
            return new GeoPoint(lat, lng);
        }

        Point screenCenter() {
            return new Point(screenX, screenY);
        }

        String names() {
            StringBuilder b = new StringBuilder();
            int max = Math.min(8, items.size());
            for (int i = 0; i < max; i++) {
                Device d = items.get(i);
                if (b.length() > 0) b.append('\n');
                b.append(d.name == null || d.name.isEmpty() ? d.series : d.name);
                if (d.registration != null && !d.registration.isEmpty()) b.append(" (").append(d.registration).append(")");
            }
            if (items.size() > max) b.append("\n+").append(items.size() - max).append(" mÃ¡s");
            return b.toString();
        }
    }

    private void setupOpenSeaMapOverlay() {
        OnlineTileSourceBase marineProfile = new OnlineTileSourceBase(
                "OpenSeaMapMarineProfile",
                0,
                18,
                256,
                ".png",
                new String[]{"https://geoserver.openseamap.org/geoserver/gwc/service/wms"}
        ) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                int z = MapTileIndex.getZoom(pMapTileIndex);
                int x = MapTileIndex.getX(pMapTileIndex);
                int y = MapTileIndex.getY(pMapTileIndex);
                double[] bbox = webMercatorBbox(x, y, z);
                return getBaseUrl()
                        + "?SERVICE=WMS"
                        + "&VERSION=1.1.1"
                        + "&REQUEST=GetMap"
                        + "&LAYERS=gebco2021:gebco_2021"
                        + "&STYLES="
                        + "&FORMAT=image/png"
                        + "&TRANSPARENT=true"
                        + "&SRS=EPSG:3857"
                        + "&BBOX=" + bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3]
                        + "&WIDTH=256"
                        + "&HEIGHT=256";
            }
        };
        MapTileProviderBasic marineProvider = new MapTileProviderBasic(this, marineProfile);
        marineProfileOverlay = new TilesOverlay(marineProvider, this);
        marineProfileOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);

        satelliteTileSource = new XYTileSource(
                "WorldImagery",
                0,
                19,
                256,
                ".jpg",
                new String[]{"https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"}
        ) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                int z = MapTileIndex.getZoom(pMapTileIndex);
                int x = MapTileIndex.getX(pMapTileIndex);
                int y = MapTileIndex.getY(pMapTileIndex);
                return getBaseUrl() + z + "/" + y + "/" + x + ".jpg";
            }
        };

        XYTileSource seaMarks = new XYTileSource(
                "OpenSeaMap",
                0,
                18,
                256,
                ".png",
                new String[]{"https://tiles.openseamap.org/seamark/"}
        );
        MapTileProviderBasic provider = new MapTileProviderBasic(this, seaMarks);
        seaMarksOverlay = new TilesOverlay(provider, this);
        seaMarksOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
        addOpenSeaMapOverlay();
    }

    private void addOpenSeaMapOverlay() {
        if (bathymetryVisible && marineProfileOverlay != null && !map.getOverlays().contains(marineProfileOverlay)) {
            map.getOverlays().add(0, marineProfileOverlay);
        }
        if (seaMarksOverlay != null && !map.getOverlays().contains(seaMarksOverlay)) {
            map.getOverlays().add(Math.min(1, map.getOverlays().size()), seaMarksOverlay);
        }
    }

    private void toggleBathymetry() {
        bathymetryVisible = !bathymetryVisible;
        if (bathymetryVisible) {
            if (marineProfileOverlay != null && !map.getOverlays().contains(marineProfileOverlay)) {
                map.getOverlays().add(0, marineProfileOverlay);
            }
        } else {
            map.getOverlays().remove(marineProfileOverlay);
        }
        bathymetryButton.setText(bathymetryVisible ? "SIN BAT." : "BATIMETRIA");
        map.invalidate();
    }

    private void toggleSatellite() {
        satelliteVisible = !satelliteVisible;
        if (satelliteVisible) {
            if (satelliteTileSource != null) map.setTileSource(satelliteTileSource);
        } else {
            map.setTileSource(TileSourceFactory.MAPNIK);
        }
        satelliteButton.setText(satelliteVisible ? "MAPA" : "SATELITE");
        map.invalidate();
    }

    private void toggleFishing() {
        fishingVisible = !fishingVisible;
        if (fishingVisible) {
            if (fishingOverlays.isEmpty()) loadFishingZones();
            for (Overlay overlay : fishingOverlays) {
                if (!map.getOverlays().contains(overlay)) map.getOverlays().add(overlay);
            }
        } else {
            map.getOverlays().removeAll(fishingOverlays);
        }
        fishingButton.setText(fishingVisible ? "SIN PESCA" : "PESCA");
        map.invalidate();
    }

    private void loadFishingZones() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("zonas-pesca.tsv"), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                String[] cols = line.split("\t", 2);
                if (cols.length < 2) continue;
                String name = cols[0].trim();
                String[] parts = cols[1].split("\\|");
                for (String part : parts) {
                    List<GeoPoint> points = parseFishingPart(part);
                    if (points.size() < 3) continue;
                    if (!isMaritimeFishingZone(points)) continue;
                    Polygon polygon = new Polygon(map);
                    polygon.setPoints(points);
                    polygon.setTitle(name);
                    polygon.setFillColor(Color.argb(70, 255, 214, 0));
                    polygon.setStrokeColor(Color.rgb(255, 193, 7));
                    polygon.setStrokeWidth(3f);
                    fishingOverlays.add(polygon);
                    fishingOverlays.add(new FishingZoneLabelOverlay(zoneLabelPoint(points), name));
                }
            }
            Log.i(TAG, "zonas pesca overlays=" + fishingOverlays.size());
        } catch (Exception e) {
            Log.e(TAG, "No se pudo cargar zonas de pesca", e);
            Ui.toast(this, "No se pudo cargar capa de pesca: " + e.getMessage(), Toast.LENGTH_LONG);
        }
    }

    private List<GeoPoint> parseFishingPart(String part) {
        List<GeoPoint> points = new ArrayList<>();
        String[] pairs = part.split(";");
        for (String pair : pairs) {
            String[] latLng = pair.split(",");
            if (latLng.length != 2) continue;
            try {
                points.add(new GeoPoint(Double.parseDouble(latLng[0].trim()), Double.parseDouble(latLng[1].trim())));
            } catch (Exception ignored) {
            }
        }
        return points;
    }

    private boolean isMaritimeFishingZone(List<GeoPoint> points) {
        double west = 180;
        double east = -180;
        double avgLon = 0;
        for (GeoPoint point : points) {
            double lon = point.getLongitude();
            west = Math.min(west, lon);
            east = Math.max(east, lon);
            avgLon += lon;
        }
        avgLon = avgLon / points.size();
        return west <= -79.0 || avgLon <= -79.0 || east <= -79.0;
    }

    private GeoPoint zoneLabelPoint(List<GeoPoint> points) {
        double lat = 0;
        double lon = 0;
        for (GeoPoint point : points) {
            lat += point.getLatitude();
            lon += point.getLongitude();
        }
        return new GeoPoint(lat / points.size(), lon / points.size());
    }

    private static double[] webMercatorBbox(int x, int y, int z) {
        double originShift = 20037508.342789244;
        double tiles = Math.pow(2, z);
        double resolution = (2 * originShift) / (256 * tiles);
        double minx = x * 256 * resolution - originShift;
        double maxx = (x + 1) * 256 * resolution - originShift;
        double maxy = originShift - y * 256 * resolution;
        double miny = originShift - (y + 1) * 256 * resolution;
        return new double[]{minx, miny, maxx, maxy};
    }

    private String historyLabel(Device d, int index) {
        String name = getIntent().getStringExtra("history_name");
        if (name == null || name.trim().isEmpty()) name = d.name;
        if (name == null || name.trim().isEmpty()) name = d.series;
        return name + " #" + index;
    }

    private void sortHistory(List<Device> devices) {
        Collections.sort(devices, (a, b) -> Long.compare(DateTimeUtils.utcMillis(a.located), DateTimeUtils.utcMillis(b.located)));
    }

    private static class FishingZoneLabelOverlay extends Overlay {
        private final GeoPoint point;
        private final String name;
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Rect textBounds = new Rect();

        FishingZoneLabelOverlay(GeoPoint point, String name) {
            this.point = point;
            this.name = name == null ? "" : name;
            textPaint.setColor(Color.BLACK);
            textPaint.setTextSize(24f);
            textPaint.setFakeBoldText(true);
            bgPaint.setColor(Color.argb(220, 255, 235, 59));
            bgPaint.setStyle(Paint.Style.FILL);
            borderPaint.setColor(Color.rgb(90, 70, 0));
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(2f);
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (shadow || name.isEmpty()) return;
            double zoom = mapView.getZoomLevelDouble();
            if (zoom < 6.5) return;
            Point screen = mapView.getProjection().toPixels(point, null);
            String label = name.length() > 22 ? name.substring(0, 22) + "…" : name;
            textPaint.getTextBounds(label, 0, label.length(), textBounds);
            int padX = 9;
            int padY = 6;
            float left = screen.x + 14;
            float top = screen.y - textBounds.height() - 14;
            float right = left + textBounds.width() + padX * 2;
            float bottom = top + textBounds.height() + padY * 2;
            canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, bgPaint);
            canvas.drawRoundRect(left, top, right, bottom, 8f, 8f, borderPaint);
            canvas.drawText(label, left + padX, bottom - padY, textPaint);
        }
    }

    private static class AlertPulseOverlay extends Overlay {
        private final GeoPoint point;
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);

        AlertPulseOverlay(GeoPoint point, boolean critical) {
            this.point = point;
            fill.setColor(critical ? Color.rgb(255, 40, 0) : Color.rgb(255, 152, 0));
            fill.setStyle(Paint.Style.FILL);
            stroke.setColor(critical ? Color.rgb(190, 0, 0) : Color.rgb(230, 81, 0));
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(4f);
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (shadow) return;
            long phaseMs = SystemClock.uptimeMillis() % 1200L;
            float phase = phaseMs / 1200f;
            float radius = 32f + 22f * phase;
            int alpha = (int) (150f * (1f - phase));
            Point screen = mapView.getProjection().toPixels(point, null);
            fill.setAlpha(Math.max(30, alpha));
            stroke.setAlpha(Math.max(45, alpha + 35));
            canvas.drawCircle(screen.x, screen.y - 38f, radius, fill);
            canvas.drawCircle(screen.x, screen.y - 38f, radius, stroke);
            mapView.postInvalidateDelayed(120);
        }
    }

    private static class DirectionArrowOverlay extends Overlay {
        private final List<GeoPoint> points;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        DirectionArrowOverlay(List<GeoPoint> points) {
            this.points = new ArrayList<>(points);
            paint.setColor(Color.rgb(11, 46, 94));
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (shadow || points.size() < 2) return;
            for (int i = 1; i < points.size(); i++) {
                Point a = mapView.getProjection().toPixels(points.get(i - 1), null);
                Point b = mapView.getProjection().toPixels(points.get(i), null);
                drawArrow(canvas, a, b);
            }
        }

        private void drawArrow(Canvas canvas, Point a, Point b) {
            float dx = b.x - a.x;
            float dy = b.y - a.y;
            double len = Math.hypot(dx, dy);
            if (len < 28) return;

            float ux = (float) (dx / len);
            float uy = (float) (dy / len);
            float mx = (a.x + b.x) / 2f;
            float my = (a.y + b.y) / 2f;
            float size = 15f;
            float wing = 8f;

            Path arrow = new Path();
            arrow.moveTo(mx + ux * size, my + uy * size);
            arrow.lineTo(mx - ux * size * 0.5f - uy * wing, my - uy * size * 0.5f + ux * wing);
            arrow.lineTo(mx - ux * size * 0.5f + uy * wing, my - uy * size * 0.5f - ux * wing);
            arrow.close();
            canvas.drawPath(arrow, paint);
        }
    }

    private static class DeviceNameOverlay extends Overlay {
        private final GeoPoint point;
        private final String name;
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Rect textBounds = new Rect();

        DeviceNameOverlay(GeoPoint point, String name) {
            this.point = point;
            this.name = name == null ? "" : name;
            textPaint.setColor(Color.rgb(255, 235, 59));
            textPaint.setTextSize(26f);
            textPaint.setFakeBoldText(true);
            textPaint.setShadowLayer(3f, 1f, 1f, Color.BLACK);
            bgPaint.setColor(Color.argb(145, 0, 31, 63));
            bgPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow) {
            if (shadow || name.isEmpty()) return;
            Point screen = mapView.getProjection().toPixels(point, null);
            textPaint.getTextBounds(name, 0, name.length(), textBounds);

            int x = screen.x + 18;
            int y = screen.y - 28;
            int padX = 8;
            int padY = 5;
            canvas.drawRoundRect(
                    x - padX,
                    y + textBounds.top - padY,
                    x + textBounds.width() + padX,
                    y + textBounds.bottom + padY,
                    8f,
                    8f,
                    bgPaint
            );
            canvas.drawText(name, x, y, textPaint);
        }
    }

    private void zoomTo(List<GeoPoint> points) {
        if (points.size() == 1) {
            map.getController().setZoom(14.0);
            map.getController().animateTo(points.get(0));
            return;
        }

        double north = -90, south = 90, east = -180, west = 180;
        for (GeoPoint p : points) {
            north = Math.max(north, p.getLatitude());
            south = Math.min(south, p.getLatitude());
            east = Math.max(east, p.getLongitude());
            west = Math.min(west, p.getLongitude());
        }
        double latMargin = Math.max(0.01, (north - south) * 0.18);
        double lonMargin = Math.max(0.01, (east - west) * 0.18);
        BoundingBox box = new BoundingBox(
                north + latMargin,
                east + lonMargin,
                south - latMargin,
                west - lonMargin
        );
        map.zoomToBoundingBox(box, true, 110);
        map.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        if (map != null) map.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }
}
