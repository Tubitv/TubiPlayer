package com.tubitv.media.models

import java.io.Serializable

class PlayerVideoResource(type: String, videoUrl: String, laUrl: String, authHeaderKey: String,
                          authHeaderValue: String) : Serializable {
    companion object {
        const val DASH_WIDEVINE = "dash_widevine"
        const val DASH_PLAYREADY = "dash_playready"
        const val HLSV3 = "hlsv3"
    }

    private var mType: String = type
    private var mVideoUrl: String = videoUrl
    private var mLaUrl: String = laUrl // DRM license server url
    private var mAuthHeaderKey: String = authHeaderKey
    private var mAuthHeaderValue: String = authHeaderValue

    fun getType(): String {
        return mType
    }

    fun getVideoUrl(): String {
        return mVideoUrl
    }

    fun getLaUrl(): String {
        return mLaUrl
    }

    fun getAuthHeaderKey(): String {
        return mAuthHeaderKey
    }

    fun getAuthHeaderValue(): String {
        return mAuthHeaderValue
    }
}