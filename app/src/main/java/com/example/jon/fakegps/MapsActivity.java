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

    private void setMockLocation(double latitude, double longitude) {
        newLocationGPS.setLatitude(latitude);
        newLocationGPS.setLongitude(longitude);
        newLocationGPS.setAccuracy(16F);
        newLocationGPS.setTime(System.currentTimeMillis());
        newLocationGPS.setElapsedRealtimeNanos(System.nanoTime());
        newLocationGPS.setAltitude(0D);
        newLocationGPS.setBearing(0F);
        newLocationNET.setLatitude(latitude);
        newLocationNET.setLongitude(longitude);
        newLocationNET.setAccuracy(16F);
        newLocationNET.setTime(System.currentTimeMillis());
        newLocationNET.setElapsedRealtimeNanos(System.nanoTime());
        newLocationNET.setAltitude(0D);
        newLocationNET.setBearing(0F);
        lm.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null, System.currentTimeMillis());
        lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, newLocationGPS);
        lm.setTestProviderStatus(LocationManager.NETWORK_PROVIDER,
                LocationProvider.AVAILABLE,
                null, System.currentTimeMillis());
        lm.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, newLocationNET);
        showMessage("Mock ON");
        mockEnabled = true;
        adapter.add(latLng);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
        if(!mockEnabled)
            startMockProvider();
        latLng = mMap.getCameraPosition().target;
        setMockLocation(latLng.latitude, latLng.longitude);
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
        }catch(SecurityException e) {e.printStackTrace();}
    }

    public void searchButton(View view){
        EditText editText = (EditText)findViewById(R.id.search_text);
        String address = editText.getText().toString();
        performSearch(address);
    }

    public void recentButton(View view){
        if(!arrayList.isEmpty()){
            if(list.getVisibility() == View.INVISIBLE){
                list.setVisibility(View.VISIBLE);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        LatLng recentLatLng = new LatLng(((LatLng) parent.getAdapter().getItem(position)).latitude, ((LatLng) parent.getAdapter().getItem(position)).longitude);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(recentLatLng, 10));
                        list.setVisibility(View.INVISIBLE);
                    }
                });
            }else{
                list.setVisibility(View.INVISIBLE);
            }
        }else{
            showMessage("No recent locations");
        }
        showMessage(String.valueOf(latLng));
        FileInputStream inputStream;
        try {
            inputStream = openFileInput(SAVED_SETTINGS);
            ObjectInputStream in = new ObjectInputStream(inputStream);
            showMessage("mockBoolean" +  String.valueOf(in.readBoolean()));
            showMessage("latLng" + String.valueOf(in.readObject()));
            inputStream.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stopButton(View view){
        if(mockEnabled) {
            try {
                lm.removeTestProvider(LocationManager.GPS_PROVIDER);
                lm.removeTestProvider(LocationManager.NETWORK_PROVIDER);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
            } catch (SecurityException e) {e.printStackTrace();}
        }
        mockEnabled = false;
        showMessage("Mock OFF");
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
        if(latLng != null){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }
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
        if(mockEnabled) {
            try {
                lm.removeTestProvider(LocationManager.GPS_PROVIDER);
                lm.removeTestProvider(LocationManager.NETWORK_PROVIDER);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
            } catch (SecurityException e) {e.printStackTrace();}
        }
        mockEnabled = false;
    }

    private void showMessage(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

}
