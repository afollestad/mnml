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
package com.afollestad.mnmlscreenrecord.engine.permission

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.mnmlscreenrecord.common.misc.toUri
import com.afollestad.mnmlscreenrecord.common.misc.toast
import com.afollestad.mnmlscreenrecord.common.permissions.PermissionChecker
import com.afollestad.mnmlscreenrecord.engine.R
import com.afollestad.mnmlscreenrecord.engine.service.ServiceController
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (@afollestad) */
class OverlayPermissionActivity : AppCompatActivity(), OverlayExplanationCallback {
  companion object {
    const val OVERLAY_REQUEST = 99
  }

  private val serviceController by inject<ServiceController>()
  private val permissionChecker by inject<PermissionChecker>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    OverlayExplanationDialog.show(this)
  }

  override fun onShouldAskForOverlayPermission() {
    val intent = Intent(
        ACTION_MANAGE_OVERLAY_PERMISSION,
        "package:$packageName".toUri()
    )
    startActivityForResult(
        intent,
        OVERLAY_REQUEST
    )
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == OVERLAY_REQUEST) {
      if (!permissionChecker.hasOverlayPermission()) {
        toast(R.string.permission_denied_note)
      } else {
        serviceController.startRecording()
      }
      finish()
    }
  }
}
