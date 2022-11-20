package com.example.sample01;

import static com.example.sample01.R.id.edittextAddress;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import com.example.sample01.DataBase.Choice;
import com.example.sample01.DataBase.ChoiceData;
import com.example.sample01.DataBase.ChoiceDataBaseHelper;
import com.example.sample01.DataBase.KoreaGPSDataBaseHelper;
import com.example.sample01.DataBase.NoSmokeDataBaseHelper;
import com.example.sample01.DataBase.NoSmokingData;
import com.example.sample01.DataBase.SmokeDataBaseHelper;
import com.example.sample01.DataBase.SmokingData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserChoiceActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener, View.OnClickListener, MapView.CurrentLocationEventListener {
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

    // 흡연 구역 설치 장소
    MapPOIItem selectPoint = new MapPOIItem();
    double selectX; // 마커 찍은 위도
    double selectY; // 마커 찍은 경도

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

        // 지도 클릭 시 마커
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);


        //getNoSmokingData();
        getSmokingData();



    }


    public void markNoSmoking(){
        // 금연 구역 표시
        int n = nosmokingArea.size();
        for(int i=0;i<100;i++){
            MapPoint n_mapPoints = MapPoint.mapPointWithGeoCoord((double)nosmokingX.get(i), (double)nosmokingY.get(i));
            int radius = (int) Math.sqrt((double)nosmokingArea.get(i)); // 반지름
            if(radius <= 100){
                radius = 100;
            }
            MapCircle n_circle = new MapCircle(n_mapPoints,radius,Color.argb(0, 0, 0, 0),Color.argb(90, 255, 0, 0));
            mapView.addCircle(n_circle);

        }
    }

    public void markSmoking(){
        //흡연 구역 표시
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

        bigList.add("시도");
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

                if(i!=0){
                    MiddleSpinner.setAdapter(null);
                    SmallSpinner.setAdapter(null);
                    Toast.makeText(UserChoiceActivity.this, bigList.get(i).toString() , Toast.LENGTH_LONG).show();
                    InitializeMiddle(bigList.get(i).toString());
                }

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

        middleList.add("시군구");

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

                if(i!=0){
                    SmallSpinner.setAdapter(null);
                    Toast.makeText(UserChoiceActivity.this, middleList.get(i).toString() , Toast.LENGTH_LONG).show();
                    InitializeSmall(middleList.get(i).toString(),big);
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    public void InitializeSmall(String middle,String big){
        smallList = new ArrayList<>();
        x_coordinate = new ArrayList<>();
        y_coordinate = new ArrayList<>();

        KoreaGPSDataBaseHelper dbHelper = new KoreaGPSDataBaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String small;
        double x_lat;
        double y_long;
        Cursor smallCursor = db.rawQuery("SELECT * FROM Korea_GPS WHERE 시군구 =" + "'"+ middle +"' AND 시도 =" + "'"+ big +"'",null);

        smallList.add("읍면동");

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
                if(i!=0){
                    x = (double) x_coordinate.get(i);
                    y = (double) y_coordinate.get(i);
                    MapPoint wantPoint = MapPoint.mapPointWithGeoCoord(x,y);
                    mapView.setMapCenterPoint(wantPoint,true);
                    mapView.setZoomLevel(3,true);

                    getNoSmokingData();
                    markNoSmoking();
                    markSmoking();
                }

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

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

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

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    // 지도에 한번 탭 하면 핑 찍기
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        mapView.removePOIItem(selectPoint);
        selectPoint.setItemName("흡연 구역 설치 희망 구역");
        selectPoint.setTag(0);
        selectPoint.setMapPoint(mapPoint);
        selectPoint.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
        selectPoint.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        selectX = mapPoint.getMapPointGeoCoord().latitude;
        selectY = mapPoint.getMapPointGeoCoord().longitude;

        mapView.addPOIItem(selectPoint);
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

    // 마커 선택하면 띄우기
    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {
        Context context = getBaseContext();
        String address = getAddress(context,selectX,selectY);
        AlertDialog.Builder newsmokingArea = new AlertDialog.Builder(UserChoiceActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.application,null);
        newsmokingArea.setView(view);

        TextView Address = (TextView) view.findViewById(R.id.edittextAddress);
        EditText reason = (EditText) view.findViewById(R.id.edittextreason);

        Address.setText(address);
        // 신청 버튼
        newsmokingArea.setPositiveButton("신청", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ChoiceDataBaseHelper dbHelper = new ChoiceDataBaseHelper(context);
                String choiceReason = reason.getText().toString();
                ChoiceData choiceData = new ChoiceData(address,selectX,selectY,choiceReason);
                dbHelper.addChoice(choiceData);
                reason.setText("");
                Toast.makeText(UserChoiceActivity.this,"접수되었습니다.", Toast.LENGTH_LONG).show();
                dialogInterface.dismiss();
            }
        });

        // 취소 버튼
        newsmokingArea.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        newsmokingArea.show();
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }

    public static String getAddress(Context mContext,double lat, double lng) {
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;

                }
            }

        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
        return nowAddress;
    }

}

