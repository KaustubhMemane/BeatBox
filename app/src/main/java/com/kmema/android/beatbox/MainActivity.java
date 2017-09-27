package com.kmema.android.beatbox;
import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kmema.android.beatbox.Adapter.RecyclerViewAdapter;
import com.kmema.android.beatbox.Database.DataBaseContract;
import com.kmema.android.beatbox.Database.SongDataModel;
import com.kmema.android.beatbox.Services.MusicService;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnItemClick, UpdateFromService, View.OnClickListener{

    String SONG_NAME_KEY = "000";
    String ALBUM_NAME_KEY = "001";
    String ARTIST_NAME_KEY = "002";
    String ALBUM_ART_KEY = "003";
    String SONG_DURATION_KEY = "004";

    @BindView(R.id.tvGlobalSongName)
    public TextView mGlobalSongName;

    @BindView(R.id.tvGlobalAlbumName)
    public TextView mGlobalAlbumName;

    @BindView(R.id.tvGlobalArtistName)
    public TextView mGlobalArtistName;

    @BindView(R.id.imageGlobalAlbumArtImage)
    public ImageView mGlobalAlbumArt;

    @BindView(R.id.recyclerView_albumSongs)
    private RecyclerView recyclerView;

    @BindView(R.id.fab)
    private FloatingActionButton fab;

    @BindView(R.id.btnPrevious)
    private FloatingActionButton fabPrevious;

    @BindView(R.id.btnPlay)
    private  FloatingActionButton fabPlay;

    @BindView(R.id.btnNext)
    private FloatingActionButton fabNext;

    @BindView(R.id.btnRandom)
    private FloatingActionButton fabRandomButton;

    @BindView(R.id.btnRepeat)
    FloatingActionButton fabRepeat;

    private MusicService serviceMusic;
    private Intent playIntent;

    public TextView mGlobalSongDuration;
    private Cursor cursorAlbumArt = null;
    private Cursor cursor = null;
    private boolean permission = false;
    private RecyclerViewAdapter adapterRecyclerView;
    private final String[] STAR = {"*"};

    ArrayList<SongDataModel> list = null;

    private String albumArtValue;
    private String songDurationValue;
    private ContentResolver mContentResolver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        animationBackground();

        mContentResolver = getContentResolver();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initBeatBox();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab.setOnClickListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        fabPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceMusic.previousSong();
            }
        });
        fabNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceMusic.nextSong();
            }
        });
        fabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceMusic.playPauseSong();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void initBeatBox() {
        permission = checkAvailablePermission();
        if (permission) {
            //if we have permission available we need to get data from sql database not from memory
            list = getDataFromSQL();
            if (!list.isEmpty())
            {
                adapterRecyclerView = new RecyclerViewAdapter(MainActivity.this, list, MainActivity.this);
                recyclerView.setAdapter(adapterRecyclerView);
                sendDatatoService();
            }
            else
            {
                Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Need Permission to display songs", Toast.LENGTH_LONG).show();
        }
    }

    private ArrayList<SongDataModel> getDataFromSQL() {
        ArrayList<SongDataModel> returnList = new ArrayList<>();

        String[] projection = new String[]{DataBaseContract.ListOfSong._ID, DataBaseContract.ListOfSong.COLUMN_SONG_NAME,
                DataBaseContract.ListOfSong.COLUMN_SONG_ALBUM_NAME, DataBaseContract.ListOfSong.COLUMN_SONG_ARTIST,
                DataBaseContract.ListOfSong.COLUMN_SONG_ARTWOTK, DataBaseContract.ListOfSong.COLUMN_SONG_DURATION,
                DataBaseContract.ListOfSong.COLUMN_SONG_FULL_PATH, DataBaseContract.ListOfSong.COLUMN_SONG_URI};

        String order = new String(DataBaseContract.ListOfSong.COLUMN_SONG_NAME);
        Cursor c = mContentResolver.query(DataBaseContract.ListOfSong.CONTENT_URI,projection,null,null,order);

        if(c.moveToFirst()) {
            do {
                SongDataModel song = new SongDataModel();

                song.setSongName(c.getString(c.getColumnIndex(DataBaseContract.ListOfSong.COLUMN_SONG_NAME)));
                song.setSongAlbumName(c.getString(c.getColumnIndex(DataBaseContract.ListOfSong.COLUMN_SONG_ALBUM_NAME)));
                song.setSongArtist(c.getString(c.getColumnIndex(DataBaseContract.ListOfSong.COLUMN_SONG_ARTIST)));
                song.setAlbumArt(c.getString(c.getColumnIndex(DataBaseContract.ListOfSong.COLUMN_SONG_ARTWOTK)));
                song.setSongDuration(c.getString(c.getColumnIndex(DataBaseContract.ListOfSong.COLUMN_SONG_DURATION)));
                song.setSongFullPath(c.getString(c.getColumnIndex(DataBaseContract.ListOfSong.COLUMN_SONG_FULL_PATH)));
                song.setSongUri(c.getString(c.getColumnIndex(DataBaseContract.ListOfSong.COLUMN_SONG_URI)));

                returnList.add(song);
            }while (c.moveToNext());

            return returnList;
        }else
        {
            Toast.makeText(this, "Refresh list", Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private void animationBackground() {
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.root_layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) coordinatorLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.btnRefresh) {

            permission = checkAvailablePermission();
            if(permission)
            {
                setDataToSQLite();
                list = getDataFromSQL();
                adapterRecyclerView = new RecyclerViewAdapter(MainActivity.this, list, MainActivity.this);
                recyclerView.setAdapter(adapterRecyclerView);
                sendDatatoService();
            }

            //calling this function to refresh the song list
            // because we do not want to access/refresh memory with a user permission
            // Handle the camera action
        } else if (id == R.id.btnEqualizer) {

        } else if (id == R.id.btnStore) {

        } else if (id == R.id.btnShare) {

        } else if (id == R.id.btnAboutUs) {

        } else if (id == R.id.btnYouTube) {

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @SuppressWarnings("deprecation")
    private boolean setDataToSQLite() { //Fetch path to all the files from internal & external storage and store it into songList
        ContentValues mValues = new ContentValues();

        Uri allSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        if (isSdPresent()) {
            cursor = managedQuery(allSongsUri, STAR, selection, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {

                        String songAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                        String songFullpath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                        if (!checkDuplicateData(songAlbum,songFullpath)) {


                            String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

                            String[] res = data.split("\\.");

                            mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_NAME, (res[0]));

                            mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_FULL_PATH,
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));

/*                        song.setSongId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));*/

                            mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_ALBUM_NAME,
                                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));


                            mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_URI,
                                    String.valueOf(ContentUris.withAppendedId(
                                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                            cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))));

                            mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_DURATION,
                                    getDuration(Integer.parseInt(cursor.getString(cursor.getColumnIndex
                                            (MediaStore.Audio.Media.DURATION)))));


                            String songArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                            if (songArtist != null && songArtist != "")
                                mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_ARTIST, songArtist);
                            else
                                mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_ARTIST, "<Unknown>");


                            cursorAlbumArt = managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums.ARTIST},
                                    MediaStore.Audio.Albums._ID + "=?",
                                    new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))},
                                    null);

                            if (cursorAlbumArt.getCount() > 0) {
                                if (cursorAlbumArt.moveToFirst()) {
                                    String coverPath = null;

                                    if (MediaStore.Audio.Albums.ALBUM_ART != null && MediaStore.Audio.Albums.ALBUM_ART != "") {
                                        coverPath = cursorAlbumArt.getString(cursorAlbumArt.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
                                    }

                                    if (coverPath != null && coverPath != "") {
                                        //Log.i("PATH::::", coverPath);
                                        mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_ARTWOTK, coverPath);
                                    } else {
                                        mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_ARTWOTK, "null");
                                    }
                                }

                            } else {
                                //Testing purpose
                                Toast.makeText(this, "no Album Art", Toast.LENGTH_SHORT).show();
                                mValues.put(DataBaseContract.ListOfSong.COLUMN_SONG_ARTWOTK, "null");
                            }
                            //Toast.makeText(this, MediaStore.Audio.Albums.ALBUM_ART"", Toast.LENGTH_SHORT).show();
                            mContentResolver.insert(DataBaseContract.ListOfSong.CONTENT_URI, mValues);
                        }
                    }while (cursor.moveToNext());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDuplicateData(String songAlbum, String songFullpath) {



        String[] projection = new String[]{DataBaseContract.ListOfSong._ID, DataBaseContract.ListOfSong.COLUMN_SONG_NAME,
                DataBaseContract.ListOfSong.COLUMN_SONG_ALBUM_NAME, DataBaseContract.ListOfSong.COLUMN_SONG_ARTIST,
                DataBaseContract.ListOfSong.COLUMN_SONG_ARTWOTK, DataBaseContract.ListOfSong.COLUMN_SONG_DURATION,
                DataBaseContract.ListOfSong.COLUMN_SONG_FULL_PATH, DataBaseContract.ListOfSong.COLUMN_SONG_URI};

        String order = new String(DataBaseContract.ListOfSong.COLUMN_SONG_NAME);

        String selection = DataBaseContract.ListOfSong.COLUMN_SONG_ALBUM_NAME +" = ? "+
                DataBaseContract.ListOfSong.COLUMN_SONG_FULL_PATH + " = ?";

        String[] selectionArgs = new String[]{songAlbum, songFullpath};
        Cursor c = mContentResolver.query(DataBaseContract.ListOfSong.CONTENT_URI,projection,selection,selectionArgs,order);

        if(c.moveToFirst()) {
            if(c.getCount() > 0)
            {
                return true;
            }
        }
        return false;
    }


    //Check whether sdcard is present or not
    private static boolean isSdPresent() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    //Method to convert the millisecs to min & sec
    private static String getDuration(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(6);
        sb.append(minutes < 10 ? "0" + minutes : minutes);
        sb.append(":");
        sb.append(seconds < 10 ? "0" + seconds : seconds);
        //sb.append(" Secs");
        return sb.toString();
    }


    private boolean checkAvailablePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Click Yes To Display Songs", Toast.LENGTH_SHORT).show();
                if (!Settings.System.canWrite(this)) {
                    requestPermissions(new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.INTERNET
                    }, 2909);
                }
            }
            else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }


    @Override
    public void onupdateClick(int position, String songName, String albumName, String artistName, String albumArt, String songDuration) {
        mGlobalSongName.setText(songName);
        mGlobalSongName.setSelected(true);
        mGlobalAlbumName.setText(albumName);
        mGlobalArtistName.setText(artistName);
        mGlobalArtistName.setSelected(true);
        if (albumArt != null && albumArt != " ") {
            Drawable img = Drawable.createFromPath(albumArt);
            mGlobalAlbumArt.setImageDrawable(img);
        } else {
            mGlobalAlbumArt.setImageResource(R.drawable.beat_box_art);
        }
    }


    //my method for finding the click in Recycle view
    @Override
    public void onClick(int position, String songName, String albumName, String artistName, String albumArt, String songDuration) {
        mGlobalSongName.setText(songName);
        mGlobalSongName.setSelected(true);
        mGlobalAlbumName.setText(albumName);
        mGlobalArtistName.setText(artistName);
        mGlobalArtistName.setSelected(true);
        albumArtValue = albumArt;
        songDurationValue = songDuration;
        //mGlobalSongDuration.setText(songDuration);

        if (albumArt != null && albumArt != " ") {
            Drawable img = Drawable.createFromPath(albumArt);
            mGlobalAlbumArt.setImageDrawable(img);
        } else {
            mGlobalAlbumArt.setImageResource(R.drawable.beat_box_art);
        }
        playMyMusic(position);
    }

    private void playMyMusic(int position) {
        serviceMusic.setSelectedSong(position, MusicService.NOTIFICATION_ID, this, MainActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void sendDatatoService() {
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_ABOVE_CLIENT);
            startService(playIntent);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Activity State:::","RESUME");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("Activity State:::","RESTART");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putString(SONG_NAME_KEY,mGlobalSongName.getText().toString());
        outState.putString(ALBUM_NAME_KEY,mGlobalAlbumName.getText().toString());
        outState.putString(ARTIST_NAME_KEY,mGlobalArtistName.getText().toString());
        outState.putString(ALBUM_ART_KEY, albumArtValue);
        outState.putString(SONG_DURATION_KEY, songDurationValue);

        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        mGlobalSongName.setText(savedInstanceState.getString(SONG_NAME_KEY));
        mGlobalArtistName.setText(savedInstanceState.getString(ARTIST_NAME_KEY));
        mGlobalAlbumName.setText(savedInstanceState.getString(ALBUM_NAME_KEY));

        String albumArtAddress = savedInstanceState.getString(ALBUM_ART_KEY);

        if (albumArtAddress != null && albumArtAddress != " ") {
            Drawable img = Drawable.createFromPath(albumArtAddress);
            mGlobalAlbumArt.setImageDrawable(img);
        } else {
            mGlobalAlbumArt.setImageResource(R.drawable.beat_box_art);
        }
    }


    private final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.PlayerBinder binder = (MusicService.PlayerBinder) service;
            serviceMusic = binder.getService();
            serviceMusic.setListofSongs(list);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMusic = null;
        }
    };


    @Override
    protected void onStop() {
        super.onStop();
        Log.e("Activity State:::","Stop");
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);

        if (!cursor.isClosed())
            cursor.close();

        if (!cursorAlbumArt.isClosed())
            cursorAlbumArt.close();

        Log.e("Activity State:::","Destroy");
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case (R.id.fab):
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                break;

            case (R.id.btnNext):
                break;

            case (R.id.btnPrevious):
                break;

            case (R.id.btnPlay):
                break;
        }
    }
}
