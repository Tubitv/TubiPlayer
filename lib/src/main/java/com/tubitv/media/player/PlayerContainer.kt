package com.tubitv.media.player

import android.content.Context
import android.os.Handler
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto
import com.google.android.exoplayer2.drm.FrameworkMediaDrm
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.tubitv.media.helpers.MediaHelper
import com.tubitv.media.models.MediaModel
import com.tubitv.media.utilities.EventLogger
import java.lang.ref.WeakReference

class PlayerContainer {

    companion object {
        private const val PLAYREADY_LICENSE_URL =
                "http://haofei-drm-test.adrise.tv/playready.php"
        private const val WIDEVINE_LICENSE_URL = "https://wv-keyos.licensekeyserver.com"

        private val sBandwidthMeter = DefaultBandwidthMeter()
        private val sVideoTrackSelectionFactory = AdaptiveTrackSelection.Factory(sBandwidthMeter)
        private val sTrackSelector = DefaultTrackSelector(sVideoTrackSelectionFactory)
        private val sEventLogger = EventLogger(sTrackSelector)

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

        @JvmStatic
        fun initialize(context: Context, handler: Handler) {
            sContextRef = WeakReference(context)
            sHandlerRef = WeakReference(handler)

            sDataSourceFactory = MediaHelper.buildDataSourceFactory(context, sBandwidthMeter)
            sDataSourceFactoryWithoutBandwidthMeter = MediaHelper.buildDataSourceFactory(context, null)
            sHttpDataSourceFactory = MediaHelper.buildHttpDataSourceFactory(context, sBandwidthMeter)
        }

        @JvmStatic
        fun initialize(context: Context, handler: Handler, mediaModel: MediaModel) {
            initialize(context, handler)
            preparePlayer(mediaModel)
        }

        @JvmStatic
        fun preparePlayer(mediaModel: MediaModel) {
            preparePlayer(mediaModel, false, false, false)
        }

        @JvmStatic
        fun preparePlayer(mediaModel: MediaModel,
                          resetPosition: Boolean,
                          resetState: Boolean,
                          isForAds: Boolean) {
            // Build MediaSource based on created data source factory per context
            mediaModel.buildMediaSourceIfNeeded(
                    sHandlerRef?.get(),
                    sDataSourceFactory,
                    sDataSourceFactoryWithoutBandwidthMeter,
                    sEventLogger
            )

            if (!mediaModel.isDRM) {
                initializeRegularPlayer()
            } else { // Initialize DRM player accordingly

            }

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

        @JvmStatic
        fun addEventListener(eventListener: Player.EventListener) {
            sEventListener = eventListener
            sPlayer?.addListener(sEventListener)
        }

        private fun initializeWidvinePlayer() {
            sWidevineCallback = HttpMediaDrmCallback(WIDEVINE_LICENSE_URL, sHttpDataSourceFactory)

            val customData =
                    "PEtleU9TQXV0aGVudGljYXRpb25YTUw+PERhdGE+PEdlbmVyYXRpb25UaW1lPjIwMTgtMDktMjEgMjE6MTM6MDkuNjI3PC9HZW5lcmF0aW9uVGltZT48RXhwaXJhdGlvblRpbWU+MjAxOC0wOS0yMiAyMToxMzowOS42Mjc8L0V4cGlyYXRpb25UaW1lPjxVbmlxdWVJZD44NzU0NDI1Yy1iMGM3LTQxN2MtOTdkNS03ZWNmM2I1YjcyYjE8L1VuaXF1ZUlkPjxSU0FQdWJLZXlJZD45YzZiZjE3M2M4OTM3MmEzOGRkZWUwMWQxMjVjZThhNTwvUlNBUHViS2V5SWQ+PFdpZGV2aW5lUG9saWN5IGZsX0NhblBsYXk9InRydWUiIC8+PFdpZGV2aW5lQ29udGVudEtleVNwZWMgVHJhY2tUeXBlPSJIRCI+PFNlY3VyaXR5TGV2ZWw+MTwvU2VjdXJpdHlMZXZlbD48L1dpZGV2aW5lQ29udGVudEtleVNwZWM+PExpY2Vuc2UgdHlwZT0ic2ltcGxlIiAvPjxGYWlyUGxheVBvbGljeSAvPjwvRGF0YT48U2lnbmF0dXJlPmo1UzJTUFRDd3JZRkY2MmtCNFdRMWxYRDZMaXlYY3VkeTlhQlJLKzlWRzZBcEdFTkR6NjVlTzZ6TTB5b3FEdzdmWTdnT1AxV2tNancvcklWczluQ002dkFyZ0lrYmdRZTNBY3Q3KzJIY1BzN1l4elNaSzk1SFlYVHpLdEYyR3E1c3poWU1pTlF2VVJqamtMV2NBVVc2VXhQRGkxY3hLVWh0Z0Q5Wmtwc2Q5aVVWMTlFQURXMmtPNWVmQ1g3Vm5WNXBoQjN2SU0wbUdENjBESUVpSzNuY1RMa3hRZTI1bEVZc3RLYzdYaUFVNkZyOHY1YUtCOVAxdWY0UXlETFVlNEkwQldmUlhES04yMXZTQ1dYRGh5VlpUb0UvZWh4VG15RTRvNGM1emQ2R1BnNjJPNjBzR3h6NmNDdGIrY0x2TGFuaUxWY2xwR2lnVUMvc1RaUmFEdXlodz09PC9TaWduYXR1cmU+PC9LZXlPU0F1dGhlbnRpY2F0aW9uWE1MPg=="

            val keyRequestProperties = HashMap<String, String>()
            keyRequestProperties.put("customdata", customData)

            sWidevineCallback?.setKeyRequestProperty("customdata", customData)

            //            DefaultDrmSessionManager.newWidevineInstance(mWidevineCallback, null)

            sWidevineDrmSessionManager = DefaultDrmSessionManager(C.WIDEVINE_UUID,
                    FrameworkMediaDrm.newInstance(C.WIDEVINE_UUID), sWidevineCallback, keyRequestProperties)
            if (sContextRef != null && sContextRef?.get() != null) {
                sPlayer =
                        ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(sContextRef?.get()), sTrackSelector, sWidevineDrmSessionManager)
            }
        }

        private fun initializePlayReadyPlayer() {
            sPlayReadyCallback = HttpMediaDrmCallback(PLAYREADY_LICENSE_URL, sHttpDataSourceFactory)

            val customData =
                    "PEtleU9TQXV0aGVudGljYXRpb25YTUw+PERhdGE+PEdlbmVyYXRpb25UaW1lPjIwMTgtMDktMjEgMjE6MTM6MDkuNjI3PC9HZW5lcmF0aW9uVGltZT48RXhwaXJhdGlvblRpbWU+MjAxOC0wOS0yMiAyMToxMzowOS42Mjc8L0V4cGlyYXRpb25UaW1lPjxVbmlxdWVJZD44NzU0NDI1Yy1iMGM3LTQxN2MtOTdkNS03ZWNmM2I1YjcyYjE8L1VuaXF1ZUlkPjxSU0FQdWJLZXlJZD45YzZiZjE3M2M4OTM3MmEzOGRkZWUwMWQxMjVjZThhNTwvUlNBUHViS2V5SWQ+PFdpZGV2aW5lUG9saWN5IGZsX0NhblBsYXk9InRydWUiIC8+PFdpZGV2aW5lQ29udGVudEtleVNwZWMgVHJhY2tUeXBlPSJIRCI+PFNlY3VyaXR5TGV2ZWw+MTwvU2VjdXJpdHlMZXZlbD48L1dpZGV2aW5lQ29udGVudEtleVNwZWM+PExpY2Vuc2UgdHlwZT0ic2ltcGxlIiAvPjxGYWlyUGxheVBvbGljeSAvPjwvRGF0YT48U2lnbmF0dXJlPmo1UzJTUFRDd3JZRkY2MmtCNFdRMWxYRDZMaXlYY3VkeTlhQlJLKzlWRzZBcEdFTkR6NjVlTzZ6TTB5b3FEdzdmWTdnT1AxV2tNancvcklWczluQ002dkFyZ0lrYmdRZTNBY3Q3KzJIY1BzN1l4elNaSzk1SFlYVHpLdEYyR3E1c3poWU1pTlF2VVJqamtMV2NBVVc2VXhQRGkxY3hLVWh0Z0Q5Wmtwc2Q5aVVWMTlFQURXMmtPNWVmQ1g3Vm5WNXBoQjN2SU0wbUdENjBESUVpSzNuY1RMa3hRZTI1bEVZc3RLYzdYaUFVNkZyOHY1YUtCOVAxdWY0UXlETFVlNEkwQldmUlhES04yMXZTQ1dYRGh5VlpUb0UvZWh4VG15RTRvNGM1emQ2R1BnNjJPNjBzR3h6NmNDdGIrY0x2TGFuaUxWY2xwR2lnVUMvc1RaUmFEdXlodz09PC9TaWduYXR1cmU+PC9LZXlPU0F1dGhlbnRpY2F0aW9uWE1MPg=="

            val keyRequestProperties = HashMap<String, String>()
            keyRequestProperties.put("customdata", customData)

            sPlayReadyCallback?.setKeyRequestProperty("customdata", customData)

            sPlayReadyDrmSessionManager = DefaultDrmSessionManager(C.PLAYREADY_UUID,
                    FrameworkMediaDrm.newInstance(C.PLAYREADY_UUID), sPlayReadyCallback, keyRequestProperties)


            if (sContextRef != null && sContextRef?.get() != null) {
                sPlayer =
                        ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(sContextRef?.get()), sTrackSelector, sPlayReadyDrmSessionManager)
            }
        }

        private fun initializeRegularPlayer() {
            if (sContextRef != null && sContextRef?.get() != null) {
                sPlayer = ExoPlayerFactory.newSimpleInstance(sContextRef?.get(), sTrackSelector)
            }
        }

        @JvmStatic
        fun getPlayer(): SimpleExoPlayer? {
            return sPlayer
        }

        @JvmStatic
        fun releasePlayer() {
            sPlayer?.release()

            sPlayReadyCallback = null
            sWidevineCallback = null
            sPlayReadyDrmSessionManager = null
            sWidevineDrmSessionManager = null
        }

        @JvmStatic
        fun cleanUp() {
            sDataSourceFactory = null
            sDataSourceFactoryWithoutBandwidthMeter = null
            sHttpDataSourceFactory = null
        }
    }
}