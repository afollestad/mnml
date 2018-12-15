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
package com.afollestad.mnmlscreenrecord.engine.loader

import android.database.Cursor
import com.afollestad.mnmlscreenrecord.common.misc.friendlyDate
import com.afollestad.mnmlscreenrecord.common.misc.friendlySize
import com.afollestad.mnmlscreenrecord.common.misc.toUri

/** @author Aidan Follestad (@afollestad) */
data class Recording(
  val id: Long,
  val path: String,
  val name: String,
  val timestamp: Long,
  val size: Long
) {
  companion object {
    fun pull(cursor: Cursor): Recording {
      return Recording(
          id = cursor.getLong(cursor.getColumnIndex("_id")),
          path = cursor.getString(cursor.getColumnIndex("_data")),
          name = cursor.getString(cursor.getColumnIndex("title")),
          timestamp = cursor.getLong(cursor.getColumnIndex("date_added")),
          size = cursor.getLong(cursor.getColumnIndex("_size"))
      )
    }
  }

  /**
   * Gets a human-readable string representing the size of this recording in bytes.
   */
  fun sizeString() = size.friendlySize()

  /**
   * Gets a timestamp string representing when this recording was saved.
   */
  fun timestampString() = timestamp.friendlyDate()

  /**
   * Gets the content provider URI for this recording.
   */
  fun toUri() = "$VIDEOS_URI/$id".toUri()
}
