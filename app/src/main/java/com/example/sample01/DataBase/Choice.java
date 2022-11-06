package com.example.sample01.DataBase;

import android.provider.BaseColumns;

public class Choice {

    public static class ChoiceEntry implements BaseColumns{
        public static final String NAME = "name";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String LIKE_NUM = "like_num";
        public static final String TABLE_NAME = "choice";

        public static final String CREATE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                        +_ID + "INTEGER PRIMARY KEY,"
                        +




    }


}
