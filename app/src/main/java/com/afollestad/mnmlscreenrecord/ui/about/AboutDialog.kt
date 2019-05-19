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
package com.afollestad.mnmlscreenrecord.ui.about

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.mnmlscreenrecord.BuildConfig.VERSION_NAME
import com.afollestad.mnmlscreenrecord.R
import com.afollestad.mnmlscreenrecord.common.intent.UrlLauncher
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

/** @author Aidan Follestad (afollestad) */
class AboutDialog : DialogFragment() {
  private val urlLauncher by inject<UrlLauncher> { parametersOf(activity!!) }

  companion object {
    private const val TAG = "[ABOUT_DIALOG]"

    /** Shows the about dialog inside of [context]. */
    fun show(context: AppCompatActivity) = AboutDialog().show(context.supportFragmentManager, TAG)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context = activity ?: throw IllegalStateException("Oh no!")
    return MaterialDialog(context)
        .title(text = getString(R.string.about_title, VERSION_NAME))
        .message(res = R.string.about_body) {
          html { urlLauncher.viewUrl(it) }
          lineSpacing(1.4f)
        }
        .positiveButton(R.string.dismiss)
  }
}
