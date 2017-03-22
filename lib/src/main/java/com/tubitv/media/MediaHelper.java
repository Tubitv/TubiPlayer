package com.tubitv.media;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by stoyan on 3/22/17.
 */
public class MediaHelper {

    public static
    @NonNull
    DataSource.Factory buildDataSourceFactory(@NonNull Context context, @Nullable DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(context, bandwidthMeter,
                buildHttpDataSourceFactory(context, bandwidthMeter));
    }

    //TODO put user agent in meta or attrs
    public static
    @NonNull
    HttpDataSource.Factory buildHttpDataSourceFactory(@NonNull Context context, @NonNull DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "TubiPlayer"), bandwidthMeter);
    }
}
