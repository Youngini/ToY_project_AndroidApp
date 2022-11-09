package com.example.sample01;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteOpenHelper;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.os.Bundle;
import android.util.Log;

import com.example.sample01.DataBase.ChoiceDataBaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.view.ViewGroup;
import net.daum.mf.map.api.MapView;

import com.example.sample01.DataBase.SmokeDataBaseHelper;
import com.example.sample01.DataBase.NoSmokeDataBaseHelper;
import com.example.sample01.DataBase.ChoiceDataBaseHelper;
import com.google.android.material.snackbar.Snackbar;



public class MainActivity extends AppCompatActivity {

    int nCurrentPermission = 0;
    static final int PERMISSIONS_REQUEST = 0x0000001;
    float val1 = 0;
    float val2 = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);

        MapView mapView = new MapView(this);

        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        onCheckPermission();

        getNoSmoke();
        getSmoke();
        getChoice();
    }

    public void onCheckPermission(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "앱 실행을 위해서는 권한을 설정해야 합니다", Toast.LENGTH_LONG).show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST);
            }

            else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSIONS_REQUEST);
                }
        }



    }


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



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case PERMISSIONS_REQUEST:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "앱 실행을 위한 권한이 설정 되엇습니다.", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "앱 실행을 위한 권한이 취소 되었습니다.", Toast.LENGTH_LONG).show();
                }

                break;
        }
    }
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
