package com.example.sample01;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.sample01.DataBase.KoreaGPSDataBaseHelper;

import java.util.ArrayList;

public class UserChoiceActivity extends AppCompatActivity {
    ArrayList bigList;
    ArrayList middleList;
    ArrayList smallList;
    ArrayList x_coordinate;
    ArrayList y_coordinate;

    LinearLayoutCompat Content;
    Spinner BigSpinner;
    Spinner MiddleSpinner;
    Spinner SmallSpinner;

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

        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view2);
        mapViewContainer.addView(mapView);
        MapPoint wantPoint = MapPoint.mapPointWithGeoCoord(x,y);
        mapView.setMapCenterPoint(wantPoint,true);

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
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

}

