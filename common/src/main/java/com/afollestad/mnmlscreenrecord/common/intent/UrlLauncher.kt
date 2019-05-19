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
package com.afollestad.mnmlscreenrecord.common.intent

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.annotation.AttrRes
import androidx.browser.customtabs.CustomTabsIntent
import com.afollestad.mnmlscreenrecord.common.R
import com.afollestad.mnmlscreenrecord.common.misc.toUri

/** @author Aidan Follestad (@afollestad) */
interface UrlLauncher {
  fun viewUrl(url: String)
}

class RealUrlLauncher(
  private val currentActivity: Activity
) : UrlLauncher {

  override fun viewUrl(url: String) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setToolbarColor(resolveColor(R.attr.colorPrimary))
        .build()
    try {
      customTabsIntent.launchUrl(currentActivity, url.toUri())
    } catch (_: ActivityNotFoundException) {
      val chooser = Intent.createChooser(
          Intent(ACTION_VIEW)
              .setData(url.toUri()), "View URL"
      )
      currentActivity.startActivity(chooser)
    }
  }

  private fun resolveColor(@AttrRes attr: Int): Int {
    val a = currentActivity.theme.obtainStyledAttributes(intArrayOf(attr))
    try {
      return a.getColor(0, 0)
    } finally {
      a.recycle()
    }
  }
}
