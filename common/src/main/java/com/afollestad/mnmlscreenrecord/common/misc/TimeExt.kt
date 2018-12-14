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
package com.afollestad.mnmlscreenrecord.common.misc

import kotlin.math.ceil

const val SECOND: Long = 1000
const val MINUTE = SECOND * 60
const val HOUR = MINUTE * 60
const val DAY = HOUR * 24
const val WEEK = DAY * 7
const val MONTH = WEEK * 4

fun Long.timeString() = when {
  this <= 0 -> "??"
  this >= MONTH ->
    "${ceil((this.toFloat() / MONTH.toFloat()).toDouble()).toInt()}mo"
  this >= WEEK ->
    "${ceil((this.toFloat() / WEEK.toFloat()).toDouble()).toInt()}w"
  this >= DAY ->
    "${ceil((this.toFloat() / DAY.toFloat()).toDouble()).toInt()}d"
  this >= HOUR ->
    "${ceil((this.toFloat() / HOUR.toFloat()).toDouble()).toInt()}h"
  this >= MINUTE -> {
    val result = "${ceil((this.toFloat() / MINUTE.toFloat()).toDouble()).toInt()}m"
    if (result == "60m") {
      "1h"
    } else {
      result
    }
  }
  else -> "<1m"
}
