package com.akm.jon.fakegps;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class MockService extends Service {
    private static final String CHANNEL_ID = "MockLocationChannel";

    @Override
    public void onCreate() {
        super.onCreate();
        // Membuat saluran (channel) notifikasi untuk Android 8.0 ke atas
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

        // Desain notifikasi latar belakang
        Notification notification = builder
                .setContentTitle("GPS Manual Aktif")
                .setContentText("Lokasi palsu sedang berjalan di latar belakang.")
                .setSmallIcon(R.mipmap.ic_launcher) // Menggunakan ikon utama aplikasi
                .setOngoing(true) // Tidak bisa di-swipe oleh pengguna
                .build();

        // Mulai layanan agar aplikasi kebal dari kill sistem
        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
