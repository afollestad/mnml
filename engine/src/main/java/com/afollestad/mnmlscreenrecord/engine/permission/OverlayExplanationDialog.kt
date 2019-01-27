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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onCancel
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.mnmlscreenrecord.engine.R
import com.crashlytics.android.Crashlytics
import java.lang.reflect.InvocationTargetException

interface OverlayExplanationCallback {

  fun onShouldAskForOverlayPermission()
}

/** @author Aidan Follestad (@afollestad) */
class OverlayExplanationDialog : DialogFragment() {

  companion object {
    private const val TAG = "[OverlayExplanationDialog]"

    fun <T> show(context: T) where T : FragmentActivity, T : OverlayExplanationCallback {
      val dialog = OverlayExplanationDialog()
      try {
        dialog.show(context.supportFragmentManager, TAG)
      } catch (_: java.lang.IllegalStateException) {
        Crashlytics.log("Not showing OverlayExplanationDialog due to IllegalStateException.")
      }
    }

    fun <T> show(context: T) where T : Fragment, T : OverlayExplanationCallback {
      val dialog = OverlayExplanationDialog()
      try {
        dialog.show(context.childFragmentManager, TAG)
      } catch (_: java.lang.IllegalStateException) {
        Crashlytics.log("Not showing OverlayExplanationDialog due to IllegalStateException.")
      }
    }
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context = activity ?: throw IllegalStateException("Oh no!")
    var callback = context as? OverlayExplanationCallback
    if (callback == null) {
      callback = parentFragment as? OverlayExplanationCallback ?: throw IllegalStateException(
          "Couldn't find callback from Activity or parent Fragment."
      )
    }

    try {
      return MaterialDialog(context)
          .title(R.string.overlay_permission_prompt)
          .message(R.string.overlay_permission_prompt_desc)
          .cancelOnTouchOutside(false)
          .positiveButton(R.string.okay) {
            dismiss()
            callback.onShouldAskForOverlayPermission()
          }
          .onDismiss { dismiss() }
          .onCancel { dismiss() }
    } catch (e: InvocationTargetException) {
      // Workaround for Samsung/Huawei bug with AndroidX font retrieval.
      return AlertDialog.Builder(context)
          .setTitle(R.string.overlay_permission_prompt)
          .setMessage(R.string.overlay_permission_prompt_desc)
          .setPositiveButton(R.string.okay) { _, _ ->
            dismiss()
            callback.onShouldAskForOverlayPermission()
          }
          .setOnCancelListener { dismiss() }
          .setOnDismissListener { dismiss() }
          .create()
    }
  }

  override fun onCancel(dialog: DialogInterface?) {
    super.onCancel(dialog)
    val callback = activity as? OverlayExplanationCallback ?: return
    callback.onShouldAskForOverlayPermission()
  }
}
