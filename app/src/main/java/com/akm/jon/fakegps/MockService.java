package com.akm.jon.fakegps;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

public class MockService extends Service {
    private static final String CHANNEL_ID = "MockLocationChannel";
    private LocationManager lm;
    private boolean isRunning = false;
    private Thread mockThread;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "GPS Manual Active", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        Notification notification = builder
                .setContentTitle("GPS Manual Aktif")
                .setContentText("Lokasi terkunci dan memancar di latar belakang.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
        startForeground(1, notification);

        // Tangkap koordinat yang dikirim dari MapsActivity
        if (intent != null) {
            final double lat = intent.getDoubleExtra("lat", 0);
            final double lng = intent.getDoubleExtra("lng", 0);

            if (lm == null) lm = (LocationManager) getSystemService(LOCATION_SERVICE);

            isRunning = true;
            if (mockThread != null) mockThread.interrupt();

            // Mesin pemancar lokasi dipindah ke sini agar kebal swipe
            mockThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            Location loc = new Location(LocationManager.GPS_PROVIDER);
                            loc.setLatitude(lat);
                            loc.setLongitude(lng);
                            loc.setAltitude(0);
                            loc.setAccuracy(1f);
                            loc.setTime(System.currentTimeMillis());
                            loc.setElapsedRealtimeNanos(android.os.SystemClock.elapsedRealtimeNanos());
                            lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc);
                            Thread.sleep(1000); // Pancarkan setiap 1 detik
                        } catch (Exception e) {
                            // Abaikan error diam-diam agar thread tidak mati
                        }
                    }
                }
            });
            mockThread.start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        isRunning = false; // Matikan mesin pemancar saat Service dihentikan
        if (mockThread != null) mockThread.interrupt();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
