package com.kmema.android.beatbox.Database;

import android.os.Parcel;
import android.os.Parcelable;

public class SongDataModel implements Parcelable{

    private String mSongName, mSongAlbumName , mSongFullPath , mSongDuration ;
    private String mSongUri, mAlbumArt, mSongArtist;
    private int mSongId;

    public SongDataModel(){ }
// --Commented out by Inspection START (8/3/2017 4:30 PM):
//    public Song(String name , int id ,  String album_name , String full_path , String duration , String songuri,String albumArt,
//                String artist){
//        this.mSongName = name;
//        this.mSongId = id;
//        this.mSongAlbumName = album_name;
//        this.mSongFullPath = full_path;
//        this.mSongDuration = duration;
//        this.mSongUri = songuri;
//        this.mAlbumArt = albumArt;
//        this.mSongArtist = artist;
//    }
// --Commented out by Inspection STOP (8/3/2017 4:30 PM)


    public String getSongArtist() {return  mSongArtist;}

    public void setSongArtist(String artist){this.mSongArtist = artist;}

    public String getAlbumArt() { return mAlbumArt;}

    public  void setAlbumArt(String albumArt) {  this.mAlbumArt = albumArt;}

    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String mSongName) {
        this.mSongName = mSongName;
    }

    public String getSongFullPath() {
        return mSongFullPath;
    }

    public void setSongFullPath(String mSongFullPath) {
        this.mSongFullPath = mSongFullPath;
    }

    public String getSongAlbumName() {
        return mSongAlbumName;
    }

    public void setSongAlbumName(String mSongAlbumName) {
        this.mSongAlbumName = mSongAlbumName;
    }

    public String getSongDuration() {
        return mSongDuration;
    }

    public void setSongDuration(String mSongDuration) {
        this.mSongDuration = mSongDuration;
    }

// --Commented out by Inspection START (8/3/2017 4:30 PM):
//    public int getSongId() {
//        return mSongId;
//    }
// --Commented out by Inspection STOP (8/3/2017 4:30 PM)

    public void setSongId(int mSongId) {
        this.mSongId = mSongId;
    }

    public void setSongUri(String uri){ this.mSongUri = uri; }

    public String getSongUri(){
        return this.mSongUri;
    }


    public static final Parcelable.Creator<SongDataModel> CREATOR = new Parcelable.Creator<SongDataModel>() {
        @Override
        public SongDataModel createFromParcel(Parcel in) {
            return new SongDataModel(in);
        }

        @Override
        public SongDataModel[] newArray(int size) {
            return new SongDataModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;

    }

    public SongDataModel(Parcel in) {
        mSongName = in.readString();
        mSongAlbumName = in.readString();
        mSongFullPath = in.readString();
        mSongDuration = in.readString();
        mSongUri = in.readString();
        mSongId = in.readInt();
        mAlbumArt = in.readString();
        mSongArtist = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSongName);
        dest.writeString(mSongAlbumName);
        dest.writeString(mSongFullPath);
        dest.writeString(mSongDuration);
        dest.writeString(String.valueOf(mSongUri));
        dest.writeInt(mSongId);
        dest.writeString(mAlbumArt);
        dest.writeString(mSongArtist);
    }
}
