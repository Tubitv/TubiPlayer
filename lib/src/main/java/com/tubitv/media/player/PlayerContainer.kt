package com.tubitv.media.player

import android.content.Context
import android.os.Handler
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto
import com.google.android.exoplayer2.drm.FrameworkMediaDrm
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.tubitv.media.fsm.state_machine.FsmPlayer
import com.tubitv.media.helpers.MediaHelper
import com.tubitv.media.models.MediaModel
import com.tubitv.media.models.PlayerVideoResource
import com.tubitv.media.utilities.EventLogger
import com.tubitv.media.utilities.ExoPlayerLogger
import com.tubitv.media.utilities.PlayerLog
import java.lang.IllegalStateException
import java.lang.ref.WeakReference

/**
 * Central Player class that takes care of all player instance related logic
 */
class PlayerContainer {

    companion object {
        private val TAG = PlayerContainer::class.simpleName

        private val sBandwidthMeter = DefaultBandwidthMeter()
        private val sVideoTrackSelectionFactory = AdaptiveTrackSelection.Factory(sBandwidthMeter)
        private val sTrackSelector = DefaultTrackSelector(sVideoTrackSelectionFactory)
        // Override event logger function to have DRM fallback
        private val sEventLogger = object : EventLogger(sTrackSelector) {
            override fun onPlayerError(eventTime: AnalyticsListener.EventTime, e: ExoPlaybackException) {
                super.onPlayerError(eventTime, e)
                // If this is DRM, we'll just retry next one
                PlayerContainer.repreparePlayerForNextVideoResource()
            }
        }

        private var sEventListener: Player.EventListener? = null // Custom listener

        // Data source factory to be used for video chunk
        private var sDataSourceFactory: DataSource.Factory? = null
        // Data source factory without bandwidth meter
        private var sDataSourceFactoryWithoutBandwidthMeter: DataSource.Factory? = null
        // Data source factory to be used for DRM
        private var sHttpDataSourceFactory: HttpDataSource.Factory? = null

        private var sPlayReadyCallback: HttpMediaDrmCallback? = null
        private var sWidevineCallback: HttpMediaDrmCallback? = null
        private var sPlayReadyDrmSessionManager: DefaultDrmSessionManager<FrameworkMediaCrypto>? = null
        private var sWidevineDrmSessionManager: DefaultDrmSessionManager<FrameworkMediaCrypto>? = null

        private var sPlayer: SimpleExoPlayer? = null

        private var sContextRef: WeakReference<Context>? = null
        private var sHandlerRef: WeakReference<Handler>? = null
        private var sFsmPlayerRef: WeakReference<FsmPlayer>? = null

        private var sMediaModel: MediaModel? = null
        private var sResetPosition = false
        private var sResetState = false
        private var sIsForAds = false

        private var sIsDrmRunning = false

        private var sPlayerLog: PlayerLog? = null

        /**
         * Initialize player instance and prepare for video
         *
         * @param context Activity Context to be used for player instance
         * @param handler UI thread handler to be used for player
         * @param mediaModel MediaModel for video
         */
        @JvmStatic
        fun initialize(context: Context, handler: Handler, mediaModel: MediaModel) {
            initialize(context, handler)
            preparePlayer(mediaModel)
        }

        /**
         * Set FsmPlayer instance, which will be used for video resource fallback
         *
         * @param fsmPlayer FsmPlayer instance
         */
        @JvmStatic
        fun setFsmPlayer(fsmPlayer: FsmPlayer) {
            sFsmPlayerRef = WeakReference(fsmPlayer)
        }

        @JvmStatic
        fun setPlayerLog(playerLog: PlayerLog) {
            sPlayerLog = playerLog
        }

        /**
         * Prepare player instance for MediaModel
         *
         * @param mediaModel MediaModel instance
         */
        @JvmStatic
        fun preparePlayer(mediaModel: MediaModel) {
            preparePlayer(mediaModel, false, false, false)
        }

        /**
         * Prepare player instance for MediaModel
         *
         * @param mediaModel MediaModel instance
         * @param resetPosition True if video position need to be reset
         * @param resetState True if player state need to be reset
         * @param isForAds True if this is preparing for ads
         */
        @JvmStatic
        fun preparePlayer(mediaModel: MediaModel,
                          resetPosition: Boolean,
                          resetState: Boolean,
                          isForAds: Boolean) {
            // Build MediaSource per context if needed
            mediaModel.buildMediaSourceIfNeeded(
                    sContextRef?.get(),
                    sHandlerRef?.get(),
                    sBandwidthMeter,
                    sEventLogger
            )
            sMediaModel = mediaModel
            sResetPosition = resetPosition
            sResetState = resetState
            sIsForAds = isForAds

            initializePlayer(mediaModel, isForAds)

            // Currently ads have separate tracking
            if (!isForAds) {
                sPlayer?.addAnalyticsListener(sEventLogger)
                sPlayer?.addMetadataOutput(sEventLogger)

                if (sEventListener != null) {
                    sPlayer?.addListener(sEventListener)
                }
            }

            sPlayer?.prepare(mediaModel.mediaSource, resetPosition, resetState)
        }

        /**
         * Reprepare player with previous setup, used for DRM fallback
         * If current content is already non-DRM, do nothing
         */
        @JvmStatic
        fun repreparePlayerForNextVideoResource() {
            ExoPlayerLogger.d(TAG, "start repreparePlayerForNextVideoResource")
            // If currently not DRM, no need to try next one
            if (!sIsDrmRunning) {
                return
            }

            releasePlayer()
            val mediaModel = sMediaModel
            if (mediaModel != null) {
                mediaModel.useNextVideoResource()
                preparePlayer(mediaModel, sResetPosition, sResetState, sIsForAds)
            }

            val fsmPlayer = sFsmPlayerRef?.get()

            if (fsmPlayer == null) {
                sPlayerLog?.e(IllegalStateException(), "fsmPlayer should not be null")
            }

            fsmPlayer?.currentState?.performWorkAndUpdatePlayerUI(fsmPlayer)
        }

        /**
         * Add extra EventListener for non-ads content
         */
        @JvmStatic
        fun addEventListener(eventListener: Player.EventListener) {
            sEventListener = eventListener
            sPlayer?.addListener(sEventListener)
        }

        /**
         * Get current player instance
         *
         * @return Current player instance
         */
        @JvmStatic
        fun getPlayer(): SimpleExoPlayer? {
            return sPlayer
        }

        /**
         * Release current player instance
         */
        @JvmStatic
        fun releasePlayer() {
            sPlayer?.release()

            sPlayReadyCallback = null
            sWidevineCallback = null
            sPlayReadyDrmSessionManager = null
            sWidevineDrmSessionManager = null
        }

        /**
         * Clean up when video playing is done
         */
        @JvmStatic
        fun cleanUp() {
            sDataSourceFactory = null
            sDataSourceFactoryWithoutBandwidthMeter = null
            sHttpDataSourceFactory = null
        }

        private fun initialize(context: Context, handler: Handler) {
            sContextRef = WeakReference(context)
            sHandlerRef = WeakReference(handler)

            sHttpDataSourceFactory = MediaHelper.buildHttpDataSourceFactory(context, sBandwidthMeter)
        }

        private fun initializePlayer(mediaModel: MediaModel, isForAds: Boolean) {
            if (isForAds) {
                initializeRegularPlayer()
            } else {
                val videoResource = mediaModel.videoResource

                if (videoResource != null) {
                    when (videoResource.getType()) {
                        PlayerVideoResource.DASH_WIDEVINE -> initializeWidevinePlayer(videoResource)
                        PlayerVideoResource.DASH_PLAYREADY -> initializePlayReadyPlayer(videoResource)
                        else -> initializeRegularPlayer()
                    }
                } else {
                    initializeRegularPlayer()
                }
            }
        }

        private fun initializeWidevinePlayer(videoResource: PlayerVideoResource) {
            ExoPlayerLogger.d(TAG, "initialize WidevinePlayer")
            sIsDrmRunning = true

            sWidevineCallback = HttpMediaDrmCallback(videoResource.getLaUrl(), sHttpDataSourceFactory)

            val keyRequestProperties = HashMap<String, String>()
            keyRequestProperties[videoResource.getAuthHeaderKey()] = videoResource.getAuthHeaderValue()

            sWidevineCallback?.setKeyRequestProperty(videoResource.getAuthHeaderKey(), videoResource.getAuthHeaderValue())

            sWidevineDrmSessionManager = DefaultDrmSessionManager(C.WIDEVINE_UUID,
                    FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID), sWidevineCallback, keyRequestProperties)
            if (sContextRef != null && sContextRef?.get() != null) {
                sPlayer =
                        ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(sContextRef?.get()), sTrackSelector, sWidevineDrmSessionManager)
            }
        }

        private fun initializePlayReadyPlayer(videoResource: PlayerVideoResource) {
            ExoPlayerLogger.d(TAG, "initialize PlayReadyPlayer")
            sIsDrmRunning = true

            sPlayReadyCallback = HttpMediaDrmCallback(videoResource.getLaUrl(), sHttpDataSourceFactory)

            val keyRequestProperties = HashMap<String, String>()
            keyRequestProperties[videoResource.getAuthHeaderKey()] = videoResource.getAuthHeaderValue()

            sPlayReadyCallback?.setKeyRequestProperty(videoResource.getAuthHeaderKey(), videoResource.getAuthHeaderValue())

            sPlayReadyDrmSessionManager = DefaultDrmSessionManager(C.PLAYREADY_UUID,
                    FrameworkMediaDrm.newInstance(C.PLAYREADY_UUID), sPlayReadyCallback, keyRequestProperties)

            if (sContextRef != null && sContextRef?.get() != null) {
                sPlayer =
                        ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(sContextRef?.get()), sTrackSelector, sPlayReadyDrmSessionManager)
            }
        }

        private fun initializeRegularPlayer() {
            ExoPlayerLogger.d(TAG, "initialize RegularPlayer")

            sIsDrmRunning = false

            if (sContextRef != null && sContextRef?.get() != null) {
                sPlayer = ExoPlayerFactory.newSimpleInstance(sContextRef?.get(), sTrackSelector)
            }
        }
    }
}