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
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.assent.Permission.WRITE_EXTERNAL_STORAGE
import com.afollestad.assent.askForPermissions
import com.afollestad.mnmlscreenrecord.common.misc.toast
import com.afollestad.mnmlscreenrecord.engine.R
import com.afollestad.mnmlscreenrecord.engine.service.BackgroundService.Companion.PERMISSION_DENIED
import com.afollestad.mnmlscreenrecord.engine.service.ServiceController
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (@afollestad) */
class StoragePermissionActivity : AppCompatActivity() {
  companion object {
    const val STORAGE_REQUEST = 98
  }

  private val serviceController by inject<ServiceController>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    askForPermissions(
        WRITE_EXTERNAL_STORAGE,
        requestCode = STORAGE_REQUEST,
        rationaleHandler = MnmlRationaleHandler(this)
    ) { res ->
      if (!res.isAllGranted(WRITE_EXTERNAL_STORAGE)) {
        sendBroadcast(Intent(PERMISSION_DENIED))
        toast(R.string.permission_denied_note)
      } else {
        serviceController.startRecording()
      }
      finish()
    }
  }
}
