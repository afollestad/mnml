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
package com.afollestad.mnmlscreenrecord.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_COUNTDOWN
import com.afollestad.mnmlscreenrecord.common.prefs.PrefNames.PREF_RECORDINGS_FOLDER
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.common.view.onProgressChanged
import com.afollestad.rxkprefs.Pref
import kotlinx.android.synthetic.main.dialog_number_selector.view.label
import kotlinx.android.synthetic.main.dialog_number_selector.view.seeker
import org.koin.android.ext.android.inject
import java.io.File

/** @author Aidan Follestad (afollestad) */
class SettingsFragment : PreferenceFragmentCompat() {

  private val countdownPref by inject<Pref<Int>>(name = PREF_COUNTDOWN)
  private val recordingsFolderPref by inject<Pref<String>>(name = PREF_RECORDINGS_FOLDER)

  override fun onCreatePreferences(
    savedInstanceState: Bundle?,
    rootKey: String?
  ) {
    setPreferencesFromResource(R.xml.settings, rootKey)

    val countdownEntry = findPreference(PREF_COUNTDOWN).apply {
      setOnPreferenceClickListener {
        showNumberSelector(
            title.toString(),
            0,
            10,
            countdownPref.get()
        ) { selection -> countdownPref.set(selection) }
        true
      }
    }
    countdownPref.observe()
        .subscribe {
          countdownEntry.summary = resources.getString(
              R.string.setting_countdown_desc, it
          )
        }
        .attachLifecycle(this)

    val recordingsFolderEntry = findPreference(PREF_RECORDINGS_FOLDER).apply {
      setOnPreferenceClickListener {
        val initialFolder = File(recordingsFolderPref.get()).apply {
          mkdirs()
        }
        MaterialDialog(activity!!).show {
          title(text = title.toString())
          folderChooser(
              allowFolderCreation = true,
              initialDirectory = initialFolder
          ) { _, folder ->
            recordingsFolderPref.set(folder.absolutePath)
          }
          positiveButton(R.string.select)
        }
        true
      }
    }
    recordingsFolderPref.observe()
        .subscribe {
          recordingsFolderEntry.summary = resources.getString(
              R.string.setting_recordings_folder_desc, it
          )
        }
        .attachLifecycle(this)
  }

  private fun showNumberSelector(
    title: String,
    min: Int,
    max: Int,
    current: Int,
    onSelection: (Int) -> Unit
  ) {
    val dialog = MaterialDialog(activity!!).show {
      title(text = title)
      message(R.string.setting_countdown_zero_note)
      customView(R.layout.dialog_number_selector)
      positiveButton(android.R.string.ok) {
        val seekBar = getCustomView()!!.seeker
        onSelection(seekBar.progress)
      }
    }

    val customView = dialog.getCustomView() ?: return
    customView.label.text = "$current"
    customView.seeker.min = min
    customView.seeker.max = max
    customView.seeker.progress = current
    customView.seeker.onProgressChanged {
      customView.label.text = "$it"
    }
  }
}
