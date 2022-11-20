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


    //밑에 두개가 현재위치 좌표 저장할 변수(기본값으로 경대 해뒀음!)
    double x = 35.88807390081719;
    double y = 128.61130207129662;

    ArrayList<SmokingData> smokingDataList;
    ArrayList smokingMarkerX;
    ArrayList smokingMarkerY;
    ArrayList somkingMarkerName;

    // 구글 장소 검색 자동 완성
    PlacesClient placesClient;
    MapPOIItem marker = new MapPOIItem();
    MapPOIItem markers = new MapPOIItem();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 지도 띄우기
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        MapPoint current = MapPoint.mapPointWithGeoCoord(x,y);
        mapView.setMapCenterPoint(current,true);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving); // 현위치 트래킹 모드

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }


        // 구글 장소 검색 자동 완성
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

                MapPoint search = MapPoint.mapPointWithGeoCoord(latLng.latitude,latLng.longitude);
                mapView.setMapCenterPoint(search,true);
                mapView.setZoomLevel(2,true);
                mapView.removePOIItem(marker);
                marker.setItemName(place.getName());
                marker.setTag(0);
                marker.setMapPoint(search);
                marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

                mapView.addPOIItem(marker);

                for(int i=0;i<10;i++){
                    MapPoint mapPoints = MapPoint.mapPointWithGeoCoord((double)smokingMarkerX.get(i), (double)smokingMarkerY.get(i));

                    markers.setMapPoint(mapPoints);
                    markers.setMarkerType(MapPOIItem.MarkerType.RedPin);
                    //mapView.addPOIItem(markers);
                    //markerArr.add(markers);
                    mapView.addPOIItem(markers);
                }

            }
        });

        // 현재 위치 찾기
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // gps 기능 허가 확인
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( MainActivity.this, new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION}, 0 );
        }

        else{
            // 가장최근 위치정보 가져오기
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null) {
                y = location.getLongitude();
                x = location.getLatitude();
            }

            // 위치정보를 원하는 시간, 거리마다 갱신해준다.
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    gpsLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000,
                    1,
                    gpsLocationListener);
        }

        //슬라이딩드로어 부분 밑줄저거는 호환성 문제라는데 신경안써도 된디유
        findViewById(R.id.handle).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                SlidingDrawer slidingDrawer = (SlidingDrawer) findViewById(R.id.slidingdrawer);
                slidingDrawer.animateClose();
            }
        });


        //리스트뷰
        this.InitializeData();

        ListView listview = (ListView) findViewById(R.id.smokingListview);
        final ListViewAdapter listViewAdapter = new ListViewAdapter(this, smokingDataList);

        listview.setAdapter(listViewAdapter);

        //경로찾기 연결
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

        //마커 여러개 띄우기
        ArrayList<MapPOIItem> markerArr = new ArrayList<MapPOIItem>();
        int n = smokingMarkerX.size();
        for(int i=0;i<n;i++){
            MapPoint mapPoints = MapPoint.mapPointWithGeoCoord((double)smokingMarkerX.get(i), (double)smokingMarkerY.get(i));
            markers.setItemName("Default Marker");
            markers.setTag(0);
            markers.setMapPoint(mapPoints);
            markers.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
            mapView.addPOIItem(markers);
            //mapView.addPOIItem(markers);
            //markerArr.add(markers);
        }
        //mapView.addPOIItems(markerArr.toArray(new MapPOIItem[markerArr.size()]));
        //mapView.addPOIItems(markerArr);

        // gps 버튼 클릭 이벤트
        final ImageButton currentlocation = (ImageButton) findViewById(R.id.currentlocation);
        currentlocation.bringToFront();
        // 버튼 클릭 시
        currentlocation.setOnClickListener(new View.OnClickListener() {

            long delay = 0;

            @Override
            public void onClick(View view) {
                // 한 번 클릭했을 때
                if (System.currentTimeMillis() > delay) {

                    delay = System.currentTimeMillis() + 200;
                    // 현재위치 받아오기
                    MapPoint current = MapPoint.mapPointWithGeoCoord(x,y);
                    mapView.setMapCenterPoint(current,true);
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving); // 현위치 표시
                    mapView.setZoomLevel(2,true);
                    return;
                }

                // 두 번 클릭했을 때
                if(System.currentTimeMillis() <= delay){
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeadingWithoutMapMoving); // 현위치 트래킹 모드
                    mapView.setZoomLevel(2,true);
                    delay = 0;
                }


            }
        });

        // 검색창
        final LinearLayout search_content = (LinearLayout) findViewById(R.id.search_content);
        search_content.bringToFront();

    }


    //다음페이지(사용자 신청)로 넘어가는 함수
    public void nextPage(View view){
        Intent intent = new Intent(this, UserChoiceActivity.class);
        startActivity(intent);
        onDestroy(); // 맵 뷰 2개 못띄워서
        //밑에 깔려있는 액티비티 삭제
        //finish();
    }



    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // 위치 리스너는 위치정보를 전달할 때 호출되므로 onLocationChanged()메소드 안에 위지청보를 처리를 작업을 구현 해야합니다.
            y = location.getLongitude(); // 위도
            x = location.getLatitude(); // 경도

        } public void onStatusChanged(String provider, int status, Bundle extras) {

        } public void onProviderEnabled(String provider) {

        } public void onProviderDisabled(String provider) {

        }
    };

    public void InitializeData() {
        int cnt=0;

        double distance = 0.0;
        smokingDataList = new ArrayList<SmokingData>();
        smokingMarkerX = new ArrayList<>();
        smokingMarkerY = new ArrayList<>();
        somkingMarkerName = new ArrayList<>();
        SmokeDataBaseHelper dbHelper = new SmokeDataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor getLocation = db.rawQuery("SELECT * FROM smoking_area ORDER BY (("+x+"-위도)*("+x+"-위도)) + (("+y+"-경도)*("+y+"-경도)) ASC",null);

        ListViewAdapter adapter = new ListViewAdapter(this, smokingDataList);


        while (getLocation.moveToNext()) {
            adapter.addItemToList(getLocation.getString(1), getLocation.getString(2),getLocation.getDouble(3),getLocation.getDouble(4));
            somkingMarkerName.add(getLocation.getString(1));
            smokingMarkerX.add(getLocation.getDouble(3));
            smokingMarkerY.add(getLocation.getDouble(4));
            distance = Math.sqrt((x-getLocation.getDouble(3))*(x-getLocation.getDouble(3)) + (y-getLocation.getDouble(4))*(y-getLocation.getDouble(4)));

        }

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


    //현재위치랑 가까운 점 반환
    private String nearLocation(double latitude, double longitude){
        double SIN_LATITUDE = Math.sin(Math.toRadians(x));
        double COS_LATITUDE = Math.cos(Math.toRadians(x));
        double SIN_LONGITUDE = Math.sin(Math.toRadians(y));
        double COS_LONGITUDE = Math.cos(Math.toRadians(y));

        final double sinLat = Math.sin(Math.toRadians(latitude));
        final double cosLat = Math.cos(Math.toRadians(latitude));
        final double sinLng = Math.sin(Math.toRadians(longitude));
        final double cosLng = Math.cos(Math.toRadians(longitude));

        return "(" + cosLat + "*" + COS_LATITUDE
                + "*(" + COS_LONGITUDE + "*" + cosLng
                + "+" + SIN_LONGITUDE + "*" + sinLng
                + ")+" + sinLat + "*" + SIN_LATITUDE
                + ")";
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
                // 위치 값을 가져올 수 있음
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission() {
        // 런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크한다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션 가지고 있다면 위치값 가져 옴

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    // gps 활성화 위함
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" + "위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
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
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
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
