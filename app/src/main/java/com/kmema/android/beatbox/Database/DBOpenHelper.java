package com.kmema.android.beatbox.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kmema on 9/21/2017.
 */

public class DBOpenHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "BeatBoxDatabase.db";
    private static final int DATABASE_VERSION = 1;


    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TABLE_BEAT_BOX = "CREATE TABLE " +
                DataBaseContract.ListOfSong.TABLE_NAME + " (" +
                DataBaseContract.ListOfSong._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DataBaseContract.ListOfSong.COLUMN_SONG_NAME + " TEXT NOT NULL," +
                DataBaseContract.ListOfSong.COLUMN_SONG_ALBUM_NAME + " TEXT NOT NULL," +
                DataBaseContract.ListOfSong.COLUMN_SONG_ARTIST + " TEXT NOT NULL," +
                DataBaseContract.ListOfSong.COLUMN_SONG_ARTWOTK + " TEXT," +
                DataBaseContract.ListOfSong.COLUMN_SONG_DURATION + " TEXT NOT NULL," +
                DataBaseContract.ListOfSong.COLUMN_SONG_URI + " TEXT NOT NULL," +
                DataBaseContract.ListOfSong.COLUMN_SONG_FULL_PATH + " TEXT NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_BEAT_BOX);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ DataBaseContract.ListOfSong.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
