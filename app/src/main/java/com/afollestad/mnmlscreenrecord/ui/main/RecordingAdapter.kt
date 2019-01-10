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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.view.onDebouncedClick
import com.afollestad.mnmlscreenrecord.common.view.showOrHide
import com.afollestad.mnmlscreenrecord.engine.recordings.Recording
import com.afollestad.mnmlscreenrecord.ui.main.RecordingAdapter.RecordingViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.list_item_recording.view.checkBox
import kotlinx.android.synthetic.main.list_item_recording.view.details
import kotlinx.android.synthetic.main.list_item_recording.view.name
import kotlinx.android.synthetic.main.list_item_recording.view.thumbnail

interface AdapterCallback {
  fun onRecordingClicked(recording: Recording)

  fun onEditModeChange(
    inEditMode: Boolean,
    selection: Int
  )
}

/** @author Aidan Follestad (@afollestad) */
class RecordingAdapter(
  private val callback: AdapterCallback
) : RecyclerView.Adapter<RecordingViewHolder>() {

  private var recordings = mutableListOf<Recording>()
  private val checkedItems = mutableListOf<Int>()

  fun set(recordings: List<Recording>) {
    this.recordings = recordings.toMutableList()
    this.checkedItems.clear()
    notifyDataSetChanged()
  }

  fun enterEditMode(withIndex: Int) {
    checkedItems.add(withIndex)
    notifyItemRangeChanged(0, itemCount)
    callback.onEditModeChange(true, checkedItems.size)
  }

  fun exitEditMode() {
    if (checkedItems.isEmpty()) return
    checkedItems.clear()
    notifyItemRangeChanged(0, itemCount)
    callback.onEditModeChange(false, 0)
  }

  fun isEditMode() = checkedItems.isNotEmpty()

  fun getSelection(): List<Recording> {
    return if (checkedItems.isEmpty()) listOf()
    else List(checkedItems.size) { index ->
      recordings[checkedItems[index]]
    }
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

  private fun itemClicked(position: Int) {
    if (checkedItems.isEmpty()) {
      callback.onRecordingClicked(recordings[position])
      return
    }
    if (checkedItems.contains(position)) {
      checkedItems.remove(position)
    } else {
      checkedItems.add(position)
    }
    callback.onEditModeChange(checkedItems.isNotEmpty(), checkedItems.size)
    if (checkedItems.isEmpty()) {
      notifyItemRangeChanged(0, itemCount)
    } else {
      notifyItemChanged(position)
    }
  }

  class RecordingViewHolder(
    itemView: View,
    private val adapter: RecordingAdapter
  ) : RecyclerView.ViewHolder(itemView) {

    init {
      itemView.onDebouncedClick { adapter.itemClicked(adapterPosition) }
      itemView.setOnLongClickListener {
        adapter.enterEditMode(adapterPosition)
        true
      }
    }

    /** Binds a recording to this list item's view.*/
    @SuppressLint("SetTextI18n")
    fun bind(recording: Recording) {
      Glide.with(itemView.thumbnail)
          .asBitmap()
          .apply(RequestOptions().frame(0))
          .load(recording.toUri())
          .into(itemView.thumbnail)
      itemView.name.text = recording.name
      itemView.details.text = "${recording.sizeString()} – ${recording.timestampString()}"
      itemView.checkBox.showOrHide(adapter.checkedItems.isNotEmpty())
      itemView.checkBox.isChecked = adapter.checkedItems.contains(adapterPosition)
    }
  }
}
