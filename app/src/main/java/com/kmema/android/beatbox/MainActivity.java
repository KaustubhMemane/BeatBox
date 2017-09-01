package com.kmema.android.beatbox;
import android.Manifest;
import android.content.ComponentName;
import android.content.ContentUris;
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
import com.kmema.android.beatbox.Database.SongDataModel;
import com.kmema.android.beatbox.Services.MusicService;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnItemClick {

    public TextView mGlobalSongName;
    public TextView mGlobalAlbumName;
    public TextView mGlobalArtistName;
    public ImageView mGlobalAlbumArt;
    private MusicService serviceMusic;
    private Intent playIntent;
    private ArrayList<SongDataModel> mySongList;
    //public TextView mGlobalSongDuration;


    private Cursor cursorAlbumArt = null;
    private Cursor cursor = null;
    private boolean permission = false;
    private RecyclerViewAdapter adapterRecyclerView;
    private final String[] STAR = {"*"};
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animationBackground();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView_albumSongs);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mGlobalSongName = (TextView) findViewById(R.id.tvGlobalSongName);
        mGlobalAlbumName = (TextView) findViewById(R.id.tvGlobalAlbumName);
        mGlobalArtistName = (TextView) findViewById(R.id.tvGlobalArtistName);
        mGlobalAlbumArt = (ImageView) findViewById(R.id.imageGlobalAlbumArtImage);
//        mGlobalSongDuration = (TextView) findViewById(R.id.seekBar);


        permission = checkAvailablePermission();


        if (permission) {
            getDataFromMemory();            //attaching data to recycler view
            sendDatatoService();            //sending data(list of songs to process) to service to process
        }
        else {
            Toast.makeText(this, "Need Permission to display songs", Toast.LENGTH_LONG).show();
        }



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
            checkAvailablePermission();
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
    private ArrayList<SongDataModel> listAllSongs() { //Fetch path to all the files from internal & external storage and store it into songList

        ArrayList<SongDataModel> songList = new ArrayList<>();

        Uri allSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

        if (isSdPresent()) {
            cursor = managedQuery(allSongsUri, STAR, selection, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        SongDataModel song = new SongDataModel();

                        String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                        String[] res = data.split("\\.");
                        song.setSongName(res[0]);
                        //Log.d("test",res[0] );
                        song.setSongFullPath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
                        song.setSongId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                        song.setSongAlbumName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));

                        song.setSongUri(String.valueOf(ContentUris.withAppendedId(
                                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)))));

                        String duration = getDuration(Integer.parseInt(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))));

                        song.setSongDuration(duration);

                        String songArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                        if (songArtist != null && songArtist !="")
                            song.setSongArtist(songArtist);
                        else
                            song.setSongArtist("<No Info>");


                        cursorAlbumArt = managedQuery(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART, MediaStore.Audio.Albums.ARTIST},
                                MediaStore.Audio.Albums._ID + "=?",
                                new String[]{String.valueOf(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))},
                                null);

                        if (cursorAlbumArt.getCount() > 0) {
                            if (cursorAlbumArt.moveToFirst()) {
                                String coverPath = null;

                                if (MediaStore.Audio.Albums.ALBUM_ART != null && MediaStore.Audio.Albums.ALBUM_ART != "")
                                {coverPath = cursorAlbumArt.getString(cursorAlbumArt.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));}

                                if (coverPath != null && coverPath != "") {
                                    //Log.i("PATH::::", coverPath);
                                    song.setAlbumArt(coverPath);
                                } else {
                                    song.setAlbumArt(null);
                                }
                            }
                        } else {
                            Toast.makeText(this, "NULL C", Toast.LENGTH_SHORT).show();
                        }
                        //Toast.makeText(this, MediaStore.Audio.Albums.ALBUM_ART"", Toast.LENGTH_SHORT).show();
                        songList.add(song);
                    } while (cursor.moveToNext());

                    return songList;
                }
            }

        }
        return null;
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
                checkPermission();
            }
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getDataFromMemory();
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    //check if user permissions are available
    private void checkPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Toast.makeText(this, "Provide access Read Songs", Toast.LENGTH_SHORT).show();

                if (!Settings.System.canWrite(this)) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
                }
            } else {
            }
        } catch (SecurityException se) {
            Log.d("FragmentCreate", "You don't have permissions");

         /*   errortext.setVisibility(View.VISIBLE);
            errortext.setText("Please provide Location permission to continue, Settings->Apps->RecommendedApp->Permissions");
         */
            Toast.makeText(this, "Storage permissions Needed", Toast.LENGTH_SHORT).show();
        }
    }


    public void getDataFromMemory() {
        Observable<ArrayList<SongDataModel>> mObservable = null;

        mObservable.defer(new Callable<ObservableSource<ArrayList<SongDataModel>>>() {
            @Override
            public ObservableSource<ArrayList<SongDataModel>> call() throws Exception {
                ArrayList<SongDataModel> data = listAllSongs();
                return Observable.just(data);
                }})
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<SongDataModel>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull ArrayList<SongDataModel> songDataModels) {
                        Toast.makeText(MainActivity.this, "WorkingFine", Toast.LENGTH_SHORT).show();
                        /*mySongList = songDataModels;*/

                        adapterRecyclerView = new RecyclerViewAdapter(MainActivity.this, songDataModels, MainActivity.this);
                        recyclerView.setAdapter(adapterRecyclerView);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e("Observable",e.getMessage());
                        Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(serviceMusic, "Refreshed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //my method for finding the click in Recycle view
    @Override
    public void onClick(int position, String songName, String albumName, String artistName, String albumArt, String songDuration) {
        mGlobalSongName.setText(songName);
        mGlobalSongName.setSelected(true);
        mGlobalAlbumName.setText(albumName);
        mGlobalArtistName.setText(artistName);
        mGlobalArtistName.setSelected(true);
        //      mGlobalSongDuration.setText(songDuration);

        if (albumArt != null && albumArt != " ") {
            Drawable img = Drawable.createFromPath(albumArt);
            mGlobalAlbumArt.setImageDrawable(img);
        } else {
            mGlobalAlbumArt.setImageResource(R.drawable.beat_box_art);
        }
        playMyMusic(position);
    }

    private void playMyMusic(int position) {
        serviceMusic.setSelectedSong(position, MusicService.NOTIFICATION_ID, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_ABOVE_CLIENT);
            startService(playIntent);
        }*/
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

    }

    private final ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicService.PlayerBinder binder = (MusicService.PlayerBinder) service;
            serviceMusic = binder.getService();
            if (permission)
                serviceMusic.setListofSongs(listAllSongs());

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMusic = null;
        }
    };


    @Override
    protected void onStop() {
        super.onStop();

/*        if (!cursor.isClosed())
            cursor.close();

        if(!cursorAlbumArt.isClosed())
            cursorAlbumArt.close();*/
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(musicConnection);

        if (!cursor.isClosed())
            cursor.close();

        if (!cursorAlbumArt.isClosed())
            cursorAlbumArt.close();

    }

    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);

    }


}
