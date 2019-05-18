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
package com.afollestad.mnmlscreenrecord.common.view

import android.view.MenuItem
import androidx.appcompat.widget.Toolbar

private const val DEFAULT_DEBOUNCE_INTERVAL = 750L

/** @author Aidan Follestad (@afollestad) */
abstract class DebouncedMenuItemClickListener(
  private val delayBetweenClicks: Long = DEFAULT_DEBOUNCE_INTERVAL
) : Toolbar.OnMenuItemClickListener {
  private var lastClickTimestamp = -1L

  @Deprecated(
      message = "onDebouncedMenuItemClick should be overridden instead.",
      replaceWith = ReplaceWith("onDebouncedMenuItemClick(item)")
  )
  override fun onMenuItemClick(item: MenuItem): Boolean {
    val now = System.currentTimeMillis()
    if (lastClickTimestamp == -1L || now >= (lastClickTimestamp + delayBetweenClicks)) {
      onDebouncedMenuItemClick(item)
    }
    lastClickTimestamp = now
    return true
  }

  abstract fun onDebouncedMenuItemClick(item: MenuItem)
}

fun Toolbar.setOnMenuItemDebouncedClickListener(
  delayBetweenClicks: Long = DEFAULT_DEBOUNCE_INTERVAL,
  block: (MenuItem) -> Unit
) {
  setOnMenuItemClickListener(object : DebouncedMenuItemClickListener(delayBetweenClicks) {
    override fun onDebouncedMenuItemClick(item: MenuItem) = block(item)
  })
}
