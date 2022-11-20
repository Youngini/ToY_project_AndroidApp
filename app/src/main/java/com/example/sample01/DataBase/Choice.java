package com.example.sample01.DataBase;

import android.provider.BaseColumns;

public class Choice {

    public static class ChoiceEntry implements BaseColumns{
        public static final String ADDLESS = "address";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String REASON = "reason";
        public static final String TABLE_NAME = "choice";

        public static final String CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        +_ID + " INTEGER PRIMARY KEY,"
                        +ADDLESS+ " TEXT,"
                        +LATITUDE+ " REAL,"
                        +LONGITUDE+" REAL,"
                        +REASON+" INTEGER);";

        public static final String DELETE_TABLE =
                "DROP TABLE IF EXISTS "+TABLE_NAME;
    }
}
