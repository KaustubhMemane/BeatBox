package com.kmema.android.beatbox.Adapter;
import android.content.Context;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.kmema.android.beatbox.Database.SongDataModel;
import com.kmema.android.beatbox.MainActivity;
import com.kmema.android.beatbox.OnItemClick;
import com.kmema.android.beatbox.R;
import com.kmema.android.beatbox.UpdateFromService;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.myRecyclerViewHolder> {

    private final Context mContext;
    private TextView mGlobalSongName;
    private ArrayList<SongDataModel> mySongs;
    private OnItemClick mCallBack;

    public RecyclerViewAdapter(Context context, ArrayList<SongDataModel> mySongs,
                               OnItemClick listner)
    {
        this.mContext = context;
        this.mySongs = mySongs;
        this.mCallBack = listner;
    }



    @Override
    public myRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.one_song,parent,false);

        return new myRecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(myRecyclerViewHolder holder, final int position) {

        if(mySongs.isEmpty())
            return;

        String songName = null;
        String albumName = null;
        String artistName = null;
        String albumArt = null;
        String songDuration = null;

        if(position < mySongs.size()-1)
        {
            if (mySongs.get(position).getSongName() !=null)
                songName = mySongs.get(position).getSongName();
            else
                songName = "No Song Name";

            if(mySongs.get(position).getSongAlbumName() != null)
                albumName = mySongs.get(position).getSongAlbumName();
            else
                albumName = "<unknown>";

            if(mySongs.get(position).getSongArtist() != null)
                artistName = mySongs.get(position).getSongArtist();
            else
                artistName = "<unknown>";


            if(mySongs.get(position).getAlbumArt() != null &&
                    mySongs.get(position).getAlbumArt() != "")
                albumArt = mySongs.get(position).getAlbumArt();
            else
                albumArt =" ";

            if(mySongs.get(position).getSongDuration()!=null)
                songDuration = mySongs.get(position).getSongDuration();
            else
                songDuration = "0.0";

            holder.mSongName.setText(songName);
            holder.mArtistName.setText(artistName);
            holder.mDuration.setText(songDuration);

            holder.itemView.setTag(R.string.tag1_SongName,mySongs.get(position).getSongName());
            holder.itemView.setTag(R.string.tag2_albumName,mySongs.get(position).getSongAlbumName());
            holder.itemView.setTag(R.string.tag3_albumArt,mySongs.get(position).getAlbumArt());
            holder.itemView.setTag(R.string.tag4_artistName,mySongs.get(position).getSongArtist());
            holder.itemView.setTag(R.string.tag5_songDuration,mySongs.get(position).getSongDuration());
        }
        else {
            holder.mSongName.setText(" ");
            holder.mArtistName.setText(" ");
            holder.mDuration.setText(" ");
            holder.mMenuButton.setVisibility(View.INVISIBLE);
        }

        final String finalSongName = songName;
        final String finalAlbumName = albumName;
        final String finalArtistName = artistName;
        final String finalAlbumArt = albumArt;
        final String finalSongDuration = songDuration;

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalSongName==null && position >= mySongs.size()-1)
                {
                    return;
                }


                mCallBack.onClick(position,finalSongName, finalAlbumName, finalArtistName, finalAlbumArt, finalSongDuration);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mySongs.size()+1;
    }

/*
    public void swapCursor(Cursor newCursor)
    {
        if(mCursor != null)
        {
            mCursor.close();
        }
        mCursor = newCursor;

        if(newCursor != null)
        {
            this.notifyDataSetChanged();
        }
    }
*/
    class myRecyclerViewHolder extends RecyclerView.ViewHolder {

        final TextView mSongName;
        final TextView mArtistName;
        final TextView mDuration;
        final Button mMenuButton;


        public myRecyclerViewHolder(View itemView) {
            super(itemView);
            mSongName = (TextView) itemView.findViewById(R.id.tvSongName);
            mArtistName = (TextView) itemView.findViewById(R.id.tvArtistName);
            mDuration = (TextView) itemView.findViewById(R.id.tvDuration);
            mMenuButton = (Button) itemView.findViewById(R.id.buttonMenu);
        }
    }
}
