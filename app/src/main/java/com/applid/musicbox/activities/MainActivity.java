package com.applid.musicbox.activities;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.ViewPager;

import com.applid.musicbox.R;
import com.applid.musicbox.adapters.SongAdapter;
import com.applid.musicbox.adapters.ViewPagerAdapter;
import com.applid.musicbox.fragments.AlbumsFragment;
import com.applid.musicbox.fragments.SongsFragment;
import com.applid.musicbox.models.DataReading;
import com.applid.musicbox.models.Song;
import com.applid.musicbox.notification.NotiService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.applid.musicbox.notification.NofiticationCenter.channel_1_ID;
import static com.applid.musicbox.adapters.SongAdapter.songs;

public class MainActivity extends AppCompatActivity {



    private int Storage_Permission_code=1;
    private int REQUEST_CODE = 4;
    private static final String TAG = "MainActivity";
    DataReading dataReading;
    protected static MainActivity instance;
    private byte arts[];

     private HashMap<String, List<Song>>albums=new HashMap<>();
     public static  ArrayList<ArrayList<Song>>al=new ArrayList<>();
    private static final int MY_PERMISSIONS_RECORD_AUDIO = 111;

    private NotificationManagerCompat notificationManager;
    MediaMetadataRetriever metadataRetriever;
    private MediaSessionCompat mediaSession;

     TabLayout tableLayout;
     ViewPager viewPager;
     protected ViewPagerAdapter viewPagerAdapter;
    LinearLayout miniplayer;
    TextView textView;
    public static ImageView imageView;
    public static Notification notification;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;

    private final static int DAYS_UNTIL_PROMPT = 3;//Min number of days
    private final static int LAUNCHES_UNTIL_PROMPT = 3;
    private final static String APP_TITLE = "MusicBox";// App Name
    private final static String APP_PNAME = "com.applid.musicbox";// Package Name


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        textView=findViewById(R.id.mini_player_title);
        tableLayout=findViewById(R.id.table_Layout);
        viewPager=findViewById(R.id.view_Pager);
        viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());


        viewPagerAdapter.addFragment(new SongsFragment(),"Songs");
        viewPagerAdapter.addFragment(new AlbumsFragment(),"Albums");

        viewPager.setAdapter(viewPagerAdapter);
        tableLayout.setupWithViewPager(viewPager);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setElevation(0);
        imageView=findViewById(R.id.mini_player_play_pause_button);
        miniplayer=findViewById(R.id.mini_player);
        imageView.setBackgroundResource(R.drawable.play_arrow_24dp);
       intializeMini();
        notificationManager = NotificationManagerCompat.from(this);

        mediaSession = new MediaSessionCompat(this, "tag");
        instance= this;
        songs=new ArrayList<>();


        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
        if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.RECORD_AUDIO)){
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this
            );
            builder.setTitle("Grant those Permissions");
            builder.setMessage("Allow read external storage and record audio permissions.");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
                }
            });
            builder.setNegativeButton("Cancel", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else{
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO}, REQUEST_CODE);
        }
        }
        else{
            start();
        }

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });



        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });


        AdRequest adRequest1 = new AdRequest.Builder().build();
        InterstitialAd.load(this,"ca-app-pub-2281074336539826/7424669340", adRequest1,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });


        //Rating dialog

        SharedPreferences prefs = getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) { return ; }

        SharedPreferences.Editor editor = prefs.edit();

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        // Get date of first launch
        Long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        // Wait at least n days before opening
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)) {
                showRateDialog(getApplicationContext(),editor);
            }
        }

        editor.apply();



    }

    public void intializeMini(){
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlayerActivity.getInstance().play();
                    if (PlayerActivity.playin) {
                        imageView.setBackgroundResource(R.drawable.pause_24dp);
                    } else {
                        imageView.setBackgroundResource(R.drawable.play_arrow_24dp);

                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

            miniplayer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.getInstance(), PlayerActivity.class).putExtra("index", 0).putExtra("val", 0).putExtra("from", false);
                    startActivity(intent);


                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(MainActivity.this);
                    } else {
                        Log.d("TAG", "The interstitial ad wasn't ready yet.");
                    }

                }
            });

    }
    public static MainActivity getInstance() {
        return instance;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if ((grantResults.length > 0) && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                finish();
                startActivity(getIntent());
            } else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }

    }




    private void start(){
        dataReading=new DataReading(this);
        songs=new ArrayList<>();
        songs.add(new Song());
        ArrayList<Song> songs = dataReading.getAllAudioFromDevice();
        Collections.sort(songs);
        SongAdapter.songs=songs;
        albums=dataReading.getAlbums();
        textView.setText(songs.get(0).getName());
        shift();


    }
    public void shift(){
        Iterator it = albums.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            al.add((ArrayList<Song>) pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.search_menu,menu);

        MenuItem searchItem=menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
               SongsFragment.search(newText);
                return false;
            }
        });
        return true;
    }

    public void sendOnChannel(String name,String artist,int position) {

        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, activityIntent, 0);

        int plaorpa;
        if(PlayerActivity.playin){
            plaorpa=R.drawable.pause_24dp;
        }else{
            plaorpa=R.drawable.play_arrow_24dp;
        }


        metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(songs.get(position).getPath());
        arts= metadataRetriever.getEmbeddedPicture();
        Bitmap artwork;
        try {
            artwork=BitmapFactory.decodeByteArray(arts,0,arts.length);
        }catch (Exception e){
            artwork = BitmapFactory.decodeResource(getResources(), R.drawable.icon_);
        }
         notification = new NotificationCompat.Builder(this, channel_1_ID)
                .setSmallIcon(R.drawable.music_note_24dp)
                .setContentTitle(name)
                .setContentText("Song")
                .setLargeIcon(artwork)
                .addAction(R.drawable.previous_24dp, "Previous", playbackAction(3))
                .addAction(plaorpa, "Pause", playbackAction(1))
                .addAction(R.drawable.next_24dp, "Next", playbackAction(2))
               //  .setDeleteIntent(playbackAction(4))
                 .setContentIntent(contentIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.getSessionToken()))
                .setSubText(artist)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();


        notificationManager.notify(1, notification);
    }


    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, NotiService.class);
        switch (actionNumber) {
            case 1:
                // Pause
                playbackAction.setAction("com.mypackage.ACTION_PAUSE_MUSIC");
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction("com.mypackage.ACTION_NEXT_MUSIC");
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction("com.mypackage.ACTION_PREV_MUSIC");
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 4:
                playbackAction.setAction("com.mypackage.ACTION_STOP_MUSIC");
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    //Rate app dialog

    public static void showRateDialog(final Context mContext, final SharedPreferences.Editor editor) {
        final Dialog dialog = new Dialog(mContext);
        dialog.setTitle("Rate " + APP_TITLE);

        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);

        TextView tv = new TextView(mContext);
        tv.setText("If you enjoy using " + APP_TITLE + ", please take a moment to rate it. Thanks for your support!");
        tv.setWidth(240);
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);

        Button b1 = new Button(mContext);
        b1.setText("Rate " + APP_TITLE);
        b1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + APP_PNAME)));
                dialog.dismiss();
            }
        });
        ll.addView(b1);

        Button b2 = new Button(mContext);
        b2.setText("Remind me later");
        b2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ll.addView(b2);

        Button b3 = new Button(mContext);
        b3.setText("No, thanks");
        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (editor != null) {
                    editor.putBoolean("dontshowagain", true);
                    editor.commit();
                }
                dialog.dismiss();
            }
        });
        ll.addView(b3);

        dialog.setContentView(ll);
        dialog.show();
    }



}
