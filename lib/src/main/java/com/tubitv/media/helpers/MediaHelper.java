package com.tubitv.media.helpers;

import android.support.annotation.NonNull;

import com.google.android.exoplayer2.util.Assertions;
import com.tubitv.media.models.MediaModel;

import java.util.Iterator;
import java.util.Stack;

/**
 * Created by stoyan on 6/21/17.
 */

public class MediaHelper implements Iterable<MediaModel> {
    private static Stack<MediaModel> modelStack;
    private static MediaHelper instance;

    public synchronized static MediaHelper create(@NonNull MediaModel... models){
        instance = new MediaHelper(models);
        return instance;
    }

    private MediaHelper(MediaModel[] models) {
        modelStack = new Stack<>();
        for(MediaModel model : models){
            modelStack.push(model);
        }
    }

    public synchronized static MediaHelper getInstance(){
        Assertions.checkNotNull(instance);
        return instance;
    }

    @Override
    public Iterator<MediaModel> iterator() {
        return new Iterator<MediaModel>() {
            @Override
            public boolean hasNext() {
                return modelStack.get(0) != null;
            }

            @Override
            public MediaModel next() {
                return modelStack.pop();
            }
        };
    }

}
