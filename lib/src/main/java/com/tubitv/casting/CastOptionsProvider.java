package com.tubitv.casting;

import android.content.Context;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.List;

/**
 * {@link CastOptions} provider that is set for the cast framework in the {@code AndroidManifest} as
 * a meta attribute
 * <p>
 * Created by stoyan on 11/29/16.
 */
@SuppressWarnings("unused")
public class CastOptionsProvider implements OptionsProvider {

    /**
     * The namespace filter for our cast receiver app
     */
    public static final String TUBI_TV_CAST_NAMESPACE = "urn:x-cast:com.tubitv.channel.data";

    @Override
    public CastOptions getCastOptions(Context appContext) {
        return new CastOptions.Builder()
                .setReceiverApplicationId("AB2C7FED")
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}
