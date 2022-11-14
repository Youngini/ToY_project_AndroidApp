package com.example.sample01;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.database.sqlite.SQLiteOpenHelper;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.example.sample01.DataBase.ChoiceDataBaseHelper;

import android.view.ViewGroup;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import com.example.sample01.DataBase.SmokeDataBaseHelper;
import com.example.sample01.DataBase.NoSmokeDataBaseHelper;
import com.example.sample01.DataBase.ChoiceDataBaseHelper;
import com.google.android.material.snackbar.Snackbar;

import java.util.Map;


public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapView.MapViewEventListener{

    private static final String LOG_TAG = "MAinActivity";
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    int nCurrentPermission = 0;
    static final int PERMISSIONS_REQUEST = 0x0000001;
    float val1 = 0;
    float val2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
         // 지도 띄우기
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setMapViewEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading); // 현위치 트래킹 모드
        mapView.setCurrentLocationRadius(100); // 현위치 마커 중심으로 그릴 원의 반경 지정
        mapView.setCurrentLocationRadiusStrokeColor(Color.RED); // 현위치 마커 중심으로 그릴 원의 선 색상 지정
        //mapView.setCurrentLocationRadiusFillColor(Color.RED); // 현위치 마커 중심으로 그릴 원의 채우기 색상 지정

        if(!checkLocationServicesStatus()){
            showDialogForLocationServiceSetting();
        }
        else{
            checkRunTimePermission();
        }
//        onCheckPermission();

        getNoSmoke();
        getSmoke();
        getChoice();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mapViewContainer.removeAllViews();
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint currentLocation, float accuracyInMeters){
        MapPoint.GeoCoordinate mapPointGeo = currentLocation.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapVie OnCurrentLocationUpdate (%f, %f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v){
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView){
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView){
    }

    private void onFinishReverseGeoCoding(String result){

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

    void checkRunTimePermission(){
        // 런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크한다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION);

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED){
            // 2. 이미 퍼미션 가지고 있다면 위치값 못가져 옴
        }
        else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])){
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE);
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    // gps 활성화 위함
    private void showDialogForLocationServiceSetting(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"+"위치 설정을 수정하시겠습니까?");
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

        switch (requestCode){
            case GPS_ENABLE_REQUEST_CODE:
                if(checkLocationServicesStatus()){
                    if(checkLocationServicesStatus()){
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onMapViewInitialized(MapView mapView){

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint){

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i){

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint){

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint){

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint){

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint){

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint){

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint){

    }

// 위치 기반 허락 과거 ver
//    public void onCheckPermission(){
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
//
//            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
//                Toast.makeText(this, "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
//                        PERMISSIONS_REQUEST);
//            }
//
//            else{
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
//                        PERMISSIONS_REQUEST);
//                }
//        }
//
//
//
//    }


    public void getChoice(){
        ChoiceDataBaseHelper dbHelper = new ChoiceDataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        dbHelper.close();
    }




    public void getNoSmoke() {

        NoSmokeDataBaseHelper dbHelper = new NoSmokeDataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        Cursor cursor1 = db.rawQuery("SELECT * FROM noSmoking_area where COUNT_ID <=10000 ",null);
        Cursor cursor2 = db.rawQuery("SELECT * FROM noSmoking_area where COUNT_ID BETWEEN 10001 and 20000 ",null);
        Cursor cursor3 = db.rawQuery("SELECT * FROM noSmoking_area where COUNT_ID BETWEEN 20001 and 30000 ",null);
        Cursor cursor4 = db.rawQuery("SELECT * FROM noSmoking_area where COUNT_ID BETWEEN 30001 and 40000 ",null);
        Cursor cursor5 = db.rawQuery("SELECT * FROM noSmoking_area where COUNT_ID >=40001 ",null);


        if (cursor1.moveToNext())
            val1 = cursor1.getFloat(5);
        if (cursor2.moveToNext())
            val1 = cursor2.getFloat(5);
        if (cursor3.moveToNext())
            val1 = cursor3.getFloat(5);
        if (cursor4.moveToNext())
            val1 = cursor4.getFloat(5);
        if (cursor5.moveToNext())
            val1 = cursor5.getFloat(5);

        cursor1.close();
        cursor2.close();
        cursor3.close();
        cursor4.close();
        cursor5.close();

        dbHelper.close();
    }



    public void getSmoke(){
        SmokeDataBaseHelper dbHelper = new SmokeDataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM smoking_area",null);

        cursor.close();
        dbHelper.close();
    }


// 위치 기반 허락 과거 ver
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//
//            case PERMISSIONS_REQUEST:
//
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    Toast.makeText(this, "앱 실행을 위한 권한이 설정 되엇습니다.", Toast.LENGTH_LONG).show();
//                }
//                else {
//                    Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다.", Toast.LENGTH_LONG).show();
//                }
//
//                break;
//        }
//    }
}



/*
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.sample01.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}*/
