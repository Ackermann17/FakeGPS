package com.example.jon.fakegps;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng latLng;
    private boolean mockEnabled = false;
    private LocationManager lm;
    private LocationListener ll;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    //Sets Mock Location
    private void setMockLocation(double latitude, double longitude) {
        if(!mockEnabled){
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
        }
        Location newLocation = new Location(LocationManager.GPS_PROVIDER);
        Location newLocation1 = new Location(LocationManager.NETWORK_PROVIDER);
        newLocation.setLatitude(latitude);
        newLocation.setLongitude(longitude);
        newLocation.setAccuracy(16F);
        newLocation.setTime(System.currentTimeMillis());
        newLocation.setElapsedRealtimeNanos(System.nanoTime());
        newLocation.setAltitude(0D);
        newLocation.setBearing(0F);
        newLocation1.setLatitude(latitude);
        newLocation1.setLongitude(longitude);
        newLocation1.setAccuracy(16F);
        newLocation1.setTime(System.currentTimeMillis());
        newLocation1.setElapsedRealtimeNanos(System.nanoTime());
        newLocation1.setAltitude(0D);
        newLocation1.setBearing(0F);
        lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        lm.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null, System.currentTimeMillis());
        lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocation);
        lm.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        lm.setTestProviderStatus(LocationManager.NETWORK_PROVIDER,
                LocationProvider.AVAILABLE,
                null,System.currentTimeMillis());
        lm.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, newLocation1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void startButton(View view) {
        latLng = mMap.getCameraPosition().target;
        try {
            setMockLocation(latLng.latitude, latLng.longitude);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
        }catch(SecurityException e) {e.printStackTrace();}
        showMessage("Mock ON");
        mockEnabled = true;
    }

    public void searchButton(View view){
        EditText editText = (EditText)findViewById(R.id.search_text);
        String address = editText.getText().toString();
        performSearch(address);
    }
    public void recentButton(View view){

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
        ImageView img=(ImageView)findViewById(R.id.imageView);
        Drawable myDrawable = ContextCompat.getDrawable(this, R.drawable.pin);
        img.setImageDrawable(myDrawable);
        EditText editText = (EditText)findViewById(R.id.search_text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH)) {
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(v.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        if(mockEnabled) {
            try {
                lm.removeTestProvider(LocationManager.GPS_PROVIDER);
                lm.removeTestProvider(LocationManager.NETWORK_PROVIDER);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
            } catch (SecurityException e) {e.printStackTrace();}
        }
        super.onDestroy();
    }

    private void showMessage(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}
