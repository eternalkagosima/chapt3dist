package jp.co.etlab.chapt3dist;

import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Location location;
    private LocationRequest locationRequest;
    TextView textView;
    private GoogleApiClient mLocationClient = null;
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(60000)
            .setFastestInterval(10000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        textView = (TextView)findViewById(R.id.textView1);
        textView.setText("ここにデータが表示されます");
        mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        if(mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if(mLocationClient != null){
            mLocationClient.connect();
        }

        //iconタップのイベント登録
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                double dist;
                int distM;
                String name = marker.getTitle().toString();
                LatLng tapP=null;
                tapP = marker.getPosition();
                LatLng curP = new LatLng(location.getLatitude(), location.getLongitude());
                dist = getDistance(tapP,curP);
                distM = (int)dist;

                //取得したタイトルをトーストで表示
                Toast.makeText(MapsActivity.this, name+"\nここから直線で"+distM+"m", Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    public double getDistance(LatLng LatLng1, LatLng LatLng2) {
        double distance = 0;
        Location locationA = new Location("A");
        locationA.setLatitude(LatLng1.latitude);
        locationA.setLongitude(LatLng1.longitude);
        Location locationB = new Location("B");
        locationB.setLatitude(LatLng2.latitude);
        locationB.setLongitude(LatLng2.longitude);
        distance = locationA.distanceTo(locationB);
        return distance;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "西郷銅像");
        menu.add(0, 1, 1, "大久保銅像");
        menu.add(0, 2, 2, "ザビエル胸像");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemID = item.getItemId();
        CameraPosition.Builder builder = new CameraPosition.Builder();
        builder.zoom(16.0f);
        builder.bearing(0);

        switch (itemID) {
            case 0:
                LatLng b = new LatLng(31.595182, 130.553596);
                builder.target(b);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                mMap.addMarker(new MarkerOptions().position(b).title("西郷銅像"));
                break;
            case 1:
                LatLng z = new LatLng(31.586524, 130.546083);
                builder.target(z);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                mMap.addMarker(new MarkerOptions().position(z).title("大久保銅像"));
                break;
            case 2:
                LatLng q = new LatLng(31.591314, 130.551123);
                builder.target(q);
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                mMap.addMarker(new MarkerOptions().position(q).title("ザビエル胸像"));
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location loc) {
        if(location ==null) {
            CameraPosition cameraPos = new CameraPosition.Builder()
                    .target(new LatLng(loc.getLatitude(),loc.getLongitude())).zoom(17.0f)
                    .bearing(0).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        }
        location = loc;
        dispLatLog(loc,textView);
    }
    public void dispLatLog(Location loc,TextView tx) {
        String textLog="";
        textLog += "----------\n";
        textLog += "Latitude="+ String.valueOf(loc.getLatitude())+"\n";
        textLog += "Longitude="+ String.valueOf(loc.getLongitude())+"\n";
        textLog += "Accuracy="+ String.valueOf(loc.getAccuracy())+"\n";
        textLog += "Altitude="+ String.valueOf(loc.getAltitude())+"\n";
        textLog += "Time="+ String.valueOf(loc.getTime())+"\n";
        textLog += "Speed="+ String.valueOf(loc.getSpeed())+"\n";
        textLog += "Bearing="+ String.valueOf(loc.getBearing())+"\n";
        tx.setText(textLog);
    }

    @Override
    public void onConnected(Bundle bundle) {
        fusedLocationProviderApi.requestLocationUpdates(mLocationClient, REQUEST, this);
        Location currentLocation = fusedLocationProviderApi.getLastLocation(mLocationClient);
        if (currentLocation != null && (System.currentTimeMillis()-currentLocation.getTime()) > 20000) {
            location = currentLocation;
            dispLatLog(location, textView);
            CameraPosition cameraPos = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(),location.getLongitude())).zoom(17.0f)
                    .bearing(0).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        } else {
            fusedLocationProviderApi.requestLocationUpdates(mLocationClient, REQUEST, this);
            // Schedule a Thread to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                @Override
                public void run() {
                    fusedLocationProviderApi.removeLocationUpdates(mLocationClient, MapsActivity.this);
                }
            }, 5000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
