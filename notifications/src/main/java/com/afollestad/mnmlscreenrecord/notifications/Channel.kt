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

import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH

/**
 * Keeps a set of notification channels that the app uses.
 *
 * @author Aidan Follestad (@afollestad)
 */
enum class Channel(
  val id: String,
  val title: String,
  val description: String,
  val importance: Int
) {
  FOREGROUND_SERVICE(
      "foreground_services",
      "Foreground Services",
      "Notifications for persistent controls.",
      IMPORTANCE_DEFAULT
  ),
  VIDEO_RECORDED(
      "video_recorded",
      "After Video Recording",
      "Used to display a notification when you've finished recording a screen capture.",
      IMPORTANCE_HIGH
  )
}
