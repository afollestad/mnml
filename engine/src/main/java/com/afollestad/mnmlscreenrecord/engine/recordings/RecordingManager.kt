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
package com.afollestad.mnmlscreenrecord.engine.recordings

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
import com.afollestad.rxkprefs.Pref
import java.io.File

/**
 * Handles loading and deleting recordings.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface RecordingManager {

  /**
   * Gets a single recording from its content provider URI.
   */
  fun getRecording(uri: Uri): Recording?

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
@SuppressLint("Recycle")
class RealRecordingManager(
  app: Application,
  recordingsFolderPref: Pref<String>
) : RecordingManager {

  private val contentResolver = app.contentResolver
  private val recordingsBucket = File(recordingsFolderPref.get()).name

  override fun getRecording(uri: Uri): Recording? {
    val cursor = contentResolver.query(
        uri,
        null, // projection
        null, // selection
        null, // selectionArgs
        null // sortOrder
    ) ?: throw IllegalStateException()

    var result: Recording? = null
    if (cursor.moveToFirst()) {
      result = Recording.pull(cursor)
    }

    cursor.close()
    return result
  }

  override fun getRecordings(): List<Recording> {
    val cursor = contentResolver.query(
        EXTERNAL_CONTENT_URI, // uri
        null, // projection
        "bucket_display_name = ?", // selection
        arrayOf(recordingsBucket), // selectionArgs
        "date_added DESC" // sortOrder
    ) ?: throw RecordingManagerException("Unable to access $EXTERNAL_CONTENT_URI :(")

    return mutableListOf<Recording>().also { list ->
      if (cursor.moveToFirst()) {
        do {
          list.add(Recording.pull(cursor))
        } while (cursor.moveToNext())
      }
      cursor.close()
    }
  }

  override fun deleteRecording(recording: Recording) {
    File(recording.path).delete()
    contentResolver.delete(recording.toUri(), null, null)
  }
}

private class RecordingManagerException(msg: String) : Exception(msg)
