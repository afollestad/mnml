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

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.assent.Permission.WRITE_EXTERNAL_STORAGE
import com.afollestad.assent.askForPermissions
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.misc.toUri
import com.afollestad.mnmlscreenrecord.common.misc.toast
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.common.view.onDebouncedClick
import com.afollestad.mnmlscreenrecord.common.view.onScroll
import com.afollestad.mnmlscreenrecord.engine.recordings.Recording
import com.afollestad.mnmlscreenrecord.engine.service.BackgroundService.Companion.PERMISSION_DENIED
import com.afollestad.mnmlscreenrecord.theming.DarkModeSwitchActivity
import com.afollestad.mnmlscreenrecord.ui.about.AboutDialog
import com.afollestad.mnmlscreenrecord.ui.settings.SettingsActivity
import com.afollestad.mnmlscreenrecord.views.asBackgroundTint
import com.afollestad.mnmlscreenrecord.views.asEnabled
import com.afollestad.mnmlscreenrecord.views.asIcon
import com.afollestad.mnmlscreenrecord.views.asText
import com.afollestad.mnmlscreenrecord.views.asVisibility
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.include_appbar.toolbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlinx.android.synthetic.main.activity_main.empty_view as emptyView
import kotlinx.android.synthetic.main.include_appbar.app_toolbar as appToolbar

/** @author Aidan Follestad (afollestad) */
class MainActivity : DarkModeSwitchActivity() {
  companion object {
    private const val DRAW_OVER_OTHER_APP_PERMISSION = 68
    private const val STORAGE_PERMISSION = 64
  }

  private val viewModel by viewModel<MainViewModel>()

  private lateinit var adapter: RecordingAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setupToolbar()
    setupGrid()

    fab.onDebouncedClick { viewModel.fabClicked() }

    lifecycle.addObserver(viewModel)

    viewModel.onRecordings()
        .observe(this, Observer { adapter.set(it) })
    viewModel.onEmptyViewVisibility()
        .asVisibility(this, emptyView)
    viewModel.onFabColorRes()
        .asBackgroundTint(this, fab)
    viewModel.onFabIconRes()
        .asIcon(this, fab)
    viewModel.onFabTextRes()
        .asText(this, fab)
    viewModel.onFabEnabled()
        .asEnabled(this, fab)

    viewModel.onNeedOverlayPermission()
        .subscribe {
          MaterialDialog(this).show {
            title(R.string.overlay_permission_prompt)
            message(R.string.overlay_permission_prompt_desc)
            cancelOnTouchOutside(false)
            positiveButton(R.string.okay) { openOverlaySettings() }
          }
        }
        .attachLifecycle(this)

    viewModel.onNeedStoragePermission()
        .subscribe {
          MaterialDialog(this).show {
            title(R.string.storage_permission_prompt)
            message(R.string.storage_permission_prompt_desc)
            cancelOnTouchOutside(false)
            positiveButton(R.string.okay) { askForStoragePermission() }
          }
        }
        .attachLifecycle(this)
  }

  private fun openOverlaySettings() {
    val intent = Intent(
        ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:$packageName".toUri()
    )
    startActivityForResult(
        intent,
        DRAW_OVER_OTHER_APP_PERMISSION
    )
  }

  private fun askForStoragePermission() {
    askForPermissions(WRITE_EXTERNAL_STORAGE, requestCode = STORAGE_PERMISSION) { res ->
      if (!res.isAllGranted(WRITE_EXTERNAL_STORAGE)) {
        sendBroadcast(Intent(PERMISSION_DENIED))
        toast(R.string.permission_denied_note)
      } else {
        viewModel.permissionGranted()
      }
    }
  }

  private fun setupToolbar() = toolbar.run {
    inflateMenu(R.menu.main)
    menu.findItem(R.id.dark_mode_toggle)
        .isChecked = isDarkMode()

    setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.dark_mode_toggle -> {
          toggleDarkMode()
        }
        R.id.about -> {
          AboutDialog.show(this@MainActivity)
        }
        R.id.settings -> {
          startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
        }
      }
      true
    }
  }

  private fun setupGrid() {
    adapter = RecordingAdapter { recording, longClick ->
      if (longClick) {
        showRecordingOptions(recording)
      } else {
        openRecording(recording)
      }
    }

    list.layoutManager = GridLayoutManager(this, resources.getInteger(R.integer.grid_span))
    list.adapter = adapter

    if (!isDarkMode()) {
      // In light mode, the toolbar should always have elevation.
      appToolbar.elevation = resources.getDimension(R.dimen.raised_toolbar_elevation)
    } else {
      // In dark mode, the toolbar has elevation when we scroll down.
      list.onScroll { invalidateToolbarElevation() }
    }
  }

  private fun invalidateToolbarElevation() {
    if (!isDarkMode()) {
      appToolbar.elevation = resources.getDimension(R.dimen.raised_toolbar_elevation)
      return
    }

    val scrollPosition = list.computeVerticalScrollRange()
    if (scrollPosition > (toolbar.measuredHeight / 2)) {
      appToolbar.elevation = resources.getDimension(R.dimen.raised_toolbar_elevation)
    } else {
      appToolbar.elevation = 0f
    }
  }

  override fun onResume() {
    super.onResume()
    invalidateToolbarElevation()
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    viewModel.permissionGranted()
  }

  private fun openRecording(recording: Recording) {
    startActivity(Intent(ACTION_VIEW).apply {
      setDataAndType(recording.toUri(), "video/*")
    })
  }

  private fun shareRecording(recording: Recording) {
    startActivity(Intent(Intent.ACTION_SEND).apply {
      setDataAndType(recording.toUri(), "video/*")
    })
  }

  private fun showRecordingOptions(recording: Recording) {
    MaterialDialog(this).show {
      title(text = recording.name)
      listItems(R.array.recording_options_dialog) { _, index, _ ->
        when (index) {
          0 -> shareRecording(recording)
          1 -> viewModel.deleteRecording(recording)
        }
      }
    }
  }
}
