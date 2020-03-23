package com.example.retogmaps;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener, LocationListener,
        GoogleMap.OnMarkerClickListener, AddDialog.AddDialogListener {

    private GoogleMap mMap;
    private Marker locationU;
    private Geocoder geocoder;
    private ArrayList<Marker> markers;
    private Button addMarker;
    private TextView boxInfo;
    private boolean selectionMode;
    private String lastNameMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        addMarker = findViewById(R.id.addBt);
        boxInfo = findViewById(R.id.infoBox);

        addMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openDialog();

            }
        });

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
        }, 11);

        markers = new ArrayList<Marker>();
        selectionMode = false;
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        geocoder = new Geocoder(this, Locale.getDefault());
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        // Add a marker in Sydney and move the camera
        //LocationManager locationManager  = (LocationManager) getSystemService(LOCATION_SERVICE);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        LatLng cali = new LatLng(3.42158, -76.5205);
        locationU=mMap.addMarker(new MarkerOptions().position(cali).title("Estas en esta ciudad").icon(
                BitmapDescriptorFactory.fromResource(R.drawable.actualmarker)
        ));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cali,15));

        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,  1000, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());
        locationU.setPosition(pos);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));

        if(!selectionMode){
            showMarkerNearest();
        }
    }

    private void showMarkerNearest() {
        if(markers.size()> 0 ) {
            double distanceNearest = Double.MAX_VALUE;
            Marker nearest=null;
            for (Marker marker : markers
            ) {
                double distance = calculateDistance(locationU, marker);
                if (distance < distanceNearest) {
                    distanceNearest = distance;
                    nearest = marker;

                }
            }

            if (distanceNearest < 50) {
                boxInfo.setText("Usted esta en " + nearest.getTitle());
            } else {
                boxInfo.setText("El lugar mas cercano es " + nearest.getTitle());
            }
        }else{
            boxInfo.setText("No hay marcadores");
        }
    }
    public double calculateDistance(Marker a, Marker b){
        double x=a.getPosition().latitude-b.getPosition().latitude;
        double y=a.getPosition().longitude-b.getPosition().longitude;
        double distance = Math.sqrt(Math.pow(x, 2)
                + Math.pow(y,2));

        distance = distance* 90999.80314;


        Log.d("Distnacia",""+distance);

        return distance;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if(selectionMode){
            Marker marker =  mMap.addMarker(new MarkerOptions().position(latLng).title(lastNameMarker));
            markers.add(marker);
            selectionMode = false;
            showMarkerNearest();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(locationU)){
            try {
                ArrayList<Address> addresses = (ArrayList<Address>) geocoder.getFromLocation(locationU.getPosition().latitude, locationU.getPosition().longitude,1);
                locationU.setSnippet(addresses.get(0).getAddressLine(0));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        DecimalFormat format = new DecimalFormat("#.##");

        boolean bandera = false;
        for(int i = 0; i<markers.size() && !bandera; i++){
            if(marker.equals(markers.get(i))){
                bandera = true;
                double d = calculateDistance(locationU, markers.get(i));
                String dintence="Metros";
                if(d>=1000) {
                    dintence = "Kilometros";
                    d = d / 1000;
                }
                markers.get(i).setSnippet("Este marcador esta a : " + format.format(d) + dintence );
            }
        }
        return false;
    }

    public void openDialog(){
        AddDialog addDialog = new AddDialog();
        addDialog.show(getSupportFragmentManager(), "AddDialog");
    }

    @Override
    public void getMarkerName(String markerName) {
        selectionMode = true;
        lastNameMarker = markerName;
        boxInfo.setText("Marque un lugar en el mapa dejando precionado");
    }
}
