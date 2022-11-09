package com.example.sample01.DataBase;

import static com.example.sample01.DataBase.Choice.ChoiceEntry.CREATE_TABLE;
import static com.example.sample01.DataBase.Choice.ChoiceEntry.DELETE_TABLE;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import com.example.sample01.DataBase.Choice;

public class ChoiceDataBaseHelper extends SQLiteOpenHelper {

    private final static String TAG = "ChoiceDataBaseHelper";
    private static String DB_PATH = "";
    private static String DB_NAME = "choice.db";
    private SQLiteDatabase mDataBase;
    private Context mContext;

    public ChoiceDataBaseHelper(@Nullable Context context) {
        super(context, DB_NAME, null, 1);

        DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(DELETE_TABLE);
        onCreate(sqLiteDatabase);
    }
}
