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
import android.content.res.ColorStateList.valueOf
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.provider.Settings.canDrawOverlays
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.assent.Permission.WRITE_EXTERNAL_STORAGE
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.isAllGranted
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.files.FileScanner
import com.afollestad.mnmlscreenrecord.common.misc.toUri
import com.afollestad.mnmlscreenrecord.common.misc.toast
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.common.view.onScroll
import com.afollestad.mnmlscreenrecord.common.view.scopeWhileAttached
import com.afollestad.mnmlscreenrecord.common.view.showOrHide
import com.afollestad.mnmlscreenrecord.engine.capture.CaptureEngine
import com.afollestad.mnmlscreenrecord.engine.loader.Recording
import com.afollestad.mnmlscreenrecord.engine.loader.RecordingQueryer
import com.afollestad.mnmlscreenrecord.engine.service.BackgroundService
import com.afollestad.mnmlscreenrecord.engine.service.BackgroundService.Companion.PERMISSION_DENIED
import com.afollestad.mnmlscreenrecord.notifications.EXTRA_STOP_FOREGROUND
import com.afollestad.mnmlscreenrecord.notifications.Notifications
import com.afollestad.mnmlscreenrecord.notifications.RECORD_ACTION
import com.afollestad.mnmlscreenrecord.notifications.STOP_ACTION
import com.afollestad.mnmlscreenrecord.theming.DarkModeSwitchActivity
import com.afollestad.mnmlscreenrecord.ui.about.AboutDialog
import com.afollestad.mnmlscreenrecord.ui.settings.SettingsActivity
import io.reactivex.Observable.merge
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.include_appbar.toolbar
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File
import kotlinx.android.synthetic.main.activity_main.empty_view as emptyText
import kotlinx.android.synthetic.main.include_appbar.app_toolbar as appToolbar

/** @author Aidan Follestad (afollestad) */
class MainActivity : DarkModeSwitchActivity() {
  companion object {
    private const val DRAW_OVER_OTHER_APP_PERMISSION = 68
    private const val STORAGE_PERMISSION = 64
  }

  private var isAskingPermissions = false

  private val captureEngine by inject<CaptureEngine>()
  private val notifications by inject<Notifications>()
  private val recordingQueryer by inject<RecordingQueryer>()
  private val fileScanner by inject<FileScanner>()

  private lateinit var adapter: RecordingAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Toolbar
    setupToolbar()
    // Notifications
    notifications.createChannels()
    // Lifecycle
    lifecycle.addObserver(recordingQueryer)
    // Recycler View Grid
    setupGrid()
    // FAB
    setupFab()

    captureEngine.onStart()
        .subscribe {
          fab.isEnabled = true
          invalidateFab()
        }
        .attachLifecycle(this)
    merge(captureEngine.onStop(), captureEngine.onCancel())
        .subscribe {
          fab.isEnabled = true
          invalidateFab()
        }
        .attachLifecycle(this)
    fileScanner.onScan()
        .subscribe { refreshRecordings() }
        .attachLifecycle(this)
  }

  private fun setupToolbar() = toolbar.run {
    inflateMenu(R.menu.main)
    menu.findItem(R.id.dark_mode_toggle)
        .isChecked = darkModePref.get()

    setOnMenuItemClickListener { item ->
      when (item.itemId) {
        R.id.dark_mode_toggle -> {
          darkModePref.set(!darkModePref.get())
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

    if (!darkModePref.get()) {
      appToolbar.elevation = resources.getDimension(R.dimen.raised_toolbar_elevation)
      list.onScroll { invalidateToolbarElevation() }
    }
  }

  private fun invalidateToolbarElevation() {
    val scrollPosition = list.computeVerticalScrollRange()
    if (!darkModePref.get() || scrollPosition > (toolbar.measuredHeight / 2)) {
      appToolbar.elevation = resources.getDimension(R.dimen.raised_toolbar_elevation)
    } else {
      appToolbar.elevation = 0f
    }
  }

  private fun setupFab() {
    fab.setOnClickListener {
      if (captureEngine.isStarted()) {
        sendBroadcast(Intent(STOP_ACTION).apply {
          putExtra(EXTRA_STOP_FOREGROUND, true)
        })
      } else {
        maybeAskForSystemOverlayPermission()
        if (!isAskingPermissions) {
          startService(true)
        }
      }
      fab.isEnabled = false
    }
  }

  private fun refreshRecordings() {
    toolbar.scopeWhileAttached(Main) {
      launch(coroutineContext) {
        val recordings = withContext(IO) { recordingQueryer.queryRecordings() }
        adapter.set(recordings)
        emptyText.showOrHide(recordings.isEmpty())
      }
    }
  }

  override fun onResume() {
    super.onResume()
    maybeAskForSystemOverlayPermission()
    if (!isAskingPermissions) {
      refreshRecordings()
    }
    invalidateFab()
    invalidateToolbarElevation()
    notifications.setIsAppOpen(true)
  }

  override fun onPause() {
    notifications.setIsAppOpen(false)
    super.onPause()
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    isAskingPermissions = false
  }

  private fun maybeAskForSystemOverlayPermission() {
    if (!canDrawOverlays(this)) {
      isAskingPermissions = true
      MaterialDialog(this).show {
        title(R.string.overlay_permission_prompt)
        message(R.string.overlay_permission_prompt_desc)
        cancelOnTouchOutside(false)
        positiveButton(R.string.okay) {
          val intent = Intent(
              ACTION_MANAGE_OVERLAY_PERMISSION,
              "package:$packageName".toUri()
          )
          startActivityForResult(
              intent,
              DRAW_OVER_OTHER_APP_PERMISSION
          )
        }
      }
    } else if (!isAllGranted(WRITE_EXTERNAL_STORAGE)) {
      isAskingPermissions = true
      MaterialDialog(this).show {
        title(R.string.storage_permission_prompt)
        message(R.string.storage_permission_prompt_desc)
        cancelOnTouchOutside(false)
        positiveButton(R.string.okay) {
          askForPermissions(WRITE_EXTERNAL_STORAGE, requestCode = STORAGE_PERMISSION) { res ->
            isAskingPermissions = false
            if (!res.isAllGranted(WRITE_EXTERNAL_STORAGE)) {
              sendBroadcast(Intent(PERMISSION_DENIED))
              toast(R.string.permission_denied_note)
            }
          }
        }
      }
    }
  }

  private fun startService(startRecording: Boolean) =
    startService(Intent(this, BackgroundService::class.java).apply {
      if (startRecording) {
        action = RECORD_ACTION
      }
    })

  private fun invalidateFab() {
    if (captureEngine.isStarted()) {
      val red = ContextCompat.getColor(this, R.color.red)
      fab.backgroundTintList = valueOf(red)
      fab.setIconResource(R.drawable.ic_stop_32dp)
      fab.setText(R.string.stop_recording)
    } else {
      val accent = ContextCompat.getColor(this, R.color.colorAccent)
      fab.backgroundTintList = valueOf(accent)
      fab.setIconResource(R.drawable.ic_record_32dp)
      fab.setText(R.string.start_recording)
    }
  }

  private fun openRecording(recording: Recording) {
    startActivity(Intent(ACTION_VIEW).apply {
      setDataAndType(recording.toUri(), "video/*")
    })
  }

  private fun showRecordingOptions(recording: Recording) {
    MaterialDialog(this).show {
      title(text = recording.name)
      listItems(R.array.recording_options_dialog) { _, index, _ ->
        when (index) {
          0 -> startActivity(Intent(Intent.ACTION_SEND).apply {
            setDataAndType(recording.toUri(), "video/*")
          })
          1 -> deleteRecording(recording)
        }
      }
    }
  }

  private fun deleteRecording(recording: Recording) {
    toolbar.scopeWhileAttached(Main) {
      launch(coroutineContext) {
        withContext(IO) {
          File(recording.path).delete()
          contentResolver.delete(recording.toUri(), null, null)
        }
        refreshRecordings()
      }
    }
  }
}
