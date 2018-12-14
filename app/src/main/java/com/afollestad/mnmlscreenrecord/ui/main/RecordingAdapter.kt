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

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.engine.loader.Recording
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.list_item_recording.view.details
import kotlinx.android.synthetic.main.list_item_recording.view.name
import kotlinx.android.synthetic.main.list_item_recording.view.thumbnail

class RecordingViewHolder(
  itemView: View,
  private val adapter: RecordingAdapter
) : RecyclerView.ViewHolder(itemView), OnClickListener, OnLongClickListener {

  override fun onClick(v: View) = adapter.itemClicked(adapterPosition, false)

  override fun onLongClick(v: View?): Boolean {
    adapter.itemClicked(adapterPosition, true)
    return false
  }

  init {
    itemView.setOnClickListener(this)
    itemView.setOnLongClickListener(this)
  }

  @SuppressLint("SetTextI18n")
  fun bind(recording: Recording) {
    Glide.with(itemView.thumbnail)
        .asBitmap()
        .apply(RequestOptions().frame(0))
        .load(recording.toUri())
        .into(itemView.thumbnail)
    itemView.name.text = recording.name
    itemView.details.text = "${recording.sizeString()} – ${recording.timestampString()}"
  }
}

/** @author Aidan Follestad (@afollestad) */
class RecordingAdapter(
  private val onClick: (recording: Recording, longClick: Boolean) -> Unit
) : RecyclerView.Adapter<RecordingViewHolder>() {

  private var recordings = mutableListOf<Recording>()

  fun itemClicked(
    position: Int,
    long: Boolean
  ) = onClick(recordings[position], long)

  fun set(recordings: List<Recording>) {
    this.recordings = recordings.toMutableList()
    notifyDataSetChanged()
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): RecordingViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.list_item_recording, parent, false)
    return RecordingViewHolder(view, this)
  }

  override fun getItemCount() = recordings.size

  override fun onBindViewHolder(
    holder: RecordingViewHolder,
    position: Int
  ) = holder.bind(recordings[position])
}
