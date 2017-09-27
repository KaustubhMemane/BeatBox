package com.kmema.android.beatbox.Database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by kmema on 9/21/2017.
 */

public class DataBaseContract {

    public static final String AUTHORITY = "com.kmema.android.beatbox.Database";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://"+ AUTHORITY);
    static final String PATH_TO_TABLE_BEATBOX = "BeatBoxTable";

    public static class ListOfSong implements BaseColumns{
        public static  final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TO_TABLE_BEATBOX).build();
        public static final String TABLE_NAME = "BeatBoxTable";
        public static final String COLUMN_SONG_NAME = "songNameTag";
        public static final String COLUMN_SONG_ALBUM_NAME = "songAlbumName";
        public static final String COLUMN_SONG_FULL_PATH = "songFullPath";
        public static final String COLUMN_SONG_DURATION = "songDuration";
        public static final String COLUMN_SONG_URI = "songURI";
        public static final String COLUMN_SONG_ARTWOTK = "songAlbumArt";
        public static final String COLUMN_SONG_ARTIST = "songArtist";
    }
}
