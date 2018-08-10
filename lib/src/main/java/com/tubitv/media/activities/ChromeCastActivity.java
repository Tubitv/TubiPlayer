package com.tubitv.media.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.tubitv.casting.GoogleServicesHelper;
import com.tubitv.media.utilities.ExoPlayerLogger;

/**
 * Created by allensun on 3/27/18.
 * on Tubitv.com, allengotstuff@gmail.com
 */
public class ChromeCastActivity extends FragmentActivity implements SessionManagerListener, CastStateListener {

    public final static String ENABLE_CHROMECAST = "_enable_chromecast_";
    /**
     * The session with the cast device, managed by the {@link com.google.android.gms.cast.framework.SessionManager}
     */

    private static final String TAG = "ChromeCastActivity";
    private CastSession mCastSession;
    private CastContext mCastContext;
    private SessionManager mSessionManager;
    private boolean isChromeCastEnable = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();

        if (isChromeCastEnable) {
            initCastConnection();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isChromeCastEnable) {
            if (GoogleServicesHelper.available(this)) {
                try {
                    //the following line of code will cause fatal exception on old version of google play service,
                    //must surrounded with try catch
                    if (mSessionManager != null) {
                        mCastSession = mSessionManager.getCurrentCastSession();
                    }
                } catch (Exception exception) {
                    ExoPlayerLogger.e(TAG, exception.getMessage());
                }
            }
            addCastListeners();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isChromeCastEnable) {
            removeCastListeners();
        }
    }

    private void parseIntent() {

        Intent intent = getIntent();
        Bundle bundle = null;

        if (intent != null) {
            bundle = intent.getExtras();
        }

        if (bundle != null && bundle.containsKey(ENABLE_CHROMECAST)) {
            isChromeCastEnable = bundle.getBoolean(ENABLE_CHROMECAST);
        }
    }

    public boolean isChromeCastEnable() {
        return isChromeCastEnable;
    }

    /**
     * init cast connection.
     */
    private void initCastConnection() {
        if (GoogleServicesHelper.available(this)) {
            try {
                //the following line of code will cause fatal exception on old version of google play service,
                //must surrounded with try catch
                mCastContext = CastContext.getSharedInstance(this);
                if (mCastContext != null) {
                    mSessionManager = mCastContext.getSessionManager();
                }
            } catch (Exception exception) {
                ExoPlayerLogger.e("ChromeCastActivity", exception.getMessage());
            }
        }
    }

    /**
     *
     */
    private void addCastListeners() {
        if (GoogleServicesHelper.available(this)) {
            try {
                //the following line of code will cause fatal exception on old version of google play service,
                //must surrounded with try catch
                if (mCastContext != null) {
                    mCastContext.addCastStateListener(this);
                    mSessionManager.addSessionManagerListener(this);
                }
            } catch (Exception exception) {
                ExoPlayerLogger.e("ChromeCastActivity", exception.getMessage());
            }
        }
    }

    protected void removeCastListeners() {
        if (mCastContext != null) {
            mCastContext.removeCastStateListener(this);
        }
        if (mSessionManager != null) {
            mSessionManager.removeSessionManagerListener(this);
        }
        mCastSession = null;
    }

    @Override
    public void onCastStateChanged(int i) {
        ExoPlayerLogger.i("casting", "onCastStateChanged");
    }

    @Override
    public void onSessionStarting(Session session) {
        ExoPlayerLogger.i("casting", "onSessionStarting");
    }

    @Override
    public void onSessionStarted(Session session, String s) {
        ExoPlayerLogger.i("casting", "onSessionStarted");
        startCasting(session);
        finish();
    }

    @Override
    public void onSessionStartFailed(Session session, int i) {
        ExoPlayerLogger.i("casting", "onSessionStartFailed");
    }

    @Override
    public void onSessionEnding(Session session) {
        ExoPlayerLogger.i("casting", "onSessionEnding");
    }

    @Override
    public void onSessionEnded(Session session, int i) {
        ExoPlayerLogger.i("casting", "onSessionEnded");
    }

    @Override
    public void onSessionResuming(Session session, String s) {
        ExoPlayerLogger.i("casting", "onSessionResuming");
    }

    @Override
    public void onSessionResumed(Session session, boolean b) {
        ExoPlayerLogger.i("casting", "onSessionResumed");
    }

    @Override
    public void onSessionResumeFailed(Session session, int i) {
        ExoPlayerLogger.i("casting", "onSessionResumeFailed");
    }

    @Override
    public void onSessionSuspended(Session session, int i) {
        ExoPlayerLogger.i("casting", "onSessionSuspended");
    }

    /**
     * when chromecast connection has established, call this routine; The remote code repository should implement this method.
     *
     * @param session
     */
    protected void startCasting(Session session) {
    }

}

