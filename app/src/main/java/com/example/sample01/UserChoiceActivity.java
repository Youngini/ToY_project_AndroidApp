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


    //???????????? ???????????? ?????? ????????????
    double x = 37.5642135;
    double y = 127.0016958;

    // ?????? ????????? ?????? ??? ??????
    private static final String LOG_TAG = "UserChoiceActivity";
    private MapView mapView;
    private ViewGroup mapViewContainer;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION};

    // ?????? ?????? ??????
    MapPOIItem s_markers = new MapPOIItem();
    MapPOIItem n_markers = new MapPOIItem();

    // ?????? ?????? ?????? ??????
    MapPOIItem selectPoint = new MapPOIItem();
    double selectX; // ?????? ?????? ??????
    double selectY; // ?????? ?????? ??????

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savdeInstanceState) {
        super.onCreate(savdeInstanceState);
        setContentView(R.layout.activity_userchoice);

        Content = (LinearLayoutCompat)findViewById(R.id.content);
        BigSpinner = (Spinner)findViewById(R.id.big_spinner);
        MiddleSpinner = (Spinner)findViewById(R.id.middle_spinner);
        SmallSpinner = (Spinner)findViewById(R.id.small_spinner);

        BigSpinner.setPrompt("??????");
        MiddleSpinner.setPrompt("?????????");
        SmallSpinner.setPrompt("?????????");

        Content.bringToFront();
        BigSpinner.bringToFront();
        MiddleSpinner.bringToFront();
        SmallSpinner.bringToFront();

        InitializeBig();

        // ?????? ?????????
        mapView = new MapView(this);
        mapViewContainer = (ViewGroup) findViewById(R.id.map_view2);
        mapViewContainer.addView(mapView);
        MapPoint wantPoint = MapPoint.mapPointWithGeoCoord(x,y);
        mapView.setMapCenterPoint(wantPoint,true);

        // ?????? ?????? ??? ??????
        mapView.setMapViewEventListener(this);
        mapView.setPOIItemEventListener(this);


        //getNoSmokingData();
        getSmokingData();



    }


    public void markNoSmoking(){
        // ?????? ?????? ??????
        int n = nosmokingArea.size();
        for(int i=0;i<100;i++){
            MapPoint n_mapPoints = MapPoint.mapPointWithGeoCoord((double)nosmokingX.get(i), (double)nosmokingY.get(i));
            int radius = (int) Math.sqrt((double)nosmokingArea.get(i)); // ?????????
            if(radius <= 100){
                radius = 100;
            }
            MapCircle n_circle = new MapCircle(n_mapPoints,radius,Color.argb(0, 0, 0, 0),Color.argb(90, 255, 0, 0));
            mapView.addCircle(n_circle);

        }
    }

    public void markSmoking(){
        //?????? ?????? ??????
        int s = smokingX.size();
        for(int i=0;i<s;i++){
            MapPoint s_mapPoints = MapPoint.mapPointWithGeoCoord((double)smokingX.get(i), (double)smokingY.get(i));
            s_markers.setItemName("Default Marker");
            s_markers.setTag(0);
            s_markers.setMapPoint(s_mapPoints);
            s_markers.setMarkerType(MapPOIItem.MarkerType.BluePin); // ???????????? ???????????? BluePin ?????? ??????.
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
        cursor = db.rawQuery("SELECT * FROM noSmoking_area ORDER BY (("+x+"-??????)*("+x+"-??????)) + (("+y+"-??????)*("+y+"-??????)) ASC",null);

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
        String cmp = "?????? ?????????";

        bigList.add("??????");
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
        String cmp = "????????? ?????????";
        Cursor middleCursor = db.rawQuery("SELECT * FROM Korea_GPS WHERE ?????? =" + "'"+ big +"'",null);

        middleList.add("?????????");

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
        Cursor smallCursor = db.rawQuery("SELECT * FROM Korea_GPS WHERE ????????? =" + "'"+ middle +"' AND ?????? =" + "'"+ big +"'",null);

        smallList.add("?????????");

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

    // ?????? ?????? ??????
    public void previousPage(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        onDestroy(); // ??? ??? 2??? ????????????
        //?????? ???????????? ???????????? ??????
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

    // ????????? ?????? ??? ?????? ??? ??????
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        mapView.removePOIItem(selectPoint);
        selectPoint.setItemName("?????? ?????? ?????? ?????? ??????");
        selectPoint.setTag(0);
        selectPoint.setMapPoint(mapPoint);
        selectPoint.setMarkerType(MapPOIItem.MarkerType.RedPin); // ???????????? ???????????? BluePin ?????? ??????.
        selectPoint.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.

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

    // ?????? ???????????? ?????????
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
        // ?????? ??????
        newsmokingArea.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ChoiceDataBaseHelper dbHelper = new ChoiceDataBaseHelper(context);
                String choiceReason = reason.getText().toString();
                ChoiceData choiceData = new ChoiceData(address,selectX,selectY,choiceReason);
                dbHelper.addChoice(choiceData);
                reason.setText("");
                Toast.makeText(UserChoiceActivity.this,"?????????????????????.", Toast.LENGTH_LONG).show();
                dialogInterface.dismiss();
            }
        });

        // ?????? ??????
        newsmokingArea.setNegativeButton("??????", new DialogInterface.OnClickListener() {
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
        String nowAddress ="?????? ????????? ?????? ??? ??? ????????????.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //????????? ??????????????? ????????? ?????? ????????? ?????? ?????? ?????????
                //???????????? ?????? ??????????????? ????????? ????????????????????? ??????????????? ???????????? ?????? ???????????? ??????
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // ?????? ????????????
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;

                }
            }

        } catch (IOException e) {
            Toast.makeText(mContext, "????????? ?????? ??? ??? ????????????.", Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
        return nowAddress;
    }

}

