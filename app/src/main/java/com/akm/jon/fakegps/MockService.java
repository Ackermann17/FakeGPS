package com.akm.jon.fakegps;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class MockService extends Service {
    private static final String CHANNEL_ID = "MockLocationChannel";
    public static boolean isRunning = false;

    private LocationManager lm;
    private Thread mockThread;
    
    // UI Overlay (Floating Button)
    private WindowManager windowManager;
    private Button floatingButton;
    private android.widget.FrameLayout floatingContainer;
    private WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 1. Notifikasi Foreground
        Notification.Builder builder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                ? new Notification.Builder(this, CHANNEL_ID) 
                : new Notification.Builder(this);

        Notification notification = builder.setContentTitle("GPS Manual Aktif")
                .setContentText("Lokasi terkunci di latar belakang.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .build();
                
        startForeground(1, notification);

        if (intent != null) {
            final double lat = intent.getDoubleExtra("lat", 0.0d);
            final double lng = intent.getDoubleExtra("lng", 0.0d);

            // 2. Inisialisasi Mock Provider
            if (this.lm == null) {
                this.lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
            try {
                this.lm.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, false, false, false, 1, 1);
                this.lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            } catch (Exception ignored) {}

            isRunning = true;

            // 3. Jalankan Thread Mocking Location
            if (this.mockThread != null) {
                this.mockThread.interrupt();
            }
            this.mockThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        try {
                            Location loc = new Location(LocationManager.GPS_PROVIDER);
                            loc.setLatitude(lat);
                            loc.setLongitude(lng);
                            loc.setAltitude(0.0d);
                            loc.setAccuracy(1.0f);
                            loc.setTime(System.currentTimeMillis());
                            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                            
                            MockService.this.lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, loc);
                            Thread.sleep(1000L);
                        } catch (Exception e) {
                            break;
                        }
                    }
                }
            });
            this.mockThread.start();

            // 4. Tampilkan Floating Overlay Button jika belum ada
            if (floatingButton == null) {
                createFloatingWindow();
            }
        }
        return START_STICKY;
    }

    private void createFloatingWindow() {
        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            
            int layoutFlag;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
            }

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );

            params.gravity = Gravity.TOP | Gravity.START;
            params.x = 100;
            params.y = 300;

            // 1. Buat Wadah secara Global
            floatingContainer = new android.widget.FrameLayout(this);
            int windowSize = (int) (70 * getResources().getDisplayMetrics().density);
            params.width = windowSize;
            params.height = windowSize;

            // 2. Buat Tombol Bulat
            floatingButton = new Button(this);
            floatingButton.setText("■");
            floatingButton.setTextSize(24f);
            floatingButton.setTextColor(Color.WHITE);

	    floatingButton.setPadding(0, 0, 0, 0); // Hapus jarak tepi bawaan Android
            floatingButton.setGravity(Gravity.CENTER); // Paksa teks persis di tengah
            floatingButton.setIncludeFontPadding(false); // Hapus spasi font ekstra
            // Sangat Penting: Biarkan wadah (container) yang merespon sentuhan, bukan tombolnya
            floatingButton.setClickable(false);
            floatingButton.setFocusable(false);

            android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
            shape.setShape(android.graphics.drawable.GradientDrawable.OVAL);
            shape.setColor(Color.RED);
            floatingButton.setBackground(shape);

            int btnSize = (int) (45 * getResources().getDisplayMetrics().density);
            android.widget.FrameLayout.LayoutParams btnParams = new android.widget.FrameLayout.LayoutParams(btnSize, btnSize);
            btnParams.gravity = Gravity.CENTER;
            floatingContainer.addView(floatingButton, btnParams);

            // 3. Listener Sentuhan pada Container
            floatingContainer.setOnTouchListener(new View.OnTouchListener() {
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(floatingContainer, params);
                            return true;

                        case MotionEvent.ACTION_UP:
                            int diffX = (int) (event.getRawX() - initialTouchX);
                            int diffY = (int) (event.getRawY() - initialTouchY);
                            // Jika klik (bukan digeser)
                            if (Math.abs(diffX) < 10 && Math.abs(diffY) < 10) {
                                stopSelf(); 
                            }
                            return true;
                    }
                    return false;
                }
            });

            // 4. Masukkan Wadah ke Layar
            windowManager.addView(floatingContainer, params);

            // 5. Jalankan Animasi Denyut
            android.animation.ObjectAnimator scaleX = android.animation.ObjectAnimator.ofFloat(floatingButton, "scaleX", 1.0f, 1.25f);
            android.animation.ObjectAnimator scaleY = android.animation.ObjectAnimator.ofFloat(floatingButton, "scaleY", 1.0f, 1.25f);
            scaleX.setDuration(800);
            scaleY.setDuration(800);
            scaleX.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            scaleY.setRepeatCount(android.animation.ValueAnimator.INFINITE);
            scaleX.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            scaleY.setRepeatMode(android.animation.ValueAnimator.REVERSE);
            scaleX.start();
            scaleY.start();

        } catch (Exception e) {
            // Pengaman agar tidak Force Close, melainkan memunculkan pesan error
            android.widget.Toast.makeText(this, "Error Tombol: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, 
                    "GPS Manual Active", 
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        
        // Hentikan Thread
        if (this.mockThread != null) {
            this.mockThread.interrupt();
        }

        // Hapus Mock Provider
        try {
            if (this.lm != null) {
                this.lm.removeTestProvider(LocationManager.GPS_PROVIDER);
            }
        } catch (Exception ignored) {}

        // Hapus Floating Overlay View dari Layar
	if (floatingContainer != null && windowManager != null) {
            try {
                windowManager.removeView(floatingContainer);
            } catch (Exception e) {}
            floatingContainer = null;
            floatingButton = null;
        }
	super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
