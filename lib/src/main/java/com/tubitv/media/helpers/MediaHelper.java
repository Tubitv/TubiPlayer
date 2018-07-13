package com.tubitv.media.helpers;

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
 * Created by stoyan on 6/21/17.
 */

public class MediaHelper {
    //    private static LinkedList<MediaModel> linkedList;
    //    private static MediaHelper instance;

    //    public synchronized static MediaHelper create(@NonNull MediaModel... models) {
    //        instance = new MediaHelper(models);
    //        return instance;
    //    }

    //    private MediaHelper(MediaModel[] models) {
    //        linkedList = new LinkedList<>();
    //        for (MediaModel model : models) {
    //            linkedList.add(model);
    //        }
    //    }
    //
    //    public synchronized static MediaHelper getInstance() {
    //        Assertions.checkNotNull(instance);
    //        return instance;
    //    }

    //    public MediaSource getConcatenatedMedia() {
    //        return new ConcatenatingMediaSource(concatenateMedia());
    //    }
    //
    //    private MediaSource concatenateMedia() {
    //        MediaSource[] mediaSources = new MediaSource[linkedList.size()];
    //        for (int i = 0; i < linkedList.size(); i++) {
    //            mediaSources[i] = linkedList.get(i).getMediaSource();
    //        }
    //        return new ConcatenatingMediaSource(mediaSources);
    //    }
    //
    //    @Nullable
    //    public static MediaModel getMediaByIndex(int index) {
    //        if (linkedList == null || linkedList.size() <= index) {
    //            return null;
    //        }
    //        return linkedList.get(index);
    //    }

    public static
    @NonNull
    DataSource.Factory buildDataSourceFactory(@NonNull Context context,
            @Nullable DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(context, bandwidthMeter,
                buildHttpDataSourceFactory(context, bandwidthMeter));
    }

    //TODO put user agent in meta or attrs
    public static
    @NonNull
    HttpDataSource.Factory buildHttpDataSourceFactory(@NonNull Context context,
            @NonNull DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(context, "TubiExoPlayer"), bandwidthMeter);
    }

}
