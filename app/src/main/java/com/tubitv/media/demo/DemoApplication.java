package com.tubitv.media.demo;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.UUID;

import io.fabric.sdk.android.Fabric;

/**
 * Created by stoyan tubi_tv_quality_on 3/21/17.
 */
public class DemoApplication extends Application {

    protected String userAgent;

    @Override
    public void onCreate() {
        super.onCreate();

        initFabric(this);

        userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter);
    }

    public static void initFabric(@NonNull Context context) {
        final Fabric.Builder fabric = new Fabric.Builder(context)
                .kits(new Crashlytics(),new Answers());
        fabric.debuggable(true);
        Fabric.with(fabric.build());

        Crashlytics.setString("com.crashlytics.android.build_id", UUID.randomUUID().toString());
    }
}
