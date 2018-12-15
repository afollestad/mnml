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

import android.annotation.SuppressLint
import android.app.Application
import com.afollestad.mnmlscreenrecord.common.misc.toUri
import com.afollestad.rxkprefs.Pref
import java.io.File

const val VIDEOS_URI = "content://media/external/video/media"

/**
 * Handles loading and deleting recordings.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface RecordingManager {

  /**
   * Gets a list of saved recordings from the set recordings folder.
   */
  fun getRecordings(): List<Recording>

  /**
   * Deletes a recording's file and deletes the entry from the system content provider.
   */
  fun deleteRecording(recording: Recording)
}

/** @author Aidan Follestad (@afollestad) */
class RealRecordingManager(
  app: Application,
  private val recordingsFolderPref: Pref<String>
) : RecordingManager {

  private val contentResolver = app.contentResolver

  @SuppressLint("Recycle")
  override fun getRecordings(): List<Recording> {
    val folder = File(recordingsFolderPref.get())
    val cursor = contentResolver.query(
        VIDEOS_URI.toUri(), // uri
        null, // projection
        "bucket_display_name = ?", // selection
        arrayOf(folder.name), // selectionArgs
        "date_added DESC" // sortOrder
    ) ?: return emptyList()

    cursor.use {
      return mutableListOf<Recording>().also { list ->
        if (it.moveToFirst()) {
          do {
            list.add(Recording.pull(it))
          } while (cursor.moveToNext())
        }
      }
    }
  }

  override fun deleteRecording(recording: Recording) {
    File(recording.path).delete()
    contentResolver.delete(recording.toUri(), null, null)
  }
}
