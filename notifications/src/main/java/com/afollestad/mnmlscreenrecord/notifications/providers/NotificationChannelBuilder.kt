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
package com.afollestad.mnmlscreenrecord.notifications.providers

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.os.Build.VERSION_CODES.O
import com.afollestad.mnmlscreenrecord.notifications.Channel

/** @author Aidan Follestad (@afollestad) */
interface NotificationChannelBuilder {

  /**
   * Builds a [NotificationChannel] instance. Returns null
   * if the device doesn't support notification channels.
   */
  fun createChannel(channel: Channel): NotificationChannel
}

/**
 * Provides an abstraction layer for creating framework NotificationChannel instances.
 *
 * @author Aidan Follestad (@afollestad)
 */
class RealNotificationChannelBuilder : NotificationChannelBuilder {

  @TargetApi(O)
  override fun createChannel(channel: Channel): NotificationChannel {
    return NotificationChannel(
        channel.id,
        channel.title,
        channel.importance
    ).apply { description = channel.description }
  }
}
