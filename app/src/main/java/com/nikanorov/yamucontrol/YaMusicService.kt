/*
 * Copyright 2020 Andrey Nikanorov (andrey@nikanorov.com) and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package com.nikanorov.yamucontrol

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.MediaSessionManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat


class YaMusicService : MediaBrowserServiceCompat() {

    private lateinit var session: MediaSessionCompat
    private lateinit var mMediaSessionManager: MediaSessionManager
    private var mController: MediaControllerCompat? = null

    private val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            mController?.transportControls?.play()
        }

        override fun onSkipToQueueItem(queueId: Long) {}

        override fun onSeekTo(position: Long) {}

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            if (mediaId.equals(PLAY_YMUSIC))
                loadPlayer()
        }

        override fun onPause() {
            mController?.transportControls?.pause()
        }

        override fun onStop() {
            mController?.transportControls?.stop()
        }

        override fun onSkipToNext() {
            mController?.transportControls?.skipToNext()

        }

        override fun onSkipToPrevious() {
            mController?.transportControls?.skipToPrevious()

        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            when (action) {
                ACTION_LIKE -> {
                    mController?.transportControls?.apply {
                        setRating(
                            RatingCompat.newHeartRating(
                                mController!!.metadata.getRating(
                                    MediaMetadataCompat.METADATA_KEY_USER_RATING
                                ).hasHeart().not()
                            )
                        )
                    }
                }
                ACTION_BLOCK -> {
                    mController?.transportControls?.sendCustomAction("actionBlocking", Bundle())
                }
            }
        }

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {}
    }


    override fun onCreate() {
        super.onCreate()
        session = MediaSessionCompat(this, "YaMuService")
        session.setCallback(callback)
        session.setFlags(
            MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        )
        sessionToken = session.sessionToken
        loadPlayer()
    }


    fun loadPlayer() {
        mMediaSessionManager =
            getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val listenerComponent = ComponentName(this, NotificationListener::class.java)

        val yaMusicToken =
            getTokenForYaMusic(mMediaSessionManager.getActiveSessions(listenerComponent))
        if (yaMusicToken != null) {
            val sessionToken = MediaSessionCompat.Token.fromToken(yaMusicToken)
            mController = MediaControllerCompat(this, sessionToken)
            mController?.registerCallback(mCallback)
        }


    }

    override fun onDestroy() {
        session.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        val mediaItems: MutableList<MediaItem> = mutableListOf()
        val desc = MediaDescriptionCompat.Builder()
            .setMediaId(PLAY_YMUSIC)
            .setTitle("Yandex Music")
            .setSubtitle("Yandex Music")
            .build()

        mediaItems.add(
            MediaItem(
                desc,
                MediaItem.FLAG_PLAYABLE
            )
        )

        result.sendResult(mediaItems)
    }


    private val mCallback: MediaControllerCompat.Callback =
        object : MediaControllerCompat.Callback() {
            override fun onPlaybackStateChanged(playbackState: PlaybackStateCompat) {
                session.setPlaybackState(getModifiedPlaybackState(playbackState))
                mController?.metadata?.let {
                    session.setMetadata(it)
                }
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat) {
                session.setMetadata(metadata)
            }

            override fun onSessionDestroyed() {
            }

        }

    fun getModifiedPlaybackState(playbackState: PlaybackStateCompat): PlaybackStateCompat? {
        val stateBuilder =
            PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_STOP
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

                )
        val rating =
            mController?.metadata?.getRating(MediaMetadataCompat.METADATA_KEY_USER_RATING)
        val favoriteIcon: Int =
            if (rating != null && rating.hasHeart()) R.drawable.ic_baseline_favorite_24 else R.drawable.ic_baseline_favorite_border_24

        //add some custom action buttons
        stateBuilder.addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                ACTION_BLOCK,
                getString(R.string.action_block),
                R.drawable.ic_baseline_not_interested_24
            )
                .build()
        )

        stateBuilder.addCustomAction(
            PlaybackStateCompat.CustomAction.Builder(
                ACTION_LIKE,
                getString(R.string.action_like),
                favoriteIcon
            )
                .build()
        )

        return stateBuilder.setState(
            playbackState.state,
            playbackState.position, playbackState.playbackSpeed
        ).build()
    }

    private fun getTokenForYaMusic(controllers: Collection<MediaController>): MediaSession.Token? {
        for (controller in controllers) {
            val packageName = controller.packageName
            if (packageName == "ru.yandex.music")
                return controller.sessionToken

        }
        return null
    }

    companion object {
        const val ACTION_LIKE = "LIKE_ACTION"
        const val ACTION_BLOCK = "DISLIKE_ACTION"
        const val PLAY_YMUSIC = "PLAY_YMUSIC"

    }

}