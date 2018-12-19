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

import static com.tubitv.media.helpers.Constants.PIP_ENABLE_KET;
import static com.tubitv.media.helpers.Constants.PIP_ENABLE_VALUE_DEFAULT;

/**
 * Created by stoyan on 6/5/17.
 */
public class SelectionActivity extends Activity {

    private static final String VIDEO_URL = "https://tungsten.aaplimg.com/VOD/bipbop_adv_fmp4_example/master.m3u8";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
        Button playHls1 = (Button) findViewById(R.id.activity_selection_play_hls_1);
        playHls1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subs = "http://s.adrise.tv/88703acf-66a2-4071-8231-d6cffe579f33.srt";
                String artwork = "http://images.adrise.tv/6sjdZy7rGz23YZ62_diTF26BfgE=/214x306/smart/img.adrise.tv/4b85521c-c3af-41d5-bf52-40b698c6d56d.jpg";
                String name = "longest weekend";
                Intent intent = new Intent(SelectionActivity.this, DoubleViewTubiPlayerActivity.class);
                intent.putExtra(TubiPlayerActivity.TUBI_MEDIA_KEY, MediaModel.video(name, VIDEO_URL, artwork, null));
                intent.putExtra(PIP_ENABLE_KET, PIP_ENABLE_VALUE_DEFAULT);
                startActivity(intent);
            }
        });

        Button playHls2 = (Button) findViewById(R.id.activity_selection_play_hls_2);
        playHls2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String artwork = "http://images.adrise.tv/q4v7JUQPPHqn8nTmYiudW6l8w_0=/214x306/smart/img.adrise.tv/1c31dfce-5338-4a09-bcb0-f68789153f33.png";
                String name = "Man on the ledge";
                Intent intent = new Intent(SelectionActivity.this, DoubleViewTubiPlayerActivity.class);
                intent.putExtra(TubiPlayerActivity.TUBI_MEDIA_KEY, MediaModel.video(name, VIDEO_URL, artwork, null));
                intent.putExtra(PIP_ENABLE_KET, PIP_ENABLE_VALUE_DEFAULT);
                startActivity(intent);
            }
        });

    }
}