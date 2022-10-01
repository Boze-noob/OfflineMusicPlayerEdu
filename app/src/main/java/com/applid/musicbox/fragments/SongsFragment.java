package com.applid.musicbox.fragments;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applid.musicbox.activities.MainActivity;
import com.applid.musicbox.activities.PlayerActivity;
import com.applid.musicbox.R;
import com.applid.musicbox.adapters.SongAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;


import interfaces.OnClickListen;

import static com.applid.musicbox.adapters.SongAdapter.songs;

public class SongsFragment extends Fragment implements OnClickListen {
   private View v;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mmanager;
    private static SongAdapter songAdapter;
    private InterstitialAd mInterstitialAd;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AdRequest adRequest1 = new AdRequest.Builder().build();
        InterstitialAd.load(getContext(),"ca-app-pub-2281074336539826/9859260993", adRequest1,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("TAG", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i("TAG", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });


    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.songs_fragment,container,false);
        recyclerView = v.findViewById(R.id.recycleview);
        recyclerView.setHasFixedSize(true);
        mmanager=new LinearLayoutManager(getContext());
        songAdapter = new SongAdapter(MainActivity.getInstance(), songs, this);
        recyclerView.setLayoutManager(mmanager);
        recyclerView.setAdapter(songAdapter);

        return v;
    }


    @Override
    public void onClick(int position) {
        if(position>0) {
            Intent intent = new Intent(MainActivity.getInstance(), PlayerActivity.class).putExtra("index", position).putExtra("val", 0).putExtra("from",true);
            startActivity(intent);
        }
        else{
            Intent intent = new Intent(MainActivity.getInstance(), PlayerActivity.class).putExtra("index", position).putExtra("val", 2).putExtra("from",true);
            startActivity(intent);
        }

        if (mInterstitialAd != null) {
            mInterstitialAd.show(getActivity());
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }




    }
    public static void search(String text){
        songAdapter.getFilter().filter(text);
    }
}
