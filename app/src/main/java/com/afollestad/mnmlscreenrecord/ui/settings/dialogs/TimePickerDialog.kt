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
package com.afollestad.mnmlscreenrecord.ui.settings.dialogs

import android.app.Dialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.theming.splitTime
import com.afollestad.rxkprefs.Pref
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

interface TimeCallback {
  fun onTimeSelected(
    key: String,
    hour: Int,
    minute: Int
  )
}

/** @author Aidan Follestad (@afollestad) */
class TimePickerDialog : DialogFragment() {

  companion object {
    private const val TAG = "[TimePickerDialog]"
    private const val KEY_ID = "id"
    private const val KEY_TITLE = "title"

    fun <T> show(
      fragment: T,
      id: String,
      title: CharSequence
    ) where T : Fragment, T : TimeCallback {
      val dialog = TimePickerDialog().apply {
        arguments = Bundle().apply {
          putString(KEY_ID, id)
          putString(KEY_TITLE, title.toString())
        }
      }
      dialog.show(fragment.childFragmentManager, TAG)
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context = activity ?: blowUp()
    val args = arguments ?: blowUp()
    val id = args.getString(KEY_ID) ?: blowUp()

    val dialog = MaterialDialog(context)
        .title(text = args.getString(KEY_TITLE))
        .customView(R.layout.dialog_clock)
        .noAutoDismiss()
        .positiveButton(android.R.string.ok) {
          val callback = parentFragment as? TimeCallback ?: blowUp()
          val customView = it.getCustomView()
          val clock = customView.findViewById<TimePicker>(R.id.clock)
          callback.onTimeSelected(id, clock.hour, clock.minute)
          dismiss()
        }
        .onDismiss { dismiss() }
        .onCancel { dismiss() }

    val customView = dialog.getCustomView()
    val clock = customView.findViewById<TimePicker>(R.id.clock)
    val pref by inject<Pref<String>>(named(id))
    val time = pref.get()
        .splitTime()
    clock.hour = time[0]
    clock.minute = time[1]

    return dialog
  }

  private fun <T> blowUp(): T {
    throw IllegalStateException("Oh no!")
  }
}
