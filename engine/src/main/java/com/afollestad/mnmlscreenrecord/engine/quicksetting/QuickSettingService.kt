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
package com.afollestad.mnmlscreenrecord.engine.quicksetting

import android.graphics.drawable.Icon
import android.os.Build.VERSION_CODES.N
import android.service.quicksettings.Tile.STATE_ACTIVE
import android.service.quicksettings.Tile.STATE_INACTIVE
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.afollestad.mnmlscreenrecord.engine.R
import com.afollestad.mnmlscreenrecord.engine.capture.CaptureEngine
import com.afollestad.mnmlscreenrecord.engine.service.ServiceController
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (@afollestad) */
@RequiresApi(N)
class QuickSettingService : TileService() {

  private val captureEngine by inject<CaptureEngine>()
  private val serviceController by inject<ServiceController>()

  override fun onTileAdded() {
    super.onTileAdded()
    updateTile()
  }

  override fun onStartListening() {
    super.onStartListening()
    updateTile()
  }

  private fun updateTile() {
    val tile = qsTile ?: return
    if (captureEngine.isStarted()) {
      tile.icon = Icon.createWithResource(this, R.drawable.ic_quick_setting_stop)
      tile.label = getString(R.string.cd_stop_recording)
      tile.contentDescription = getString(R.string.cd_stop_recording)
      tile.state = STATE_ACTIVE
    } else {
      tile.icon = Icon.createWithResource(this, R.drawable.ic_quick_setting_record)
      tile.label = getString(R.string.cd_start_recording)
      tile.contentDescription = getString(R.string.cd_start_recording)
      tile.state = STATE_INACTIVE
    }
    tile.updateTile()
  }

  override fun onClick() {
    super.onClick()
    if (captureEngine.isStarted()) {
      serviceController.stopRecording(true)
    } else {
      serviceController.startRecording()
    }
  }
}
