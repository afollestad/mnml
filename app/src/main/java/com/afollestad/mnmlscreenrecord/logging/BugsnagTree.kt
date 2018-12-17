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
package com.afollestad.mnmlscreenrecord.logging

import android.util.Log.ASSERT
import android.util.Log.DEBUG
import android.util.Log.ERROR
import android.util.Log.INFO
import android.util.Log.VERBOSE
import android.util.Log.WARN
import com.bugsnag.android.BreadcrumbType.STATE
import com.bugsnag.android.Bugsnag
import timber.log.Timber

/** @author Aidan Follestad (@afollestad) */
class BugsnagTree : Timber.Tree() {

  override fun log(
    priority: Int,
    tag: String?,
    message: String,
    t: Throwable?
  ) {
    if (t != null) {
      Bugsnag.leaveBreadcrumb("crash_tag", STATE, mutableMapOf("tag" to tag))
      Bugsnag.notify(t)
    } else {
      Bugsnag.leaveBreadcrumb("${priority.priorityString()}${tag ?: "??"}: $message")
    }
  }

  private fun Int.priorityString() = when (this) {
    VERBOSE -> "W/"
    DEBUG -> "D/"
    INFO -> "I/"
    WARN -> "W/"
    ERROR -> "E/"
    ASSERT -> "A/"
    else -> "?"
  }
}
