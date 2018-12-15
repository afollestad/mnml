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
package com.afollestad.mnmlscreenrecord.common.permissions

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Application
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.provider.Settings

/**
 * An abstraction layer for checking common permission access.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface PermissionChecker {

  /**
   * Returns true if the app has permission to show system overlays.
   */
  fun hasOverlayPermission(): Boolean

  /**
   * Returns true if the app has permission to write external storage.
   */
  fun hasStoragePermission(): Boolean
}

/** @author Aidan Follestad (@afollestad) */
class RealPermissionChecker(
  private val app: Application
) : PermissionChecker {

  override fun hasOverlayPermission(): Boolean {
    return Settings.canDrawOverlays(app)
  }

  override fun hasStoragePermission(): Boolean {
    return app.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED
  }
}
