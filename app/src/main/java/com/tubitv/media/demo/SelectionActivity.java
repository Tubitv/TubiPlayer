package com.tubitv.media.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import com.tubitv.demo.R;
import com.tubitv.media.activities.TubiPlayerActivity;
import com.tubitv.media.models.MediaModel;

/**
 * Created by stoyan on 6/5/17.
 */
public class SelectionActivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        Button playHls = (Button) findViewById(R.id.activity_selection_play_hls);
        playHls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://c13.adrise.tv/v2/sources/content-owners/paramount/312926/v201604161517-1024x436-,434,981,1533,2097,k.mp4.m3u8?Ku-zvPPeC4amIvKktZuE4IU69WFe1z2sTp84yvomcFQOsMka6d0EyZy1tHl3VT6-";
                String subs = "http://s.adrise.tv/94335ae6-c5d3-414d-8ff2-177c955441c6.srt";
                String artwork = "http://images.adrise.tv/J3cemMAwJC8aX_Eb4MzIaAcNJvY=/768x362/smart/img.adrise.tv/0c690475-ea32-4b91-b704-7d3fab94a48f.jpg";
                String name = "Gladiator";
                Intent intent = new Intent(SelectionActivity.this, DemoActivity.class);
                intent.putExtra(TubiPlayerActivity.TUBI_MEDIA_KEY, new MediaModel(name, url, artwork, subs));
                startActivity(intent);
            }
        });
    }
}
