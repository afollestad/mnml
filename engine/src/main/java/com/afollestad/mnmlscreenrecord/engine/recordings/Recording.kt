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

import android.database.Cursor
import android.os.Parcel
import android.os.Parcelable.Creator
import android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
import com.afollestad.mnmlscreenrecord.common.misc.friendlyDate
import com.afollestad.mnmlscreenrecord.common.misc.friendlySize
import com.afollestad.mnmlscreenrecord.notifications.RecordingStub

/** @author Aidan Follestad (@afollestad) */
data class Recording(
  val id: Long,
  val path: String,
  val name: String,
  val timestamp: Long,
  val size: Long
) : RecordingStub {

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

    // Return true if items represent the same entity, e.g. by ID or name
    fun areTheSame(
      left: Recording,
      right: Recording
    ): Boolean {
      return right.id == left.id
    }

    // Return true if all contents in the items are equal
    fun areContentsTheSame(
      left: Recording,
      right: Recording
    ): Boolean {
      return right.path == left.path && right.timestamp == left.timestamp
    }

    @Suppress("unused")
    @JvmField
    val CREATOR = object : Creator<Recording> {
      override fun createFromParcel(parcel: Parcel): Recording {
        return Recording(parcel)
      }

      override fun newArray(size: Int): Array<Recording?> {
        return arrayOfNulls(size)
      }
    }
  }

  constructor(parcel: Parcel) : this(
      parcel.readLong(),
      parcel.readString()!!,
      parcel.readString()!!,
      parcel.readLong(),
      parcel.readLong()
  )

  override fun writeToParcel(
    parcel: Parcel,
    flags: Int
  ) {
    parcel.writeLong(id)
    parcel.writeString(path)
    parcel.writeString(name)
    parcel.writeLong(timestamp)
    parcel.writeLong(size)
  }

  override fun describeContents(): Int {
    return 0
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
  override fun toUri() = EXTERNAL_CONTENT_URI.buildUpon().appendPath(id.toString()).build()!!
}
