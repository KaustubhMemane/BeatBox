package com.kmema.android.beatbox.Database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.kmema.android.beatbox.Database.DataBaseContract.AUTHORITY;
import static com.kmema.android.beatbox.Database.DataBaseContract.PATH_TO_TABLE_BEATBOX;

/**
 * Created by kmema on 9/22/2017.
 */

public class BeatContentProvider extends ContentProvider {

    public static final int SONGS = 100;
    public static final int SONG_WITH_ID = 200;
    private static final UriMatcher mURI_MATCHER = buildURIMatcher();
    DBOpenHelper helper;


    private static UriMatcher buildURIMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH_TO_TABLE_BEATBOX, SONGS);
        uriMatcher.addURI(AUTHORITY, PATH_TO_TABLE_BEATBOX + "/#", SONG_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        helper = new DBOpenHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {


        SQLiteDatabase sqLiteDatabase = helper.getReadableDatabase();
        int match = mURI_MATCHER.match(uri);

        Cursor cursor;

        String[] projection = new String[]{DataBaseContract.ListOfSong._ID, DataBaseContract.ListOfSong.COLUMN_SONG_NAME,
                DataBaseContract.ListOfSong.COLUMN_SONG_ALBUM_NAME, DataBaseContract.ListOfSong.COLUMN_SONG_ARTIST,
                DataBaseContract.ListOfSong.COLUMN_SONG_ARTWOTK, DataBaseContract.ListOfSong.COLUMN_SONG_DURATION,
                DataBaseContract.ListOfSong.COLUMN_SONG_FULL_PATH, DataBaseContract.ListOfSong.COLUMN_SONG_URI};

        String order = new String(DataBaseContract.ListOfSong.COLUMN_SONG_NAME);

        switch (match) {
            case SONGS:
                cursor = sqLiteDatabase.query(DataBaseContract.ListOfSong.TABLE_NAME,
                        projection, null, null, null, null, order);
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        int match = mURI_MATCHER.match(uri);

        Uri returnUri;

        switch (match) {
            case SONGS:
                long id = sqLiteDatabase.insert(DataBaseContract.ListOfSong.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(DataBaseContract.ListOfSong.CONTENT_URI, id);
                } else
                    throw new SQLException("Failed to insert a row into" + uri);
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {

        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
        int match = mURI_MATCHER.match(uri);
        int songToDelete;

        switch (match) {
            case SONG_WITH_ID:
                String id = uri.getPathSegments().get(1);

                songToDelete = sqLiteDatabase.delete(DataBaseContract.ListOfSong.TABLE_NAME, "_id = ?", new String[]{id});
                break;

            default:
                throw new UnsupportedOperationException("Unknown URI" + uri);
        }

        if (songToDelete != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return songToDelete;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
