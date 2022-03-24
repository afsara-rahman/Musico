package com.example.musico;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;

public class Newmain extends AppCompatActivity {
    ListView listview;
    String[] items;
    private Context context;
    private static ArrayList<SongModel> mainList = new ArrayList<SongModel>();
    ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newmain);
        listview = findViewById(R.id.songlist);
        runtimePermission();
    }

    public void runtimePermission(){
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySongs();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

    }

    private void grabIfEmpty() {
        if (mainList.isEmpty()) {
            grabData();
        } else {
            Log.d("H","Data is present. Just setting context.");
        }
    }

    private void grabData() {
        String[] STAR = {"*"};

        boolean excludeShortSounds = true;
        boolean excludeWhatsApp = true;
        ContentResolver musicResolver = this.getContentResolver();
        Cursor cursor;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        cursor = musicResolver.query(uri,null,null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String duration = cursor
                            .getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.DURATION));
                    int currentDuration = Math.round(Integer
                            .parseInt(duration));
                    if (currentDuration > ((excludeShortSounds) ? 60000 : 0)) {
                        if (!excludeWhatsApp || !cursor.getString(cursor
                                .getColumnIndex(MediaStore.Audio.Media.ALBUM)).equals("WhatsApp Audio")) {
                            String songName = cursor
                                    .getString(
                                            cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME))
                                    .replace("_", " ").trim().replaceAll(" +", " ");
                            String path = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.DATA));
                            String title = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.TITLE)).replace("_", " ").trim().replaceAll(" +", " ");
                            String artistName = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.ARTIST));
                            String albumName = cursor.getString(cursor
                                    .getColumnIndex(MediaStore.Audio.Media.ALBUM));

                            String albumID = cursor
                                    .getString(
                                            cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                                    );

                            TimeZone tz = TimeZone.getTimeZone("UTC");
                            SimpleDateFormat df = new SimpleDateFormat("mm:ss", Locale.getDefault());
                            df.setTimeZone(tz);
                            String time = String.valueOf(df.format(currentDuration));

                            // Adding song to list
                            SongModel songModel = new SongModel();
                            songModel.setFileName(songName);
                            songModel.setTitle(title);
                            songModel.setArtist(artistName);
                            songModel.setAlbum(albumName);
                            songModel.setAlbumID(albumID);
                            songModel.setPath(path);
                            songModel.setDuration(time);

                            mainList.add(songModel);
                        }
                    }
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
    }


    public ArrayList<SongModel> allSongs() {
        grabIfEmpty(); // If no song in list

        // Sorted list of 0-9 A-Z
        ArrayList<SongModel> songs = new ArrayList<>(mainList);
        Collections.sort(songs, new Comparator<SongModel>() {
            @Override
            public int compare(SongModel song1, SongModel song2) {
                return song1.getTitle().compareTo(song2.getTitle());
            }
        });
        return songs;
    }

    void displaySongs(){
        final ArrayList<SongModel> mySongs = allSongs();

        customAdapter customadapter = new customAdapter();
        listview.setAdapter(customadapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songname = (String) listview.getItemAtPosition(position);
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class).putExtra("songs", mySongs).
                        putExtra("songname",songname).putExtra("pos",position));

            }
        });

    }


    class customAdapter extends BaseAdapter{

        @Override
        public int getCount() {

            return mainList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myview = getLayoutInflater().inflate(R.layout.list_item, null);
            TextView textsong = myview.findViewById(R.id.txtsongname);
            textsong.setSelected(true);
            textsong.setText(mainList.get(position).getTitle());

            return myview;
        }
    }
}