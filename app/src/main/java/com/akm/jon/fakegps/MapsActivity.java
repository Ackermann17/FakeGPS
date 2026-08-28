package com.akm.jon.fakegps;

import com.akm.jon.fakegps.R;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.provider.Settings;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng latLng;
    private boolean mockEnabled;
    private LocationManager lm;
    private LocationListener ll;
    private Location newLocationGPS, newLocationNET;
    private ArrayList<LatLng> arrayList;
    private ListView list;
    private android.os.Handler mockHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable mockRunnable;
    private ArrayAdapter<LatLng> adapter;
    private static final String SAVED_SETTINGS = "SAVED_SETTINGS";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void startMockProvider(){
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {}
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };
        lm.addTestProvider(LocationManager.GPS_PROVIDER,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE);
        lm.addTestProvider(LocationManager.NETWORK_PROVIDER,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE);
        newLocationGPS = new Location(LocationManager.GPS_PROVIDER);
        newLocationNET = new Location(LocationManager.NETWORK_PROVIDER);
        lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        lm.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
    }

private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;

private void checkAndRequestOverlayPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Izinkan 'Tampilkan di atas aplikasi lain' / 'Jendela pop-up' agar Floating Button aktif", Toast.LENGTH_LONG).show();
            
            Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
            );
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            // Izin sudah diberikan, aman untuk jalankan MockService
            startMockService();
        }
    } else {
        startMockService();
    }
}

private void startMockService() {
    latLng = mMap.getCameraPosition().target;
    android.content.Intent serviceIntent = new android.content.Intent(this, MockService.class);
    serviceIntent.putExtra("lat", latLng.latitude);
    serviceIntent.putExtra("lng", latLng.longitude);

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        startForegroundService(serviceIntent);
    } else {
        startService(serviceIntent);
    }
    
    mockEnabled = true;
    android.widget.Toast.makeText(this, "✅ Lokasi Dimulai!", android.widget.Toast.LENGTH_SHORT).show();
}

private void stopMockService() {
    android.content.Intent serviceIntent = new android.content.Intent(this, MockService.class);
    stopService(serviceIntent);
    
    mockEnabled = false;
    android.widget.Toast.makeText(this, "🛑 Lokasi Dihentikan!", android.widget.Toast.LENGTH_SHORT).show();
}

private void setMockLocation(final double latitude, final double longitude) {
    if (mockRunnable != null) {
        mockHandler.removeCallbacks(mockRunnable);
    }

    mockRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                long nowRealtime = android.os.SystemClock.elapsedRealtimeNanos();
                long nowTime = System.currentTimeMillis();

                newLocationGPS.setLatitude(latitude);
                newLocationGPS.setLongitude(longitude);
                newLocationGPS.setAccuracy(16F);
                newLocationGPS.setTime(nowTime);
                newLocationGPS.setElapsedRealtimeNanos(nowRealtime);
                newLocationGPS.setAltitude(0D);
                newLocationGPS.setBearing(0F);

                newLocationNET.setLatitude(latitude);
                newLocationNET.setLongitude(longitude);
                newLocationNET.setAccuracy(16F);
                newLocationNET.setTime(nowTime);
                newLocationNET.setElapsedRealtimeNanos(nowRealtime);
                newLocationNET.setAltitude(0D);
                newLocationNET.setBearing(0F);

                lm.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                        LocationProvider.AVAILABLE,
                        null, nowTime);
                lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocationGPS);
                
                lm.setTestProviderStatus(LocationManager.NETWORK_PROVIDER,
                        LocationProvider.AVAILABLE,
                        null, nowTime);
                lm.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, newLocationNET);

                // Ulangi terus setiap 1 detik (1000 milidetik)
                mockHandler.postDelayed(this, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    mockHandler.post(mockRunnable);
    
    // Pesan status awal
    showMessage("Mock ON");
    mockEnabled = true;
    // adapter.add(latLng); // Sesuaikan dengan variabel list Anda jika ada
}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
	if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        
        androidx.core.app.ActivityCompat.requestPermissions(this,
                new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                }, 1);
	    }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        FileInputStream inputStream;
        try {
            inputStream = openFileInput(SAVED_SETTINGS);
            ObjectInputStream in = new ObjectInputStream(inputStream);
            mockEnabled = in.readBoolean();
            latLng = (LatLng) in.readObject();
            arrayList = (ArrayList<LatLng>) in.readObject();
            adapter = (ArrayAdapter<LatLng>) in.readObject();
            inputStream.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (latLng != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
        if(arrayList == null)
            arrayList = new ArrayList<>();
        if(adapter == null)
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_selectable_list_item, arrayList);
        list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);
    }
   
public void startButton(View view) {
    try {
        if (lm == null) lm = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);

        // Cek Developer Options
        try {
            lm.addTestProvider(android.location.LocationManager.GPS_PROVIDER, false, false, false, false, false, false, false, 1, 1);
            lm.removeTestProvider(android.location.LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            android.widget.Toast.makeText(this, "⚠️ Harap atur Mock Location di Developer Options", android.widget.Toast.LENGTH_SHORT).show();
            return;
        } catch (Exception e) {}

        // Cek izin Overlay / Pop-up Xiaomi sebelum kirim Intent
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                android.widget.Toast.makeText(this, "Izinkan 'Tampilkan di atas aplikasi lain' / 'Jendela pop-up'", android.widget.Toast.LENGTH_LONG).show();
                openMiuiPermissionScreen();
                return;
            }
        }

        // Ambil koordinat
        latLng = mMap.getCameraPosition().target;

        // Kirim perintah ke Service
        android.content.Intent serviceIntent = new android.content.Intent(this, MockService.class);
        serviceIntent.putExtra("lat", latLng.latitude);
        serviceIntent.putExtra("lng", latLng.longitude);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
	if (arrayList != null && adapter != null) {
            // Mencegah duplikat berurutan
            if (arrayList.isEmpty() || !arrayList.get(0).equals(latLng)) {
                arrayList.add(0, latLng); // Tambahkan ke urutan paling atas (index 0)
                adapter.notifyDataSetChanged(); // Beritahu ListView untuk update
            }
        }
	 else {
            startService(serviceIntent);
        }

        mockEnabled = true;
        android.widget.Toast.makeText(this, "✅ Lokasi Dimulai!", android.widget.Toast.LENGTH_SHORT).show();

    } catch (Exception e) {
        android.widget.Toast.makeText(this, "Sistem memproses... Silakan ulangi.", android.widget.Toast.LENGTH_SHORT).show();
    }
}

    public void searchButton(View view){
        EditText editText = (EditText)findViewById(R.id.search_text);
        String address = editText.getText().toString();
        performSearch(address);
    }

  public void recentButton(View view) {
    // Pastikan arrayList tidak kosong
    if (arrayList != null && !arrayList.isEmpty()) {
        
        // Toggle (Buka/Tutup) ListView
        if (list.getVisibility() == android.view.View.INVISIBLE || list.getVisibility() == android.view.View.GONE) {
            list.setVisibility(android.view.View.VISIBLE);
            
            // Set aksi ketika salah satu riwayat diklik
            list.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    // Pindah kamera ke lokasi recent yang diklik
                    com.google.android.gms.maps.model.LatLng recentLatLng = arrayList.get(position);
                    mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(recentLatLng, 15)); // Zoom level 15
                    
                    // Sembunyikan list setelah diklik
                    list.setVisibility(android.view.View.INVISIBLE);
                }
            });
        } else {
            // Jika sedang terbuka, maka tutup
            list.setVisibility(android.view.View.INVISIBLE);
        }
        
    } else {
        // Tampilkan pesan jika belum ada riwayat
        android.widget.Toast.makeText(this, "Belum ada riwayat lokasi", android.widget.Toast.LENGTH_SHORT).show();
    }
}

public void stopButton(View view) {
    try {
        // Langsung matikan service, sisanya akan diurus otomatis oleh MockService
        android.content.Intent serviceIntent = new android.content.Intent(this, MockService.class);
        stopService(serviceIntent);

        // Pengaman ganda untuk mematikan mock provider
        if (lm == null) lm = (android.location.LocationManager) getSystemService(LOCATION_SERVICE);
        lm.removeTestProvider(android.location.LocationManager.GPS_PROVIDER);
    } catch (Exception e) {
        // Abaikan jika sudah mati
    }

    mockEnabled = false; // Reset status
    android.widget.Toast.makeText(this, "🛑 Lokasi Dihentikan", android.widget.Toast.LENGTH_SHORT).show();
}

    private void performSearch(String address){
        EditText editText = (EditText)findViewById(R.id.search_text);
        editText.clearFocus();
        InputMethodManager in = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        if(!address.equals("")){
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try{
                List<Address> ad = geocoder.getFromLocationName(address, 1);
                double lati = ad.get(0).getLatitude();
                double longi = ad.get(0).getLongitude();
                LatLng yerp = new LatLng(lati, longi);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(yerp, 10));
            }catch(IOException e){e.printStackTrace();}
        }
    }
// Letakkan method ini berdiri sendiri di bawah stopButton()
public void openMiuiPermissionScreen() {
    android.content.Intent intent = new android.content.Intent("miui.intent.action.APP_PERM_EDITOR");
    intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
    intent.putExtra("extra_pkgname", getPackageName());
    try {
        startActivity(intent);
    } catch (Exception e) {
        // Fallback jika bukan perangkat MIUI / gagal buka menu khusus
        android.content.Intent defaultIntent = new android.content.Intent(
            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:" + getPackageName())
        );
        startActivity(defaultIntent);
    }
}

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
 
   @Override
public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

	// Minta izin notifikasi untuk Android 13 ke atas (Tiramisu/API 33+)
	if (android.os.Build.VERSION.SDK_INT >= 33) {
	    if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
	            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
	        androidx.core.app.ActivityCompat.requestPermissions(this, 
	                new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
	    }
	}

    try {
        if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) 
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Kamera otomatis mengarah ke titik biru saat lokasi pertama kali didapat
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                private boolean isFirstLocation = true;

                @Override
                public void onMyLocationChange(android.location.Location location) {
                    if (isFirstLocation && location != null) {
                        com.google.android.gms.maps.model.LatLng currentLatLng = 
                            new com.google.android.gms.maps.model.LatLng(location.getLatitude(), location.getLongitude());

                        mMap.moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                        isFirstLocation = false;
                    }
                }
	    });
	    mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
        private com.google.android.gms.maps.model.Marker currentMarker;

        @Override
        public void onMapClick(com.google.android.gms.maps.model.LatLng point) {
            // Hapus marker sebelumnya jika sudah ada supaya tidak menumpuk
            if (currentMarker != null) {
                currentMarker.remove();
            }

            // Tambahkan marker baru di titik yang diklik
            currentMarker = mMap.addMarker(new com.google.android.gms.maps.model.MarkerOptions()
                    .position(point)
                    .title("Tujuan Mock Lokasi"));

            // Geser kamera secara halus ke titik yang diklik
            mMap.animateCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLng(point));
        }
            });
        }
    } catch (SecurityException e) {
        e.printStackTrace();
    }
}

    @Override
    protected void onPause() {
        super.onPause();
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(SAVED_SETTINGS, MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeBoolean(mockEnabled);
            out.writeObject(latLng);
            out.writeObject(arrayList);
            out.writeObject(adapter);
            outputStream.close();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

@Override
protected void onDestroy() {
    super.onDestroy();
    // Semua logika pemati lokasi dihapus dari sini 
    // agar lokasi palsu tetap hidup walau aplikasi di-swipe!
}

    private void showMessage(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}
