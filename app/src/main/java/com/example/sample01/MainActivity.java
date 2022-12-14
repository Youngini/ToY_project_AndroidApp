package com.example.sample01;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
//import android.location.Location;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sample01.DataBase.ChoiceDataBaseHelper;

import android.view.ViewGroup;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import com.example.sample01.DataBase.NoSmokingData;
import com.example.sample01.DataBase.SmokingData;
import com.example.sample01.DataBase.SmokeDataBaseHelper;
import com.example.sample01.DataBase.NoSmokeDataBaseHelper;
import com.example.sample01.DataBase.ChoiceDataBaseHelper;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import com.google.android.gms.location.FusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener {

    private static final String LOG_TAG = "MAinActivity";
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    int nCurrentPermission = 0;
    static final int PERMISSIONS_REQUEST = 0x0000001;


    //?????? ????????? ???????????? ?????? ????????? ??????(??????????????? ?????? ?????????!)
    double x = 35.88807390081719;
    double y = 128.61130207129662;

    public ListViewAdapter listViewAdapter;
    public ListView listview;

    ArrayList<SmokingData> smokingDataList;
    ArrayList smokingMarkerX;
    ArrayList smokingMarkerY;
    ArrayList somkingMarkerName;

    // ?????? ?????? ?????? ?????? ??????
    PlacesClient placesClient;
    MapPOIItem marker = new MapPOIItem();
    MapPOIItem markers = new MapPOIItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //????????????????????? ?????? ??????????????? ????????? ??????????????? ??????????????? ?????????
        findViewById(R.id.handle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingdrawer);
                slidingDrawer.animateClose();
            }
        });

        //???????????? ????????? ????????????
        this.InitializeData();
        listview = (ListView) findViewById(R.id.smokingListview);
        listViewAdapter = new ListViewAdapter(this, smokingDataList);
        listview.setAdapter(listViewAdapter);



        // ?????? ?????????
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        MapPoint current = MapPoint.mapPointWithGeoCoord(x,y);
        mapView.setMapCenterPoint(current,true);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving); // ????????? ????????? ??????

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }


        // ?????? ?????? ?????? ?????? ??????
        String Apikey = "AIzaSyBa8koUZ6pzntQGN0AaL884n-llNZZym8U";
        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),Apikey);
        }
        placesClient = Places.createClient(this);

        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.LAT_LNG,Place.Field.NAME));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
            }
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                final LatLng latLng = place.getLatLng();

                Log.i(TAG, "onPlaceSelected : "+latLng.latitude+"\n"+latLng.longitude);
                x = latLng.latitude;
                y = latLng.longitude;
                updateList();

                MapPoint search = MapPoint.mapPointWithGeoCoord(latLng.latitude,latLng.longitude);
                mapView.setMapCenterPoint(search,true);
                mapView.setZoomLevel(2,true);
                mapView.removePOIItem(marker);
                marker.setItemName(place.getName());
                marker.setTag(0);
                marker.setMapPoint(search);
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // ???????????? ???????????? BluePin ?????? ??????.
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.BluePin); // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.

                mapView.addPOIItem(marker);

                for(int i=0;i<10;i++){
                    MapPoint mapPoints = MapPoint.mapPointWithGeoCoord((double)smokingMarkerX.get(i), (double)smokingMarkerY.get(i));

                    markers.setMapPoint(mapPoints);
                    markers.setMarkerType(MapPOIItem.MarkerType.RedPin);
                    mapView.addPOIItem(markers);
                }

            }
        });

        // ?????? ?????? ??????
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // gps ?????? ?????? ??????
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( MainActivity.this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0 );
        }

        else{
            // ???????????? ???????????? ????????????
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                y = location.getLongitude();
                x = location.getLatitude();
            }

            // ??????????????? ????????? ??????, ???????????? ???????????????.
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    gpsLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000,
                    1,
                    gpsLocationListener);
        }






        //???????????? ??????
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int i, long id) {
                Toast.makeText(getApplicationContext(),
                        listViewAdapter.getItem(i).getName(),
                        Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "("+x+", "+y+")",Toast.LENGTH_LONG).show();

                String targetX = Double.toString(listViewAdapter.getItem(i).getX());
                String targetY = Double.toString(listViewAdapter.getItem(i).getY());

                String url = "kakaomap://route?sp=x,y&ep=" + targetX + ","+targetY+"&by=FOOT";
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse(url));
                startActivity(intent);


            }
        });

        //?????? ????????? ?????????
        int n = smokingMarkerX.size();
        for(int i=0;i<n;i++){
            MapPoint mapPoints = MapPoint.mapPointWithGeoCoord((double)smokingMarkerX.get(i), (double)smokingMarkerY.get(i));
            markers.setItemName((String)somkingMarkerName.get(i)); // ?????? ?????? ?????????
            markers.setTag(0);
            markers.setMapPoint(mapPoints);
            markers.setMarkerType(MapPOIItem.MarkerType.RedPin); // ???????????? ???????????? BluePin ?????? ??????.
            mapView.addPOIItem(markers);
        }

        // gps ?????? ?????? ?????????
        final ImageButton currentlocation = (ImageButton) findViewById(R.id.currentlocation);
        currentlocation.bringToFront();
        // ?????? ?????? ???
        currentlocation.setOnClickListener(new View.OnClickListener() {
            long delay = 0;
            @Override
            public void onClick(View view) {
                // ??? ??? ???????????? ???

                if (System.currentTimeMillis() > delay) {
                    delay = System.currentTimeMillis() + 200;
                    // ???????????? ????????????
                    MapPoint current = MapPoint.mapPointWithGeoCoord(x,y);
                    mapView.setMapCenterPoint(current,true);
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving); // ????????? ??????
                    mapView.setZoomLevel(2,true);
                    updateList();
                    return;
                }

                // ??? ??? ???????????? ???
                if(System.currentTimeMillis() <= delay){
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeadingWithoutMapMoving); // ????????? ????????? ??????
                    mapView.setZoomLevel(2,true);
                    delay = 0;
                }

            }
        });


        // ?????????
        final LinearLayout search_content = (LinearLayout) findViewById(R.id.search_content);
        search_content.bringToFront();

    }

    public void updateList(){
        InitializeData();
        listViewAdapter = new ListViewAdapter(this ,smokingDataList);
        listview.setAdapter(listViewAdapter);
    }


    //???????????????(????????? ??????)??? ???????????? ??????
    public void nextPage(View view){
        Intent intent = new Intent(this, UserChoiceActivity.class);
        startActivity(intent);
        onDestroy(); // ??? ??? 2??? ????????????
        //onStop();
        //?????? ???????????? ???????????? ??????
        //finish();
    }



    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // ?????? ???????????? ??????????????? ????????? ??? ??????????????? onLocationChanged()????????? ?????? ??????????????? ????????? ????????? ?????? ???????????????.
            y = location.getLongitude(); // ??????
            x = location.getLatitude(); // ??????


        } public void onStatusChanged(String provider, int status, Bundle extras) {

        } public void onProviderEnabled(String provider) {

        } public void onProviderDisabled(String provider) {

        }
    };

    public void InitializeData() {
        double Distance = 0.0;
        smokingDataList = new ArrayList<SmokingData>();
        smokingMarkerX = new ArrayList<>();
        smokingMarkerY = new ArrayList<>();
        somkingMarkerName = new ArrayList<>();
        SmokeDataBaseHelper dbHelper = new SmokeDataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor getLocation = null;
        getLocation = db.rawQuery("SELECT * FROM smoking_area ORDER BY (("+x+"-??????)*("+x+"-??????)) + (("+y+"-??????)*("+y+"-??????)) ASC",null);
        ListViewAdapter adapter = new ListViewAdapter(this, smokingDataList);


        while (getLocation.moveToNext()) {
            somkingMarkerName.add(getLocation.getString(1));
            smokingMarkerX.add(getLocation.getDouble(3));
            smokingMarkerY.add(getLocation.getDouble(4));
            Distance = distance(x,y,getLocation.getDouble(3),getLocation.getDouble(4));
            adapter.addItemToList(getLocation.getString(1), getLocation.getString(2),getLocation.getDouble(3),getLocation.getDouble(4),Distance);
        }

        getLocation.close();
        dbHelper.close();

    }
    //????????? ???????????? ?????????
    private static double distance(double lat1, double lon1, double lat2, double lon2){
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))* Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))*Math.cos(deg2rad(lat2))*Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60*1.1515*1609.344;

        return dist;
    }

    private static double deg2rad(double deg){
        return (deg * Math.PI/180.0);
    }
    private static double rad2deg(double rad){
        return (rad * 180 / Math.PI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapViewContainer.removeAllViews();
    }


    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView OnCurrentLocationUpdate (%f, %f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));

    }





    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
    }

    private void onFinishReverseGeoCoding(String result) {

    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions, @NonNull int[] grandResults) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grandResults);
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            boolean check_result = true;

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                Log.d("@@@", "start");
                // ?????? ?????? ????????? ??? ??????
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ?????? ?????? ???????????? ???????????? ??????????????????.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "???????????? ?????????????????????. ??????(??? ??????)?????? ???????????? ???????????? ?????????.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission() {
        // ????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. ?????? ????????? ????????? ????????? ????????? ?????? ???

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(MainActivity.this, "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    // gps ????????? ??????
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n" + "?????? ????????? ?????????????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }
}
