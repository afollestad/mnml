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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_STREAM
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import androidx.lifecycle.Observer
import com.afollestad.assent.Permission.WRITE_EXTERNAL_STORAGE
import com.afollestad.assent.askForPermissions
import com.afollestad.inlineactivityresult.startActivityForResult
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.intent.UrlLauncher
import com.afollestad.mnmlscreenrecord.common.misc.startActivity
import com.afollestad.mnmlscreenrecord.common.misc.toUri
import com.afollestad.mnmlscreenrecord.common.misc.toast
import com.afollestad.mnmlscreenrecord.common.rx.attachLifecycle
import com.afollestad.mnmlscreenrecord.common.view.onDebouncedClick
import com.afollestad.mnmlscreenrecord.common.view.onScroll
import com.afollestad.mnmlscreenrecord.common.view.setOnMenuItemDebouncedClickListener
import com.afollestad.mnmlscreenrecord.common.view.showOrHide
import com.afollestad.mnmlscreenrecord.engine.permission.MnmlRationaleHandler
import com.afollestad.mnmlscreenrecord.engine.permission.OverlayExplanationCallback
import com.afollestad.mnmlscreenrecord.engine.permission.OverlayExplanationDialog
import com.afollestad.mnmlscreenrecord.engine.recordings.Recording
import com.afollestad.mnmlscreenrecord.engine.service.BackgroundService.Companion.PERMISSION_DENIED
import com.afollestad.mnmlscreenrecord.engine.service.ErrorDialogActivity
import com.afollestad.mnmlscreenrecord.theming.DarkModeSwitchActivity
import com.afollestad.mnmlscreenrecord.ui.about.AboutDialog
import com.afollestad.mnmlscreenrecord.ui.settings.SettingsActivity
import com.afollestad.mnmlscreenrecord.views.asBackgroundTint
import com.afollestad.mnmlscreenrecord.views.asEnabled
import com.afollestad.mnmlscreenrecord.views.asIcon
import com.afollestad.mnmlscreenrecord.views.asText
import com.afollestad.recyclical.datasource.emptySelectableDataSourceTyped
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.viewholder.hasSelection
import com.afollestad.recyclical.viewholder.isSelected
import com.afollestad.recyclical.withItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import kotlinx.android.synthetic.main.activity_main.empty_view
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.include_appbar.toolbar
import kotlinx.android.synthetic.main.list_item_recording.view.thumbnail
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlinx.android.synthetic.main.include_appbar.app_toolbar as appToolbar
import kotlinx.android.synthetic.main.include_appbar.toolbar_title as toolbarTitle

/** @author Aidan Follestad (afollestad) */
class MainActivity : DarkModeSwitchActivity(), OverlayExplanationCallback {

  private val viewModel by viewModel<MainViewModel>()
  private val urlLauncher by inject<UrlLauncher> { parametersOf(this) }

  private val dataSource =
    emptySelectableDataSourceTyped<Recording>().apply {
      onSelectionChange {
        if (it.hasSelection()) {
          if (toolbar.navigationIcon == null) {
            toolbar.run {
              setNavigationIcon(R.drawable.ic_close)
              menu.clear()
              inflateMenu(R.menu.edit_mode)
            }
          }
          toolbarTitle.text = getString(R.string.app_name_short_withNumber, it.getSelectionCount())
          toolbar.menu.run {
            findItem(R.id.share).isVisible = it.getSelectionCount() == 1
            findItem(R.id.delete).isEnabled = it.getSelectionCount() > 0
          }
        } else {
          toolbar.run {
            navigationIcon = null
            menu.clear()
            inflateMenu(R.menu.main)
          }
          toolbarTitle.text = getString(R.string.app_name_short)
        }
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setupToolbar()
    setupGrid()

    fab.onDebouncedClick { viewModel.fabClicked() }
    lifecycle.addObserver(viewModel)

    viewModel.onRecordings()
        .observe(this, Observer {
          dataSource.set(
              newItems = it,
              areTheSame = Recording.Companion::areTheSame,
              areContentsTheSame = Recording.Companion::areContentsTheSame
          )
        })
    viewModel.onFabColorRes()
        .asBackgroundTint(this, fab)
    viewModel.onFabIconRes()
        .asIcon(this, fab)
    viewModel.onFabTextRes()
        .asText(this, fab)
    viewModel.onFabEnabled()
        .asEnabled(this, fab)

    viewModel.onNeedOverlayPermission()
        .observeOn(mainThread())
        .subscribe { OverlayExplanationDialog.show(this) }
        .attachLifecycle(this)
    viewModel.onNeedStoragePermission()
        .observeOn(mainThread())
        .subscribe { onShouldAskForStoragePermission() }
        .attachLifecycle(this)
    viewModel.onError()
        .observeOn(mainThread())
        .subscribe { ErrorDialogActivity.show(this, it) }
        .attachLifecycle(this)

    checkForMediaProjectionAvailability()
  }

  override fun onResume() {
    super.onResume()
    invalidateToolbarElevation(list.computeVerticalScrollOffset())
  }

  override fun onBackPressed() {
    if (dataSource.hasSelection()) {
      dataSource.deselectAll()
    } else {
      super.onBackPressed()
    }
  }

  override fun onShouldAskForOverlayPermission() {
    val intent = Intent(
        ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:$packageName".toUri()
    )
    startActivityForResult(
        intent = intent,
        requestCode = DRAW_OVER_OTHER_APP_PERMISSION
    ) { _, _ ->
      viewModel.permissionGranted()
    }
  }

  private fun onShouldAskForStoragePermission() {
    askForPermissions(
        WRITE_EXTERNAL_STORAGE,
        requestCode = STORAGE_PERMISSION,
        rationaleHandler = MnmlRationaleHandler(this)
    ) { res ->
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
    setNavigationOnClickListener { dataSource.deselectAll() }
    setOnMenuItemDebouncedClickListener { item ->
      when (item.itemId) {
        R.id.about -> AboutDialog.show(this@MainActivity)
        R.id.provide_feedback -> urlLauncher.viewUrl(
            "https://github.com/afollestad/mnml/issues/new/choose"
        )
        R.id.settings -> startActivity<SettingsActivity>()
        R.id.share -> shareRecording(dataSource.getSelectedItems().single())
        R.id.delete -> {
          viewModel.deleteRecordings(dataSource.getSelectedItems())
          dataSource.deselectAll()
        }
      }
    }
  }

  @SuppressLint("SetTextI18n")
  private fun setupGrid() {
    list.setup {
      withDataSource(dataSource)
      withEmptyView(empty_view)
      withItem<Recording, RecordingViewHolder>(R.layout.list_item_recording) {
        onBind(::RecordingViewHolder) { _, item ->
          Glide.with(thumbnail)
              .asBitmap()
              .apply(RequestOptions().frame(0))
              .load(item.toUri())
              .into(itemView.thumbnail)
          name.text = item.name
          details.text = "${item.sizeString()} – ${item.timestampString()}"
          checkBox.showOrHide(hasSelection())
          checkBox.isChecked = isSelected()
        }
        onClick {
          if (hasSelection()) {
            toggleSelection()
          } else {
            onRecordingClicked(item)
          }
        }
        onLongClick {
          toggleSelection()
        }
      }
    }
    list.onScroll { invalidateToolbarElevation(it) }
  }

  private fun onRecordingClicked(recording: Recording) {
    try {
      startActivity(Intent(ACTION_VIEW).apply {
        setDataAndType(recording.toUri(), "video/*")
      })
    } catch (_: ActivityNotFoundException) {
      toast(R.string.install_video_viewer)
    }
  }

  private fun invalidateToolbarElevation(scrollY: Int) {
    if (scrollY > (toolbar.measuredHeight / 2)) {
      appToolbar.elevation = resources.getDimension(R.dimen.raised_toolbar_elevation)
    } else {
      appToolbar.elevation = 0f
    }
  }

  private fun shareRecording(recording: Recording) {
    val uri = recording.toUri()
    startActivity(Intent(ACTION_SEND).apply {
      setDataAndType(uri, "video/*")
      putExtra(EXTRA_STREAM, uri)
    })
  }

  private fun checkForMediaProjectionAvailability() {
    try {
      Class.forName("android.media.projection.MediaProjectionManager")
    } catch (e: ClassNotFoundException) {
      MaterialDialog(this).show {
        title(text = "Device Unsupported")
        message(
            text = "Your device lacks support for MediaProjectionManager. Either the manufacturer " +
                "of your device left it out, or you are using an emulator."
        )
        positiveButton(android.R.string.ok) { finish() }
        cancelOnTouchOutside(false)
        cancelable(false)
        onCancel { finish() }
        onDismiss { finish() }
      }
    }
  }

  private companion object {
    private const val DRAW_OVER_OTHER_APP_PERMISSION = 68
    private const val STORAGE_PERMISSION = 64
  }
}
