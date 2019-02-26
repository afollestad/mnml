/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.mnmlscreenrecord.notifications

import android.annotation.TargetApi
import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_STREAM
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.media.MediaMetadataRetriever
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.afollestad.mnmlscreenrecord.common.misc.setViewVisibility
import com.afollestad.mnmlscreenrecord.notifications.providers.NotificationChannelBuilder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber.d as log

const val RECORD_ACTION = "com.afollestad.mnmlscreenrecord.service.START_RECORDING"
const val STOP_ACTION = "com.afollestad.mnmlscreenrecord.service.STOP_RECORDING"
const val DELETE_ACTION = "com.afollestad.mnmlscreenrecord.service.DELETE_RECORDING"
const val EXIT_ACTION = "com.afollestad.mnmlscreenrecord.service.EXIT_FOREGROUND"

const val EXTRA_STOP_FOREGROUND = "stop_foreground"
const val EXTRA_RECORDING = "recording"

/**
 * Manages all-things-notifications in the app.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface Notifications {

  /**
   * Marks whether the app is visible - when visible, post-record notifications aren't shown.
   */
  fun setIsAppOpen(open: Boolean)

  /**
   * Returns the last value given to [setIsAppOpen], or the default value of false.
   */
  fun isAppOpen(): Boolean

  /**
   * Creates all notification channels within the [Channel] enum.
   */
  fun createChannels()

  /**
   * Creates the persistent notification used when the background service is foregrounded.
   */
  fun createWidgetServiceNotification(
    mainActivity: Class<*>,
    backgroundService: Class<*>,
    action: String,
    isRecording: Boolean
  ): Notification

  /**
   * Shows a notification for a screen recording that was just completed.
   */
  fun showPostRecordNotification(
    recording: RecordingStub,
    backgroundService: Class<*>
  )

  /**
   * Cancels any visible notification created by [showPostRecordNotification].
   */
  fun cancelPostRecordNotification()
}

/** @author Aidan Follestad (@afollestad) */
class RealNotifications(
  private val app: Application,
  private val channelBuilder: NotificationChannelBuilder,
  private val stockManager: NotificationManager
) : Notifications {

  companion object {
    private const val ID_POST_RECORD = 33

    private const val MAIN_ACTIVITY_REQUEST = 90
    private const val RECORD_REQUEST = 91
    private const val STOP_REQUEST = 92
    private const val EXIT_REQUEST = 93

    private const val VIEW_REQUEST = 94
    private const val SHARE_REQUEST = 95
    private const val DELETE_REQUEST = 96
  }

  private var currentIsAppOpen: Boolean = false

  override fun setIsAppOpen(open: Boolean) {
    log("Is app open? $open")
    currentIsAppOpen = open
  }

  override fun isAppOpen(): Boolean = currentIsAppOpen

  @TargetApi(O)
  override fun createChannels() {
    if (SDK_INT < O) {
      return
    }
    Channel.values()
        .map { channelBuilder.createChannel(it) }
        .forEach {
          log("Created notification channel ${it.id}")
          stockManager.createNotificationChannel(it)
        }
  }

  override fun createWidgetServiceNotification(
    mainActivity: Class<*>,
    backgroundService: Class<*>,
    action: String,
    isRecording: Boolean
  ): Notification {
    val channel = Channel.FOREGROUND_SERVICE.id
    val contentText = if (isRecording) {
      R.string.recording_notification_text
    } else {
      R.string.widget_service_notification_text
    }

    val appIntent = PendingIntent.getActivity(
        app,
        MAIN_ACTIVITY_REQUEST,
        Intent(app, mainActivity).addFlags(FLAG_ACTIVITY_NEW_TASK),
        FLAG_CANCEL_CURRENT
    )
    val recordIntent = PendingIntent.getService(
        app,
        RECORD_REQUEST,
        Intent(app, backgroundService).setAction(RECORD_ACTION),
        FLAG_CANCEL_CURRENT
    )

    val remoteViews = RemoteViews(
        "com.afollestad.mnmlscreenrecord",
        R.layout.foreground_notification
    ).apply {
      setTextViewText(R.id.notification_text, app.getString(contentText))
    }

    val remoteViewsExpanded = RemoteViews(
        "com.afollestad.mnmlscreenrecord",
        R.layout.foreground_notification_expanded
    ).apply {
      setTextViewText(R.id.notification_text, app.getString(contentText))
      setOnClickPendingIntent(R.id.action_record, recordIntent)
      setViewVisibility(R.id.action_record, !isRecording)
      setOnClickPendingIntent(R.id.action_stop, broadcast(STOP_REQUEST, STOP_ACTION))
      setViewVisibility(R.id.action_stop, isRecording)
      setOnClickPendingIntent(R.id.action_exit, broadcast(EXIT_REQUEST, EXIT_ACTION))
    }

    return NotificationCompat.Builder(app, channel)
        .apply {
          setSmallIcon(R.drawable.ic_record_32dp)
          setContentIntent(appIntent)
          setStyle(NotificationCompat.DecoratedCustomViewStyle())
          setCustomContentView(remoteViews)
          setCustomBigContentView(remoteViewsExpanded)
        }
        .build()
  }

  @ExperimentalCoroutinesApi
  override fun showPostRecordNotification(
    recording: RecordingStub,
    backgroundService: Class<*>
  ) {
    if (isAppOpen()) {
      log("App is open, won't create a post-record notification.")
      return
    }

    log("Creating post-record notification for: $recording")
    val channel = Channel.VIDEO_RECORDED.id

    val viewPendingIntent = PendingIntent.getActivity(
        app,
        VIEW_REQUEST,
        Intent(ACTION_VIEW).apply {
          setDataAndType(recording.toUri(), "video/*")
        },
        FLAG_CANCEL_CURRENT
    )
    val shareUri = recording.toUri()
    val sharePendingIntent = PendingIntent.getActivity(
        app,
        SHARE_REQUEST,
        Intent(ACTION_SEND).apply {
          setDataAndType(shareUri, "video/*")
          putExtra(EXTRA_STREAM, shareUri)
        },
        FLAG_CANCEL_CURRENT
    )
    val deletePendingIntent = PendingIntent.getService(
        app,
        DELETE_REQUEST,
        Intent(app, backgroundService).apply {
          action = DELETE_ACTION
          putExtra(EXTRA_RECORDING, recording)
        },
        FLAG_CANCEL_CURRENT
    )

    val notification = NotificationCompat.Builder(app, channel)
        .setSmallIcon(R.drawable.ic_video_32dp)
        .setContentTitle(app.getString(R.string.app_name))
        .setContentText(app.getString(R.string.video_recorded_notification_text))
        .setContentIntent(viewPendingIntent)
        .setAutoCancel(true)
        .addAction(
            R.drawable.ic_share_32dp,
            app.getString(R.string.share),
            sharePendingIntent
        )
        .addAction(
            R.drawable.ic_delete_32dp,
            app.getString(R.string.delete),
            deletePendingIntent
        )

    stockManager.notify(ID_POST_RECORD, notification.build())

    GlobalScope.launch(Unconfined) {
      delay(250)
      val bitmap = withContext(IO) {
        val uri = recording.toUri()
        val retriever = MediaMetadataRetriever()
        try {
          retriever.setDataSource(app, uri)
          retriever.frameAtTime
        } catch (e: Exception) {
          log("Failed to retrieve a preview frame for $uri!")
          null
        }
      }
      val updatedNotification = notification
          .apply {
            setLargeIcon(bitmap)
            setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
            )
          }
          .build()
      stockManager.notify(ID_POST_RECORD, updatedNotification)
    }
  }

  override fun cancelPostRecordNotification() = stockManager.cancel(ID_POST_RECORD)

  private fun broadcast(
    code: Int,
    action: String
  ) = PendingIntent.getBroadcast(
      app,
      code,
      Intent(action),
      FLAG_CANCEL_CURRENT
  )
}
