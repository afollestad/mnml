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
package com.afollestad.mnmlscreenrecord.theming

import androidx.annotation.Size
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PRIVATE
import java.util.Calendar
import java.util.Calendar.DAY_OF_YEAR
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.GregorianCalendar

/** @author Aidan Follestad (afollestad) */
data class DarkModeSettings(
  val on: Boolean,
  val auto: Boolean,
  val start: String,
  val end: String,
  private val nowProvider: () -> (Calendar) = { GregorianCalendar() }
) {
  fun isEnabled(): Boolean {
    if (!on) {
      return false
    } else if (!auto) {
      return true
    }
    val rightNow = nowProvider()
    val start = calendarForTime(time = start.splitTime(), end = false)
    val end = calendarForTime(time = end.splitTime(), end = true)
    return (rightNow == start || rightNow.after(start)) && rightNow.before(end)
  }
}

@VisibleForTesting(otherwise = PRIVATE)
internal fun calendarForTime(
  time: IntArray,
  end: Boolean
): Calendar {
  require(time.size == 2)
  return GregorianCalendar().apply {
    if (end) {
      set(DAY_OF_YEAR, get(DAY_OF_YEAR) + 1)
    }
    set(HOUR_OF_DAY, time[0])
    set(MINUTE, time[1])
  }
}

@Size(2)
fun String.splitTime(): IntArray {
  val splitString = split(':')
  require(splitString.size == 2)
  return intArrayOf(
      splitString[0].toInt(),
      splitString[1].toInt()
  )
}
