package com.example.nursingjournal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.jar.Attributes;

public class MyDBHelper extends SQLiteOpenHelper {

    public MyDBHelper(Context context) {
        super(context, "diaryDB", null, 1);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

       // db.execSQL("CREATE TABLE childrenTBL (id INTEGER,name TEXT);");

        db.execSQL("CREATE TABLE children (id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,fetusName TEXT,birthdate TEXT NOT NULL," +
                "birthtime TEXT,birthWeight REAL,currentWeight REAL,birthHeight REAL,currentHeight REAL,bloodType TEXT NOT NULL," +
                "bornHospital TEXT, profilePicture TEXT);");
        db.execSQL("CREATE TABLE diaries (date text PRIMARY KEY,title text NOT NULL,content text,pottyCount integer,pottyStatus text,significant text,health text,childid integer, constraint fk_childid FOREIGN KEY (childid) REFERENCES children (id))");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            
    }
}
