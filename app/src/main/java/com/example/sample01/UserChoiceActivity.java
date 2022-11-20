package com.example.sample01;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.sample01.DataBase.KoreaGPSDataBaseHelper;
import com.example.sample01.DataBase.NoSmokeDataBaseHelper;
import com.example.sample01.DataBase.NoSmokingData;
import com.example.sample01.DataBase.SmokeDataBaseHelper;
import com.example.sample01.DataBase.SmokingData;

import java.util.ArrayList;

public class UserChoiceActivity extends AppCompatActivity {
    ArrayList bigList;
    ArrayList middleList;
    ArrayList smallList;
    ArrayList x_coordinate;
    ArrayList y_coordinate;

    ArrayList nosmokingX;
    ArrayList nosmokingY;
    ArrayList nosmokingArea;

    ArrayList smokingX;
    ArrayList smokingY;


    LinearLayoutCompat Content;
    public Spinner BigSpinner;
    public Spinner MiddleSpinner;
    public Spinner SmallSpinner;


    //행정구역 중심좌표 담을 전역변수
    double x = 37.5642135;
    double y = 127.0016958;

    // 지도 띄울떄 사용 할 변수
    private static final String LOG_TAG = "UserChoiceActivity";
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    // 흡연 구역 표시
    MapPOIItem s_markers = new MapPOIItem();
    MapPOIItem n_markers = new MapPOIItem();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savdeInstanceState) {
        super.onCreate(savdeInstanceState);
        setContentView(R.layout.activity_userchoice);

        Content = (LinearLayoutCompat)findViewById(R.id.content);
        BigSpinner = (Spinner)findViewById(R.id.big_spinner);
        MiddleSpinner = (Spinner)findViewById(R.id.middle_spinner);
        SmallSpinner = (Spinner)findViewById(R.id.small_spinner);

        BigSpinner.setPrompt("시도");
        MiddleSpinner.setPrompt("시군구");
        SmallSpinner.setPrompt("읍면동");

        Content.bringToFront();
        BigSpinner.bringToFront();
        MiddleSpinner.bringToFront();
        SmallSpinner.bringToFront();

        InitializeBig();

        // 지도 띄우기
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view2);
        mapViewContainer.addView(mapView);
        MapPoint wantPoint = MapPoint.mapPointWithGeoCoord(x,y);
        mapView.setMapCenterPoint(wantPoint,true);

        getNoSmokingData();
        getSmokingData();
//
//        double no_x = (double)nosmokingX.get(0); // 위도
//        double no_y = (double)nosmokingY.get(0); // 경도
//        int r = (int) Math.sqrt((double)nosmokingArea.get(0)); // 반지름
//        MapPoint circlePoint = MapPoint.mapPointWithGeoCoord(35.255323,128.9030254);
//        MapCircle circle2 = new MapCircle(
//                MapPoint.mapPointWithGeoCoord(35.255323, 128.9030254), // center
//                1000, // radius
//                Color.argb(128, 255, 0, 0), // strokeColor
//                Color.argb(128, 255, 255, 0) // fillColor
//        );
//        circle2.setTag(5678);
//        mapView.addCircle(circle2);
//        Log.i(LOG_TAG,"lagitude = "+no_x+" longitude = "+no_y+" area = "+r);


        // 금연 구역 표시
        int n = nosmokingArea.size();
        for(int i=0;i<1000;i++){
            MapPoint n_mapPoints = MapPoint.mapPointWithGeoCoord((double)nosmokingX.get(i), (double)nosmokingY.get(i));
//            s_markers.setItemName("Default Marker");
//            s_markers.setTag(0);
//            s_markers.setMapPoint(n_mapPoints);
//            s_markers.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
//            mapView.addPOIItem(s_markers);

            int radius = (int) Math.sqrt((double)nosmokingArea.get(i)); // 반지름
            if(radius <= 100){
                radius = 100;
            }
            MapCircle n_circle = new MapCircle(n_mapPoints,radius,Color.argb(128, 255, 0, 0),Color.argb(128, 0, 255, 0));
            mapView.addCircle(n_circle);

//            double no_x = (double)nosmokingX.get(i); // 위도
//            double no_y = (double)nosmokingY.get(i); // 경도
//            int radius = (int) Math.sqrt((double)nosmokingArea.get(i)); // 반지름
//
//            MapPoint circlePoint = MapPoint.mapPointWithGeoCoord(no_x,no_y);
//            MapCircle circle = new MapCircle(circlePoint,
//                    radius,
//                    Color.argb(128, 255, 0, 0),Color.argb(128, 255, 255, 0));
//            mapView.addCircle(circle);

//            Log.i(LOG_TAG,"lagitude = "+no_x+" longitude = "+no_y+" area = "+radius);
        }

//        //흡연 구역 표시
        int s = smokingX.size();
        for(int i=0;i<s;i++){
            MapPoint s_mapPoints = MapPoint.mapPointWithGeoCoord((double)smokingX.get(i), (double)smokingY.get(i));
            s_markers.setItemName("Default Marker");
            s_markers.setTag(0);
            s_markers.setMapPoint(s_mapPoints);
            s_markers.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
            mapView.addPOIItem(s_markers);
        }
    }

    public void getSmokingData(){
        smokingX = new ArrayList<>();
        smokingY = new ArrayList<>();
        double x;
        double y;

        SmokeDataBaseHelper dataBaseHelper = new SmokeDataBaseHelper(this);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM smoking_area ",null);

        while(cursor.moveToNext()) {
            x = cursor.getDouble(3);
            y = cursor.getDouble(4);
            smokingX.add(x);
            smokingY.add(y);
        }

    }

    public void getNoSmokingData(){
        nosmokingX = new ArrayList<>();
        nosmokingY = new ArrayList<>();
        nosmokingArea = new ArrayList<>();
        double xx;
        double yy;
        double area;

        NoSmokeDataBaseHelper dataBaseHelper = new NoSmokeDataBaseHelper(this);
        SQLiteDatabase db = dataBaseHelper.getReadableDatabase();
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT * FROM noSmoking_area ORDER BY (("+x+"-위도)*("+x+"-위도)) + (("+y+"-경도)*("+y+"-경도)) ASC",null);

        while(cursor.moveToNext()) {
            xx = cursor.getDouble(4);
            yy = cursor.getDouble(5);
            area = cursor.getDouble(2);
            nosmokingX.add(xx);
            nosmokingY.add(yy);
            nosmokingArea.add(area);
        }

        cursor.close();
        dataBaseHelper.close();
    }

    public void InitializeBig(){
        bigList = new ArrayList<>();
        KoreaGPSDataBaseHelper dbHelper = new KoreaGPSDataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String big;
        String cmp = "시도 데이터";
        Cursor bigCursor = db.rawQuery("SELECT * FROM Korea_GPS",null);

        while(bigCursor.moveToNext()){
            big = bigCursor.getString(0);
            if(!big.equals(cmp)){
                bigList.add(big);
                cmp = big;
            }
        }

        ArrayAdapter<String> adapter1 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, bigList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        BigSpinner.setAdapter(adapter1);

        BigSpinner.setSelection(0,false);
        BigSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(UserChoiceActivity.this, bigList.get(i).toString() , Toast.LENGTH_LONG).show();
                InitializeMiddle(bigList.get(i).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void InitializeMiddle(String big){
        middleList = new ArrayList<>();

        KoreaGPSDataBaseHelper dbHelper = new KoreaGPSDataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String middle;
        String cmp = "시군구 데이터";
        Cursor middleCursor = db.rawQuery("SELECT * FROM Korea_GPS WHERE 시도 =" + "'"+ big +"'",null);

        while(middleCursor.moveToNext()){
            middle = middleCursor.getString(1);
            if(!middle.equals(cmp)){
                middleList.add(middle);
                cmp = middle;
            }

        }

        ArrayAdapter<String> adapter2 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, middleList);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        MiddleSpinner.setAdapter(adapter2);


        MiddleSpinner.setSelection(0,false);
        MiddleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(UserChoiceActivity.this, middleList.get(i).toString() , Toast.LENGTH_LONG).show();
                InitializeSmall(middleList.get(i).toString());
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    public void InitializeSmall(String middle){
        smallList = new ArrayList<>();
        x_coordinate = new ArrayList<>();
        y_coordinate = new ArrayList<>();

        KoreaGPSDataBaseHelper dbHelper = new KoreaGPSDataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String small;
        double x_lat;
        double y_long;
        Cursor smallCursor = db.rawQuery("SELECT * FROM Korea_GPS WHERE 시군구 =" + "'"+ middle +"'",null);

        while(smallCursor.moveToNext()){
            small = smallCursor.getString(2);
            smallList.add(small);

            x_lat = smallCursor.getDouble(3);
            y_long = smallCursor.getDouble(4);
            x_coordinate.add(x_lat);
            y_coordinate.add(y_long);

            //Toast.makeText(UserChoiceActivity.this,"("+x+","+y+")", Toast.LENGTH_LONG).show();

        }

        ArrayAdapter<String> adapter3 = new ArrayAdapter(this, android.R.layout.simple_spinner_item, smallList);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        SmallSpinner.setAdapter(adapter3);

        SmallSpinner.setSelection(0,false);
        SmallSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(UserChoiceActivity.this, "( "+x_coordinate.get(i).toString()+", "+y_coordinate.get(i).toString()+")" , Toast.LENGTH_LONG).show();
                x = (double) x_coordinate.get(i);
                y = (double) y_coordinate.get(i);
                MapPoint wantPoint = MapPoint.mapPointWithGeoCoord(x,y);
                mapView.setMapCenterPoint(wantPoint,true);
                mapView.setZoomLevel(3,true);

                getNoSmokingData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    // 흡연 구역 검색
    public void previousPage(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        onDestroy(); // 맵 뷰 2개 못띄워서
        //밑에 깔려있는 액티비티 삭제
        //finish();
    }
}

