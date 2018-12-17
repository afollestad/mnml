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
import com.afollestad.mnmlscreenrecord.common.misc.toast
import com.afollestad.mnmlscreenrecord.engine.capture.CaptureEngine
import com.afollestad.mnmlscreenrecord.engine.service.BackgroundService.Companion.PERMISSION_DENIED
import org.koin.android.ext.android.inject

/** @author Aidan Follestad (@afollestad) */
class CapturePermissionActivity : AppCompatActivity() {
  companion object {
    const val PROJECTION_REQUEST = 97
  }

  private val captureEngine by inject<CaptureEngine>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    captureEngine.requestPermission(
        this,
        PROJECTION_REQUEST
    )
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    when {
      requestCode != PROJECTION_REQUEST -> return
      resultCode != RESULT_OK -> {
        toast("Screen cast permission was denied to MNML!")
        captureEngine.cancel()
        sendBroadcast(Intent(PERMISSION_DENIED))
      }
      else -> data?.let {
        captureEngine.onActivityResult(this@CapturePermissionActivity, resultCode, it)
      }
    }
    finish()
  }
}
