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

import android.os.Environment.DIRECTORY_DCIM
import android.os.Environment.getExternalStoragePublicDirectory
import androidx.fragment.app.Fragment
import com.afollestad.assent.Permission.WRITE_EXTERNAL_STORAGE
import com.afollestad.assent.isAllGranted
import com.afollestad.assent.runWithPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.view.onProgressChanged
import com.afollestad.mnmlscreenrecord.ui.settings.sub.SettingsRecordingFragment
import kotlinx.android.synthetic.main.dialog_number_selector.view.label
import kotlinx.android.synthetic.main.dialog_number_selector.view.seeker
import java.io.File

internal fun Fragment.showNumberSelector(
  title: String,
  max: Int,
  current: Int,
  onSelection: (Int) -> Unit
) {
  val context = activity ?: throw IllegalStateException("Oh no!")
  val dialog = MaterialDialog(context).show {
    title(text = title)
    message(R.string.setting_countdown_zero_note)
    customView(R.layout.dialog_number_selector)
    positiveButton(android.R.string.ok) {
      val seekBar = getCustomView()?.seeker ?: return@positiveButton
      onSelection(seekBar.progress)
    }
  }

  val customView = dialog.getCustomView() ?: return
  customView.label.text = "$current"
  customView.seeker.max = max
  customView.seeker.progress = current
  customView.seeker.onProgressChanged {
    customView.label.text = "$it"
  }
}

internal fun SettingsRecordingFragment.showOutputFolderSelector(title: String) {
  if (!isAllGranted(WRITE_EXTERNAL_STORAGE)) {
    runWithPermissions(WRITE_EXTERNAL_STORAGE) {
      showOutputFolderSelector(title)
    }
    return
  }

  val context = activity ?: return
  var initialFolder = File(recordingsFolderPref.get())
  if (!initialFolder.canWrite()) {
    val dcim = getExternalStoragePublicDirectory(DIRECTORY_DCIM)
    initialFolder = File(dcim, "MNML Recordings")
  }
  initialFolder.mkdirs()

  MaterialDialog(context).show {
    title(text = title)
    folderChooser(
        allowFolderCreation = true,
        initialDirectory = initialFolder
    ) { _, folder ->
      recordingsFolderPref.set(folder.absolutePath)
    }
    positiveButton(R.string.select)
  }
}

internal fun Int.bitRateString(): String {
  return if (this >= 1_000_000) {
    "${this / 1_000_000}mbps"
  } else {
    "${this / 1_000}kbps"
  }
}
