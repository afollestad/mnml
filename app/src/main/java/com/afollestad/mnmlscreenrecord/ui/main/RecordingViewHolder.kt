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
package com.afollestad.mnmlscreenrecord.ui.main

import android.view.View
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.recyclical.ViewHolder
import kotlinx.android.synthetic.main.list_item_recording.view.checkBox
import kotlinx.android.synthetic.main.list_item_recording.view.details
import kotlinx.android.synthetic.main.list_item_recording.view.name
import kotlinx.android.synthetic.main.list_item_recording.view.thumbnail

class RecordingViewHolder(itemView: View) : ViewHolder(itemView) {
  val thumbnail: ImageView = itemView.thumbnail
  val name: TextView = itemView.name
  val details: TextView = itemView.details
  val checkBox: CheckBox = itemView.checkBox
}
