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

import android.app.Activity
import com.afollestad.assent.Permission
import com.afollestad.assent.Permission.WRITE_EXTERNAL_STORAGE
import com.afollestad.assent.askForPermissions
import com.afollestad.assent.rationale.ConfirmCallback
import com.afollestad.assent.rationale.RationaleHandler
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.mnmlscreenrecord.engine.R

/** @author Aidan Follestad (@afollestad) */
class MnmlRationaleHandler(
  private val context: Activity
) : RationaleHandler(context, context::askForPermissions) {
  private var dialog: MaterialDialog? = null

  init {
    onPermission(WRITE_EXTERNAL_STORAGE, R.string.storage_permission_prompt_desc)
  }

  override fun showRationale(
    permission: Permission,
    message: CharSequence,
    confirm: ConfirmCallback
  ) {
    if (permission == WRITE_EXTERNAL_STORAGE) {
      dialog = MaterialDialog(context).show {
        title(R.string.storage_permission_prompt)
        message(text = message)
        cancelOnTouchOutside(false)
        positiveButton(R.string.okay) { confirm(true) }
        onCancel { confirm(false) }
      }
    } else {
      throw IllegalStateException("Don't know about permission $permission")
    }
  }

  override fun onDestroy() {
    dialog?.dismiss()
    dialog = null
  }
}
