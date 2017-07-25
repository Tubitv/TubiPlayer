package com.tubitv.media.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.tubitv.demo.R;
import com.tubitv.media.activities.DoubleViewTubiPlayerActivity;
import com.tubitv.media.activities.TubiPlayerActivity;
import com.tubitv.media.models.MediaModel;

/**
 * Created by stoyan on 6/5/17.
 */
public class SelectionActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        Button playHls1 = (Button) findViewById(R.id.activity_selection_play_hls_1);
        playHls1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://c13.adrise.tv/v2/sources/content-owners/cinedigm-tubi/339391/v201701300137-,210,415,677,1134,1552,k.mp4.m3u8?pVJiicetIe13CLViqLW3ET8ICu4kSyZBnO7dOxOQ3VZLrcdaRRV9c-BUz03_drbn";
                String subs = "http://s.adrise.tv/94335ae6-c5d3-414d-8ff2-177c955441c6.srt";
                String artwork = "http://images.adrise.tv/J3cemMAwJC8aX_Eb4MzIaAcNJvY=/768x362/smart/img.adrise.tv/0c690475-ea32-4b91-b704-7d3fab94a48f.jpg";
                String name = "Real movies";
                Intent intent = new Intent(SelectionActivity.this, DoubleViewTubiPlayerActivity.class);
                intent.putExtra(TubiPlayerActivity.TUBI_MEDIA_KEY, MediaModel.video(name, url, artwork, subs));
                startActivity(intent);
            }
        });

        Button playHls2 = (Button) findViewById(R.id.activity_selection_play_hls_2);
        playHls2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://c13.adrise.tv/v2/sources/content-owners/cinedigm-tubi/339391/v201701300137-,210,415,677,1134,1552,k.mp4.m3u8?pVJiicetIe13CLViqLW3ET8ICu4kSyZBnO7dOxOQ3VZLrcdaRRV9c-BUz03_drbn";
                String artwork = "http://images.adrise.tv/7V6_jCHzkl0lQARtDgWCnOKUozs=/492x118:1909x905/768x362/smart/img.adrise.tv/1541c7d3-9a50-4c5c-9bf7-ba63e4018f36.jpg";
                String name = "Wild Bill";
                Intent intent = new Intent(SelectionActivity.this, DoubleViewTubiPlayerActivity.class);
                intent.putExtra(TubiPlayerActivity.TUBI_MEDIA_KEY, MediaModel.video(name, url, artwork, null));
                startActivity(intent);
            }
        });
    }
}
